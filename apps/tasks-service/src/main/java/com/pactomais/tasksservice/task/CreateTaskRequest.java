package com.pactomais.tasksservice.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank(message = "Título é obrigatório") String titulo,
        String descricao,
        @NotNull(message = "Prioridade é obrigatória") TaskPriority prioridade,
        @NotNull(message = "ID do time é obrigatório") UUID timeId,
        @NotBlank(message = "Nome do time é obrigatório") String timeNome,
        UUID responsavelId,
        String responsavelNome,
        LocalDate prazo) {}
