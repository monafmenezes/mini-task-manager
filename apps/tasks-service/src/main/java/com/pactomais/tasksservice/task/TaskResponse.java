package com.pactomais.tasksservice.task;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String titulo,
        String descricao,
        TaskStatus status,
        TaskPriority prioridade,
        UUID responsavelId,
        String responsavelNome,
        UUID timeId,
        String timeNome,
        Instant dataCriacao,
        LocalDate prazo) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitulo(),
                task.getDescricao(),
                task.getStatus(),
                task.getPrioridade(),
                task.getResponsavelId(),
                task.getResponsavelNome(),
                task.getTimeId(),
                task.getTimeNome(),
                task.getDataCriacao(),
                task.getPrazo());
    }
}
