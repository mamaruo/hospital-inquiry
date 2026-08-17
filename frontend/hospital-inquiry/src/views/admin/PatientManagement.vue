<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getAllPatients, searchUsers, toggleUserStatus } from '@/lib/api'
import type { UserResponse } from '@/lib/api'
import { toast } from 'vue-sonner'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
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

const showToggleConfirm = ref(false)
const toggleTarget = ref<UserResponse | null>(null)
const toggleLoading = ref(false)

function askToggleStatus(patient: UserResponse) {
  toggleTarget.value = patient
  showToggleConfirm.value = true
}

async function handleToggleStatus() {
  if (!toggleTarget.value) return
  try {
    toggleLoading.value = true
    const updated = await toggleUserStatus(toggleTarget.value.id)
    const index = patients.value.findIndex((p) => p.id === toggleTarget.value?.id)
    if (index !== -1) {
      patients.value[index] = updated
    }
    toast.success(`已${toggleTarget.value.enabled ? '禁用' : '启用'} ${toggleTarget.value.name || '该患者'}`)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    toggleLoading.value = false
    showToggleConfirm.value = false
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
                  @click="askToggleStatus(patient)"
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

    <!-- 禁用/启用确认 -->
    <AlertDialog v-model:open="showToggleConfirm">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>
            确定要{{ toggleTarget?.enabled ? '禁用' : '启用' }}该患者账号吗？
          </AlertDialogTitle>
          <AlertDialogDescription>
            {{ toggleTarget?.enabled ? '禁用后该患者将无法登录系统。' : '启用后该患者可正常登录。' }}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel :disabled="toggleLoading">取消</AlertDialogCancel>
          <AlertDialogAction :disabled="toggleLoading" @click="handleToggleStatus">确定</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>
