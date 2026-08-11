package com.pactomais.authservice.team;

import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(@NotBlank(message = "Nome é obrigatório") String nome) {}
