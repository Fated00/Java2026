package com.autosalon.service;

import com.autosalon.TestContext;
import com.autosalon.domain.enums.Role;
import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserAndAdminServiceTest {
    @Test
    void listsUsersByRoleAndDeletesUser() {
        TestContext context = new TestContext();
        User client = context.userService.createUser("Client", Role.CLIENT);
        context.userService.createUser("Manager", Role.DEALERSHIP_MANAGER);

        assertEquals(1, context.userService.listUsersByRole(Role.CLIENT).size());
        assertEquals(client.getId(), context.userService.findUser(client.getId()).getId());

        context.userService.deleteUser(client.getId());

        assertThrows(EntityNotFoundException.class, () -> context.userService.findUser(client.getId()));
    }

    @Test
    void systemAdminCanCrudAnyRepositoryEntity() {
        TestContext context = new TestContext();
        SystemAdminService adminService = new SystemAdminService();
        User user = User.create("System user", Role.SYSTEM_ADMIN);

        adminService.create(context.userRepository, user);
        assertEquals(user.getId(), adminService.view(context.userRepository, user.getId(), "User").getId());
        assertEquals(1, adminService.list(context.userRepository).size());

        User updated = new User(user.getId(), "Updated system user", Role.SYSTEM_ADMIN);
        adminService.update(context.userRepository, updated);
        assertEquals("Updated system user", context.userService.findUser(user.getId()).getFullName());

        adminService.delete(context.userRepository, user.getId());
        assertThrows(EntityNotFoundException.class, () -> adminService.view(context.userRepository, user.getId(), "User"));
    }

    @Test
    void userDeleteRejectsUnknownId() {
        TestContext context = new TestContext();

        assertThrows(EntityNotFoundException.class, () -> context.userService.deleteUser(UUID.randomUUID()));
    }
}
