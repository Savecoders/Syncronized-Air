package org.ad.repositories.users;

import org.ad.core.users.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByCedula(String cedula);

    List<User> findAll();

    void delete(String cedula);
}
