package com.autosalon.service;

import com.autosalon.domain.enums.Role;
import com.autosalon.domain.model.User;
import com.autosalon.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.autosalon.domain.validation.DomainValidator.requireNonNull;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = requireNonNull(userRepository, "user repository");
    }

    public User createUser(String fullName, Role role) {
        return userRepository.save(User.create(fullName, role));
    }

    @Transactional(readOnly = true)
    public User findUser(UUID id) {
        return ServiceSupport.findActiveOrThrow(userRepository, id, "User");
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findByRemovedFalse();
    }

    @Transactional(readOnly = true)
    public List<User> listUsersByRole(Role role) {
        requireNonNull(role, "role");
        return userRepository.findByRoleAndRemovedFalse(role);
    }

    public void deleteUser(UUID id) {
        User user = findUser(id);
        user.markRemoved();
        userRepository.save(user);
    }
}
