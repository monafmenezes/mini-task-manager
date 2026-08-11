export type TaskStatus = "PENDENTE" | "EM_ANDAMENTO" | "CONCLUIDA"

export type TaskPriority = "BAIXA" | "MEDIA" | "ALTA"

export interface Task {
  id: string
  titulo: string
  descricao: string | null
  status: TaskStatus
  prioridade: TaskPriority
  responsavelId: string | null
  responsavelNome: string | null
  timeId: string
  timeNome: string
  dataCriacao: string
  prazo: string | null
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface TaskFilters {
  status?: TaskStatus
  responsavelId?: string
  prioridade?: TaskPriority
  page?: number
}
