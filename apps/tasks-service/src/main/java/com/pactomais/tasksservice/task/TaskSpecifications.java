package com.pactomais.tasksservice.task;

import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {

    private TaskSpecifications() {}

    public static Specification<Task> comFiltros(
            TaskStatus status, UUID responsavelId, TaskPriority prioridade) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (responsavelId != null) {
                predicates =
                        cb.and(predicates, cb.equal(root.get("responsavelId"), responsavelId));
            }
            if (prioridade != null) {
                predicates = cb.and(predicates, cb.equal(root.get("prioridade"), prioridade));
            }
            return predicates;
        };
    }
}
