<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDepartments, getAllPatients, getAllDoctors } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Building2, Users, Stethoscope } from 'lucide-vue-next'

const router = useRouter()

const departmentCount = ref(0)
const patientCount = ref(0)
const doctorCount = ref(0)
const loading = ref(true)

onMounted(async () => {
  try {
    const [depts, patients, doctors] = await Promise.all([
      getDepartments(),
      getAllPatients(),
      getAllDoctors(),
    ])
    departmentCount.value = depts.length
    patientCount.value = patients.length
    doctorCount.value = doctors.length
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
})

function goTo(path: string) {
  router.push(path)
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold">管理后台</h1>
      <p class="text-muted-foreground">系统数据概览</p>
    </div>

    <div class="grid gap-4 md:grid-cols-3">
      <Card class="cursor-pointer hover:bg-muted/50 transition-colors" @click="goTo('/admin/departments')">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">科室数量</CardTitle>
          <Building2 class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ departmentCount }}</div>
          <p class="text-xs text-muted-foreground">个科室</p>
        </CardContent>
      </Card>

      <Card class="cursor-pointer hover:bg-muted/50 transition-colors" @click="goTo('/admin/patients')">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">患者数量</CardTitle>
          <Users class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ patientCount }}</div>
          <p class="text-xs text-muted-foreground">位注册患者</p>
        </CardContent>
      </Card>

      <Card class="cursor-pointer hover:bg-muted/50 transition-colors" @click="goTo('/admin/doctors')">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">医生数量</CardTitle>
          <Stethoscope class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ doctorCount }}</div>
          <p class="text-xs text-muted-foreground">位注册医生</p>
        </CardContent>
      </Card>
    </div>

    <Card>
      <CardHeader>
        <CardTitle>快捷操作</CardTitle>
        <CardDescription>常用管理功能</CardDescription>
      </CardHeader>
      <CardContent class="grid gap-4 md:grid-cols-3">
        <div
          class="p-4 border rounded-lg cursor-pointer hover:bg-muted/50 transition-colors"
          @click="goTo('/admin/departments')"
        >
          <Building2 class="h-8 w-8 mb-2 text-primary" />
          <h3 class="font-medium">科室管理</h3>
          <p class="text-sm text-muted-foreground">管理医院科室信息</p>
        </div>
        <div
          class="p-4 border rounded-lg cursor-pointer hover:bg-muted/50 transition-colors"
          @click="goTo('/admin/patients')"
        >
          <Users class="h-8 w-8 mb-2 text-primary" />
          <h3 class="font-medium">患者管理</h3>
          <p class="text-sm text-muted-foreground">管理患者账号</p>
        </div>
        <div
          class="p-4 border rounded-lg cursor-pointer hover:bg-muted/50 transition-colors"
          @click="goTo('/admin/doctors')"
        >
          <Stethoscope class="h-8 w-8 mb-2 text-primary" />
          <h3 class="font-medium">医生管理</h3>
          <p class="text-sm text-muted-foreground">管理医生账号</p>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
