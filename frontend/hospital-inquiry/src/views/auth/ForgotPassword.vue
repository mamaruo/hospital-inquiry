<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { toast } from 'vue-sonner'
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
import { forgotPassword } from '@/lib/api'

const router = useRouter()

const mobile = ref('')
const idCard = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')

const mobilePattern = /^1\d{10}$/
const idCardPattern = /^\d{17}[\dXx]$/

function validate(): string | null {
  if (!mobilePattern.test(mobile.value)) {
    return '请输入正确的手机号'
  }
  if (!idCardPattern.test(idCard.value)) {
    return '请输入18位身份证号码'
  }
  if (newPassword.value.length < 8) {
    return '新密码长度至少为8个字符'
  }
  if (confirmPassword.value !== newPassword.value) {
    return '两次输入的新密码不一致'
  }
  return null
}

async function handleSubmit() {
  if (isSubmitting.value) {
    return
  }

  errorMessage.value = ''
  const validationError = validate()
  if (validationError) {
    errorMessage.value = validationError
    return
  }

  isSubmitting.value = true
  try {
    await forgotPassword(mobile.value, idCard.value, newPassword.value)
    toast.success('密码重置成功，请使用新密码登录')
    router.push('/login')
  } catch (error) {
    const message = error instanceof Error ? error.message : '密码重置失败，请重试'
    errorMessage.value = message
    toast.error(message)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="flex min-h-svh w-full items-center justify-center p-6 md:p-10">
    <div class="w-full max-w-sm">
      <Card>
        <CardHeader>
          <CardTitle>重置密码</CardTitle>
          <CardDescription>
            输入您的手机号与身份证号以重置账户密码
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="handleSubmit" class="space-y-4">
            <FieldGroup>
              <Field>
                <FieldLabel for="phone">
                  手机号
                </FieldLabel>
                <Input
                  id="phone"
                  type="tel"
                  placeholder="13812345678"
                  v-model="mobile"
                  :disabled="isSubmitting"
                  required
                />
              </Field>
              <Field>
                <FieldLabel for="idCard">
                  身份证号
                </FieldLabel>
                <Input
                  id="id_card"
                  type="text"
                  placeholder="110101199001011234"
                  v-model="idCard"
                  :disabled="isSubmitting"
                  required
                />
                <FieldDescription>请输入18位身份证号码。</FieldDescription>
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
                <FieldLabel for="confirm-password">
                  确认新密码
                </FieldLabel>
                <Input
                  id="confirm-password"
                  type="password"
                  v-model="confirmPassword"
                  :disabled="isSubmitting"
                  required
                />
                <FieldDescription>请再次输入新密码进行确认。</FieldDescription>
              </Field>
              <Field>
                <Button type="submit" :disabled="isSubmitting">
                  重置密码
                </Button>
                <FieldDescription v-if="errorMessage" class="text-sm text-destructive">
                  {{ errorMessage }}
                </FieldDescription>
                <FieldDescription class="text-center">
                  想起密码了？
                  <RouterLink :to="{ name: 'login' }">
                    返回登录
                  </RouterLink>
                </FieldDescription>
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
