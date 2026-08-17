<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from 'vue-sonner'
import { ArrowLeft } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import { useAuthStore } from '@/stores/auth'
import { changePassword, getCurrentUser } from '@/lib/api'

const router = useRouter()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const isLoading = ref(false)

const roleLabels: Record<string, string> = {
  PATIENT: '患者',
  DOCTOR: '医生',
  ADMIN: '管理员',
}
const roleLabel = computed(() => {
  const role = user.value?.role
  return role ? roleLabels[role] ?? role : '未知'
})

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')

onMounted(async () => {
  isLoading.value = true
  try {
    const fresh = await getCurrentUser()
    authStore.setUser(fresh)
  } catch {
    // 拉取失败时回退展示本地缓存的用户信息
  } finally {
    isLoading.value = false
  }
})

function validatePasswordForm(): string | null {
  if (!oldPassword.value) {
    return '请输入原密码'
  }
  if (newPassword.value.length < 8) {
    return '新密码长度至少为8个字符'
  }
  if (confirmPassword.value !== newPassword.value) {
    return '两次输入的新密码不一致'
  }
  return null
}

async function handleChangePassword() {
  if (isSubmitting.value) {
    return
  }

  errorMessage.value = ''
  const validationError = validatePasswordForm()
  if (validationError) {
    errorMessage.value = validationError
    return
  }

  isSubmitting.value = true
  try {
    await changePassword(oldPassword.value, newPassword.value)
    toast.success('密码修改成功')
    oldPassword.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
  } catch (error) {
    const message = error instanceof Error ? error.message : '密码修改失败，请重试'
    errorMessage.value = message
    toast.error(message)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="flex min-h-svh w-full flex-col items-center justify-center p-6 md:p-10">
    <div class="w-full max-w-md">
      <Button
        variant="ghost"
        size="sm"
        class="mb-4 -ml-2"
        @click="router.back()"
      >
        <ArrowLeft class="h-4 w-4" />
        返回
      </Button>

      <div class="flex flex-col gap-6">
        <Card>
          <CardHeader>
            <CardTitle>个人中心</CardTitle>
            <CardDescription>
              查看您的账户信息
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div v-if="isLoading && !user" class="text-sm text-muted-foreground">
              加载中...
            </div>
            <dl v-else class="space-y-4">
              <div class="flex items-center justify-between gap-4">
                <dt class="text-sm text-muted-foreground">姓名</dt>
                <dd class="text-sm font-medium">{{ user?.name || '-' }}</dd>
              </div>
              <Separator />
              <div class="flex items-center justify-between gap-4">
                <dt class="text-sm text-muted-foreground">手机号</dt>
                <dd class="text-sm font-medium">{{ user?.mobile || '-' }}</dd>
              </div>
              <Separator />
              <div class="flex items-center justify-between gap-4">
                <dt class="text-sm text-muted-foreground">身份证号</dt>
                <dd class="text-sm font-medium">{{ user?.id_card || '-' }}</dd>
              </div>
              <Separator />
              <div class="flex items-center justify-between gap-4">
                <dt class="text-sm text-muted-foreground">角色</dt>
                <dd class="text-sm font-medium">{{ roleLabel }}</dd>
              </div>
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>修改密码</CardTitle>
            <CardDescription>
              输入原密码与新密码以更新您的登录密码
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form @submit.prevent="handleChangePassword" class="space-y-4">
              <FieldGroup>
                <Field>
                  <FieldLabel for="old-password">
                    原密码
                  </FieldLabel>
                  <Input
                    id="old-password"
                    type="password"
                    v-model="oldPassword"
                    :disabled="isSubmitting"
                    required
                  />
                </Field>
                <Field>
                  <FieldLabel for="new-password">
                    新密码
                  </FieldLabel>
                  <Input
                    id="new-password"
                    type="password"
                    v-model="newPassword"
                    :disabled="isSubmitting"
                    required
                  />
                  <FieldDescription>新密码长度至少为8个字符。</FieldDescription>
                </Field>
                <Field>
                  <FieldLabel for="confirm-new-password">
                    确认新密码
                  </FieldLabel>
                  <Input
                    id="confirm-new-password"
                    type="password"
                    v-model="confirmPassword"
                    :disabled="isSubmitting"
                    required
                  />
                  <FieldDescription>请再次输入新密码进行确认。</FieldDescription>
                </Field>
                <Field>
                  <Button type="submit" :disabled="isSubmitting">
                    修改密码
                  </Button>
                  <FieldDescription v-if="errorMessage" class="text-sm text-destructive">
                    {{ errorMessage }}
                  </FieldDescription>
                </Field>
              </FieldGroup>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  </div>
</template>
