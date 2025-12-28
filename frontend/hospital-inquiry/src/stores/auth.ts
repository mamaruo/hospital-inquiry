import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { LoginPayload, SignupPayload, UserResponse, LoginResponse, Role } from '@/lib/api'
import { login as loginRequest, signup as signupRequest } from '@/lib/api'

const TOKEN_STORAGE_KEY = 'hospital-inquiry.token'
const USER_STORAGE_KEY = 'hospital-inquiry.user'

function getInitialToken() {
  if (typeof window === 'undefined') {
    return null
  }
  return window.localStorage.getItem(TOKEN_STORAGE_KEY)
}

function getInitialUser(): UserResponse | null {
  if (typeof window === 'undefined') {
    return null
  }
  const userStr = window.localStorage.getItem(USER_STORAGE_KEY)
  if (userStr) {
    try {
      return JSON.parse(userStr) as UserResponse
    } catch {
      return null
    }
  }
  return null
}

function persistToken(value: string | null) {
  if (typeof window === 'undefined') {
    return
  }

  if (value) {
    window.localStorage.setItem(TOKEN_STORAGE_KEY, value)
  } else {
    window.localStorage.removeItem(TOKEN_STORAGE_KEY)
  }
}

function persistUser(value: UserResponse | null) {
  if (typeof window === 'undefined') {
    return
  }

  if (value) {
    window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(value))
  } else {
    window.localStorage.removeItem(USER_STORAGE_KEY)
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getInitialToken())
  const user = ref<UserResponse | null>(getInitialUser())
  const isAuthenticated = computed(() => Boolean(token.value))
  const userRole = computed<Role | null>(() => user.value?.role ?? null)
  const isPatient = computed(() => userRole.value === 'PATIENT')
  const isDoctor = computed(() => userRole.value === 'DOCTOR')
  const isAdmin = computed(() => userRole.value === 'ADMIN')

  function setToken(nextToken: string | null) {
    token.value = nextToken
    persistToken(nextToken)
  }

  function setUser(nextUser: UserResponse | null) {
    user.value = nextUser
    persistUser(nextUser)
  }

  async function login(payload: LoginPayload) {
    const response = await loginRequest(payload)
    setToken(response.token)
    setUser(response.user)
    return response
  }

  async function signup(payload: SignupPayload) {
    const createdUser = await signupRequest(payload)
    return createdUser
  }

  function logout() {
    setToken(null)
    setUser(null)
  }

  return {
    token,
    user,
    isAuthenticated,
    userRole,
    isPatient,
    isDoctor,
    isAdmin,
    login,
    signup,
    logout,
    setToken,
    setUser,
  }
})
