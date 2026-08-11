package com.pactomais.tasksservice.ai;

import jakarta.validation.constraints.NotBlank;

public record PrioritySuggestionRequest(
        @NotBlank(message = "Título é obrigatório") String titulo, String descricao) {}
