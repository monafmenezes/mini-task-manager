export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

interface RequestOptions extends RequestInit {
  token?: string | null
}

export async function apiRequest<T>(
  baseUrl: string,
  path: string,
  { token, headers, ...options }: RequestOptions = {},
): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
  })

  if (response.status === 204) {
    return undefined as T
  }

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    const message = data?.message ?? `Erro ${response.status}`
    throw new ApiError(message, response.status)
  }

  return data as T
}

export const AUTH_API_URL = import.meta.env.VITE_AUTH_API_URL as string
export const TASKS_API_URL = import.meta.env.VITE_TASKS_API_URL as string
