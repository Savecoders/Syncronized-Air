package org.ad.service;

import org.ad.core.users.User;
import org.ad.repositories.users.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public List<User> getClientes() {
        return userRepo.findAll();
    }

    public Optional<User> buscarPorCedula(String cedula) {
        return userRepo.findByCedula(cedula);
    }

    public String registrarCliente(String cedula, String nombre) {
        if (cedula == null || !cedula.matches("\\d{10}")) {
            return "La cedula debe tener exactamente 10 digitos.";
        }
        if (userRepo.findByCedula(cedula).isPresent()) {
            return "Ya existe un cliente con esa cedula.";
        }
        if (nombre == null || !nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{2,}$")) {
            return "El nombre debe tener al menos 2 letras y no puede contener numeros ni caracteres especiales.";
        }
        userRepo.save(new User(cedula, nombre.trim()));
        return null;
    }
}
