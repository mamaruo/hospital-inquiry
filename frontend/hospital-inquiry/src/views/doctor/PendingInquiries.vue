<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPendingInquiries, acceptInquiry } from '@/lib/api'
import type { InquiryDto } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Check, MessageSquare } from 'lucide-vue-next'

const router = useRouter()
const inquiries = ref<InquiryDto[]>([])
const loading = ref(true)

onMounted(async () => {
  await loadInquiries()
})

async function loadInquiries() {
  try {
    loading.value = true
    inquiries.value = await getPendingInquiries()
  } catch (error) {
    console.error('加载待接诊列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleAccept(inquiry: InquiryDto) {
  try {
    await acceptInquiry(inquiry.id)
    router.push(`/doctor/chat/${inquiry.id}`)
  } catch (error) {
    alert(error instanceof Error ? error.message : '接受问诊失败')
  }
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold">待接诊</h1>
      <p class="text-muted-foreground">等待您接诊的患者</p>
    </div>

    <div v-if="loading" class="text-center py-8 text-muted-foreground">
      加载中...
    </div>

    <div v-else-if="inquiries.length === 0" class="text-center py-8 text-muted-foreground">
      暂无待接诊的患者
    </div>

    <div v-else class="grid gap-4">
      <Card v-for="inquiry in inquiries" :key="inquiry.id">
        <CardHeader>
          <div class="flex items-start justify-between">
            <div>
              <CardTitle class="text-base">{{ inquiry.patient_profile.name }}</CardTitle>
              <CardDescription>
                {{ inquiry.patient_profile.gender }} · 创建于 {{ formatDate(inquiry.created_at) }}
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent class="space-y-4">
          <div v-if="inquiry.patient_profile.medical_history" class="text-sm">
            <span class="text-muted-foreground">病史：</span>
            {{ inquiry.patient_profile.medical_history }}
          </div>
          <div class="text-sm">
            <span class="text-muted-foreground">病情描述：</span>
            {{ inquiry.symptom_description || '暂无描述' }}
          </div>
          <div class="flex gap-2">
            <Button @click="handleAccept(inquiry)">
              <Check class="mr-2 h-4 w-4" />
              接受问诊
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
