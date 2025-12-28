<script setup lang="ts">
import type { DateValue } from '@internationalized/date'
import { DateFormatter, getLocalTimeZone, today } from '@internationalized/date'
import { toDate } from 'reka-ui/date'
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  getDepartments, 
  getDoctorsByDepartment, 
  getMyPatientProfiles, 
  createInquiry,
  createPatientProfile
} from '@/lib/api'
import type { DepartmentDto, DoctorDto, PatientProfileDto } from '@/lib/api'
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
import { Badge } from '@/components/ui/badge'
import { ArrowLeft, ArrowRight, Check, Plus, CalendarIcon } from 'lucide-vue-next'

const router = useRouter()

const step = ref(1)
const loading = ref(true)

// 数据
const departments = ref<DepartmentDto[]>([])
const doctors = ref<DoctorDto[]>([])
const profiles = ref<PatientProfileDto[]>([])

// 选择
const selectedDepartmentId = ref<number | null>(null)
const selectedDoctorId = ref<number | null>(null)
const selectedProfileId = ref<number | null>(null)
const symptomDescription = ref('')

// 新建问诊人表单
const showNewProfileForm = ref(false)
const newProfile = ref({
  name: '',
  gender: '男',
  birth_date: null as string | null,
  medical_history: '' as string,
})

const birthDatePlaceholder = today(getLocalTimeZone())
const dateFormatter = new DateFormatter('zh-CN', { dateStyle: 'medium' })
const newProfileBirthDate = ref<DateValue | null>(null)

const selectedDepartment = computed(() => 
  departments.value.find(d => d.id === selectedDepartmentId.value)
)
const selectedDoctor = computed(() => 
  doctors.value.find(d => d.id === selectedDoctorId.value)
)
const selectedProfile = computed(() => 
  profiles.value.find(p => p.id === selectedProfileId.value)
)

