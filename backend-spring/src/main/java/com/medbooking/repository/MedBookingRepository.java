package com.medbooking.repository;

import com.medbooking.model.Appointment;
import com.medbooking.model.Doctor;
import com.medbooking.model.Specialty;
import com.medbooking.model.UserRecord;
import com.medbooking.model.UserResponse;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class MedBookingRepository {
    private final JdbcTemplate jdbc;

    public MedBookingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void healthCheck() {
        jdbc.queryForObject("SELECT 1", Integer.class);
    }

    public List<Specialty> findSpecialties() {
        return jdbc.query(
                "SELECT id, name, icon, description FROM specialties ORDER BY id",
                (rs, rowNum) -> new Specialty(rs.getLong("id"), rs.getString("name"), rs.getString("icon"), rs.getString("description"))
        );
    }

    public List<Doctor> findDoctors(String search, String specialty, Integer limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT d.id, d.name, s.name AS specialty, d.experience, d.rating, d.image, d.description
                FROM doctors d JOIN specialties s ON s.id = d.specialty_id WHERE 1=1
                """);
        List<Object> values = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (d.name LIKE ? OR s.name LIKE ?)");
            values.add("%" + search + "%");
            values.add("%" + search + "%");
        }
        if (specialty != null && !specialty.isBlank()) {
            sql.append(" AND s.name = ?");
            values.add(specialty);
        }
        sql.append(" ORDER BY d.id");
        if (limit != null) {
            sql.append(" LIMIT ?");
            values.add(limit);
        }
        return jdbc.query(sql.toString(), (rs, rowNum) -> new Doctor(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("specialty"),
                rs.getInt("experience"),
                rs.getBigDecimal("rating"),
                rs.getString("image"),
                rs.getString("description")
        ), values.toArray());
    }

    public Optional<Doctor> findDoctor(long id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject("""
                    SELECT d.id, d.name, s.name AS specialty, d.experience, d.rating, d.image, d.description
                    FROM doctors d JOIN specialties s ON s.id = d.specialty_id WHERE d.id = ?
                    """, (rs, rowNum) -> new Doctor(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("specialty"),
                    rs.getInt("experience"),
                    rs.getBigDecimal("rating"),
                    rs.getString("image"),
                    rs.getString("description")
            ), id));
        } catch (EmptyResultDataAccessException error) {
            return Optional.empty();
        }
    }

    public long createUser(String name, String email, String phone, String passwordHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (name, email, phone, password_hash) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, passwordHash);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Optional<UserRecord> findUserByEmail(String email) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(
                    "SELECT id, name, email, phone, password_hash FROM users WHERE email = ?",
                    (rs, rowNum) -> new UserRecord(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("password_hash")
                    ),
                    email
            ));
        } catch (EmptyResultDataAccessException error) {
            return Optional.empty();
        }
    }

    public Optional<UserResponse> findUser(long id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(
                    "SELECT id, name, email, phone FROM users WHERE id = ?",
                    (rs, rowNum) -> new UserResponse(rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getString("phone")),
                    id
            ));
        } catch (EmptyResultDataAccessException error) {
            return Optional.empty();
        }
    }

    public boolean doctorExists(long id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM doctors WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public List<Appointment> findAppointments(long userId) {
        return jdbc.query("""
                SELECT a.id, a.appointment_date AS date, a.appointment_time AS time, a.status,
                d.id AS doctor_id, d.name AS doctor_name, s.name AS specialty
                FROM appointments a JOIN doctors d ON d.id = a.doctor_id JOIN specialties s ON s.id = d.specialty_id
                WHERE a.user_id = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC
                """, (rs, rowNum) -> new Appointment(
                rs.getLong("id"),
                rs.getDate("date").toLocalDate().toString(),
                rs.getTime("time").toLocalTime().toString(),
                rs.getString("status"),
                rs.getLong("doctor_id"),
                rs.getString("doctor_name"),
                rs.getString("specialty")
        ), userId);
    }

    public long createAppointment(long userId, long doctorId, LocalDate date, LocalTime time) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO appointments (user_id, doctor_id, appointment_date, appointment_time) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, userId);
            ps.setLong(2, doctorId);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, Time.valueOf(time));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public boolean cancelAppointment(long appointmentId, long userId) {
        int updated = jdbc.update(
                "UPDATE appointments SET status = 'cancelled' WHERE id = ? AND user_id = ? AND status = 'confirmed'",
                appointmentId,
                userId
        );
        return updated > 0;
    }
}
