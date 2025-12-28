<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getPendingInquiries, getInProgressInquiries, getDoctorInquiries } from '@/lib/api'
import type { InquiryDto } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Clock, MessageSquare, CheckCircle, ArrowRight } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const pendingCount = ref(0)
const inProgressCount = ref(0)
const completedCount = ref(0)
const recentInquiries = ref<InquiryDto[]>([])
const loading = ref(true)

const statusMap: Record<string, { label: string; variant: 'default' | 'secondary' | 'outline' }> = {
  PENDING: { label: '待接诊', variant: 'secondary' },
  IN_PROGRESS: { label: '进行中', variant: 'default' },
  COMPLETED: { label: '已结束', variant: 'outline' },
}

onMounted(async () => {
  try {
    const [pending, inProgress, all] = await Promise.all([
      getPendingInquiries(),
      getInProgressInquiries(),
      getDoctorInquiries(),
    ])
    pendingCount.value = pending.length
    inProgressCount.value = inProgress.length
    completedCount.value = all.filter(i => i.status === 'COMPLETED').length
    recentInquiries.value = all.slice(0, 5)
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
})

function goToPending() {
  router.push('/doctor/pending')
}

function goToInProgress() {
  router.push('/doctor/in-progress')
}

function goToChat(inquiryId: number) {
  router.push(`/doctor/chat/${inquiryId}`)
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold">欢迎，{{ authStore.user?.name || '医生' }}</h1>
      <p class="text-muted-foreground">管理您的问诊工作</p>
    </div>

    <div class="grid gap-4 md:grid-cols-3">
      <Card class="cursor-pointer hover:bg-muted/50 transition-colors" @click="goToPending">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">待接诊</CardTitle>
          <Clock class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ pendingCount }}</div>
          <p class="text-xs text-muted-foreground">位患者等待中</p>
        </CardContent>
      </Card>

      <Card class="cursor-pointer hover:bg-muted/50 transition-colors" @click="goToInProgress">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">进行中</CardTitle>
          <MessageSquare class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ inProgressCount }}</div>
          <p class="text-xs text-muted-foreground">个问诊会话</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">已完成</CardTitle>
          <CheckCircle class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ completedCount }}</div>
          <p class="text-xs text-muted-foreground">次问诊服务</p>
        </CardContent>
      </Card>
    </div>

    <Card>
      <CardHeader>
        <div class="flex items-center justify-between">
          <div>
            <CardTitle>最近问诊</CardTitle>
            <CardDescription>您最近的问诊记录</CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div v-if="loading" class="text-center py-8 text-muted-foreground">
          加载中...
        </div>
        <div v-else-if="recentInquiries.length === 0" class="text-center py-8 text-muted-foreground">
          暂无问诊记录
        </div>
        <div v-else class="space-y-4">
          <div
            v-for="inquiry in recentInquiries"
            :key="inquiry.id"
            class="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 cursor-pointer"
            @click="goToChat(inquiry.id)"
          >
            <div class="space-y-1">
              <div class="flex items-center gap-2">
                <span class="font-medium">{{ inquiry.patient_profile.name }}</span>
                <span class="text-muted-foreground text-sm">{{ inquiry.patient_profile.gender }}</span>
              </div>
              <p class="text-sm text-muted-foreground line-clamp-1">
                {{ inquiry.symptom_description || '暂无病情描述' }}
              </p>
              <p class="text-xs text-muted-foreground">
                {{ formatDate(inquiry.created_at) }}
              </p>
            </div>
            <Badge :variant="statusMap[inquiry.status]?.variant ?? 'secondary'">
              {{ statusMap[inquiry.status]?.label ?? inquiry.status }}
            </Badge>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
