package com.pactomais.authservice.team;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddMemberRequest(@NotNull(message = "ID do usuário é obrigatório") UUID userId) {}
