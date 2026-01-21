package com.sofka.hotel_booking_api.infrastructure.dto;

import com.sofka.hotel_booking_api.domain.model.UserRole;

public record LoginResponse(
    Long id,
    String nombre,
    String cargo,
    String username,
    String celular,
    String dni,
    UserRole role,
    String token
) {
}

