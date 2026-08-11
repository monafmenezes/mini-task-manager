package com.pactomais.tasksservice.task;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final String CACHE_TASKS = "tasks";

    private final TaskRepository taskRepository;

    @CacheEvict(value = CACHE_TASKS, allEntries = true)
    public Task create(CreateTaskRequest request) {
        Task task =
                Task.builder()
                        .titulo(request.titulo())
                        .descricao(request.descricao())
                        .status(TaskStatus.PENDENTE)
                        .prioridade(request.prioridade())
                        .responsavelId(request.responsavelId())
                        .responsavelNome(request.responsavelNome())
                        .timeId(request.timeId())
                        .timeNome(request.timeNome())
                        .prazo(request.prazo())
                        .build();
        return taskRepository.save(task);
    }

    @Cacheable(
            value = CACHE_TASKS,
            key =
                    "#status + '-' + #responsavelId + '-' + #prioridade + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public TaskPage list(
            TaskStatus status, UUID responsavelId, TaskPriority prioridade, Pageable pageable) {
        Page<Task> page =
                taskRepository.findAll(
                        TaskSpecifications.comFiltros(status, responsavelId, prioridade), pageable);
        return TaskPage.from(page);
    }

    public Task findById(UUID id) {
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    @CacheEvict(value = CACHE_TASKS, allEntries = true)
    public Task update(UUID id, UpdateTaskRequest request) {
        Task task = findById(id);
        task.setTitulo(request.titulo());
        task.setDescricao(request.descricao());
        task.setPrioridade(request.prioridade());
        task.setResponsavelId(request.responsavelId());
        task.setResponsavelNome(request.responsavelNome());
        task.setPrazo(request.prazo());
        return taskRepository.save(task);
    }

    @CacheEvict(value = CACHE_TASKS, allEntries = true)
    public Task updateStatus(UUID id, TaskStatus novoStatus) {
        Task task = findById(id);
        if (novoStatus == TaskStatus.CONCLUIDA && task.getResponsavelId() == null) {
            throw new TaskWithoutResponsavelException();
        }
        task.setStatus(novoStatus);
        return taskRepository.save(task);
    }

    @CacheEvict(value = CACHE_TASKS, allEntries = true)
    public void delete(UUID id) {
        taskRepository.delete(findById(id));
    }
}
