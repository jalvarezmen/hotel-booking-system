package com.sofka.hotel_booking_api.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.hotel_booking_api.application.service.AuthService;
import com.sofka.hotel_booking_api.domain.model.UserRole;
import com.sofka.hotel_booking_api.infrastructure.dto.LoginRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para AuthController.
 */
@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
        })
@Import(com.sofka.hotel_booking_api.infrastructure.exception.GlobalExceptionHandler.class)
@DisplayName("AuthController - Tests unitarios")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private LoginRequest validLoginRequest;
    private LoginResponse expectedResponse;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest("juan.perez", "password123");

        expectedResponse = new LoginResponse(
                1L,
                "Juan Pérez",
                "Recepcionista",
                "juan.perez",
                "+57 300 1234567",
                "12345678",
                UserRole.RECEPCIONISTA,
                "token-12345"
        );
    }

    @Test
    @DisplayName("POST /api/auth/login debe retornar 200 OK con credenciales válidas")
    void shouldReturn200OkWithValidCredentials() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.cargo").value("Recepcionista"))
                .andExpect(jsonPath("$.username").value("juan.perez"))
                .andExpect(jsonPath("$.celular").value("+57 300 1234567"))
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.role").value("RECEPCIONISTA"))
                .andExpect(jsonPath("$.token").value("token-12345"));
    }

    @Test
    @DisplayName("POST /api/auth/login debe retornar 400 cuando faltan campos obligatorios")
    void shouldReturn400WhenMissingRequiredFields() throws Exception {
        // Given - Request sin username
        LoginRequest invalidRequest = new LoginRequest(null, "password123");

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login debe retornar 400 cuando las credenciales son inválidas")
    void shouldReturn400WhenCredentialsAreInvalid() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Usuario o contraseña incorrectos"));

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
    }

    @Test
    @DisplayName("POST /api/auth/login debe validar formato JSON")
    void shouldValidateJsonFormat() throws Exception {
        // Given - JSON inválido
        String invalidJson = "{ username: \"juan.perez\", password: }";

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login debe retornar token en la respuesta")
    void shouldReturnTokenInResponse() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login debe retornar todos los datos del usuario")
    void shouldReturnAllUserData() throws Exception {
        // Given
        LoginResponse fullResponse = new LoginResponse(
                2L,
                "María García",
                "Administradora",
                "maria.garcia",
                "+57 300 9876543",
                "87654321",
                UserRole.ADMINISTRADOR,
                "admin-token-789"
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(fullResponse);

        LoginRequest adminRequest = new LoginRequest("maria.garcia", "admin123");

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nombre").value("María García"))
                .andExpect(jsonPath("$.cargo").value("Administradora"))
                .andExpect(jsonPath("$.username").value("maria.garcia"))
                .andExpect(jsonPath("$.celular").value("+57 300 9876543"))
                .andExpect(jsonPath("$.dni").value("87654321"))
                .andExpect(jsonPath("$.role").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.token").value("admin-token-789"));
    }
}

