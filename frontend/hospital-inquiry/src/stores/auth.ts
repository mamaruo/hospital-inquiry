import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { LoginPayload, SignupPayload, UserResponse } from '@/lib/api'
import { login as loginRequest, signup as signupRequest } from '@/lib/api'

const TOKEN_STORAGE_KEY = 'hospital-inquiry.token'

function getInitialToken() {
  if (typeof window === 'undefined') {
    return null
  }
  return window.localStorage.getItem(TOKEN_STORAGE_KEY)
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

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getInitialToken())
  const user = ref<UserResponse | null>(null)
  const isAuthenticated = computed(() => Boolean(token.value))

  function setToken(nextToken: string | null) {
    token.value = nextToken
    persistToken(nextToken)
  }

  async function login(payload: LoginPayload) {
    const authToken = await loginRequest(payload)
    setToken(authToken)
    return authToken
  }

  async function signup(payload: SignupPayload) {
    const createdUser = await signupRequest(payload)
    return createdUser
  }

  function logout() {
    setToken(null)
    user.value = null
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    signup,
    logout,
    setToken,
  }
})
