package com.sofka.hotel_booking_api.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.hotel_booking_api.application.service.UserService;
import com.sofka.hotel_booking_api.domain.model.UserRole;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateUserRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.UpdateUserRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para UserController.
 */
@WebMvcTest(controllers = UserController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
        })
@Import(com.sofka.hotel_booking_api.infrastructure.exception.GlobalExceptionHandler.class)
@DisplayName("UserController - Tests unitarios")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private CreateUserRequest createRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CreateUserRequest(
                "Juan Pérez",
                "Recepcionista",
                "juan.perez",
                "password123",
                "+57 300 1234567",
                "12345678",
                UserRole.RECEPCIONISTA
        );

        userResponse = new UserResponse(
                1L,
                "Juan Pérez",
                "Recepcionista",
                "juan.perez",
                "+57 300 1234567",
                "12345678",
                UserRole.RECEPCIONISTA,
                true
        );
    }

    // ============================================
    // Tests para POST /api/users
    // ============================================

    @Test
    @DisplayName("POST /api/users debe crear usuario y retornar 201 Created")
    void shouldCreateUserAndReturn201() throws Exception {
        // Given
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userResponse);

        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.cargo").value("Recepcionista"))
                .andExpect(jsonPath("$.username").value("juan.perez"))
                .andExpect(jsonPath("$.role").value("RECEPCIONISTA"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("POST /api/users debe retornar 400 cuando faltan campos obligatorios")
    void shouldReturn400WhenMissingRequiredFields() throws Exception {
        // Given - Request sin nombre
        CreateUserRequest invalidRequest = new CreateUserRequest(
                null, // Nombre faltante
                "Cargo",
                "username",
                "password123",
                null, null,
                UserRole.RECEPCIONISTA
        );

        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users debe retornar 400 cuando el username ya existe")
    void shouldReturn400WhenUsernameAlreadyExists() throws Exception {
        // Given
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new IllegalArgumentException("El nombre de usuario ya existe"));

        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.message").value("El nombre de usuario ya existe"));
    }

    // ============================================
    // Tests para GET /api/users
    // ============================================

    @Test
    @DisplayName("GET /api/users debe retornar lista de usuarios con 200 OK")
    void shouldReturnListOfUsersWith200() throws Exception {
        // Given
        UserResponse user1 = new UserResponse(1L, "Usuario 1", "Cargo 1", "user1", null, null, UserRole.RECEPCIONISTA, true);
        UserResponse user2 = new UserResponse(2L, "Usuario 2", "Cargo 2", "user2", null, null, UserRole.ADMINISTRADOR, true);
        List<UserResponse> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        // When/Then
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /api/users debe retornar lista vacía cuando no hay usuarios")
    void shouldReturnEmptyListWhenNoUsers() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        // When/Then
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ============================================
    // Tests para GET /api/users/{id}
    // ============================================

    @Test
    @DisplayName("GET /api/users/{id} debe retornar usuario con 200 OK")
    void shouldReturnUserByIdWith200() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(userResponse);

        // When/Then
        mockMvc.perform(get("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.username").value("juan.perez"));
    }

    @Test
    @DisplayName("GET /api/users/{id} debe retornar 400 cuando el usuario no existe")
    void shouldReturn400WhenUserNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        // When/Then
        mockMvc.perform(get("/api/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    // ============================================
    // Tests para GET /api/users/role/{role}
    // ============================================

    @Test
    @DisplayName("GET /api/users/role/{role} debe retornar usuarios filtrados por rol")
    void shouldReturnUsersFilteredByRole() throws Exception {
        // Given
        UserResponse receptionist = new UserResponse(1L, "Recepcionista 1", "Cargo", "rec1", null, null, UserRole.RECEPCIONISTA, true);
        List<UserResponse> users = Collections.singletonList(receptionist);

        when(userService.getUsersByRole(UserRole.RECEPCIONISTA)).thenReturn(users);

        // When/Then
        mockMvc.perform(get("/api/users/role/{role}", "RECEPCIONISTA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("RECEPCIONISTA"));
    }

    // ============================================
    // Tests para PUT /api/users/{id}
    // ============================================

    @Test
    @DisplayName("PUT /api/users/{id} debe actualizar usuario y retornar 200 OK")
    void shouldUpdateUserAndReturn200() throws Exception {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Juan Pérez Actualizado",
                "Supervisor",
                "juan.perez.actualizado",
                "newPassword123",
                "+57 300 9999999",
                "99999999",
                UserRole.ADMINISTRADOR,
                true
        );

        UserResponse updatedResponse = new UserResponse(
                1L,
                "Juan Pérez Actualizado",
                "Supervisor",
                "juan.perez.actualizado",
                "+57 300 9999999",
                "99999999",
                UserRole.ADMINISTRADOR,
                true
        );

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedResponse);

        // When/Then
        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez Actualizado"))
                .andExpect(jsonPath("$.cargo").value("Supervisor"))
                .andExpect(jsonPath("$.username").value("juan.perez.actualizado"))
                .andExpect(jsonPath("$.role").value("ADMINISTRADOR"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} debe retornar 400 cuando el usuario no existe")
    void shouldReturn400WhenUpdatingNonExistentUser() throws Exception {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Nuevo Nombre", null, null, null, null, null, null, null
        );

        when(userService.updateUser(eq(999L), any(UpdateUserRequest.class)))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        // When/Then
        mockMvc.perform(put("/api/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} debe retornar 400 cuando el username ya existe")
    void shouldReturn400WhenUpdatingToExistingUsername() throws Exception {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null, null, "usuario.existente", null, null, null, null, null
        );

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenThrow(new IllegalArgumentException("El nombre de usuario ya existe"));

        // When/Then
        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El nombre de usuario ya existe"));
    }

    // ============================================
    // Tests para DELETE /api/users/{id}
    // ============================================

    @Test
    @DisplayName("DELETE /api/users/{id} debe eliminar usuario y retornar 204 No Content")
    void shouldDeleteUserAndReturn204() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When/Then
        mockMvc.perform(delete("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} debe retornar 400 cuando el usuario no existe")
    void shouldReturn400WhenDeletingNonExistentUser() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Usuario no encontrado"))
                .when(userService).deleteUser(999L);

        // When/Then
        mockMvc.perform(delete("/api/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    @Test
    @DisplayName("POST /api/users debe validar tamaño mínimo de contraseña")
    void shouldValidateMinimumPasswordLength() throws Exception {
        // Given - Contraseña muy corta
        CreateUserRequest invalidRequest = new CreateUserRequest(
                "Nombre",
                "Cargo",
                "username",
                "12345", // Menos de 6 caracteres
                null, null,
                UserRole.RECEPCIONISTA
        );

        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users debe validar tamaño mínimo de username")
    void shouldValidateMinimumUsernameLength() throws Exception {
        // Given - Username muy corto
        CreateUserRequest invalidRequest = new CreateUserRequest(
                "Nombre",
                "Cargo",
                "ab", // Menos de 3 caracteres
                "password123",
                null, null,
                UserRole.RECEPCIONISTA
        );

        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}

