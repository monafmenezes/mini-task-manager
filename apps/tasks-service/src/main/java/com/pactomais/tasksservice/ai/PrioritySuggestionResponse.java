package com.pactomais.tasksservice.ai;

import com.pactomais.tasksservice.task.TaskPriority;

public record PrioritySuggestionResponse(
        TaskPriority prioridade, String justificativa, String descricaoSugerida) {}
