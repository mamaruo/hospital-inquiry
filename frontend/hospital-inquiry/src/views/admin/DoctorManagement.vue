<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  getAllDoctors,
  getDepartments,
  toggleUserStatus,
  createDoctor,
  updateDoctor,
  resetUserPassword,
  API_BASE_URL,
} from '@/lib/api'
import type { DoctorDto, DepartmentDto } from '@/lib/api'
import { toast } from 'vue-sonner'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
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
import { Search, Ban, CheckCircle, Plus, Pencil, KeyRound } from 'lucide-vue-next'

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
      (d) => d.mobile?.toLowerCase().includes(keyword) || d.name?.toLowerCase().includes(keyword)
    )
  }

  return result
})

// ---------- 新增医生 ----------
const showCreateDialog = ref(false)
const createSubmitting = ref(false)
const createForm = ref({
  name: '',
  mobile: '',
  password: '',
  department_id: '',
  title: '',
  expertise: '',
})

function openCreateDialog() {
  createForm.value = { name: '', mobile: '', password: '', department_id: '', title: '', expertise: '' }
  showCreateDialog.value = true
}

function validateCreateForm(): string | null {
  if (!createForm.value.name.trim()) return '请输入姓名'
  if (!/^1\d{10}$/.test(createForm.value.mobile)) return '手机号格式不正确'
  if (createForm.value.password.length < 8) return '初始密码至少 8 位'
  if (!createForm.value.department_id) return '请选择科室'
  if (!createForm.value.title.trim()) return '请输入职称'
  return null
}

async function handleCreate() {
  const error = validateCreateForm()
  if (error) {
    toast.error(error)
    return
  }
  try {
    createSubmitting.value = true
    await createDoctor({
      name: createForm.value.name.trim(),
      mobile: createForm.value.mobile,
      password: createForm.value.password,
      department_id: parseInt(createForm.value.department_id),
      title: createForm.value.title.trim(),
      expertise: createForm.value.expertise.trim() || '暂无',
    })
    showCreateDialog.value = false
    toast.success(`已创建医生 ${createForm.value.name.trim()}`)
    doctors.value = await getAllDoctors()
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '创建医生失败')
  } finally {
    createSubmitting.value = false
  }
}

// ---------- 编辑医生 ----------
const showEditDialog = ref(false)
const editSubmitting = ref(false)
const editingDoctor = ref<DoctorDto | null>(null)
const editForm = ref({
  department_id: '',
  title: '',
  expertise: '',
  available: 'true',
})

function openEditDialog(doctor: DoctorDto) {
  editingDoctor.value = doctor
  editForm.value = {
    department_id: String(doctor.department_id),
    title: doctor.title || '',
    expertise: doctor.expertise || '',
    available: String(doctor.available),
  }
  showEditDialog.value = true
}

async function handleEdit() {
  if (!editingDoctor.value) return
  if (!editForm.value.department_id) {
    toast.error('请选择科室')
    return
  }
  try {
    editSubmitting.value = true
    await updateDoctor(editingDoctor.value.id, {
      department_id: parseInt(editForm.value.department_id),
      title: editForm.value.title.trim(),
      expertise: editForm.value.expertise.trim() || '暂无',
      available: editForm.value.available === 'true',
    })
    showEditDialog.value = false
    toast.success(`已更新医生 ${editingDoctor.value.name} 的信息`)
    doctors.value = await getAllDoctors()
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '更新医生失败')
  } finally {
    editSubmitting.value = false
  }
}

// ---------- 重置密码 ----------
const showResetDialog = ref(false)
const resetSubmitting = ref(false)
const resetTarget = ref<DoctorDto | null>(null)
const newPassword = ref('')

function openResetDialog(doctor: DoctorDto) {
  resetTarget.value = doctor
  newPassword.value = ''
  showResetDialog.value = true
}

async function handleResetPassword() {
  if (!resetTarget.value) return
  if (newPassword.value.length < 8) {
    toast.error('新密码至少 8 位')
    return
  }
  try {
    resetSubmitting.value = true
    await resetUserPassword(resetTarget.value.user_id, newPassword.value)
    showResetDialog.value = false
    toast.success(`已重置 ${resetTarget.value.name} 的密码`)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '重置密码失败')
  } finally {
    resetSubmitting.value = false
  }
}

// ---------- 禁用/启用 ----------
const showToggleConfirm = ref(false)
const toggleTarget = ref<DoctorDto | null>(null)
const toggleLoading = ref(false)

function askToggleStatus(doctor: DoctorDto) {
  toggleTarget.value = doctor
  showToggleConfirm.value = true
}

async function handleToggleStatus() {
  if (!toggleTarget.value) return
  try {
    toggleLoading.value = true
    await toggleUserStatus(toggleTarget.value.user_id)
    doctors.value = await getAllDoctors()
    toast.success(`已${toggleTarget.value.available ? '禁用' : '启用'} ${toggleTarget.value.name}`)
  } catch (e) {
    toast.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    toggleLoading.value = false
    showToggleConfirm.value = false
  }
}

