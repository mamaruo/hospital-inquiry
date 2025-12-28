<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getPatientInquiries } from '@/lib/api'
import type { InquiryDto } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { MessageSquare, Plus, RefreshCw } from 'lucide-vue-next'

const router = useRouter()
const inquiries = ref<InquiryDto[]>([])
const loading = ref(true)
const refreshing = ref(false)
let refreshInterval: ReturnType<typeof setInterval> | null = null

const statusMap: Record<string, { label: string; variant: 'default' | 'secondary' | 'outline' }> = {
  PENDING: { label: '待接诊', variant: 'secondary' },
  IN_PROGRESS: { label: '进行中', variant: 'default' },
  COMPLETED: { label: '已结束', variant: 'outline' },
}

// 根据状态筛选问诊
const pendingInquiries = computed(() => inquiries.value.filter(i => i.status === 'PENDING'))
const inProgressInquiries = computed(() => inquiries.value.filter(i => i.status === 'IN_PROGRESS'))
const completedInquiries = computed(() => inquiries.value.filter(i => i.status === 'COMPLETED'))

onMounted(async () => {
  await loadInquiries()
  // 每 10 秒自动刷新，以便及时看到状态变化
  refreshInterval = setInterval(() => {
    loadInquiries(true)
  }, 10000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})

async function loadInquiries(silent = false) {
  try {
    if (!silent) loading.value = true
    else refreshing.value = true
    inquiries.value = await getPatientInquiries()
  } catch (error) {
    console.error('加载问诊列表失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function goToChat(inquiryId: number) {
  router.push(`/patient/chat/${inquiryId}`)
}

function goToNewInquiry() {
  router.push('/patient/new-inquiry')
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN')
}

function handleRefresh() {
  loadInquiries()
}

function calculateAge(birthDate: string | null): string | null {
  if (!birthDate) return null
  const birth = new Date(birthDate)
  const today = new Date()
  let age = today.getFullYear() - birth.getFullYear()
  const monthDiff = today.getMonth() - birth.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--
  }
  return `${age}岁`
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold">我的问诊</h1>
        <p class="text-muted-foreground">查看和管理您的问诊记录</p>
      </div>
      <div class="flex gap-2">
        <Button variant="outline" size="icon" @click="handleRefresh" :disabled="loading || refreshing">
          <RefreshCw class="h-4 w-4" :class="{ 'animate-spin': refreshing }" />
        </Button>
        <Button @click="goToNewInquiry">
          <Plus class="mr-2 h-4 w-4" />
          发起问诊
        </Button>
      </div>
    </div>

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

    <Tabs v-else default-value="all" class="w-full">
      <TabsList>
        <TabsTrigger value="all">全部 ({{ inquiries.length }})</TabsTrigger>
        <TabsTrigger value="pending">待接诊 ({{ pendingInquiries.length }})</TabsTrigger>
        <TabsTrigger value="in-progress">进行中 ({{ inProgressInquiries.length }})</TabsTrigger>
        <TabsTrigger value="completed">已结束 ({{ completedInquiries.length }})</TabsTrigger>
      </TabsList>

      <TabsContent value="all" class="space-y-4 mt-4">
        <Card 
          v-for="inquiry in inquiries" 
          :key="inquiry.id"
          class="cursor-pointer hover:bg-muted/50 transition-colors"
          @click="goToChat(inquiry.id)"
        >
          <CardHeader>
            <div class="flex items-start justify-between">
              <div>
                <CardTitle class="text-base flex items-center gap-2">
                  {{ inquiry.doctor.name }}
                  <Badge :variant="statusMap[inquiry.status]?.variant ?? 'secondary'">
                    {{ statusMap[inquiry.status]?.label ?? inquiry.status }}
                  </Badge>
                </CardTitle>
                <CardDescription>
                  {{ inquiry.doctor.department_name }} · {{ inquiry.doctor.title }}
                </CardDescription>
              </div>
              <Button size="sm" :variant="inquiry.status === 'IN_PROGRESS' ? 'default' : 'outline'">
                <MessageSquare class="mr-2 h-4 w-4" />
                {{ inquiry.status === 'IN_PROGRESS' ? '继续聊天' : '查看详情' }}
              </Button>
            </div>
          </CardHeader>
          <CardContent class="space-y-2">
            <div class="text-sm">
              <span class="text-muted-foreground">问诊人：</span>
              {{ inquiry.patient_profile.name }} ({{ inquiry.patient_profile.gender }}<template v-if="inquiry.patient_profile.birth_date">，{{ calculateAge(inquiry.patient_profile.birth_date) }}</template>)
            </div>
            <div class="text-sm">
              <span class="text-muted-foreground">病情描述：</span>
              {{ inquiry.symptom_description || '暂无描述' }}
            </div>
            <div class="text-xs text-muted-foreground">
              创建时间：{{ formatDate(inquiry.created_at) }}
              <template v-if="inquiry.accepted_at"> · 接诊时间：{{ formatDate(inquiry.accepted_at) }}</template>
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="pending" class="space-y-4 mt-4">
        <div v-if="pendingInquiries.length === 0" class="text-center py-8 text-muted-foreground">
          暂无待接诊的问诊
        </div>
        <Card 
          v-for="inquiry in pendingInquiries" 
          :key="inquiry.id"
          class="cursor-pointer hover:bg-muted/50 transition-colors"
          @click="goToChat(inquiry.id)"
        >
          <CardHeader>
            <div class="flex items-start justify-between">
              <div>
                <CardTitle class="text-base flex items-center gap-2">
                  {{ inquiry.doctor.name }}
                  <Badge :variant="statusMap[inquiry.status]?.variant ?? 'secondary'">
                    {{ statusMap[inquiry.status]?.label ?? inquiry.status }}
                  </Badge>
                </CardTitle>
                <CardDescription>
                  {{ inquiry.doctor.department_name }} · {{ inquiry.doctor.title }}
                </CardDescription>
              </div>
              <Button size="sm" variant="outline">
                <MessageSquare class="mr-2 h-4 w-4" />
                查看详情
              </Button>
            </div>
          </CardHeader>
          <CardContent class="space-y-2">
            <div class="text-sm">
              <span class="text-muted-foreground">问诊人：</span>
              {{ inquiry.patient_profile.name }} ({{ inquiry.patient_profile.gender }}<template v-if="inquiry.patient_profile.birth_date">，{{ calculateAge(inquiry.patient_profile.birth_date) }}</template>)
            </div>
            <div class="text-sm">
              <span class="text-muted-foreground">病情描述：</span>
              {{ inquiry.symptom_description || '暂无描述' }}
            </div>
            <div class="text-xs text-muted-foreground">
              创建时间：{{ formatDate(inquiry.created_at) }}
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="in-progress" class="space-y-4 mt-4">
        <div v-if="inProgressInquiries.length === 0" class="text-center py-8 text-muted-foreground">
          暂无进行中的问诊
        </div>
        <Card 
          v-for="inquiry in inProgressInquiries" 
          :key="inquiry.id"
          class="cursor-pointer hover:bg-muted/50 transition-colors"
          @click="goToChat(inquiry.id)"
        >
          <CardHeader>
            <div class="flex items-start justify-between">
              <div>
                <CardTitle class="text-base flex items-center gap-2">
                  {{ inquiry.doctor.name }}
                  <Badge :variant="statusMap[inquiry.status]?.variant ?? 'secondary'">
                    {{ statusMap[inquiry.status]?.label ?? inquiry.status }}
                  </Badge>
                </CardTitle>
                <CardDescription>
                  {{ inquiry.doctor.department_name }} · {{ inquiry.doctor.title }}
                </CardDescription>
              </div>
              <Button size="sm">
                <MessageSquare class="mr-2 h-4 w-4" />
                继续聊天
              </Button>
            </div>
          </CardHeader>
          <CardContent class="space-y-2">
            <div class="text-sm">
              <span class="text-muted-foreground">问诊人：</span>
              {{ inquiry.patient_profile.name }} ({{ inquiry.patient_profile.gender }}<template v-if="inquiry.patient_profile.birth_date">，{{ calculateAge(inquiry.patient_profile.birth_date) }}</template>)
            </div>
            <div class="text-sm">
              <span class="text-muted-foreground">病情描述：</span>
              {{ inquiry.symptom_description || '暂无描述' }}
            </div>
            <div class="text-xs text-muted-foreground">
              创建时间：{{ formatDate(inquiry.created_at) }}
              <template v-if="inquiry.accepted_at"> · 接诊时间：{{ formatDate(inquiry.accepted_at) }}</template>
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="completed" class="space-y-4 mt-4">
        <div v-if="completedInquiries.length === 0" class="text-center py-8 text-muted-foreground">
          暂无已结束的问诊
        </div>
        <Card 
          v-for="inquiry in completedInquiries" 
          :key="inquiry.id"
          class="cursor-pointer hover:bg-muted/50 transition-colors"
          @click="goToChat(inquiry.id)"
        >
          <CardHeader>
            <div class="flex items-start justify-between">
              <div>
                <CardTitle class="text-base flex items-center gap-2">
                  {{ inquiry.doctor.name }}
                  <Badge :variant="statusMap[inquiry.status]?.variant ?? 'secondary'">
                    {{ statusMap[inquiry.status]?.label ?? inquiry.status }}
                  </Badge>
                </CardTitle>
                <CardDescription>
                  {{ inquiry.doctor.department_name }} · {{ inquiry.doctor.title }}
                </CardDescription>
              </div>
              <Button size="sm" variant="outline">
                <MessageSquare class="mr-2 h-4 w-4" />
                查看详情
              </Button>
            </div>
          </CardHeader>
          <CardContent class="space-y-2">
            <div class="text-sm">
              <span class="text-muted-foreground">问诊人：</span>
              {{ inquiry.patient_profile.name }} ({{ inquiry.patient_profile.gender }}<template v-if="inquiry.patient_profile.birth_date">，{{ calculateAge(inquiry.patient_profile.birth_date) }}</template>)
            </div>
            <div class="text-sm">
              <span class="text-muted-foreground">病情描述：</span>
              {{ inquiry.symptom_description || '暂无描述' }}
            </div>
            <div class="text-xs text-muted-foreground">
              创建时间：{{ formatDate(inquiry.created_at) }}
              <template v-if="inquiry.accepted_at"> · 接诊时间：{{ formatDate(inquiry.accepted_at) }}</template>
            </div>
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>
  </div>
</template>
