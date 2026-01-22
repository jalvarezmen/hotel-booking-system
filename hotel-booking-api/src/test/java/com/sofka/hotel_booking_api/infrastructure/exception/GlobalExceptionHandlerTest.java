package com.sofka.hotel_booking_api.infrastructure.exception;

import com.sofka.hotel_booking_api.domain.exception.DuplicateRoomNumberException;
import com.sofka.hotel_booking_api.domain.exception.InvalidDateRangeException;
import com.sofka.hotel_booking_api.domain.exception.ReservationNotFoundException;
import com.sofka.hotel_booking_api.domain.exception.RoomNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para GlobalExceptionHandler.
 */
@DisplayName("GlobalExceptionHandler - Tests unitarios")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Debe manejar DuplicateRoomNumberException y retornar 409 CONFLICT")
    void shouldHandleDuplicateRoomNumberException() {
        // Given
        DuplicateRoomNumberException ex = new DuplicateRoomNumberException("301");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleDuplicateRoomNumber(ex);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Número de habitación duplicado", response.getBody().error());
        assertNotNull(response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Debe manejar RoomNotFoundException y retornar 404 NOT FOUND")
    void shouldHandleRoomNotFoundException() {
        // Given
        RoomNotFoundException ex = new RoomNotFoundException(999L);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleRoomNotFound(ex);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Habitación no encontrada", response.getBody().error());
        assertNotNull(response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Debe manejar InvalidDateRangeException y retornar 400 BAD REQUEST")
    void shouldHandleInvalidDateRangeException() {
        // Given
        InvalidDateRangeException ex = new InvalidDateRangeException("La fecha de entrada debe ser anterior a la fecha de salida");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleInvalidDateRange(ex);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Rango de fechas inválido", response.getBody().error());
        assertEquals("La fecha de entrada debe ser anterior a la fecha de salida", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Debe manejar ReservationNotFoundException y retornar 404 NOT FOUND")
    void shouldHandleReservationNotFoundException() {
        // Given
        ReservationNotFoundException ex = new ReservationNotFoundException(999L);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleReservationNotFound(ex);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Reserva no encontrada", response.getBody().error());
        assertNotNull(response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Debe manejar IllegalStateException y retornar 400 BAD REQUEST")
    void shouldHandleIllegalStateException() {
        // Given
        IllegalStateException ex = new IllegalStateException("Operación no permitida en este estado");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleIllegalState(ex);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Operación no permitida", response.getBody().error());
        assertEquals("Operación no permitida en este estado", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("Debe manejar IllegalArgumentException y retornar 400 BAD REQUEST")
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Datos inválidos proporcionados");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleIllegalArgument(ex);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Datos inválidos", response.getBody().error());
        assertEquals("Datos inválidos proporcionados", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }
}

