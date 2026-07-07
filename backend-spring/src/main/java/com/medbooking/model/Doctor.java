package com.medbooking.model;

import java.math.BigDecimal;

public record Doctor(
        long id,
        String name,
        String specialty,
        int experience,
        BigDecimal rating,
        String image,
        String description
) {
}
