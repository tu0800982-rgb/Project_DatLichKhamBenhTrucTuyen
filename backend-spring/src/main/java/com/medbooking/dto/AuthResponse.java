package com.medbooking.dto;

import com.medbooking.model.UserResponse;

public record AuthResponse(String token, UserResponse user) {
}
