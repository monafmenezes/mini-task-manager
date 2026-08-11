import { useQuery } from "@tanstack/react-query"
import { format, parseISO } from "date-fns"
import { useState } from "react"
import { Link } from "react-router-dom"
import { PriorityText, StatusText } from "@/components/TaskIndicators"
import { Button } from "@/components/ui/button"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { useAuth } from "@/contexts/AuthContext"
import { useTeams } from "@/hooks/useTeams"
import { priorityLabels, statusLabels } from "@/lib/labels"
import * as tasksApi from "@/lib/tasks-api"
import type { TaskFilters, TaskPriority, TaskStatus } from "@/types/task"

const FILTRO_TODOS = "TODOS"

export function TaskListPage() {
  const { token } = useAuth()
  const { data: teams } = useTeams()
  const [filters, setFilters] = useState<TaskFilters>({ page: 0 })

  const { data, isLoading } = useQuery({
    queryKey: ["tasks", filters],
    queryFn: () => tasksApi.listTasks(filters, token as string),
    enabled: Boolean(token),
  })

  const membros = teams?.flatMap((team) => team.membros) ?? []
  const membrosUnicos = Array.from(new Map(membros.map((m) => [m.id, m])).values())

  function updateFilter<K extends keyof TaskFilters>(key: K, value: TaskFilters[K]) {
    setFilters((prev) => ({ ...prev, [key]: value, page: 0 }))
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Tarefas</h1>
        <Button render={<Link to="/tasks/nova" />}>Nova tarefa</Button>
      </div>

      <div className="flex flex-wrap gap-3">
        <Select
          value={filters.status ?? FILTRO_TODOS}
          onValueChange={(value) =>
            updateFilter("status", value === FILTRO_TODOS ? undefined : (value as TaskStatus))
          }
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue>
              {(value: string) =>
                value === FILTRO_TODOS ? "Todos os status" : statusLabels[value as TaskStatus]
              }
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={FILTRO_TODOS}>Todos os status</SelectItem>
            {Object.entries(statusLabels).map(([value, label]) => (
              <SelectItem key={value} value={value}>
                {label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select
          value={filters.prioridade ?? FILTRO_TODOS}
          onValueChange={(value) =>
            updateFilter(
              "prioridade",
              value === FILTRO_TODOS ? undefined : (value as TaskPriority),
            )
          }
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue>
              {(value: string) =>
                value === FILTRO_TODOS
                  ? "Todas as prioridades"
                  : priorityLabels[value as TaskPriority]
              }
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={FILTRO_TODOS}>Todas as prioridades</SelectItem>
            {Object.entries(priorityLabels).map(([value, label]) => (
              <SelectItem key={value} value={value}>
                {label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select
          value={filters.responsavelId ?? FILTRO_TODOS}
          onValueChange={(value) =>
            updateFilter("responsavelId", value && value !== FILTRO_TODOS ? value : undefined)
          }
        >
          <SelectTrigger className="w-[200px]">
            <SelectValue>
              {(value: string) =>
                value === FILTRO_TODOS
                  ? "Todos os responsáveis"
                  : (membrosUnicos.find((m) => m.id === value)?.nome ?? value)
              }
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={FILTRO_TODOS}>Todos os responsáveis</SelectItem>
            {membrosUnicos.map((membro) => (
              <SelectItem key={membro.id} value={membro.id}>
                {membro.nome}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
        </div>
      ) : (
        <>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Título</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Prioridade</TableHead>
                <TableHead>Responsável</TableHead>
                <TableHead>Time</TableHead>
                <TableHead>Prazo</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data?.content.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} className="text-center text-muted-foreground">
                    Nenhuma tarefa encontrada.
                  </TableCell>
                </TableRow>
              )}
              {data?.content.map((task) => (
                <TableRow key={task.id}>
                  <TableCell>
                    <Link to={`/tasks/${task.id}`} className="font-medium hover:underline">
                      {task.titulo}
                    </Link>
                  </TableCell>
                  <TableCell>
                    <StatusText status={task.status} />
                  </TableCell>
                  <TableCell>
                    <PriorityText priority={task.prioridade} />
                  </TableCell>
                  <TableCell>{task.responsavelNome ?? "—"}</TableCell>
                  <TableCell>{task.timeNome}</TableCell>
                  <TableCell className="font-mono text-xs text-muted-foreground">
                    {task.prazo ? format(parseISO(task.prazo), "dd/MM/yyyy") : "—"}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-center gap-3">
              <Button
                variant="outline"
                size="sm"
                disabled={data.number === 0}
                onClick={() => setFilters((prev) => ({ ...prev, page: (prev.page ?? 0) - 1 }))}
              >
                Anterior
              </Button>
              <span className="font-mono text-sm text-muted-foreground">
                Página {data.number + 1} de {data.totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={data.number + 1 >= data.totalPages}
                onClick={() => setFilters((prev) => ({ ...prev, page: (prev.page ?? 0) + 1 }))}
              >
                Próxima
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
