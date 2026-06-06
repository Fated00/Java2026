package com.autosalon.repository;

import com.autosalon.domain.enums.Role;
import com.autosalon.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByIdAndRemovedFalse(UUID id);

    List<User> findByRemovedFalse();

    List<User> findByRoleAndRemovedFalse(Role role);
}
