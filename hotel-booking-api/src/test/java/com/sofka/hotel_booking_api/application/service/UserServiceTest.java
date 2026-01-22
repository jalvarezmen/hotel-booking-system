package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.model.User;
import com.sofka.hotel_booking_api.domain.model.UserRole;
import com.sofka.hotel_booking_api.domain.repository.UserRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateUserRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.UpdateUserRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UserService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Tests unitarios")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User existingUser;
    private CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        existingUser = new User(
                "Juan Pérez",
                "Recepcionista",
                "juan.perez",
                "$2a$10$hashedPassword",
                "+57 300 1234567",
                "12345678",
                UserRole.RECEPCIONISTA
        );
        existingUser.setId(1L);
        existingUser.setActivo(true);

        createRequest = new CreateUserRequest(
                "María García",
                "Administradora",
                "maria.garcia",
                "password123",
                "+57 300 9876543",
                "87654321",
                UserRole.ADMINISTRADOR
        );
    }

    // ============================================
    // Tests para createUser()
    // ============================================

    @Test
    @DisplayName("Debe crear usuario exitosamente cuando el username no existe")
    void shouldCreateUserSuccessfullyWhenUsernameDoesNotExist() {
        // Given
        when(userRepository.existsByUsername("maria.garcia")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        
        User savedUser = new User(
                createRequest.nombre(),
                createRequest.cargo(),
                createRequest.username(),
                "$2a$10$encodedPassword",
                createRequest.celular(),
                createRequest.dni(),
                createRequest.role()
        );
        savedUser.setId(2L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserResponse response = userService.createUser(createRequest);

        // Then
        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("María García", response.nombre());
        assertEquals("Administradora", response.cargo());
        assertEquals("maria.garcia", response.username());
        assertEquals("+57 300 9876543", response.celular());
        assertEquals("87654321", response.dni());
        assertEquals(UserRole.ADMINISTRADOR, response.role());
        assertTrue(response.activo());

        verify(userRepository, times(1)).existsByUsername("maria.garcia");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el username ya existe")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("juan.perez")).thenReturn(true);

        CreateUserRequest duplicateRequest = new CreateUserRequest(
                "Otro Nombre",
                "Otro Cargo",
                "juan.perez", // Username duplicado
                "password123",
                "+57 300 1111111",
                "11111111",
                UserRole.RECEPCIONISTA
        );

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(duplicateRequest)
        );

        assertEquals("El nombre de usuario ya existe", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("juan.perez");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debe codificar la contraseña antes de guardar")
    void shouldEncodePasswordBeforeSaving() {
        // Given
        when(userRepository.existsByUsername("maria.garcia")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        
        User savedUser = new User(
                createRequest.nombre(),
                createRequest.cargo(),
                createRequest.username(),
                "$2a$10$encodedPassword",
                createRequest.celular(),
                createRequest.dni(),
                createRequest.role()
        );
        savedUser.setId(2L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        userService.createUser(createRequest);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertEquals("$2a$10$encodedPassword", capturedUser.getPassword());
        assertNotEquals("password123", capturedUser.getPassword()); // No debe guardar la contraseña en texto plano
    }

    // ============================================
    // Tests para getAllUsers()
    // ============================================

    @Test
    @DisplayName("Debe retornar todos los usuarios activos")
    void shouldReturnAllActiveUsers() {
        // Given
        User user1 = new User("Usuario 1", "Cargo 1", "user1", "pass1", null, null, UserRole.RECEPCIONISTA);
        user1.setId(1L);
        User user2 = new User("Usuario 2", "Cargo 2", "user2", "pass2", null, null, UserRole.ADMINISTRADOR);
        user2.setId(2L);

        when(userRepository.findByActivoTrue()).thenReturn(Arrays.asList(user1, user2));

        // When
        List<UserResponse> users = userService.getAllUsers();

        // Then
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("Usuario 1", users.get(0).nombre());
        assertEquals("Usuario 2", users.get(1).nombre());
        verify(userRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay usuarios activos")
    void shouldReturnEmptyListWhenNoActiveUsers() {
        // Given
        when(userRepository.findByActivoTrue()).thenReturn(Collections.emptyList());

        // When
        List<UserResponse> users = userService.getAllUsers();

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(userRepository, times(1)).findByActivoTrue();
    }

    // ============================================
    // Tests para getUserById()
    // ============================================

    @Test
    @DisplayName("Debe retornar usuario por ID exitosamente")
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        UserResponse response = userService.getUserById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Juan Pérez", response.nombre());
        assertEquals("juan.perez", response.username());
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
                () -> userService.getUserById(999L)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
    }

    // ============================================
    // Tests para updateUser()
    // ============================================

    @Test
    @DisplayName("Debe actualizar usuario exitosamente con todos los campos")
    void shouldUpdateUserSuccessfullyWithAllFields() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        
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
        
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newEncoded");
        when(userRepository.existsByUsername("juan.perez.actualizado")).thenReturn(false);
        
        User updatedUser = new User(
                "Juan Pérez Actualizado",
                "Supervisor",
                "juan.perez.actualizado",
                "$2a$10$newEncoded",
                "+57 300 9999999",
                "99999999",
                UserRole.ADMINISTRADOR
        );
        updatedUser.setId(1L);
        updatedUser.setActivo(true);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserResponse response = userService.updateUser(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("Juan Pérez Actualizado", response.nombre());
        assertEquals("Supervisor", response.cargo());
        assertEquals("juan.perez.actualizado", response.username());
        assertEquals("+57 300 9999999", response.celular());
        assertEquals("99999999", response.dni());
        assertEquals(UserRole.ADMINISTRADOR, response.role());
        assertTrue(response.activo());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe actualizar solo los campos proporcionados (parcial)")
    void shouldUpdateOnlyProvidedFields() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        
        UpdateUserRequest partialUpdate = new UpdateUserRequest(
                "Juan Pérez Actualizado",
                null, // No actualizar cargo
                null, // No actualizar username
                null, // No actualizar password
                "+57 300 8888888", // Solo actualizar celular
                null, // No actualizar DNI
                null, // No actualizar role
                null // No actualizar activo
        );
        
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        UserResponse response = userService.updateUser(1L, partialUpdate);

        // Then
        assertNotNull(response);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando se intenta actualizar username a uno que ya existe")
    void shouldThrowExceptionWhenUpdatingToExistingUsername() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null, null,
                "usuario.existente", // Username que ya existe
                null, null, null, null, null
        );
        
        when(userRepository.existsByUsername("usuario.existente")).thenReturn(true);

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(1L, updateRequest)
        );

        assertEquals("El nombre de usuario ya existe", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("usuario.existente");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debe permitir actualizar username al mismo valor")
    void shouldAllowUpdatingUsernameToSameValue() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null, null,
                "juan.perez", // Mismo username
                null, null, null, null, null
        );
        
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.updateUser(1L, updateRequest);

        // Then - No debe verificar existencia porque es el mismo username
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe codificar nueva contraseña cuando se actualiza")
    void shouldEncodeNewPasswordWhenUpdating() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newEncoded");
        
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null, null, null,
                "newPassword123", // Nueva contraseña
                null, null, null, null
        );
        
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.updateUser(1L, updateRequest);

        // Then
        verify(passwordEncoder, times(1)).encode("newPassword123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("$2a$10$newEncoded", userCaptor.getValue().getPassword());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario a actualizar no existe")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Nuevo Nombre", null, null, null, null, null, null, null
        );

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(999L, updateRequest)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    // ============================================
    // Tests para deleteUser()
    // ============================================

    @Test
    @DisplayName("Debe realizar soft delete del usuario (marcar como inactivo)")
    void shouldPerformSoftDelete() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.deleteUser(1L);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertFalse(capturedUser.getActivo()); // Debe estar inactivo
        
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando se intenta eliminar usuario inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUser(999L)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    // ============================================
    // Tests para getUsersByRole()
    // ============================================

    @Test
    @DisplayName("Debe retornar usuarios filtrados por rol")
    void shouldReturnUsersFilteredByRole() {
        // Given
        User receptionist1 = new User("Recepcionista 1", "Cargo", "rec1", "pass", null, null, UserRole.RECEPCIONISTA);
        receptionist1.setId(1L);
        User receptionist2 = new User("Recepcionista 2", "Cargo", "rec2", "pass", null, null, UserRole.RECEPCIONISTA);
        receptionist2.setId(2L);

        when(userRepository.findByRoleAndActivoTrue(UserRole.RECEPCIONISTA))
                .thenReturn(Arrays.asList(receptionist1, receptionist2));

        // When
        List<UserResponse> users = userService.getUsersByRole(UserRole.RECEPCIONISTA);

        // Then
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals(UserRole.RECEPCIONISTA, users.get(0).role());
        assertEquals(UserRole.RECEPCIONISTA, users.get(1).role());
        verify(userRepository, times(1)).findByRoleAndActivoTrue(UserRole.RECEPCIONISTA);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay usuarios con el rol especificado")
    void shouldReturnEmptyListWhenNoUsersWithRole() {
        // Given
        when(userRepository.findByRoleAndActivoTrue(UserRole.ADMINISTRADOR))
                .thenReturn(Collections.emptyList());

        // When
        List<UserResponse> users = userService.getUsersByRole(UserRole.ADMINISTRADOR);

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(userRepository, times(1)).findByRoleAndActivoTrue(UserRole.ADMINISTRADOR);
    }

    @Test
    @DisplayName("Debe ignorar campos vacíos o en blanco en actualización")
    void shouldIgnoreBlankFieldsInUpdate() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "   ", // Nombre en blanco
                "", // Cargo vacío
                null,
                null, null, null, null, null
        );
        
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.updateUser(1L, updateRequest);

        // Then - No debe actualizar campos en blanco
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }
}

