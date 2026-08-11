import { useQuery } from "@tanstack/react-query"
import * as authApi from "@/lib/auth-api"
import { useAuth } from "@/contexts/AuthContext"

export function useTeams() {
  const { token } = useAuth()

  return useQuery({
    queryKey: ["teams"],
    queryFn: () => authApi.listTeams(token as string),
    enabled: Boolean(token),
  })
}

export function useTeamMembers(teamId: string | undefined) {
  const { token } = useAuth()

  return useQuery({
    queryKey: ["team-members", teamId],
    queryFn: () => authApi.listTeamMembers(teamId as string, token as string),
    enabled: Boolean(token) && Boolean(teamId),
  })
}
