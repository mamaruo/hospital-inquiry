<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getAllPatients, searchUsers, toggleUserStatus } from '@/lib/api'
import type { UserResponse } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Search, Ban, CheckCircle } from 'lucide-vue-next'

const patients = ref<UserResponse[]>([])
const loading = ref(true)
const searchKeyword = ref('')

const filteredPatients = computed(() => {
  if (!searchKeyword.value) return patients.value
  const keyword = searchKeyword.value.toLowerCase()
  return patients.value.filter(
    (p) =>
      p.mobile?.toLowerCase().includes(keyword) ||
      p.name?.toLowerCase().includes(keyword)
  )
})

onMounted(async () => {
  await loadPatients()
})

async function loadPatients() {
  try {
    loading.value = true
    patients.value = await getAllPatients()
  } catch (error) {
    console.error('加载患者列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus(patient: UserResponse) {
  const action = patient.enabled ? '禁用' : '启用'
  if (!confirm(`确定要${action}该用户吗？`)) return
  try {
    const updated = await toggleUserStatus(patient.id)
    const index = patients.value.findIndex((p) => p.id === patient.id)
    if (index !== -1) {
      patients.value[index] = updated
    }
  } catch (error) {
    alert(error instanceof Error ? error.message : '操作失败')
  }
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold">患者管理</h1>
      <p class="text-muted-foreground">管理患者账号</p>
    </div>

    <div class="flex gap-4">
      <div class="relative flex-1 max-w-sm">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input v-model="searchKeyword" placeholder="搜索手机号或姓名..." class="pl-9" />
      </div>
    </div>

    <Card>
      <CardHeader>
        <CardTitle>患者列表</CardTitle>
        <CardDescription>共 {{ filteredPatients.length }} 位患者</CardDescription>
      </CardHeader>
      <CardContent>
        <div v-if="loading" class="text-center py-8 text-muted-foreground">
          加载中...
        </div>
        <div v-else-if="filteredPatients.length === 0" class="text-center py-8 text-muted-foreground">
          没有找到匹配的患者
        </div>
        <Table v-else>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>手机号</TableHead>
              <TableHead>姓名</TableHead>
              <TableHead>身份证号</TableHead>
              <TableHead>状态</TableHead>
              <TableHead class="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="patient in filteredPatients" :key="patient.id">
              <TableCell>{{ patient.id }}</TableCell>
              <TableCell>{{ patient.mobile }}</TableCell>
              <TableCell>{{ patient.name || '-' }}</TableCell>
              <TableCell>{{ patient.id_card || '-' }}</TableCell>
              <TableCell>
                <Badge :variant="patient.enabled ? 'default' : 'secondary'">
                  {{ patient.enabled ? '正常' : '已禁用' }}
                </Badge>
              </TableCell>
              <TableCell class="text-right">
                <Button
                  :variant="patient.enabled ? 'outline' : 'default'"
                  size="sm"
                  @click="handleToggleStatus(patient)"
                >
                  <Ban v-if="patient.enabled" class="mr-2 h-4 w-4" />
                  <CheckCircle v-else class="mr-2 h-4 w-4" />
                  {{ patient.enabled ? '禁用' : '启用' }}
                </Button>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>
