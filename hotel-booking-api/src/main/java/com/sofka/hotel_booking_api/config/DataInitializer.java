package com.sofka.hotel_booking_api.config;

import com.sofka.hotel_booking_api.domain.model.User;
import com.sofka.hotel_booking_api.domain.model.UserRole;
import com.sofka.hotel_booking_api.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Verificar si ya existe un usuario administrador
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = new User(
                    "Administrador",
                    "Administrador del Sistema",
                    "admin",
                    passwordEncoder.encode("admin123"),
                    null,
                    null,
                    UserRole.ADMINISTRADOR
            );
            userRepository.save(admin);
            System.out.println("========================================");
            System.out.println("Usuario administrador creado:");
            System.out.println("Usuario: admin");
            System.out.println("Contraseña: admin123");
            System.out.println("========================================");
        }
    }
}

