package com.pactomais.tasksservice.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskRequest(
        @NotBlank(message = "Título é obrigatório") String titulo,
        String descricao,
        @NotNull(message = "Prioridade é obrigatória") TaskPriority prioridade,
        UUID responsavelId,
        String responsavelNome,
        LocalDate prazo) {}
