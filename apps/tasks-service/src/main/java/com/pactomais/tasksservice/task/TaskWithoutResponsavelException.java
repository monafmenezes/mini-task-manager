package com.pactomais.tasksservice.task;

public class TaskWithoutResponsavelException extends RuntimeException {

    public TaskWithoutResponsavelException() {
        super("Uma tarefa só pode ser concluída se tiver um responsável atribuído");
    }
}
