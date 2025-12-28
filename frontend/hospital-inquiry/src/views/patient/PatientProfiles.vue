<script setup lang="ts">
import type { DateValue } from '@internationalized/date'
import { DateFormatter, getLocalTimeZone, parseDate, today } from '@internationalized/date'
import { toDate } from 'reka-ui/date'
import { ref, onMounted } from 'vue'
import { 
  getMyPatientProfiles, 
  createPatientProfile, 
  updatePatientProfile, 
  deletePatientProfile 
} from '@/lib/api'
import type { PatientProfileDto } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
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
import { Plus, Edit, Trash2, CalendarIcon } from 'lucide-vue-next'

const profiles = ref<PatientProfileDto[]>([])
const loading = ref(true)
const showForm = ref(false)
const editingId = ref<number | null>(null)

const form = ref({
  name: '',
  gender: '男',
  birth_date: null as string | null,
  medical_history: '' as string,
})

const birthDate = ref<DateValue | null>(null)
const birthDatePlaceholder = today(getLocalTimeZone())
const dateFormatter = new DateFormatter('zh-CN', { dateStyle: 'medium' })

onMounted(async () => {
  await loadProfiles()
})

async function loadProfiles() {
  try {
    loading.value = true
    profiles.value = await getMyPatientProfiles()
  } catch (error) {
    console.error('加载问诊人列表失败:', error)
  } finally {
    loading.value = false
  }
}

function openCreateForm() {
  editingId.value = null
  form.value = {
    name: '',
    gender: '男',
    birth_date: null,
    medical_history: '',
  }
  birthDate.value = null
  showForm.value = true
}

function openEditForm(profile: PatientProfileDto) {
  editingId.value = profile.id
  form.value = {
    name: profile.name,
    gender: profile.gender,
    birth_date: profile.birth_date,
    medical_history: profile.medical_history || '',
  }
  birthDate.value = profile.birth_date ? parseDate(profile.birth_date) : null
  showForm.value = true
}

async function handleSubmit() {
  try {
    const submitBirthDate = formatDateValue(birthDate.value)
    const submitData = {
      ...form.value,
      birth_date: submitBirthDate,
      medical_history: form.value.medical_history || null,
    }
    if (editingId.value) {
      await updatePatientProfile(editingId.value, submitData)
    } else {
      await createPatientProfile(submitData)
    }
    showForm.value = false
    await loadProfiles()
  } catch (error) {
    console.error('保存失败:', error)
    alert(error instanceof Error ? error.message : '保存失败')
  }
}

async function handleDelete(id: number) {
  if (!confirm('确定要删除该问诊人吗？')) return
  try {
    await deletePatientProfile(id)
    await loadProfiles()
  } catch (error) {
    console.error('删除失败:', error)
    alert(error instanceof Error ? error.message : '删除失败')
  }
}

function cancelForm() {
  showForm.value = false
}

function formatDateValue(value: DateValue | null) {
  if (!value) return null
  const date = toDate(value, getLocalTimeZone())
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatDateDisplay(dateStr: string | null) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold">问诊人管理</h1>
        <p class="text-muted-foreground">管理您账号下的问诊人信息</p>
      </div>
      <Button @click="openCreateForm" v-if="!showForm">
        <Plus class="mr-2 h-4 w-4" />
        添加问诊人
      </Button>
    </div>

    <!-- 表单 -->
    <Card v-if="showForm">
      <CardHeader>
        <CardTitle>{{ editingId ? '编辑问诊人' : '添加问诊人' }}</CardTitle>
        <CardDescription>填写问诊人的基本信息</CardDescription>
      </CardHeader>
      <CardContent>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div class="grid gap-4 md:grid-cols-2">
            <div class="space-y-2">
              <Label for="name">姓名</Label>
              <Input id="name" v-model="form.name" required placeholder="请输入姓名" />
            </div>
            <div class="space-y-2">
              <Label for="gender">性别</Label>
              <Select v-model="form.gender">
                <SelectTrigger>
                  <SelectValue placeholder="请选择性别" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="男">男</SelectItem>
                  <SelectItem value="女">女</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-2">
              <Label for="birthDate">出生日期</Label>
              <Popover v-slot="{ close }">
                <PopoverTrigger as-child>
                  <Button
                    variant="outline"
                    class="w-full justify-start text-left font-normal"
                    :class="!birthDate && 'text-muted-foreground'"
                  >
                    <CalendarIcon class="mr-2 h-4 w-4" />
                    {{ birthDate ? dateFormatter.format(toDate(birthDate, getLocalTimeZone())) : '请选择日期' }}
                  </Button>
                </PopoverTrigger>
                <PopoverContent class="w-auto p-0" align="start">
                  <Calendar
                    v-model="birthDate"
                    :default-placeholder="birthDatePlaceholder"
                    layout="month-and-year"
                    locale="zh-CN"
                    @update:model-value="(value) => {
                      birthDate = value
                      form.birth_date = formatDateValue(value)
                      close()
                    }"
                  />
                </PopoverContent>
              </Popover>
            </div>
          </div>
          <div class="space-y-2">
            <Label for="medicalHistory">病史</Label>
            <Textarea
              id="medicalHistory"
              v-model="form.medical_history"
              placeholder="请填写既往病史、过敏史等信息"
              rows="3"
            />
          </div>
          <div class="flex gap-2">
            <Button type="submit">保存</Button>
            <Button type="button" variant="outline" @click="cancelForm">取消</Button>
          </div>
        </form>
      </CardContent>
    </Card>

    <!-- 列表 -->
    <Card>
      <CardHeader>
        <CardTitle>问诊人列表</CardTitle>
      </CardHeader>
      <CardContent>
        <div v-if="loading" class="text-center py-8 text-muted-foreground">
          加载中...
        </div>
        <div v-else-if="profiles.length === 0" class="text-center py-8">
          <p class="text-muted-foreground mb-4">暂无问诊人</p>
          <Button @click="openCreateForm">
            <Plus class="mr-2 h-4 w-4" />
            添加问诊人
          </Button>
        </div>
        <Table v-else>
          <TableHeader>
            <TableRow>
              <TableHead>姓名</TableHead>
              <TableHead>性别</TableHead>
              <TableHead>出生日期</TableHead>
              <TableHead>病史</TableHead>
              <TableHead class="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="profile in profiles" :key="profile.id">
              <TableCell class="font-medium">{{ profile.name }}</TableCell>
              <TableCell>{{ profile.gender }}</TableCell>
              <TableCell>{{ formatDateDisplay(profile.birth_date) }}</TableCell>
              <TableCell class="max-w-xs truncate">{{ profile.medical_history || '-' }}</TableCell>
              <TableCell class="text-right">
                <div class="flex justify-end gap-2">
                  <Button variant="ghost" size="icon" @click="openEditForm(profile)">
                    <Edit class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="icon" @click="handleDelete(profile.id)">
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>