<script setup lang="ts">
import { ref, type HTMLAttributes } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { cn } from '@/lib/utils'
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

const props = defineProps<{
  class?: HTMLAttributes['class']
}>()

const router = useRouter()
const authStore = useAuthStore()

const mobile = ref('')
const password = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')

const handleSubmit = async () => {
  if (isSubmitting.value) {
    return
  }

  errorMessage.value = ''
  isSubmitting.value = true

  try {
    await authStore.login({
      mobile: mobile.value,
      password: password.value,
    })
    // 登录成功后跳转到首页，路由会根据角色自动重定向
    router.push({ path: '/' })
  } catch (error) {
    const message = error instanceof Error ? error.message : '登录失败，请重试'
    errorMessage.value = message
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div :class="cn('flex flex-col gap-6', props.class)">
    <Card>
      <CardHeader>
        <CardTitle>登录您的账户</CardTitle>
        <CardDescription>
          输入您的手机号以登录账户
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
              <div class="flex items-center">
                <FieldLabel for="password">
                  密码
                </FieldLabel>
                <a
                  href="#"
                  class="ml-auto inline-block text-sm underline-offset-4 hover:underline"
                >
                  忘记密码？
                </a>
              </div>
              <Input
                id="password"
                type="password"
                v-model="password"
                :disabled="isSubmitting"
                required
              />
            </Field>
            <Field>
              <Button type="submit" :disabled="isSubmitting">
                登录
              </Button>
              <FieldDescription v-if="errorMessage" class="text-sm text-destructive">
                {{ errorMessage }}
              </FieldDescription>
              <FieldDescription class="text-center">
                没有账户？
                <RouterLink :to="{ name: 'signup' }">
                  注册
                </RouterLink>
              </FieldDescription>
            </Field>
          </FieldGroup>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
