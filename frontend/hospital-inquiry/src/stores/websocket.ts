import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { MessageDto, MessageType } from '@/lib/api'
import { useAuthStore } from './auth'

const WS_BASE_URL = (import.meta.env.VITE_WS_BASE_URL as string | undefined) ?? 'ws://localhost:8081/hi'

export type WebSocketStatus = 'disconnected' | 'connecting' | 'connected' | 'error'

export interface WebSocketMessage {
  type: 'message' | 'connected' | 'error'
  message?: string
  data?: MessageDto
}

export const useWebSocketStore = defineStore('websocket', () => {
  const authStore = useAuthStore()
  
  const socket = ref<WebSocket | null>(null)
  const status = ref<WebSocketStatus>('disconnected')
  const currentInquiryId = ref<number | null>(null)
  const messages = ref<MessageDto[]>([])
  const error = ref<string | null>(null)

  const isConnected = computed(() => status.value === 'connected')

  function connect(inquiryId: number) {
    if (socket.value?.readyState === WebSocket.OPEN) {
      if (currentInquiryId.value === inquiryId) {
        return // 已连接到同一问诊
      }
      disconnect()
    }

    const token = authStore.token
    if (!token) {
      error.value = '未登录'
      return
    }

    status.value = 'connecting'
    currentInquiryId.value = inquiryId
    messages.value = []
    error.value = null

    const wsUrl = `${WS_BASE_URL}/ws/chat?token=${token}&inquiryId=${inquiryId}`
    socket.value = new WebSocket(wsUrl)

    socket.value.onopen = () => {
      status.value = 'connected'
      console.log('WebSocket 连接成功')
    }

    socket.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data) as WebSocketMessage
        
        if (data.type === 'message' && data.data) {
          messages.value.push(data.data)
        } else if (data.type === 'error') {
          error.value = data.message || '未知错误'
        }
      } catch (e) {
        console.error('解析 WebSocket 消息失败:', e)
      }
    }

    socket.value.onerror = (e) => {
      console.error('WebSocket 错误:', e)
      status.value = 'error'
      error.value = '连接错误'
    }

    socket.value.onclose = (e) => {
      console.log('WebSocket 关闭:', e.reason)
      status.value = 'disconnected'
      
      // 如果非正常关闭且还在当前问诊页面，尝试重连
      if (e.code !== 1000 && currentInquiryId.value === inquiryId) {
        setTimeout(() => {
          if (currentInquiryId.value === inquiryId) {
            connect(inquiryId)
          }
        }, 3000)
      }
    }
  }

  function disconnect() {
    if (socket.value) {
      socket.value.close(1000, '主动断开')
      socket.value = null
    }
    status.value = 'disconnected'
    currentInquiryId.value = null
  }

  function sendMessage(content: string, msgType: MessageType = 'TEXT') {
    if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
      error.value = '连接已断开'
      return false
    }

    const message = {
      type: 'message',
      content,
      msgType,
    }

    socket.value.send(JSON.stringify(message))
    return true
  }

  function setInitialMessages(initialMessages: MessageDto[]) {
    messages.value = initialMessages
  }

  return {
    status,
    isConnected,
    currentInquiryId,
    messages,
    error,
    connect,
    disconnect,
    sendMessage,
    setInitialMessages,
  }
})
