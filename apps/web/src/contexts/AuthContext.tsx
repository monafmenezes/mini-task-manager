import { createContext, useContext, useMemo, useState, type ReactNode } from "react"
import { jwtDecode } from "jwt-decode"
import * as authApi from "@/lib/auth-api"

interface TokenClaims {
  sub: string
  nome: string
  email: string
  exp: number
}

interface AuthUser {
  id: string
  nome: string
  email: string
}

interface AuthContextValue {
  token: string | null
  user: AuthUser | null
  isAuthenticated: boolean
  login: (email: string, senha: string) => Promise<void>
  logout: () => void
}

const STORAGE_KEY = "mini-task-manager.token"

const AuthContext = createContext<AuthContextValue | null>(null)

function userFromToken(token: string): AuthUser {
  const claims = jwtDecode<TokenClaims>(token)
  return { id: claims.sub, nome: claims.nome, email: claims.email }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(STORAGE_KEY))

  const user = useMemo(() => {
    if (!token) return null
    try {
      return userFromToken(token)
    } catch {
      return null
    }
  }, [token])

  async function login(email: string, senha: string) {
    const response = await authApi.login(email, senha)
    localStorage.setItem(STORAGE_KEY, response.token)
    setToken(response.token)
  }

  function logout() {
    localStorage.removeItem(STORAGE_KEY)
    setToken(null)
  }

  const value: AuthContextValue = {
    token,
    user,
    isAuthenticated: Boolean(token),
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error("useAuth precisa estar dentro de um AuthProvider")
  }
  return context
}
