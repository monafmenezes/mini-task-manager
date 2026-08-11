import type { TaskPriority, TaskStatus } from "@/types/task"

export const statusLabels: Record<TaskStatus, string> = {
  PENDENTE: "Pendente",
  EM_ANDAMENTO: "Em andamento",
  CONCLUIDA: "Concluída",
}

export const priorityLabels: Record<TaskPriority, string> = {
  BAIXA: "Baixa",
  MEDIA: "Média",
  ALTA: "Alta",
}
