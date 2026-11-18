package com.vsc.service;

import com.vsc.dao.JobCardDao;
import com.vsc.dao.JobPartDao;
import com.vsc.dao.JobServiceDao;
import com.vsc.dao.MechanicDao;
import com.vsc.dao.ServiceItemDao;
import com.vsc.dao.SparePartDao;
import com.vsc.dao.VehicleDao;
import com.vsc.exception.InsufficientStockException;
import com.vsc.exception.InvalidStatusTransitionException;
import com.vsc.exception.ValidationException;
import com.vsc.model.JobCard;
import com.vsc.model.JobPartLine;
import com.vsc.model.JobServiceLine;
import com.vsc.model.JobStatus;
import com.vsc.model.Mechanic;
import com.vsc.model.ServiceItem;
import com.vsc.model.SparePart;
import com.vsc.util.DBConnection;
import com.vsc.util.InputValidator;
import com.vsc.util.Money;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Business rules for job cards - the centre of the workflow.
 *
 * <ul>
 *   <li>a job can only move to the next status in
 *       {@code RECEIVED -> IN_PROGRESS -> COMPLETED -> DELIVERED};</li>
 *   <li>a mechanic must be an existing, active mechanic;</li>
 *   <li>adding a part deducts stock and saves the line in <em>one transaction</em>,
 *       so the two can never disagree.</li>
 * </ul>
 */
public class JobCardService {

    private final JobCardDao jobCardDao;
    private final JobServiceDao jobServiceDao;
    private final JobPartDao jobPartDao;
    private final VehicleDao vehicleDao;
    private final ServiceItemDao serviceItemDao;
    private final SparePartDao sparePartDao;
    private final MechanicDao mechanicDao;

    public JobCardService() {
        this(new JobCardDao(), new JobServiceDao(), new JobPartDao(),
                new VehicleDao(), new ServiceItemDao(), new SparePartDao(), new MechanicDao());
    }

    public JobCardService(JobCardDao jobCardDao, JobServiceDao jobServiceDao, JobPartDao jobPartDao,
                          VehicleDao vehicleDao, ServiceItemDao serviceItemDao,
                          SparePartDao sparePartDao, MechanicDao mechanicDao) {
        this.jobCardDao = jobCardDao;
        this.jobServiceDao = jobServiceDao;
        this.jobPartDao = jobPartDao;
        this.vehicleDao = vehicleDao;
        this.serviceItemDao = serviceItemDao;
        this.sparePartDao = sparePartDao;
        this.mechanicDao = mechanicDao;
    }

    /** Opens a job card for a vehicle; the status always starts at RECEIVED. */
    public JobCard openJob(int vehicleId, String complaint) throws ValidationException, SQLException {
        String text = InputValidator.trim(complaint);
        if (vehicleId <= 0) {
            throw new ValidationException("vehicle", "Choose the vehicle that came in for service.");
        }
        if (InputValidator.isBlank(text) || text.length() > 2000) {
            throw new ValidationException("complaint", "Describe the customer complaint (max 2000 characters).");
        }
        try (Connection con = DBConnection.getConnection()) {
            if (vehicleDao.findById(con, vehicleId) == null) {
                throw new ValidationException("vehicle", "That vehicle is not registered.");
            }
            JobCard card = jobCardDao.insert(con, new JobCard(vehicleId, text));
            return jobCardDao.findById(con, card.getId());
        }
    }

    /** Assigns an existing, active mechanic to the job. */
    public void assignMechanic(int jobCardId, int mechanicId) throws ValidationException, SQLException {
        try (Connection con = DBConnection.getConnection()) {
            requireJob(con, jobCardId);
            Mechanic mechanic = mechanicDao.findById(con, mechanicId);
            if (mechanic == null) {
                throw new ValidationException("mechanic", "Choose a mechanic from the list.");
            }
            if (!mechanic.isActive()) {
                throw new ValidationException("mechanic", mechanic.getName() + " is not active any more.");
            }
            jobCardDao.assignMechanic(con, jobCardId, mechanicId);
        }
    }

    /** Moves the job exactly one step forward. */
    public JobCard advanceStatus(int jobCardId)
            throws SQLException, ValidationException, InvalidStatusTransitionException {
        try (Connection con = DBConnection.getConnection()) {
            JobCard card = requireJob(con, jobCardId);
            JobStatus next = card.getStatus().next();
            if (next == null) {
                throw new InvalidStatusTransitionException(card.getStatus());
            }
            applyStatus(con, card, next);
            return jobCardDao.findById(con, jobCardId);
        }
    }

