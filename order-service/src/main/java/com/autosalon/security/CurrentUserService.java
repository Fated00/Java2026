package com.autosalon.security;

import com.autosalon.domain.exception.DomainValidationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserService {
    public UUID currentAppUserId() {
        Jwt jwt = currentJwt();
        String appUserId = jwt.getClaimAsString("app_user_id");
        if (appUserId == null || appUserId.isBlank()) {
            appUserId = jwt.getSubject();
        }
        try {
            return UUID.fromString(appUserId);
        } catch (IllegalArgumentException exception) {
            throw new DomainValidationException("authenticated token does not contain valid app user id");
        }
    }

    public boolean hasRole(String role) {
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        Authentication authentication = authentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    private Jwt currentJwt() {
        Object principal = authentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt;
        }
        throw new DomainValidationException("authenticated principal is not a JWT");
    }

    private Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new DomainValidationException("authentication is required");
        }
        return authentication;
    }
}
