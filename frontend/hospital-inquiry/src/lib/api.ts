const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') ?? 'http://localhost:8081/hi'

export type LoginPayload = {
  mobile: string
  password: string
}

export type SignupPayload = {
  name: string
  mobile: string
  id_card: string
  password: string
}

export type UserResponse = {
  id: number
  name: string
  mobile: string
  id_card: string
}

async function buildError(response: Response) {
  const contentType = response.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) {
    try {
      const data = await response.json()
      const message = typeof data === 'string' ? data : data.message || JSON.stringify(data)
      return message
    } catch {
      /* fall through */
    }
  }
  const text = await response.text()
  return text || `请求失败，状态码 ${response.status}`
}

async function post<T>(path: string, body: unknown, expectJson = true): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  })

  if (!response.ok) {
    const message = await buildError(response)
    throw new Error(message)
  }

  if (!expectJson) {
    return (await response.text()) as T
  }

  return (await response.json()) as T
}

export function login(payload: LoginPayload) {
  return post<string>('/login', payload, false)
}

export function signup(payload: SignupPayload) {
  return post<UserResponse>('/signup', payload, true)
}