onMounted(async () => {
  try {
    loading.value = true
    const [doctorData, deptData] = await Promise.all([getAllDoctors(), getDepartments()])
    doctors.value = doctorData
    departments.value = deptData
  } catch (error) {
    toast.error('加载数据失败，请稍后重试')
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold">医生管理</h1>
        <p class="text-muted-foreground">管理医生账号</p>
      </div>
      <Button @click="openCreateDialog">
        <Plus class="mr-2 h-4 w-4" />
        新增医生
      </Button>
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
          <SelectItem v-for="dept in departments" :key="dept.id" :value="String(dept.id)">
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
        <div v-if="loading" class="text-center py-8 text-muted-foreground">加载中...</div>
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
                  :src="`${API_BASE_URL}${doctor.photo_url}`"
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
                <div class="flex justify-end gap-1">
                  <Button variant="ghost" size="icon" title="编辑" @click="openEditDialog(doctor)">
                    <Pencil class="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    title="重置密码"
                    @click="openResetDialog(doctor)"
                  >
                    <KeyRound class="h-4 w-4" />
                  </Button>
                  <Button
                    :variant="doctor.available ? 'outline' : 'default'"
                    size="sm"
                    @click="askToggleStatus(doctor)"
                  >
                    <Ban v-if="doctor.available" class="mr-2 h-4 w-4" />
                    <CheckCircle v-else class="mr-2 h-4 w-4" />
                    {{ doctor.available ? '禁用' : '启用' }}
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <!-- 新增医生 -->
    <Dialog v-model:open="showCreateDialog">
      <DialogContent class="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>新增医生</DialogTitle>
          <DialogDescription>创建医生账号，初始密码至少 8 位</DialogDescription>
        </DialogHeader>
        <form @submit.prevent="handleCreate" class="space-y-4">
          <div class="grid gap-4 sm:grid-cols-2">
            <div class="space-y-2">
              <Label for="create-name">姓名</Label>
              <Input id="create-name" v-model="createForm.name" placeholder="请输入姓名" />
            </div>
            <div class="space-y-2">
              <Label for="create-mobile">手机号</Label>
              <Input id="create-mobile" v-model="createForm.mobile" placeholder="登录用手机号" />
            </div>
            <div class="space-y-2">
              <Label for="create-password">初始密码</Label>
              <Input
                id="create-password"
                v-model="createForm.password"
                type="password"
                placeholder="至少 8 位"
              />
            </div>
            <div class="space-y-2">
              <Label>科室</Label>
              <Select v-model="createForm.department_id">
                <SelectTrigger>
                  <SelectValue placeholder="选择科室" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="dept in departments" :key="dept.id" :value="String(dept.id)">
                    {{ dept.name }}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div class="space-y-2">
            <Label for="create-title">职称</Label>
            <Input id="create-title" v-model="createForm.title" placeholder="如：主任医师" />
          </div>
          <div class="space-y-2">
            <Label for="create-expertise">专长</Label>
            <Textarea
              id="create-expertise"
              v-model="createForm.expertise"
              placeholder="擅长领域（可选）"
              rows="3"
            />
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" @click="showCreateDialog = false">取消</Button>
            <Button type="submit" :disabled="createSubmitting">创建</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- 编辑医生 -->
    <Dialog v-model:open="showEditDialog">
      <DialogContent class="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>编辑医生</DialogTitle>
          <DialogDescription>更新 {{ editingDoctor?.name }} 的科室、职称与专长</DialogDescription>
        </DialogHeader>
        <form @submit.prevent="handleEdit" class="space-y-4">
          <div class="space-y-2">
            <Label>科室</Label>
            <Select v-model="editForm.department_id">
              <SelectTrigger>
                <SelectValue placeholder="选择科室" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem v-for="dept in departments" :key="dept.id" :value="String(dept.id)">
                  {{ dept.name }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-2">
            <Label for="edit-title">职称</Label>
            <Input id="edit-title" v-model="editForm.title" placeholder="如：主任医师" />
          </div>
          <div class="space-y-2">
            <Label for="edit-expertise">专长</Label>
            <Textarea id="edit-expertise" v-model="editForm.expertise" rows="3" />
          </div>
          <div class="space-y-2">
            <Label>接诊状态</Label>
            <Select v-model="editForm.available">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="true">可接诊</SelectItem>
                <SelectItem value="false">暂停接诊</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" @click="showEditDialog = false">取消</Button>
            <Button type="submit" :disabled="editSubmitting">保存</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- 重置密码 -->
    <Dialog v-model:open="showResetDialog">
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>重置密码</DialogTitle>
          <DialogDescription>为 {{ resetTarget?.name }} 设置新密码，至少 8 位</DialogDescription>
        </DialogHeader>
        <form @submit.prevent="handleResetPassword" class="space-y-4">
          <div class="space-y-2">
            <Label for="reset-password">新密码</Label>
            <Input
              id="reset-password"
              v-model="newPassword"
              type="password"
              placeholder="至少 8 位"
            />
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" @click="showResetDialog = false">取消</Button>
            <Button type="submit" :disabled="resetSubmitting">确认重置</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- 禁用/启用确认 -->
    <AlertDialog v-model:open="showToggleConfirm">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>
            确定要{{ toggleTarget?.available ? '禁用' : '启用' }}该医生账号吗？
          </AlertDialogTitle>
          <AlertDialogDescription>
            {{ toggleTarget?.available ? '禁用后该医生将无法登录系统。' : '启用后该医生可正常登录。' }}
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
