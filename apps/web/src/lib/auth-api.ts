import { AUTH_API_URL, apiRequest } from "@/lib/api-client"
import type { LoginResponse, Team, User } from "@/types/auth"

export function login(email: string, senha: string) {
  return apiRequest<LoginResponse>(AUTH_API_URL, "/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, senha }),
  })
}

export function register(nome: string, email: string, senha: string) {
  return apiRequest<User>(AUTH_API_URL, "/auth/register", {
    method: "POST",
    body: JSON.stringify({ nome, email, senha }),
  })
}

export function listTeams(token: string) {
  return apiRequest<Team[]>(AUTH_API_URL, "/teams", { token })
}

export function listTeamMembers(teamId: string, token: string) {
  return apiRequest<User[]>(AUTH_API_URL, `/teams/${teamId}/membros`, { token })
}
