<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getPatientInquiries, getMyPatientProfiles } from '@/lib/api'
import type { InquiryDto, PatientProfileDto } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { MessageSquare, Users, Plus, ArrowRight } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const inquiries = ref<InquiryDto[]>([])
const profiles = ref<PatientProfileDto[]>([])
const loading = ref(true)

const statusMap: Record<string, { label: string; variant: 'default' | 'secondary' | 'outline' }> = {
  PENDING: { label: '待接诊', variant: 'secondary' },
  IN_PROGRESS: { label: '进行中', variant: 'default' },
  COMPLETED: { label: '已结束', variant: 'outline' },
}

onMounted(async () => {
  try {
    const [inquiryData, profileData] = await Promise.all([
      getPatientInquiries(),
      getMyPatientProfiles(),
    ])
    inquiries.value = inquiryData.slice(0, 5) // 只显示最近5条
    profiles.value = profileData
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
})

function goToInquiries() {
  router.push('/patient/inquiries')
}

function goToNewInquiry() {
  router.push('/patient/new-inquiry')
}

function goToProfiles() {
  router.push('/patient/profiles')
}

function goToChat(inquiryId: number) {
  router.push(`/patient/chat/${inquiryId}`)
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold">欢迎，{{ authStore.user?.name || '用户' }}</h1>
      <p class="text-muted-foreground">在这里管理您的在线问诊</p>
    </div>

    <div class="grid gap-4 md:grid-cols-3">
      <Card class="cursor-pointer hover:bg-muted/50 transition-colors" @click="goToInquiries">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">我的问诊</CardTitle>
          <MessageSquare class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ inquiries.length }}</div>
          <p class="text-xs text-muted-foreground">条问诊记录</p>
        </CardContent>
      </Card>

      <Card class="cursor-pointer hover:bg-muted/50 transition-colors" @click="goToProfiles">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">问诊人</CardTitle>
          <Users class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ profiles.length }}</div>
          <p class="text-xs text-muted-foreground">个问诊人</p>
        </CardContent>
      </Card>

      <Card class="cursor-pointer hover:bg-muted/50 transition-colors" @click="goToNewInquiry">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">发起问诊</CardTitle>
          <Plus class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">+</div>
          <p class="text-xs text-muted-foreground">新建问诊会话</p>
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
          <Button variant="outline" size="sm" @click="goToInquiries">
            查看全部
            <ArrowRight class="ml-2 h-4 w-4" />
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <div v-if="loading" class="text-center py-8 text-muted-foreground">
          加载中...
        </div>
        <div v-else-if="inquiries.length === 0" class="text-center py-8">
          <p class="text-muted-foreground mb-4">暂无问诊记录</p>
          <Button @click="goToNewInquiry">
            <Plus class="mr-2 h-4 w-4" />
            发起问诊
          </Button>
        </div>
        <div v-else class="space-y-4">
          <div
            v-for="inquiry in inquiries"
            :key="inquiry.id"
            class="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 cursor-pointer"
            @click="goToChat(inquiry.id)"
          >
            <div class="flex items-center gap-3">
              <img
                v-if="inquiry.doctor.photo_url"
                :src="`http://localhost:8081/hi${inquiry.doctor.photo_url}`"
                :alt="inquiry.doctor.name"
                class="w-10 h-10 rounded-full object-cover"
              />
              <div v-else class="w-10 h-10 rounded-full bg-muted flex items-center justify-center">
                <span class="font-medium">{{ inquiry.doctor.name.charAt(0) }}</span>
              </div>
              <div class="space-y-1">
                <div class="flex items-center gap-2">
                  <span class="font-medium">{{ inquiry.doctor.name }}</span>
                  <span class="text-muted-foreground text-sm">{{ inquiry.doctor.department_name }}</span>
                </div>
                <p class="text-sm text-muted-foreground line-clamp-1">
                  {{ inquiry.symptom_description || '暂无病情描述' }}
                </p>
              </div>
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
