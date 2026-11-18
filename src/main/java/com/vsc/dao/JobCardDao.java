package com.vsc.dao;

import com.vsc.model.JobCard;
import com.vsc.model.JobStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL for the {@code job_cards} table. The services and parts added to a
 * job live in {@code job_services} / {@code job_parts} and are handled by
 * {@link JobServiceDao} and {@link JobPartDao}.
 */
public class JobCardDao {

    private static final String SELECT_ALL =
            "SELECT id, vehicle_id, mechanic_id, complaint, status, received_on, completed_on FROM job_cards";

    /**
     * Inserts a job card; the database fills {@code received_on} with the
     * current time. Reload with {@link #findById} to get the stored dates.
     */
    public JobCard insert(Connection con, JobCard card) throws SQLException {
        String sql = "INSERT INTO job_cards (vehicle_id, mechanic_id, complaint, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, card.getVehicleId());
            setMechanicId(ps, 2, card.getMechanicId());
            ps.setString(3, card.getComplaint());
            ps.setString(4, card.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    card.setId(keys.getInt(1));
                }
            }
        }
        return card;
    }

    public JobCard findById(Connection con, int id) throws SQLException {
        String sql = SELECT_ALL + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Service history of one vehicle, newest first. */
    public List<JobCard> findByVehicle(Connection con, int vehicleId) throws SQLException {
        String sql = SELECT_ALL + " WHERE vehicle_id = ? ORDER BY received_on DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            return manyRows(ps);
        }
    }

    public List<JobCard> findByStatus(Connection con, JobStatus status) throws SQLException {
        String sql = SELECT_ALL + " WHERE status = ? ORDER BY received_on";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            return manyRows(ps);
        }
    }

    public List<JobCard> findAll(Connection con) throws SQLException {
        String sql = SELECT_ALL + " ORDER BY received_on DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return manyRows(ps);
        }
    }

    /** Writes the new status and, for COMPLETED, the completion time. */
    public void updateStatus(Connection con, int jobCardId, JobStatus status, LocalDateTime completedOn)
            throws SQLException {
        String sql = "UPDATE job_cards SET status = ?, completed_on = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            if (completedOn == null) {
                ps.setNull(2, Types.TIMESTAMP);
            } else {
                ps.setObject(2, completedOn);
            }
            ps.setInt(3, jobCardId);
            ps.executeUpdate();
        }
    }

    /** Mechanic id 0 is stored as NULL, meaning "not assigned yet". */
    public void assignMechanic(Connection con, int jobCardId, int mechanicId) throws SQLException {
        String sql = "UPDATE job_cards SET mechanic_id = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            setMechanicId(ps, 1, mechanicId);
            ps.setInt(2, jobCardId);
            ps.executeUpdate();
        }
    }

    private void setMechanicId(PreparedStatement ps, int index, int mechanicId) throws SQLException {
        if (mechanicId > 0) {
            ps.setInt(index, mechanicId);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    private List<JobCard> manyRows(PreparedStatement ps) throws SQLException {
        List<JobCard> cards = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cards.add(map(rs));
            }
        }
        return cards;
    }

    private JobCard map(ResultSet rs) throws SQLException {
        return new JobCard(
                rs.getInt("id"),
                rs.getInt("vehicle_id"),
                rs.getInt("mechanic_id"),
                rs.getString("complaint"),
                JobStatus.valueOf(rs.getString("status")),
                rs.getObject("received_on", LocalDateTime.class),
                rs.getObject("completed_on", LocalDateTime.class));
    }
}
