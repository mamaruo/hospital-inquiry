<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDepartments, createDepartment, updateDepartment, deleteDepartment } from '@/lib/api'
import type { DepartmentDto } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Plus, Edit, Trash2 } from 'lucide-vue-next'

const departments = ref<DepartmentDto[]>([])
const loading = ref(true)
const showForm = ref(false)
const editingId = ref<number | null>(null)

const form = ref({
  name: '',
  description: '',
})

onMounted(async () => {
  await loadDepartments()
})

async function loadDepartments() {
  try {
    loading.value = true
    departments.value = await getDepartments()
  } catch (error) {
    console.error('加载科室列表失败:', error)
  } finally {
    loading.value = false
  }
}

function openCreateForm() {
  editingId.value = null
  form.value = { name: '', description: '' }
  showForm.value = true
}

function openEditForm(dept: DepartmentDto) {
  editingId.value = dept.id
  form.value = {
    name: dept.name,
    description: dept.description || '',
  }
  showForm.value = true
}

async function handleSubmit() {
  try {
    if (editingId.value) {
      await updateDepartment(editingId.value, form.value.name, form.value.description || null)
    } else {
      await createDepartment(form.value.name, form.value.description || null)
    }
    showForm.value = false
    await loadDepartments()
  } catch (error) {
    alert(error instanceof Error ? error.message : '保存失败')
  }
}

async function handleDelete(id: number) {
  if (!confirm('确定要删除该科室吗？')) return
  try {
    await deleteDepartment(id)
    await loadDepartments()
  } catch (error) {
    alert(error instanceof Error ? error.message : '删除失败')
  }
}

function cancelForm() {
  showForm.value = false
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold">科室管理</h1>
        <p class="text-muted-foreground">管理医院科室信息</p>
      </div>
      <Button @click="openCreateForm" v-if="!showForm">
        <Plus class="mr-2 h-4 w-4" />
        添加科室
      </Button>
    </div>

    <!-- 表单 -->
    <Card v-if="showForm">
      <CardHeader>
        <CardTitle>{{ editingId ? '编辑科室' : '添加科室' }}</CardTitle>
      </CardHeader>
      <CardContent>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div class="space-y-2">
            <Label for="name">科室名称</Label>
            <Input id="name" v-model="form.name" required placeholder="请输入科室名称" />
          </div>
          <div class="space-y-2">
            <Label for="description">科室描述</Label>
            <Textarea id="description" v-model="form.description" placeholder="请输入科室描述（可选）" rows="3" />
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
        <CardTitle>科室列表</CardTitle>
        <CardDescription>共 {{ departments.length }} 个科室</CardDescription>
      </CardHeader>
      <CardContent>
        <div v-if="loading" class="text-center py-8 text-muted-foreground">
          加载中...
        </div>
        <Table v-else>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>科室名称</TableHead>
              <TableHead>描述</TableHead>
              <TableHead>医生数量</TableHead>
              <TableHead class="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="dept in departments" :key="dept.id">
              <TableCell>{{ dept.id }}</TableCell>
              <TableCell class="font-medium">{{ dept.name }}</TableCell>
              <TableCell class="max-w-xs truncate">{{ dept.description || '-' }}</TableCell>
              <TableCell>{{ dept.doctor_count }}</TableCell>
              <TableCell class="text-right">
                <div class="flex justify-end gap-2">
                  <Button variant="ghost" size="icon" @click="openEditForm(dept)">
                    <Edit class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="icon" @click="handleDelete(dept.id)">
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
