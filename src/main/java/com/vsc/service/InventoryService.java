package com.vsc.service;

import com.vsc.dao.SparePartDao;
import com.vsc.exception.InsufficientStockException;
import com.vsc.exception.ValidationException;
import com.vsc.model.SparePart;
import com.vsc.util.DBConnection;
import com.vsc.util.InputValidator;
import com.vsc.util.Money;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Business rules for the spare-part store: adding parts, receiving stock and the
 * rule that stock may never go negative.
 */
public class InventoryService {

    private final SparePartDao partDao;

    public InventoryService() {
        this(new SparePartDao());
    }

    public InventoryService(SparePartDao partDao) {
        this.partDao = partDao;
    }

    public SparePart addPart(SparePart part) throws ValidationException, SQLException {
        validate(part);
        try (Connection con = DBConnection.getConnection()) {
            if (partDao.findByPartNumber(con, part.getPartNumber()) != null) {
                throw new ValidationException("partNumber",
                        "Part number " + part.getPartNumber() + " already exists.");
            }
            return partDao.insert(con, part);
        }
    }

    public void updatePart(SparePart part) throws ValidationException, SQLException {
        if (part.getId() <= 0) {
            throw new ValidationException("id", "Update needs a part that was already saved.");
        }
        validate(part);
        try (Connection con = DBConnection.getConnection()) {
            if (partDao.findById(con, part.getId()) == null) {
                throw new ValidationException("id", "Spare part no longer exists.");
            }
            partDao.update(con, part);
        }
    }

    /** Adds received units to the stock of a saved part. */
    public void receiveStock(int partId, int quantity) throws ValidationException, SQLException {
        if (!InputValidator.isPositive(quantity)) {
            throw new ValidationException("quantity", "Received quantity must be more than zero.");
        }
        try (Connection con = DBConnection.getConnection()) {
            if (partDao.findById(con, partId) == null) {
                throw new ValidationException("part", "Choose a spare part first.");
            }
            partDao.addStock(con, partId, quantity);
        }
    }

    public List<SparePart> listAll() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return partDao.findAll(con);
        }
    }

    /** Parts at or below their reorder level - shown as the low-stock warning. */
    public List<SparePart> listLowStock() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return partDao.findLowStock(con);
        }
    }

    /**
     * Pure stock rule, testable without a database. The job-card service calls
     * it before deducting stock; the DAO then re-checks the same condition in
     * its UPDATE so two users cannot oversell the last unit.
     */
    public static void ensureAvailable(SparePart part, int quantity) throws InsufficientStockException {
        if (!part.canFulfil(quantity)) {
            throw new InsufficientStockException(part.getName(), quantity, part.getStockQty());
        }
    }

    private void validate(SparePart part) throws ValidationException {
        if (part == null) {
            throw new ValidationException("part", "No spare-part details were supplied.");
        }
        part.setPartNumber(part.getPartNumber() == null
                ? ""
                : part.getPartNumber().trim().toUpperCase(Locale.ROOT));
        part.setName(InputValidator.trim(part.getName()));

        if (InputValidator.isBlank(part.getPartNumber()) || part.getPartNumber().length() > 30) {
            throw new ValidationException("partNumber", "Part number is required (max 30 characters).");
        }
        if (InputValidator.isBlank(part.getName()) || part.getName().length() > 100) {
            throw new ValidationException("name", "Part name is required (max 100 characters).");
        }
        if (part.getUnitPrice() == null || Money.isNegative(part.getUnitPrice())) {
            throw new ValidationException("price", "Unit price cannot be negative.");
        }
        if (part.getUnitPrice().compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new ValidationException("price", "Unit price is too large.");
        }
        if (part.getStockQty() < 0) {
            throw new ValidationException("stock", "Stock cannot be negative.");
        }
        if (part.getReorderLevel() < 0) {
            throw new ValidationException("reorder", "Reorder level cannot be negative.");
        }
    }
}
