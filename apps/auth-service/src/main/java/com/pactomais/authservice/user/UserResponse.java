package com.pactomais.authservice.user;

import java.util.UUID;

public record UserResponse(UUID id, String nome, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getNome(), user.getEmail());
    }
}
