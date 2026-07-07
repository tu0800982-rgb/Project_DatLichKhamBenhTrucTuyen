package com.medbooking.model;

public record Appointment(
        long id,
        String date,
        String time,
        String status,
        long doctor_id,
        String doctor_name,
        String specialty
) {
}
