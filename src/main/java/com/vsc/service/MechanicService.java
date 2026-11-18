package com.vsc.service;

import com.vsc.dao.MechanicDao;
import com.vsc.exception.ValidationException;
import com.vsc.model.Mechanic;
import com.vsc.util.DBConnection;
import com.vsc.util.InputValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Business rules for mechanics: who exists and who may be assigned to a job. */
public class MechanicService {

    private final MechanicDao mechanicDao;

    public MechanicService() {
        this(new MechanicDao());
    }

    public MechanicService(MechanicDao mechanicDao) {
        this.mechanicDao = mechanicDao;
    }

    public Mechanic add(Mechanic mechanic) throws ValidationException, SQLException {
        validate(mechanic);
        try (Connection con = DBConnection.getConnection()) {
            return mechanicDao.insert(con, mechanic);
        }
    }

    public void update(Mechanic mechanic) throws ValidationException, SQLException {
        if (mechanic.getId() <= 0) {
            throw new ValidationException("id", "Update needs a mechanic that was already saved.");
        }
        validate(mechanic);
        try (Connection con = DBConnection.getConnection()) {
            if (mechanicDao.findById(con, mechanic.getId()) == null) {
                throw new ValidationException("id", "Mechanic no longer exists.");
            }
            mechanicDao.update(con, mechanic);
        }
    }

    public Mechanic findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return mechanicDao.findById(con, id);
        }
    }

    /** Active mechanics only - this is the list used by the job-card dropdown. */
    public List<Mechanic> listActive() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return mechanicDao.findAll(con, true);
        }
    }

    public List<Mechanic> listAll() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return mechanicDao.findAll(con, false);
        }
    }

    private void validate(Mechanic mechanic) throws ValidationException {
        if (mechanic == null) {
            throw new ValidationException("mechanic", "No mechanic details were supplied.");
        }
        mechanic.setName(InputValidator.trim(mechanic.getName()));
        mechanic.setPhone(InputValidator.trim(mechanic.getPhone()));
        mechanic.setSpecialization(InputValidator.trim(mechanic.getSpecialization()));

        if (!InputValidator.isValidName(mechanic.getName())) {
            throw new ValidationException("name", "Name must be 2 to 80 characters.");
        }
        if (mechanic.getPhone() != null && !mechanic.getPhone().isEmpty()
                && !InputValidator.isValidPhone(mechanic.getPhone())) {
            throw new ValidationException("phone", "Phone must contain 7 to 15 digits.");
        }
        if (mechanic.getSpecialization() != null && mechanic.getSpecialization().length() > 80) {
            throw new ValidationException("specialization", "Specialization is too long (max 80).");
        }
    }
}
