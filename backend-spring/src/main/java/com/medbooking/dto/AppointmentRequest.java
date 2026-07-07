package com.medbooking.dto;

public record AppointmentRequest(Integer doctorId, String date, String time) {
}
