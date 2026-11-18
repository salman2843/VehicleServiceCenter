package com.vsc.service;

import com.vsc.dao.ServiceItemDao;
import com.vsc.exception.ValidationException;
import com.vsc.model.ServiceItem;
import com.vsc.util.DBConnection;
import com.vsc.util.InputValidator;
import com.vsc.util.Money;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Business rules for the service catalogue (labour charges). A job card copies
 * the agreed price, so editing the catalogue later never changes old bills.
 */
public class ServiceCatalogService {

    private final ServiceItemDao serviceItemDao;

    public ServiceCatalogService() {
        this(new ServiceItemDao());
    }

    public ServiceCatalogService(ServiceItemDao serviceItemDao) {
        this.serviceItemDao = serviceItemDao;
    }

    public ServiceItem add(ServiceItem service) throws ValidationException, SQLException {
        validate(service);
        try (Connection con = DBConnection.getConnection()) {
            return serviceItemDao.insert(con, service);
        }
    }

    public ServiceItem findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return serviceItemDao.findById(con, id);
        }
    }

    public List<ServiceItem> listAll() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return serviceItemDao.findAll(con);
        }
    }

    private void validate(ServiceItem service) throws ValidationException {
        if (service == null) {
            throw new ValidationException("service", "No service details were supplied.");
        }
        service.setName(InputValidator.trim(service.getName()));
        service.setDetails(InputValidator.trim(service.getDetails()));

        if (InputValidator.isBlank(service.getName()) || service.getName().length() > 100) {
            throw new ValidationException("name", "Service name is required (max 100 characters).");
        }
        if (service.getDetails() != null && service.getDetails().length() > 255) {
            throw new ValidationException("details", "Details are too long (max 255).");
        }
        if (service.getBasePrice() == null || Money.isNegative(service.getBasePrice())) {
            throw new ValidationException("price", "Base price cannot be negative.");
        }
    }
}
