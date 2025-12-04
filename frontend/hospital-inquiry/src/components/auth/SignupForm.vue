<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
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
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const name = ref('')
const mobile = ref('')
const id_card = ref('')
const password = ref('')
const confirmPassword = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')

const handleSubmit = async () => {
  if (isSubmitting.value) {
    return
  }

  errorMessage.value = ''
  isSubmitting.value = true

  try {
    await authStore.signup({
      name: name.value,
      mobile: mobile.value,
      id_card: id_card.value,
      password: password.value,
    })

    await authStore.login({
      mobile: mobile.value,
      password: password.value,
    })

    router.push({ name: 'name' })
  } catch (error) {
    const message = error instanceof Error ? error.message : '注册失败，请重试'
    errorMessage.value = message
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle>创建账户</CardTitle>
      <CardDescription>
        输入您的信息以创建账户
      </CardDescription>
    </CardHeader>
    <CardContent>
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <FieldGroup>
          <Field>
            <FieldLabel for="name">
              姓名
            </FieldLabel>
            <Input
              id="name"
              type="text"
              placeholder="张三"
              v-model="name"
              :disabled="isSubmitting"
              required
            />
          </Field>
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
              v-model="id_card"
              :disabled="isSubmitting"
              required
            />
            <FieldDescription>请输入18位身份证号码。</FieldDescription>
          </Field>
          <Field>
            <FieldLabel for="password">
              密码
            </FieldLabel>
            <Input
              id="password"
              type="password"
              v-model="password"
              :disabled="isSubmitting"
              required
            />
            <FieldDescription>密码长度至少为8个字符。</FieldDescription>
          </Field>
          <Field>
            <FieldLabel for="confirm-password">
              确认密码
            </FieldLabel>
            <Input
              id="confirm-password"
              type="password"
              v-model="confirmPassword"
              :disabled="isSubmitting"
              required
            />
            <FieldDescription>请再次输入密码进行确认。</FieldDescription>
          </Field>
          <FieldGroup>
            <Field>
              <Button type="submit" :disabled="isSubmitting">
                创建账户
              </Button>
              <FieldDescription v-if="errorMessage" class="text-sm text-destructive">
                {{ errorMessage }}
              </FieldDescription>
              <FieldDescription class="px-6 text-center">
                已有账户？
                <RouterLink :to="{ name: 'login' }">
                  登录
                </RouterLink>
              </FieldDescription>
            </Field>
          </FieldGroup>
        </FieldGroup>
      </form>
    </CardContent>
  </Card>
</template>
