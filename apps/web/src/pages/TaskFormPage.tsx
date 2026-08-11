import { zodResolver } from "@hookform/resolvers/zod"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { useEffect, useState } from "react"
import { Sparkles } from "lucide-react"
import { Controller, useForm } from "react-hook-form"
import { useNavigate, useParams } from "react-router-dom"
import { toast } from "sonner"
import { z } from "zod"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { DatePicker } from "@/components/DatePicker"
import { useAuth } from "@/contexts/AuthContext"
import { useTeams } from "@/hooks/useTeams"
import { priorityLabels } from "@/lib/labels"
import * as tasksApi from "@/lib/tasks-api"

const SEM_RESPONSAVEL = "__SEM_RESPONSAVEL__"

const taskSchema = z.object({
  titulo: z.string().min(1, "Título é obrigatório"),
  descricao: z.string().optional(),
  prioridade: z.enum(["BAIXA", "MEDIA", "ALTA"]),
  timeId: z.string().min(1, "Time é obrigatório"),
  responsavelId: z.string().optional(),
  prazo: z.string().optional(),
})

type TaskFormValues = z.infer<typeof taskSchema>

export function TaskFormPage() {
  const { id } = useParams()
  const isEdicao = Boolean(id)
  const { token } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { data: teams } = useTeams()
  const [erro, setErro] = useState<string | null>(null)

  const { data: tarefaExistente } = useQuery({
    queryKey: ["task", id],
    queryFn: () => tasksApi.getTask(id as string, token as string),
    enabled: isEdicao && Boolean(token),
  })

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    control,
    formState: { errors, isSubmitting },
  } = useForm<TaskFormValues>({
    resolver: zodResolver(taskSchema),
    defaultValues: { prioridade: "MEDIA" },
  })

  useEffect(() => {
    if (tarefaExistente) {
      setValue("titulo", tarefaExistente.titulo)
      setValue("descricao", tarefaExistente.descricao ?? "")
      setValue("prioridade", tarefaExistente.prioridade)
      setValue("timeId", tarefaExistente.timeId)
      setValue("responsavelId", tarefaExistente.responsavelId ?? undefined)
      setValue("prazo", tarefaExistente.prazo ?? "")
    }
  }, [tarefaExistente, setValue])

  const timeIdSelecionado = watch("timeId")
  const timeSelecionado = teams?.find((team) => team.id === timeIdSelecionado)

  const { data: iaDisponivel } = useQuery({
    queryKey: ["ia-disponivel"],
    queryFn: () => tasksApi.isAiDisponivel(token as string),
    enabled: Boolean(token),
    staleTime: 5 * 60 * 1000,
  })

  const suggestionMutation = useMutation({
    mutationFn: () =>
      tasksApi.suggestPriority(watch("titulo"), watch("descricao") ?? "", token as string),
    onSuccess: (suggestion) => {
      setValue("prioridade", suggestion.prioridade)
      if (!watch("descricao")) {
        setValue("descricao", suggestion.descricaoSugerida)
      }
      toast.info(suggestion.justificativa)
    },
    onError: () => {
      toast.error("Não foi possível sugerir a prioridade agora")
    },
  })

  const mutation = useMutation({
    mutationFn: async (values: TaskFormValues) => {
      const responsavel = timeSelecionado?.membros.find((m) => m.id === values.responsavelId)

      if (isEdicao) {
        return tasksApi.updateTask(
          id as string,
          {
            titulo: values.titulo,
            descricao: values.descricao ?? "",
            prioridade: values.prioridade,
            responsavelId: values.responsavelId ?? null,
            responsavelNome: responsavel?.nome ?? null,
            prazo: values.prazo || null,
          },
          token as string,
        )
      }

      return tasksApi.createTask(
        {
          titulo: values.titulo,
          descricao: values.descricao ?? "",
          prioridade: values.prioridade,
          timeId: values.timeId,
          timeNome: timeSelecionado?.nome ?? "",
          responsavelId: values.responsavelId ?? null,
          responsavelNome: responsavel?.nome ?? null,
          prazo: values.prazo || null,
        },
        token as string,
      )
    },
    onSuccess: (task) => {
      queryClient.invalidateQueries({ queryKey: ["tasks"] })
      toast.success(isEdicao ? "Tarefa atualizada" : "Tarefa criada")
      navigate(`/tasks/${task.id}`)
    },
    onError: (err) => {
      setErro(err instanceof Error ? err.message : "Não foi possível salvar a tarefa")
    },
  })

  return (
    <Card className="mx-auto max-w-xl">
      <CardHeader>
        <CardTitle>{isEdicao ? "Editar tarefa" : "Nova tarefa"}</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit((values) => mutation.mutate(values))} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="titulo">Título</Label>
            <Input id="titulo" {...register("titulo")} />
            {errors.titulo && <p className="text-sm text-destructive">{errors.titulo.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="descricao">Descrição</Label>
            <Textarea id="descricao" rows={4} {...register("descricao")} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label>Prioridade</Label>
                {iaDisponivel && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    disabled={!watch("titulo") || suggestionMutation.isPending}
                    onClick={() => suggestionMutation.mutate()}
                  >
                    <Sparkles className="size-3.5" />
                    {suggestionMutation.isPending ? "Sugerindo..." : "Sugerir com IA"}
                  </Button>
                )}
              </div>
              <Select
                value={watch("prioridade")}
                onValueChange={(value) => setValue("prioridade", value as TaskFormValues["prioridade"])}
              >
                <SelectTrigger>
                  <SelectValue>
                    {(value: string) => priorityLabels[value as TaskFormValues["prioridade"]]}
                  </SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(priorityLabels).map(([value, label]) => (
                    <SelectItem key={value} value={value}>
                      {label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Prazo</Label>
              <Controller
                control={control}
                name="prazo"
                render={({ field }) => (
                  <DatePicker value={field.value} onChange={field.onChange} />
                )}
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label>Time</Label>
            <Select
              value={watch("timeId")}
              onValueChange={(value) => {
                if (!value) return
                setValue("timeId", value)
                setValue("responsavelId", undefined)
              }}
              disabled={isEdicao}
            >
              <SelectTrigger>
                <SelectValue placeholder="Selecione um time">
                  {(value: string | null) =>
                    value ? (teams?.find((team) => team.id === value)?.nome ?? value) : "Selecione um time"
                  }
                </SelectValue>
              </SelectTrigger>
              <SelectContent>
                {teams?.map((team) => (
                  <SelectItem key={team.id} value={team.id}>
                    {team.nome}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.timeId && <p className="text-sm text-destructive">{errors.timeId.message}</p>}
          </div>

          <div className="space-y-2">
            <Label>Responsável</Label>
            <Select
              value={watch("responsavelId") ?? SEM_RESPONSAVEL}
              onValueChange={(value) =>
                setValue("responsavelId", value && value !== SEM_RESPONSAVEL ? value : undefined)
              }
              disabled={!timeSelecionado}
            >
              <SelectTrigger>
                <SelectValue>
                  {(value: string) =>
                    value === SEM_RESPONSAVEL
                      ? "Sem responsável"
                      : (timeSelecionado?.membros.find((m) => m.id === value)?.nome ?? value)
                  }
                </SelectValue>
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={SEM_RESPONSAVEL}>Sem responsável</SelectItem>
                {timeSelecionado?.membros.map((membro) => (
                  <SelectItem key={membro.id} value={membro.id}>
                    {membro.nome}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">
              Só é possível concluir a tarefa depois de atribuir um responsável.
            </p>
          </div>

          {erro && <p className="text-sm text-destructive">{erro}</p>}

          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => navigate(-1)}>
              Cancelar
            </Button>
            <Button
              type="submit"
              disabled={isSubmitting || mutation.isPending || suggestionMutation.isPending}
            >
              Salvar
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}
