import { TASKS_API_URL, apiRequest } from "@/lib/api-client"
import type { Page, Task, TaskFilters, TaskPriority, TaskStatus } from "@/types/task"

export interface TaskInput {
  titulo: string
  descricao: string
  prioridade: TaskPriority
  timeId: string
  timeNome: string
  responsavelId: string | null
  responsavelNome: string | null
  prazo: string | null
}

export function listTasks(filters: TaskFilters, token: string) {
  const params = new URLSearchParams()
  if (filters.status) params.set("status", filters.status)
  if (filters.responsavelId) params.set("responsavelId", filters.responsavelId)
  if (filters.prioridade) params.set("prioridade", filters.prioridade)
  params.set("page", String(filters.page ?? 0))
  params.set("size", "10")

  return apiRequest<Page<Task>>(TASKS_API_URL, `/tasks?${params.toString()}`, { token })
}

export function getTask(id: string, token: string) {
  return apiRequest<Task>(TASKS_API_URL, `/tasks/${id}`, { token })
}

export function createTask(input: Partial<TaskInput> & Pick<TaskInput, "titulo" | "prioridade" | "timeId" | "timeNome">, token: string) {
  return apiRequest<Task>(TASKS_API_URL, "/tasks", {
    method: "POST",
    body: JSON.stringify(input),
    token,
  })
}

export function updateTask(id: string, input: Omit<TaskInput, "timeId" | "timeNome">, token: string) {
  return apiRequest<Task>(TASKS_API_URL, `/tasks/${id}`, {
    method: "PUT",
    body: JSON.stringify(input),
    token,
  })
}

export function updateTaskStatus(id: string, status: TaskStatus, token: string) {
  return apiRequest<Task>(TASKS_API_URL, `/tasks/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status }),
    token,
  })
}

export interface PrioritySuggestion {
  prioridade: TaskPriority
  justificativa: string
  descricaoSugerida: string
}

export function suggestPriority(titulo: string, descricao: string, token: string) {
  return apiRequest<PrioritySuggestion>(TASKS_API_URL, "/tasks/sugerir-prioridade", {
    method: "POST",
    body: JSON.stringify({ titulo, descricao }),
    token,
  })
}

export async function isAiDisponivel(token: string) {
  const resposta = await apiRequest<{ disponivel: boolean }>(
    TASKS_API_URL,
    "/tasks/sugerir-prioridade/disponivel",
    { token },
  )
  return resposta.disponivel
}

export function deleteTask(id: string, token: string) {
  return apiRequest<void>(TASKS_API_URL, `/tasks/${id}`, {
    method: "DELETE",
    token,
  })
}
