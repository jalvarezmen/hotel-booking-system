package com.sofka.hotel_booking_api.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Column(name = "cargo", nullable = false, length = 50)
    private String cargo;

    @NotBlank
    @Column(name = "username", nullable = false, unique = true, length = 50)
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Column(name = "password", nullable = false)
    private String password; // Hasheada con BCrypt

    @Column(name = "celular", length = 20)
    private String celular;

    @Column(name = "dni", length = 20)
    private String dni;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Constructores
    public User() {
    }

    public User(String nombre, String cargo, String username, String password, 
                String celular, String dni, UserRole role) {
        this.nombre = nombre;
        this.cargo = cargo;
        this.username = username;
        this.password = password;
        this.celular = celular;
        this.dni = dni;
        this.role = role;
        this.activo = true;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

