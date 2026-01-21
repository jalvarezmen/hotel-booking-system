package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.model.User;
import com.sofka.hotel_booking_api.domain.model.UserRole;
import com.sofka.hotel_booking_api.domain.repository.UserRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateUserRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.UpdateUserRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Verificar que el username no exista
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        // Crear usuario
        User user = new User(
                request.nombre(),
                request.cargo(),
                request.username(),
                passwordEncoder.encode(request.password()),
                request.celular(),
                request.dni(),
                request.role()
        );

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findByActivoTrue().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Actualizar campos si están presentes
        if (request.nombre() != null && !request.nombre().isBlank()) {
            user.setNombre(request.nombre());
        }
        if (request.cargo() != null && !request.cargo().isBlank()) {
            user.setCargo(request.cargo());
        }
        if (request.username() != null && !request.username().isBlank()) {
            // Verificar que el nuevo username no esté en uso por otro usuario
            if (!user.getUsername().equals(request.username()) && 
                userRepository.existsByUsername(request.username())) {
                throw new IllegalArgumentException("El nombre de usuario ya existe");
            }
            user.setUsername(request.username());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.celular() != null) {
            user.setCelular(request.celular());
        }
        if (request.dni() != null) {
            user.setDni(request.dni());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.activo() != null) {
            user.setActivo(request.activo());
        }

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Soft delete
        user.setActivo(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRoleAndActivoTrue(role).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }
}

