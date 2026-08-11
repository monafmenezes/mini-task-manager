package com.pactomais.tasksservice.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;

    @InjectMocks private TaskService taskService;

    @Test
    void create_deveIniciarComStatusPendente() {
        CreateTaskRequest request =
                new CreateTaskRequest(
                        "Preparar apresentação",
                        "Slides pro cliente",
                        TaskPriority.ALTA,
                        UUID.randomUUID(),
                        "Time de Produto",
                        null,
                        null,
                        null);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task task = taskService.create(request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDENTE);
        assertThat(task.getTitulo()).isEqualTo("Preparar apresentação");
    }

    @Test
    void updateStatus_deveLancarExcecaoQuandoConcluirSemResponsavel() {
        UUID taskId = UUID.randomUUID();
        Task task = umaTarefa(taskId, TaskStatus.PENDENTE, null);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.updateStatus(taskId, TaskStatus.CONCLUIDA))
                .isInstanceOf(TaskWithoutResponsavelException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateStatus_devePermitirConcluirQuandoTemResponsavel() {
        UUID taskId = UUID.randomUUID();
        Task task = umaTarefa(taskId, TaskStatus.EM_ANDAMENTO, UUID.randomUUID());
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task resultado = taskService.updateStatus(taskId, TaskStatus.CONCLUIDA);

        assertThat(resultado.getStatus()).isEqualTo(TaskStatus.CONCLUIDA);
    }

    @Test
    void updateStatus_devePermitirTransicaoSemResponsavelQuandoNaoEhConclusao() {
        UUID taskId = UUID.randomUUID();
        Task task = umaTarefa(taskId, TaskStatus.PENDENTE, null);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task resultado = taskService.updateStatus(taskId, TaskStatus.EM_ANDAMENTO);

        assertThat(resultado.getStatus()).isEqualTo(TaskStatus.EM_ANDAMENTO);
    }

    @Test
    void findById_deveLancarExcecaoQuandoTarefaNaoExiste() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(taskId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    private Task umaTarefa(UUID id, TaskStatus status, UUID responsavelId) {
        return Task.builder()
                .id(id)
                .titulo("Tarefa de teste")
                .status(status)
                .prioridade(TaskPriority.MEDIA)
                .timeId(UUID.randomUUID())
                .timeNome("Time de Produto")
                .responsavelId(responsavelId)
                .build();
    }
}
