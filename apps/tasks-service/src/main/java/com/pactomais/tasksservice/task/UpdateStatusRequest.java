package com.pactomais.tasksservice.task;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull(message = "Status é obrigatório") TaskStatus status) {}