    /** Moves the job to a requested status, rejecting skips and backwards moves. */
    public JobCard updateStatus(int jobCardId, JobStatus requested)
            throws SQLException, ValidationException, InvalidStatusTransitionException {
        try (Connection con = DBConnection.getConnection()) {
            JobCard card = requireJob(con, jobCardId);
            if (!card.getStatus().canMoveTo(requested)) {
                throw new InvalidStatusTransitionException(card.getStatus(), requested);
            }
            applyStatus(con, card, requested);
            return jobCardDao.findById(con, jobCardId);
        }
    }

    /**
     * Adds a service line. A {@code null} agreed price means "use the catalogue
     * price"; a different price is accepted because workshops negotiate.
     */
    public void addService(int jobCardId, int serviceId, BigDecimal agreedPrice)
            throws ValidationException, SQLException {
        try (Connection con = DBConnection.getConnection()) {
            requireChangeableJob(con, jobCardId);
            ServiceItem service = serviceItemDao.findById(con, serviceId);
            if (service == null) {
                throw new ValidationException("service", "Choose a service from the catalogue.");
            }
            BigDecimal price = agreedPrice == null ? service.getBasePrice() : agreedPrice;
            if (Money.isNegative(price)) {
                throw new ValidationException("price", "Agreed price cannot be negative.");
            }
            jobServiceDao.add(con, jobCardId, serviceId, price);
        }
    }

    /**
     * Adds a spare part and deducts its stock inside one transaction: either
     * both succeed or neither does.
     *
     * @throws InsufficientStockException when the store does not have enough
     */
    public void addPart(int jobCardId, int partId, int quantity)
            throws ValidationException, SQLException, InsufficientStockException {
        if (!InputValidator.isPositive(quantity)) {
            throw new ValidationException("quantity", "Quantity must be more than zero.");
        }
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                requireChangeableJob(con, jobCardId);
                SparePart part = sparePartDao.findById(con, partId);
                if (part == null) {
                    throw new ValidationException("part", "Choose a spare part first.");
                }
                InventoryService.ensureAvailable(part, quantity);

                // The UPDATE also re-checks the stock, so a second user cannot
                // take the same last unit between our read and our write.
                if (!sparePartDao.deductStock(con, partId, quantity)) {
                    throw new InsufficientStockException(part.getName(), quantity, part.getStockQty());
                }
                jobPartDao.add(con, jobCardId, partId, quantity, part.getUnitPrice());
                con.commit();
            } catch (SQLException | ValidationException | InsufficientStockException e) {
                con.rollback();
                throw e;
            } finally {
                restoreAutoCommit(con);
            }
        }
    }

    public JobCard findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return jobCardDao.findById(con, id);
        }
    }

    /** Service history of a vehicle, newest job first. */
    public List<JobCard> history(int vehicleId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return jobCardDao.findByVehicle(con, vehicleId);
        }
    }

    public List<JobCard> listAll() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return jobCardDao.findAll(con);
        }
    }

    public List<JobServiceLine> servicesOf(int jobCardId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return jobServiceDao.findByJob(con, jobCardId);
        }
    }

    public List<JobPartLine> partsOf(int jobCardId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return jobPartDao.findByJob(con, jobCardId);
        }
    }

    private void applyStatus(Connection con, JobCard card, JobStatus target) throws SQLException {
        LocalDateTime completedOn = target == JobStatus.COMPLETED
                ? LocalDateTime.now()
                : card.getCompletedOn();
        jobCardDao.updateStatus(con, card.getId(), target, completedOn);
    }

    private JobCard requireJob(Connection con, int jobCardId) throws SQLException, ValidationException {
        JobCard card = jobCardDao.findById(con, jobCardId);
        if (card == null) {
            throw new ValidationException("job", "Job card not found.");
        }
        return card;
    }

    /** A delivered job is finished history: money lines may no longer change. */
    private JobCard requireChangeableJob(Connection con, int jobCardId)
            throws SQLException, ValidationException {
        JobCard card = requireJob(con, jobCardId);
        if (card.getStatus() == JobStatus.DELIVERED) {
            throw new ValidationException("job", "This job is already delivered and cannot be edited.");
        }
        return card;
    }

    private void restoreAutoCommit(Connection con) {
        try {
            con.setAutoCommit(true);
        } catch (SQLException ignored) {
            // the connection is closed right after this method anyway
        }
    }
}
