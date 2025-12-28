<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getDepartments, getAvailableDoctors, getDoctorsByDepartment } from '@/lib/api'
import type { DepartmentDto, DoctorDto } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Input } from '@/components/ui/input'
import { Search } from 'lucide-vue-next'

const departments = ref<DepartmentDto[]>([])
const allDoctors = ref<DoctorDto[]>([])
const filteredDoctors = ref<DoctorDto[]>([])
const loading = ref(true)
const selectedDepartmentId = ref<string>('all')
const searchKeyword = ref('')

const displayDoctors = computed(() => {
  let result = filteredDoctors.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      (d) =>
        d.name.toLowerCase().includes(keyword) ||
        d.expertise?.toLowerCase().includes(keyword) ||
        d.department_name.toLowerCase().includes(keyword)
    )
  }
  return result
})

onMounted(async () => {
  try {
    const [deptData, doctorData] = await Promise.all([
      getDepartments(),
      getAvailableDoctors(),
    ])
    departments.value = deptData
    allDoctors.value = doctorData
    filteredDoctors.value = doctorData
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
})

async function handleDepartmentChange(value: unknown) {
  const strValue = String(value ?? 'all')
  selectedDepartmentId.value = strValue
  if (strValue === 'all') {
    filteredDoctors.value = allDoctors.value
  } else {
    filteredDoctors.value = await getDoctorsByDepartment(parseInt(strValue))
  }
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold">医生列表</h1>
      <p class="text-muted-foreground">浏览可问诊的医生</p>
    </div>

    <div class="flex flex-col md:flex-row gap-4">
      <div class="relative flex-1">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          v-model="searchKeyword"
          placeholder="搜索医生姓名、擅长领域..."
          class="pl-9"
        />
      </div>
      <Select :model-value="selectedDepartmentId" @update:model-value="handleDepartmentChange">
        <SelectTrigger class="w-full md:w-48">
          <SelectValue placeholder="选择科室" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">全部科室</SelectItem>
          <SelectItem 
            v-for="dept in departments" 
            :key="dept.id" 
            :value="String(dept.id)"
          >
            {{ dept.name }}
          </SelectItem>
        </SelectContent>
      </Select>
    </div>

    <div v-if="loading" class="text-center py-8 text-muted-foreground">
      加载中...
    </div>

    <div v-else-if="displayDoctors.length === 0" class="text-center py-8 text-muted-foreground">
      没有找到符合条件的医生
    </div>

    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <Card v-for="doctor in displayDoctors" :key="doctor.id">
        <CardHeader class="pb-2">
          <div class="flex items-start gap-3">
            <img
              v-if="doctor.photo_url"
              :src="`http://localhost:8081/hi${doctor.photo_url}`"
              :alt="doctor.name"
              class="w-12 h-12 rounded-full object-cover shrink-0"
            />
            <div v-else class="w-12 h-12 rounded-full bg-muted flex items-center justify-center shrink-0">
              <span class="text-lg font-medium">{{ doctor.name.charAt(0) }}</span>
            </div>
            <div class="flex-1 min-w-0">
              <CardTitle class="text-base">{{ doctor.name }}</CardTitle>
              <div class="flex items-center gap-2 mt-1">
                <Badge variant="secondary">{{ doctor.title }}</Badge>
                <span class="text-xs text-muted-foreground">{{ doctor.department_name }}</span>
              </div>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <CardDescription class="line-clamp-3">
            {{ doctor.expertise || '暂无简介' }}
          </CardDescription>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
