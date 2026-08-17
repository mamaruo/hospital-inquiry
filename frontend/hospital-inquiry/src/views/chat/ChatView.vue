<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'
import { useAuthStore } from '@/stores/auth'
import { useWebSocketStore } from '@/stores/websocket'
import {
  getInquiryById,
  getMessagesByInquiry,
  acceptInquiry,
  completeInquiry,
  uploadFile,
  API_BASE_URL,
} from '@/lib/api'
import type { InquiryDto, MessageDto } from '@/lib/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Marker, MarkerContent } from '@/components/ui/marker'
import { Message, MessageAvatar, MessageContent, MessageHeader } from '@/components/ui/message'
import { Bubble, BubbleContent } from '@/components/ui/bubble'
import { AttachmentMedia } from '@/components/ui/attachment'
import {
  MessageScroller,
  MessageScrollerButton,
  MessageScrollerContent,
  MessageScrollerItem,
  MessageScrollerProvider,
  MessageScrollerViewport,
} from '@/components/ui/message-scroller'
import { ArrowLeft, Send, Image as ImageIcon, Check, X } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const wsStore = useWebSocketStore()

const inquiry = ref<InquiryDto | null>(null)
const loading = ref(true)
const inputMessage = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const confirmingComplete = ref(false)
let confirmCompleteTimer: ReturnType<typeof setTimeout> | null = null

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

// 渲染项：按日期插入 Marker 分隔，其后为消息
type RenderItem =
  | { kind: 'marker'; key: string; label: string }
  | { kind: 'message'; key: string; message: MessageDto }

const renderItems = computed<RenderItem[]>(() => {
  const items: RenderItem[] = []
  let lastDate = ''
  for (const message of wsStore.messages) {
    const date = message.created_at.slice(0, 10)
    if (date !== lastDate) {
      lastDate = date
      items.push({ kind: 'marker', key: `marker-${message.id}`, label: formatDateLabel(message.created_at) })
    }
    items.push({ kind: 'message', key: `message-${message.id}`, message })
  }
  return items
})

onMounted(async () => {
  try {
    // 加载问诊信息
    inquiry.value = await getInquiryById(inquiryId.value)

    // 连接 WebSocket（先连接，历史消息随后写入，避免被连接逻辑清空）
    wsStore.connect(inquiryId.value)

    // 加载历史消息
    const messages = await getMessagesByInquiry(inquiryId.value)
    wsStore.setInitialMessages(messages)
  } catch (error) {
    console.error('加载问诊信息失败:', error)
    toast.error('问诊不存在或无权访问')
    goBack()
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (confirmCompleteTimer) clearTimeout(confirmCompleteTimer)
  wsStore.disconnect()
})

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

  const toastId = toast.loading('图片上传中...')
  try {
    const result = await uploadFile(file)
    wsStore.sendMessage(result.url, 'IMAGE')
    toast.success('图片已发送', { id: toastId })
  } catch (error) {
    console.error('图片上传失败:', error)
    toast.error('图片上传失败', { id: toastId })
  }

  target.value = ''
}

async function handleAccept() {
  if (!inquiry.value) return
  try {
    inquiry.value = await acceptInquiry(inquiry.value.id)
    toast.success('已接受问诊')
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '接受问诊失败')
  }
}

// 两步确认：第一次点击进入确认态，3 秒内再次点击才真正结束
function handleComplete() {
  if (!inquiry.value) return
  if (!confirmingComplete.value) {
    confirmingComplete.value = true
    toast.info('请再次点击按钮确认结束问诊', { duration: 3000 })
    if (confirmCompleteTimer) clearTimeout(confirmCompleteTimer)
    confirmCompleteTimer = setTimeout(() => {
      confirmingComplete.value = false
    }, 3000)
    return
  }
  if (confirmCompleteTimer) clearTimeout(confirmCompleteTimer)
  confirmingComplete.value = false
  doComplete()
}

async function doComplete() {
  if (!inquiry.value) return
  try {
    inquiry.value = await completeInquiry(inquiry.value.id)
    toast.success('问诊已结束')
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '结束问诊失败')
  }
}

