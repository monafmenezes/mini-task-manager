import { priorityLabels, statusLabels } from "@/lib/labels"
import { cn } from "@/lib/utils"
import type { TaskPriority, TaskStatus } from "@/types/task"

const statusStyles: Record<TaskStatus, string> = {
  PENDENTE: "text-muted-foreground",
  EM_ANDAMENTO: "text-blue-600 dark:text-blue-400",
  CONCLUIDA: "text-green-600 dark:text-green-400",
}

const priorityStyles: Record<TaskPriority, string> = {
  BAIXA: "text-muted-foreground",
  MEDIA: "text-amber-600 dark:text-amber-400",
  ALTA: "text-red-600 dark:text-red-400 font-medium",
}

export function StatusText({ status }: { status: TaskStatus }) {
  return <span className={statusStyles[status]}>{statusLabels[status]}</span>
}

export function PriorityText({ priority }: { priority: TaskPriority }) {
  return (
    <span className={cn("inline-flex items-center gap-1.5", priorityStyles[priority])}>
      <span className="size-1.5 rounded-full bg-current" aria-hidden="true" />
      {priorityLabels[priority]}
    </span>
  )
}
