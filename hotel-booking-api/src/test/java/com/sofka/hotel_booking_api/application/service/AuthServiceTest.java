package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.model.User;
import com.sofka.hotel_booking_api.domain.model.UserRole;
import com.sofka.hotel_booking_api.domain.repository.UserRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.LoginRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests unitarios")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        activeUser = new User(
                "Juan Pérez",
                "Recepcionista",
                "juan.perez",
                "$2a$10$hashedPassword",
                "+57 300 1234567",
                "12345678",
                UserRole.RECEPCIONISTA
        );
        activeUser.setId(1L);
        activeUser.setActivo(true);

        validLoginRequest = new LoginRequest("juan.perez", "password123");
    }

    @Test
    @DisplayName("Debe realizar login exitoso con credenciales válidas")
    void shouldLoginSuccessfullyWithValidCredentials() {
        // Given
        when(userRepository.findByUsernameAndActivoTrue("juan.perez"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword"))
                .thenReturn(true);

        // When
        LoginResponse response = authService.login(validLoginRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Juan Pérez", response.nombre());
        assertEquals("Recepcionista", response.cargo());
        assertEquals("juan.perez", response.username());
        assertEquals("+57 300 1234567", response.celular());
        assertEquals("12345678", response.dni());
        assertEquals(UserRole.RECEPCIONISTA, response.role());
        assertNotNull(response.token());

        verify(userRepository, times(1)).findByUsernameAndActivoTrue("juan.perez");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$hashedPassword");
    }

    @Test
    @DisplayName("Debe generar token único en cada login")
    void shouldGenerateUniqueTokenOnEachLogin() {
        // Given
        when(userRepository.findByUsernameAndActivoTrue("juan.perez"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword"))
                .thenReturn(true);

        // When
        LoginResponse response1 = authService.login(validLoginRequest);
        LoginResponse response2 = authService.login(validLoginRequest);

        // Then
        assertNotNull(response1.token());
        assertNotNull(response2.token());
        // Los tokens deben ser diferentes (aunque en producción usaríamos JWT)
        assertNotEquals(response1.token(), response2.token());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByUsernameAndActivoTrue("usuario.inexistente"))
                .thenReturn(Optional.empty());

        LoginRequest invalidRequest = new LoginRequest("usuario.inexistente", "password123");

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(invalidRequest)
        );

        assertEquals("Usuario o contraseña incorrectos", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameAndActivoTrue("usuario.inexistente");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la contraseña es incorrecta")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        when(userRepository.findByUsernameAndActivoTrue("juan.perez"))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("passwordIncorrecta", "$2a$10$hashedPassword"))
                .thenReturn(false);

        LoginRequest invalidRequest = new LoginRequest("juan.perez", "passwordIncorrecta");

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(invalidRequest)
        );

        assertEquals("Usuario o contraseña incorrectos", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameAndActivoTrue("juan.perez");
        verify(passwordEncoder, times(1)).matches("passwordIncorrecta", "$2a$10$hashedPassword");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario está inactivo")
    void shouldThrowExceptionWhenUserIsInactive() {
        // Given - Usuario inactivo no se encuentra con findByUsernameAndActivoTrue
        when(userRepository.findByUsernameAndActivoTrue("usuario.inactivo"))
                .thenReturn(Optional.empty());

        LoginRequest inactiveUserRequest = new LoginRequest("usuario.inactivo", "password123");

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(inactiveUserRequest)
        );

        assertEquals("Usuario o contraseña incorrectos", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameAndActivoTrue("usuario.inactivo");
    }

    @Test
    @DisplayName("Debe obtener usuario por ID exitosamente")
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        // When
        User result = authService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("juan.perez", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario por ID no existe")
    void shouldThrowExceptionWhenUserByIdNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.getUserById(999L)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe obtener usuario por username exitosamente")
    void shouldGetUserByUsernameSuccessfully() {
        // Given
        when(userRepository.findByUsernameAndActivoTrue("juan.perez"))
                .thenReturn(Optional.of(activeUser));

        // When
        User result = authService.getUserByUsername("juan.perez");

        // Then
        assertNotNull(result);
        assertEquals("juan.perez", result.getUsername());
        verify(userRepository, times(1)).findByUsernameAndActivoTrue("juan.perez");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario por username no existe")
    void shouldThrowExceptionWhenUserByUsernameNotFound() {
        // Given
        when(userRepository.findByUsernameAndActivoTrue("usuario.inexistente"))
                .thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.getUserByUsername("usuario.inexistente")
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository, times(1)).findByUsernameAndActivoTrue("usuario.inexistente");
    }

    @Test
    @DisplayName("Debe retornar todos los datos del usuario en LoginResponse")
    void shouldReturnAllUserDataInLoginResponse() {
        // Given
        User adminUser = new User(
                "Admin User",
                "Administrador",
                "admin",
                "$2a$10$hashed",
                "+57 300 9999999",
                "87654321",
                UserRole.ADMINISTRADOR
        );
        adminUser.setId(2L);

        when(userRepository.findByUsernameAndActivoTrue("admin"))
                .thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("admin123", "$2a$10$hashed"))
                .thenReturn(true);

        LoginRequest adminRequest = new LoginRequest("admin", "admin123");

        // When
        LoginResponse response = authService.login(adminRequest);

        // Then
        assertEquals(2L, response.id());
        assertEquals("Admin User", response.nombre());
        assertEquals("Administrador", response.cargo());
        assertEquals("admin", response.username());
        assertEquals("+57 300 9999999", response.celular());
        assertEquals("87654321", response.dni());
        assertEquals(UserRole.ADMINISTRADOR, response.role());
        assertNotNull(response.token());
    }
}

