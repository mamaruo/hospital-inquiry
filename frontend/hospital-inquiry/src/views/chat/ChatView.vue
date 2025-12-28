<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useWebSocketStore } from '@/stores/websocket'
import { getInquiryById, getMessagesByInquiry, acceptInquiry, completeInquiry, uploadFile } from '@/lib/api'
import type { InquiryDto } from '@/lib/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { ArrowLeft, Send, Image as ImageIcon, Check, X } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const wsStore = useWebSocketStore()

const inquiry = ref<InquiryDto | null>(null)
const loading = ref(true)
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

const inquiryId = computed(() => parseInt(route.params.id as string))
const isDoctor = computed(() => authStore.isDoctor)
const currentUserId = computed(() => authStore.user?.id)
const canChat = computed(() => inquiry.value?.status === 'IN_PROGRESS')
const isPending = computed(() => inquiry.value?.status === 'PENDING')

const statusMap: Record<string, { label: string; variant: 'default' | 'secondary' | 'outline' }> = {
  PENDING: { label: '待接诊', variant: 'secondary' },
  IN_PROGRESS: { label: '进行中', variant: 'default' },
  COMPLETED: { label: '已结束', variant: 'outline' },
}

onMounted(async () => {
  try {
    // 加载问诊信息
    inquiry.value = await getInquiryById(inquiryId.value)
    
    // 加载历史消息
    const messages = await getMessagesByInquiry(inquiryId.value)
    wsStore.setInitialMessages(messages)
    
    // 连接 WebSocket
    wsStore.connect(inquiryId.value)
    
    scrollToBottom()
  } catch (error) {
    console.error('加载问诊信息失败:', error)
    alert('问诊不存在或无权访问')
    goBack()
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  wsStore.disconnect()
})

watch(() => wsStore.messages.length, () => {
  nextTick(scrollToBottom)
})

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

function sendMessage() {
  if (!inputMessage.value.trim() || !canChat.value) return
  
  wsStore.sendMessage(inputMessage.value.trim(), 'TEXT')
  inputMessage.value = ''
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function triggerFileInput() {
  fileInput.value?.click()
}

async function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  try {
    const result = await uploadFile(file)
    wsStore.sendMessage(result.url, 'IMAGE')
  } catch (error) {
    alert('图片上传失败')
  }
  
  target.value = ''
}

async function handleAccept() {
  if (!inquiry.value) return
  try {
    inquiry.value = await acceptInquiry(inquiry.value.id)
  } catch (error) {
    alert(error instanceof Error ? error.message : '接受问诊失败')
  }
}

async function handleComplete() {
  if (!inquiry.value || !confirm('确定要结束本次问诊吗？')) return
  try {
    inquiry.value = await completeInquiry(inquiry.value.id)
  } catch (error) {
    alert(error instanceof Error ? error.message : '结束问诊失败')
  }
}

function goBack() {
  if (isDoctor.value) {
    router.push('/doctor')
  } else {
    router.push('/patient/inquiries')
  }
}

function openImage(url: string) {
  window.open(url, '_blank')
}

function formatTime(dateStr: string) {
  return new Date(dateStr).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function isImageUrl(content: string) {
  return content.startsWith('/api/files/') || content.startsWith('http')
}
</script>

<template>
  <div class="flex flex-col h-[calc(100vh-8rem)]">
    <!-- 头部 -->
    <div class="flex items-center justify-between pb-4 border-b">
      <div class="flex items-center gap-3">
        <Button variant="ghost" size="icon" @click="goBack">
          <ArrowLeft class="h-4 w-4" />
        </Button>
        <div v-if="inquiry">
          <div class="flex items-center gap-2">
            <span class="font-medium">
              {{ isDoctor ? inquiry.patient_profile.name : inquiry.doctor.name }}
            </span>
            <Badge :variant="statusMap[inquiry.status]?.variant ?? 'secondary'">
              {{ statusMap[inquiry.status]?.label ?? inquiry.status }}
            </Badge>
          </div>
          <p class="text-sm text-muted-foreground">
            {{ isDoctor ? '患者' : inquiry.doctor.department_name + ' · ' + inquiry.doctor.title }}
          </p>
        </div>
      </div>
      
      <!-- 医生操作按钮 -->
      <div v-if="isDoctor && inquiry" class="flex gap-2">
        <Button v-if="isPending" @click="handleAccept" size="sm">
          <Check class="mr-2 h-4 w-4" />
          接受问诊
        </Button>
        <Button v-if="canChat" variant="outline" size="sm" @click="handleComplete">
          <X class="mr-2 h-4 w-4" />
          结束问诊
        </Button>
      </div>
    </div>

    <!-- 问诊信息卡片 -->
    <Card v-if="inquiry" class="my-4">
      <CardHeader class="py-3">
        <CardTitle class="text-sm">问诊信息</CardTitle>
      </CardHeader>
      <CardContent class="py-2 text-sm space-y-1">
        <div><span class="text-muted-foreground">问诊人：</span>{{ inquiry.patient_profile.name }} ({{ inquiry.patient_profile.gender }})</div>
        <div v-if="inquiry.patient_profile.medical_history">
          <span class="text-muted-foreground">病史：</span>{{ inquiry.patient_profile.medical_history }}
        </div>
        <div><span class="text-muted-foreground">病情描述：</span>{{ inquiry.symptom_description }}</div>
      </CardContent>
    </Card>

    <!-- 消息列表 -->
    <div ref="messagesContainer" class="flex-1 overflow-y-auto space-y-4 py-4">
      <div v-if="loading" class="text-center text-muted-foreground">
        加载中...
      </div>
      
      <div v-else-if="wsStore.messages.length === 0" class="text-center text-muted-foreground py-8">
        暂无消息，{{ canChat ? '开始聊天吧' : '等待医生接诊' }}
      </div>

      <div
        v-else
        v-for="message in wsStore.messages"
        :key="message.id"
        :class="[
          'flex gap-3',
          message.sender_id === currentUserId ? 'flex-row-reverse' : ''
        ]"
      >
        <Avatar class="h-8 w-8 shrink-0">
          <AvatarFallback>{{ message.sender_name?.charAt(0) || '?' }}</AvatarFallback>
        </Avatar>
        <div :class="['max-w-[70%]', message.sender_id === currentUserId ? 'items-end' : 'items-start']">
          <div class="flex items-center gap-2 mb-1" :class="message.sender_id === currentUserId ? 'flex-row-reverse' : ''">
            <span class="text-xs font-medium">{{ message.sender_name }}</span>
            <span class="text-xs text-muted-foreground">{{ formatTime(message.created_at) }}</span>
          </div>
          <div
            :class="[
              'rounded-lg px-3 py-2',
              message.sender_id === currentUserId
                ? 'bg-primary text-primary-foreground'
                : 'bg-muted'
            ]"
          >
            <img
              v-if="message.type === 'IMAGE'"
              :src="isImageUrl(message.content) ? (message.content.startsWith('http') ? message.content : `http://localhost:8081/hi${message.content}`) : message.content"
              class="max-w-full rounded cursor-pointer"
              @click="openImage(message.content)"
            />
            <p v-else class="whitespace-pre-wrap break-words">{{ message.content }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="pt-4 border-t">
      <div v-if="!canChat" class="text-center text-muted-foreground py-4">
        {{ isPending ? '等待医生接诊后可开始聊天' : '问诊已结束' }}
      </div>
      <div v-else class="flex gap-2">
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          class="hidden"
          @change="handleFileChange"
        />
        <Button variant="outline" size="icon" @click="triggerFileInput">
          <ImageIcon class="h-4 w-4" />
        </Button>
        <Input
          v-model="inputMessage"
          placeholder="输入消息..."
          class="flex-1"
          @keydown="handleKeydown"
        />
        <Button @click="sendMessage" :disabled="!inputMessage.trim()">
          <Send class="h-4 w-4" />
        </Button>
      </div>
    </div>
  </div>
</template>
