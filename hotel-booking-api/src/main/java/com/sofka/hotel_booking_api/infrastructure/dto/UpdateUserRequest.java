package com.sofka.hotel_booking_api.infrastructure.dto;

import com.sofka.hotel_booking_api.domain.model.UserRole;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(max = 100)
    String nombre,
    
    @Size(max = 50)
    String cargo,
    
    @Size(min = 3, max = 50)
    String username,
    
    @Size(min = 6)
    String password,
    
    String celular,
    
    String dni,
    
    UserRole role,
    
    Boolean activo
) {
}

