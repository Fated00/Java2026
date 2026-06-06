package com.autosalon.service;

import com.autosalon.domain.enums.Role;
import com.autosalon.domain.model.User;
import com.autosalon.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

public final class UserService {
    private final CrudRepository<User> userRepository;

    public UserService(CrudRepository<User> userRepository) {
        this.userRepository = requireNonNull(userRepository, "user repository");
    }

    public User createUser(String fullName, Role role) {
        return userRepository.save(User.create(fullName, role));
    }

    public User findUser(UUID id) {
        return ServiceSupport.findOrThrow(userRepository, id, "User");
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public List<User> listUsersByRole(Role role) {
        requireNonNull(role, "role");
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .toList();
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            ServiceSupport.findOrThrow(userRepository, id, "User");
        }
        userRepository.deleteById(id);
    }
}
