package com.sofka.hotel_booking_api.infrastructure.dto;

import com.sofka.hotel_booking_api.domain.model.User;
import com.sofka.hotel_booking_api.domain.model.UserRole;

public record UserResponse(
    Long id,
    String nombre,
    String cargo,
    String username,
    String celular,
    String dni,
    UserRole role,
    Boolean activo
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getNombre(),
            user.getCargo(),
            user.getUsername(),
            user.getCelular(),
            user.getDni(),
            user.getRole(),
            user.getActivo()
        );
    }
}

