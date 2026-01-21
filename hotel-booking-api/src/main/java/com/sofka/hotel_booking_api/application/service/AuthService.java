package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.model.User;
import com.sofka.hotel_booking_api.domain.model.UserRole;
import com.sofka.hotel_booking_api.domain.repository.UserRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.LoginRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.LoginResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameAndActivoTrue(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }

        // Generar token simple (en producción usar JWT)
        String token = UUID.randomUUID().toString();

        return new LoginResponse(
                user.getId(),
                user.getNombre(),
                user.getCargo(),
                user.getUsername(),
                user.getCelular(),
                user.getDni(),
                user.getRole(),
                token
        );
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
}

