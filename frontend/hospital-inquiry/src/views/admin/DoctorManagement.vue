<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getAllDoctors, getDepartments, toggleUserStatus } from '@/lib/api'
import type { DoctorDto, DepartmentDto } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Search, Ban, CheckCircle } from 'lucide-vue-next'

const doctors = ref<DoctorDto[]>([])
const departments = ref<DepartmentDto[]>([])
const loading = ref(true)
const searchKeyword = ref('')
const selectedDepartment = ref<string>('all')

const filteredDoctors = computed(() => {
  let result = doctors.value
  
  if (selectedDepartment.value !== 'all') {
    result = result.filter((d) => d.department_id === parseInt(selectedDepartment.value))
  }
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      (d) =>
        d.mobile?.toLowerCase().includes(keyword) ||
        d.name?.toLowerCase().includes(keyword)
    )
  }
  
  return result
})

onMounted(async () => {
  try {
    loading.value = true
    const [doctorData, deptData] = await Promise.all([
      getAllDoctors(),
      getDepartments(),
    ])
    doctors.value = doctorData
    departments.value = deptData
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
})

async function handleToggleStatus(doctor: DoctorDto) {
  const action = doctor.available ? '禁用' : '启用'
  if (!confirm(`确定要${action}该医生账号吗？`)) return
  try {
    await toggleUserStatus(doctor.user_id)
    // 重新加载列表
    doctors.value = await getAllDoctors()
  } catch (error) {
    alert(error instanceof Error ? error.message : '操作失败')
  }
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold">医生管理</h1>
      <p class="text-muted-foreground">管理医生账号</p>
    </div>

    <div class="flex gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input v-model="searchKeyword" placeholder="搜索手机号或姓名..." class="pl-9" />
      </div>
      <Select v-model="selectedDepartment">
        <SelectTrigger class="w-48">
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

    <Card>
      <CardHeader>
        <CardTitle>医生列表</CardTitle>
        <CardDescription>共 {{ filteredDoctors.length }} 位医生</CardDescription>
      </CardHeader>
      <CardContent>
        <div v-if="loading" class="text-center py-8 text-muted-foreground">
          加载中...
        </div>
        <div v-else-if="filteredDoctors.length === 0" class="text-center py-8 text-muted-foreground">
          没有找到匹配的医生
        </div>
        <Table v-else>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>头像</TableHead>
              <TableHead>姓名</TableHead>
              <TableHead>手机号</TableHead>
              <TableHead>科室</TableHead>
              <TableHead>职称</TableHead>
              <TableHead>接诊状态</TableHead>
              <TableHead class="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="doctor in filteredDoctors" :key="doctor.id">
              <TableCell>{{ doctor.id }}</TableCell>
              <TableCell>
                <img
                  v-if="doctor.photo_url"
                  :src="`http://localhost:8081/hi${doctor.photo_url}`"
                  :alt="doctor.name"
                  class="w-8 h-8 rounded-full object-cover"
                />
                <div v-else class="w-8 h-8 rounded-full bg-muted flex items-center justify-center">
                  <span class="text-sm">{{ doctor.name.charAt(0) }}</span>
                </div>
              </TableCell>
              <TableCell class="font-medium">{{ doctor.name }}</TableCell>
              <TableCell>{{ doctor.mobile }}</TableCell>
              <TableCell>{{ doctor.department_name }}</TableCell>
              <TableCell>{{ doctor.title }}</TableCell>
              <TableCell>
                <Badge :variant="doctor.available ? 'default' : 'secondary'">
                  {{ doctor.available ? '可接诊' : '暂停接诊' }}
                </Badge>
              </TableCell>
              <TableCell class="text-right">
                <Button
                  :variant="doctor.available ? 'outline' : 'default'"
                  size="sm"
                  @click="handleToggleStatus(doctor)"
                >
                  <Ban v-if="doctor.available" class="mr-2 h-4 w-4" />
                  <CheckCircle v-else class="mr-2 h-4 w-4" />
                  {{ doctor.available ? '禁用' : '启用' }}
                </Button>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>