onMounted(async () => {
  try {
    const [deptData, profileData] = await Promise.all([
      getDepartments(),
      getMyPatientProfiles(),
    ])
    departments.value = deptData
    profiles.value = profileData
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
})

watch(selectedDepartmentId, async (deptId) => {
  if (deptId) {
    doctors.value = await getDoctorsByDepartment(deptId)
    selectedDoctorId.value = null
  } else {
    doctors.value = []
  }
})

function nextStep() {
  if (step.value < 3) {
    step.value++
  }
}

function prevStep() {
  if (step.value > 1) {
    step.value--
  }
}

function canProceed() {
  switch (step.value) {
    case 1:
      return selectedDoctorId.value !== null
    case 2:
      return selectedProfileId.value !== null
    case 3:
      return symptomDescription.value.trim().length > 0
    default:
      return false
  }
}

async function createNewProfile() {
  try {
    const created = await createPatientProfile({
      ...newProfile.value,
      birth_date: formatDateValue(newProfileBirthDate.value),
      medical_history: newProfile.value.medical_history || null,
    })
    profiles.value.push(created)
    selectedProfileId.value = created.id
    showNewProfileForm.value = false
    newProfile.value = { name: '', gender: '男', birth_date: null, medical_history: '' }
    newProfileBirthDate.value = null
  } catch (error) {
    alert(error instanceof Error ? error.message : '创建问诊人失败')
  }
}

async function handleSubmit() {
  if (!selectedProfileId.value || !selectedDoctorId.value) return
  
  try {
    const inquiry = await createInquiry({
      patient_profile_id: selectedProfileId.value,
      doctor_id: selectedDoctorId.value,
      symptom_description: symptomDescription.value,
    })
    router.push(`/patient/chat/${inquiry.id}`)
  } catch (error) {
    alert(error instanceof Error ? error.message : '创建问诊失败')
  }
}

function goBack() {
  router.push('/patient')
}

function formatDateValue(value: DateValue | null) {
  if (!value) return null
  const date = toDate(value, getLocalTimeZone())
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
</script>

<template>
  <div class="max-w-2xl mx-auto space-y-6">
    <div class="flex items-center gap-4">
      <Button variant="ghost" size="icon" @click="goBack">
        <ArrowLeft class="h-4 w-4" />
      </Button>
      <div>
        <h1 class="text-2xl font-bold">发起问诊</h1>
        <p class="text-muted-foreground">选择医生和问诊人，描述您的病情</p>
      </div>
    </div>

    <!-- 步骤指示器 -->
    <div class="flex items-center justify-center gap-4">
      <div v-for="s in 3" :key="s" class="flex items-center">
        <div
          :class="[
            'w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium',
            step >= s ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'
          ]"
        >
          <Check v-if="step > s" class="h-4 w-4" />
          <span v-else>{{ s }}</span>
        </div>
        <div v-if="s < 3" class="w-16 h-0.5 bg-muted mx-2" />
      </div>
    </div>

    <div v-if="loading" class="text-center py-8 text-muted-foreground">
      加载中...
    </div>

    <!-- 步骤 1: 选择科室和医生 -->
    <Card v-else-if="step === 1">
      <CardHeader>
        <CardTitle>选择科室和医生</CardTitle>
        <CardDescription>请先选择科室，然后选择您要咨询的医生</CardDescription>
      </CardHeader>
      <CardContent class="space-y-4">
        <div class="space-y-2">
          <Label>选择科室</Label>
          <Select v-model="selectedDepartmentId">
            <SelectTrigger>
              <SelectValue placeholder="请选择科室" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem 
                v-for="dept in departments" 
                :key="dept.id" 
                :value="dept.id"
              >
                {{ dept.name }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div v-if="selectedDepartmentId" class="space-y-2">
          <Label>选择医生</Label>
          <div v-if="doctors.length === 0" class="text-muted-foreground text-sm py-4">
            该科室暂无可接诊医生
          </div>
          <div v-else class="grid gap-3">
            <div
              v-for="doctor in doctors"
              :key="doctor.id"
              :class="[
                'p-4 border rounded-lg cursor-pointer transition-colors',
                selectedDoctorId === doctor.id ? 'border-primary bg-primary/5' : 'hover:bg-muted/50'
              ]"
              @click="selectedDoctorId = doctor.id"
            >
              <div class="flex items-start gap-3">
                <img
                  v-if="doctor.photo_url"
                  :src="`http://localhost:8081/hi${doctor.photo_url}`"
                  :alt="doctor.name"
                  class="w-12 h-12 rounded-full object-cover"
                />
                <div v-else class="w-12 h-12 rounded-full bg-muted flex items-center justify-center">
                  <span class="text-lg font-medium">{{ doctor.name.charAt(0) }}</span>
                </div>
                <div class="flex-1">
                  <div class="flex items-center gap-2">
                    <span class="font-medium">{{ doctor.name }}</span>
                    <Badge variant="secondary">{{ doctor.title }}</Badge>
                  </div>
                  <p class="text-sm text-muted-foreground mt-1 line-clamp-2">
                    {{ doctor.expertise || '暂无简介' }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- 步骤 2: 选择问诊人 -->
    <Card v-else-if="step === 2">
      <CardHeader>
        <CardTitle>选择问诊人</CardTitle>
        <CardDescription>选择需要问诊的人员</CardDescription>
      </CardHeader>
      <CardContent class="space-y-4">
        <div v-if="profiles.length === 0 && !showNewProfileForm" class="text-center py-4">
          <p class="text-muted-foreground mb-4">您还没有添加问诊人</p>
          <Button @click="showNewProfileForm = true">
            <Plus class="mr-2 h-4 w-4" />
            添加问诊人
          </Button>
        </div>

        <div v-else-if="!showNewProfileForm" class="space-y-3">
          <div
            v-for="profile in profiles"
            :key="profile.id"
            :class="[
              'p-4 border rounded-lg cursor-pointer transition-colors',
              selectedProfileId === profile.id ? 'border-primary bg-primary/5' : 'hover:bg-muted/50'
            ]"
            @click="selectedProfileId = profile.id"
          >
            <div class="flex items-center justify-between">
              <div>
                <span class="font-medium">{{ profile.name }}</span>
                <span class="text-muted-foreground ml-2">{{ profile.gender }}</span>
              </div>
              <Check v-if="selectedProfileId === profile.id" class="h-5 w-5 text-primary" />
            </div>
            <p v-if="profile.medical_history" class="text-sm text-muted-foreground mt-1 line-clamp-1">
              病史：{{ profile.medical_history }}
            </p>
          </div>
          <Button variant="outline" class="w-full" @click="showNewProfileForm = true">
            <Plus class="mr-2 h-4 w-4" />
            添加新问诊人
          </Button>
        </div>

        <!-- 新建问诊人表单 -->
        <div v-if="showNewProfileForm" class="space-y-4 border rounded-lg p-4">
          <h3 class="font-medium">添加问诊人</h3>
          <div class="grid gap-4 md:grid-cols-2">
            <div class="space-y-2">
              <Label>姓名</Label>
              <Input v-model="newProfile.name" placeholder="请输入姓名" />
            </div>
            <div class="space-y-2">
              <Label>性别</Label>
              <Select v-model="newProfile.gender">
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="男">男</SelectItem>
                  <SelectItem value="女">女</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <div class="space-y-2">
            <Label>出生日期</Label>
            <Popover v-slot="{ close }">
              <PopoverTrigger as-child>
                <Button
                  variant="outline"
                  class="w-full justify-start text-left font-normal"
                  :class="!newProfileBirthDate && 'text-muted-foreground'"
                >
                  <CalendarIcon class="mr-2 h-4 w-4" />
                  {{ newProfileBirthDate ? dateFormatter.format(toDate(newProfileBirthDate, getLocalTimeZone())) : '请选择日期' }}
                </Button>
              </PopoverTrigger>
              <PopoverContent class="w-auto p-0" align="start">
                <Calendar
                  v-model="newProfileBirthDate"
                  :default-placeholder="birthDatePlaceholder"
                  layout="month-and-year"
                  locale="zh-CN"
                  @update:model-value="(value) => {
                    newProfileBirthDate = value
                    newProfile.birth_date = formatDateValue(value)
                    close()
                  }"
                />
              </PopoverContent>
            </Popover>
          </div>
          <div class="space-y-2">
            <Label>病史（可选）</Label>
            <Textarea v-model="newProfile.medical_history" placeholder="既往病史、过敏史等" rows="2" />
          </div>
          <div class="flex gap-2">
            <Button @click="createNewProfile" :disabled="!newProfile.name">保存</Button>
            <Button variant="outline" @click="showNewProfileForm = false">取消</Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- 步骤 3: 描述病情 -->
    <Card v-else-if="step === 3">
      <CardHeader>
        <CardTitle>描述病情</CardTitle>
        <CardDescription>请详细描述您的症状和需要咨询的问题</CardDescription>
      </CardHeader>
      <CardContent class="space-y-4">
        <div class="p-4 bg-muted rounded-lg space-y-2">
          <div class="flex justify-between text-sm">
            <span class="text-muted-foreground">选择的医生：</span>
            <span>{{ selectedDoctor?.name }} ({{ selectedDoctor?.department_name }})</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="text-muted-foreground">问诊人：</span>
            <span>{{ selectedProfile?.name }}</span>
          </div>
        </div>
        <div class="space-y-2">
          <Label>病情描述</Label>
          <Textarea
            v-model="symptomDescription"
            placeholder="请详细描述您的症状、不适部位、持续时间等信息，以便医生更好地了解您的情况..."
            rows="5"
          />
        </div>
      </CardContent>
    </Card>

    <!-- 导航按钮 -->
    <div class="flex justify-between">
      <Button variant="outline" @click="prevStep" :disabled="step === 1">
        <ArrowLeft class="mr-2 h-4 w-4" />
        上一步
      </Button>
      <Button v-if="step < 3" @click="nextStep" :disabled="!canProceed()">
        下一步
        <ArrowRight class="ml-2 h-4 w-4" />
      </Button>
      <Button v-else @click="handleSubmit" :disabled="!canProceed()">
        <Check class="mr-2 h-4 w-4" />
        提交问诊
      </Button>
    </div>
  </div>
</template>