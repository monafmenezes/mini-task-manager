package com.pactomais.tasksservice.task;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;

public record TaskPage(
        List<Task> content, int pageNumber, int pageSize, long totalElements, int totalPages) {

    public static TaskPage from(Page<Task> page) {
        return new TaskPage(
                new ArrayList<>(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
