package com.sofka.hotel_booking_api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Configuración para establecer la zona horaria de Colombia (America/Bogota, UTC-5).
 * Esto asegura que todas las fechas y horas se manejen según el horario colombiano.
 */
@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        // Establecer la zona horaria por defecto del sistema a Colombia
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
    }
}

