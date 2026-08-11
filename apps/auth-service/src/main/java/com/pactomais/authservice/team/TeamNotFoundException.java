package com.pactomais.authservice.team;

import java.util.UUID;

public class TeamNotFoundException extends RuntimeException {

    public TeamNotFoundException(UUID id) {
        super("Time não encontrado: " + id);
    }
}
