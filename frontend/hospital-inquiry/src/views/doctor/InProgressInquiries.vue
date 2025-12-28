<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getInProgressInquiries } from '@/lib/api'
import type { InquiryDto } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { MessageSquare } from 'lucide-vue-next'

const router = useRouter()
const inquiries = ref<InquiryDto[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    inquiries.value = await getInProgressInquiries()
  } catch (error) {
    console.error('加载进行中的问诊失败:', error)
  } finally {
    loading.value = false
  }
})

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
      <h1 class="text-2xl font-bold">进行中的问诊</h1>
      <p class="text-muted-foreground">正在进行的问诊会话</p>
    </div>

    <div v-if="loading" class="text-center py-8 text-muted-foreground">
      加载中...
    </div>

    <div v-else-if="inquiries.length === 0" class="text-center py-8 text-muted-foreground">
      暂无进行中的问诊
    </div>

    <div v-else class="grid gap-4">
      <Card 
        v-for="inquiry in inquiries" 
        :key="inquiry.id"
        class="cursor-pointer hover:bg-muted/50 transition-colors"
        @click="goToChat(inquiry.id)"
      >
        <CardHeader>
          <div class="flex items-start justify-between">
            <div>
              <CardTitle class="text-base">{{ inquiry.patient_profile.name }}</CardTitle>
              <CardDescription>
                {{ inquiry.patient_profile.gender }} · 接诊于 {{ formatDate(inquiry.accepted_at!) }}
              </CardDescription>
            </div>
            <Button size="sm">
              <MessageSquare class="mr-2 h-4 w-4" />
              继续聊天
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div class="text-sm">
            <span class="text-muted-foreground">病情描述：</span>
            {{ inquiry.symptom_description || '暂无描述' }}
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