function goBack() {
  if (isDoctor.value) {
    router.push('/doctor')
  } else {
    router.push('/patient/inquiries')
  }
}

function resolveMediaUrl(url: string) {
  return url.startsWith('http') ? url : `${API_BASE_URL}${url}`
}

function openImage(url: string) {
  window.open(resolveMediaUrl(url), '_blank')
}

function isMine(message: MessageDto) {
  return message.sender_id === currentUserId.value
}

// 医生侧有头像时显示头像图片，否则用首字兜底
function senderAvatarUrl(message: MessageDto) {
  const photoUrl = inquiry.value?.doctor.photo_url
  if (message.sender_role === 'DOCTOR' && photoUrl) {
    return resolveMediaUrl(photoUrl)
  }
  return null
}

function formatTime(dateStr: string) {
  return new Date(dateStr).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function formatDateLabel(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const startOfDay = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime()
  const diffDays = Math.round((startOfDay(now) - startOfDay(date)) / 86400000)
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
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
        <Button
          v-if="canChat"
          :variant="confirmingComplete ? 'destructive' : 'outline'"
          size="sm"
          @click="handleComplete"
        >
          <X v-if="!confirmingComplete" class="mr-2 h-4 w-4" />
          {{ confirmingComplete ? '确认结束问诊' : '结束问诊' }}
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
    <MessageScrollerProvider auto-scroll default-scroll-position="end">
      <MessageScroller class="min-h-0 flex-1">
        <MessageScrollerViewport>
          <MessageScrollerContent class="gap-4 py-4 pe-1">
            <div v-if="loading" class="text-center text-muted-foreground">
              加载中...
            </div>

            <div v-else-if="wsStore.messages.length === 0" class="flex min-h-32 items-center justify-center text-muted-foreground">
              暂无消息，{{ canChat ? '开始聊天吧' : '等待医生接诊' }}
            </div>

            <template v-else>
              <template v-for="item in renderItems" :key="item.key">
                <Marker v-if="item.kind === 'marker'" variant="separator" class="text-xs">
                  <MarkerContent>{{ item.label }}</MarkerContent>
                </Marker>

                <MessageScrollerItem v-else :message-id="String(item.message.id)">
                  <Message :align="isMine(item.message) ? 'end' : 'start'">
                    <MessageAvatar>
                      <Avatar class="size-8">
                        <AvatarImage v-if="senderAvatarUrl(item.message)" :src="senderAvatarUrl(item.message)!" />
                        <AvatarFallback>{{ item.message.sender_name?.charAt(0) || '?' }}</AvatarFallback>
                      </Avatar>
                    </MessageAvatar>
                    <MessageContent>
                      <MessageHeader class="gap-1.5">
                        <span>{{ item.message.sender_name }}</span>
                        <span class="text-muted-foreground/70">{{ formatTime(item.message.created_at) }}</span>
                      </MessageHeader>
                      <Bubble
                        :variant="isMine(item.message) ? 'default' : 'secondary'"
                        :align="isMine(item.message) ? 'end' : 'start'"
                      >
                        <!-- 图片消息 -->
                        <BubbleContent v-if="item.message.type === 'IMAGE'" class="p-1">
                          <AttachmentMedia
                            variant="image"
                            class="w-56 cursor-pointer"
                            @click="openImage(item.message.content)"
                          >
                            <img :src="resolveMediaUrl(item.message.content)" :alt="item.message.sender_name" />
                          </AttachmentMedia>
                        </BubbleContent>
                        <!-- 文字消息 -->
                        <BubbleContent v-else class="whitespace-pre-wrap">
                          {{ item.message.content }}
                        </BubbleContent>
                      </Bubble>
                    </MessageContent>
                  </Message>
                </MessageScrollerItem>
              </template>
            </template>
          </MessageScrollerContent>
        </MessageScrollerViewport>
        <MessageScrollerButton />
      </MessageScroller>
    </MessageScrollerProvider>

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
