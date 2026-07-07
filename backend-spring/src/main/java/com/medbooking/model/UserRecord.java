package com.medbooking.model;

public record UserRecord(long id, String name, String email, String phone, String password_hash) {
}
