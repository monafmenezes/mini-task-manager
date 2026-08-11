import { Link, Outlet } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/contexts/AuthContext"

export function AppLayout() {
  const { user, logout } = useAuth()

  return (
    <div className="min-h-svh flex flex-col">
      <header className="border-b">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
          <Link to="/" className="font-semibold">
            Mini Task Manager
          </Link>
          <div className="flex items-center gap-3 text-sm">
            <span className="text-muted-foreground">{user?.nome}</span>
            <Button variant="outline" size="sm" onClick={logout}>
              Sair
            </Button>
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-6">
        <Outlet />
      </main>
      <footer className="border-t">
        <div className="mx-auto max-w-5xl px-4 py-4 text-center font-mono text-xs text-muted-foreground">
          Mini Task Manager · desafio técnico Pacto Mais
        </div>
      </footer>
    </div>
  )
}
