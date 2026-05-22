package org.ad.repositories.users;

import org.ad.core.users.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserMemoryRepository implements UserRepository {

    private final Map<String, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        users.put(user.getCedula(), user);
        return user;
    }

    @Override
    public Optional<User> findByCedula(String cedula) {
        return Optional.ofNullable(users.get(cedula));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(String cedula) {
        users.remove(cedula);
    }
}
