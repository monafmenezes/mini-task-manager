export interface User {
  id: string
  nome: string
  email: string
}

export interface Team {
  id: string
  nome: string
  membros: User[]
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresInSeconds: number
}
