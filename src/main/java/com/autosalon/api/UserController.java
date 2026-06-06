package com.autosalon.api;

import com.autosalon.api.dto.Dtos.CreateUserRequest;
import com.autosalon.api.dto.Dtos.UserDto;
import com.autosalon.domain.enums.Role;
import com.autosalon.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService userService;
    private final ApiMapper mapper;

    public UserController(UserService userService, ApiMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<UserDto> listUsers(@RequestParam(required = false) Role role) {
        var users = role == null ? userService.listUsers() : userService.listUsersByRole(role);
        return users.stream().map(mapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable UUID id) {
        return mapper.toDto(userService.findUser(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody CreateUserRequest request) {
        return mapper.toDto(userService.createUser(request.fullName(), request.role()));
    }
}
