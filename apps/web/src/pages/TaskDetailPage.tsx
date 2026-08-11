import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { format, parseISO } from "date-fns"
import { Link, useNavigate, useParams } from "react-router-dom"
import { toast } from "sonner"
import { PriorityText, StatusText } from "@/components/TaskIndicators"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuth } from "@/contexts/AuthContext"
import { ApiError } from "@/lib/api-client"
import { statusLabels } from "@/lib/labels"
import * as tasksApi from "@/lib/tasks-api"
import type { TaskStatus } from "@/types/task"

export function TaskDetailPage() {
  const { id } = useParams()
  const { token } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: task, isLoading } = useQuery({
    queryKey: ["task", id],
    queryFn: () => tasksApi.getTask(id as string, token as string),
    enabled: Boolean(token) && Boolean(id),
  })

  const statusMutation = useMutation({
    mutationFn: (status: TaskStatus) => tasksApi.updateTaskStatus(id as string, status, token as string),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["task", id] })
      queryClient.invalidateQueries({ queryKey: ["tasks"] })
      toast.success("Status atualizado")
    },
    onError: (err) => {
      toast.error(err instanceof ApiError ? err.message : "Não foi possível atualizar o status")
    },
  })

  const deleteMutation = useMutation({
    mutationFn: () => tasksApi.deleteTask(id as string, token as string),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tasks"] })
      toast.success("Tarefa excluída")
      navigate("/tasks")
    },
  })

  if (isLoading) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-8 w-1/2" />
        <Skeleton className="h-32 w-full" />
      </div>
    )
  }

  if (!task) {
    return <p className="text-muted-foreground">Tarefa não encontrada.</p>
  }

  return (
    <Card className="mx-auto max-w-2xl">
      <CardHeader className="flex flex-row items-start justify-between">
        <div>
          <CardTitle className="text-xl">{task.titulo}</CardTitle>
          <div className="mt-2 flex items-center gap-3 text-sm">
            <StatusText status={task.status} />
            <span className="text-border">·</span>
            <PriorityText priority={task.prioridade} />
          </div>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" render={<Link to={`/tasks/${task.id}/editar`} />}>
            Editar
          </Button>
          <Button variant="destructive" size="sm" onClick={() => deleteMutation.mutate()}>
            Excluir
          </Button>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <p className="text-sm">{task.descricao || "Sem descrição."}</p>

        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-muted-foreground">Responsável</p>
            <p>{task.responsavelNome ?? "Sem responsável"}</p>
          </div>
          <div>
            <p className="text-muted-foreground">Time</p>
            <p>{task.timeNome}</p>
          </div>
          <div>
            <p className="text-muted-foreground">Criada em</p>
            <p className="font-mono">{new Date(task.dataCriacao).toLocaleDateString("pt-BR")}</p>
          </div>
          <div>
            <p className="text-muted-foreground">Prazo</p>
            <p className="font-mono">
              {task.prazo ? format(parseISO(task.prazo), "dd/MM/yyyy") : "Sem prazo"}
            </p>
          </div>
        </div>

        <div className="space-y-2">
          <p className="text-sm text-muted-foreground">Alterar status</p>
          <Select
            value={task.status}
            onValueChange={(value) => statusMutation.mutate(value as TaskStatus)}
          >
            <SelectTrigger className="w-[200px]">
              <SelectValue>{(value: string) => statusLabels[value as TaskStatus]}</SelectValue>
            </SelectTrigger>
            <SelectContent>
              {Object.entries(statusLabels).map(([value, label]) => (
                <SelectItem key={value} value={value}>
                  {label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {!task.responsavelId && (
            <p className="text-xs text-muted-foreground">
              Atribua um responsável antes de marcar como concluída.
            </p>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
