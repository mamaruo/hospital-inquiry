<script setup lang="ts">
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
      <form>
        <FieldGroup>
          <Field>
            <FieldLabel for="name">
              姓名
            </FieldLabel>
            <Input id="name" type="text" placeholder="张三" required />
          </Field>
          <Field>
            <FieldLabel for="phone">
              手机号
            </FieldLabel>
            <Input
              id="phone"
              type="tel"
              placeholder="13812345678"
              required
            />
            <FieldDescription>
              我们将使用此手机号与您联系。我们不会与他人共享您的手机号。
            </FieldDescription>
          </Field>
          <Field>
            <FieldLabel for="idCard">
              身份证号
            </FieldLabel>
            <Input
              id="idCard"
              type="text"
              placeholder="110101199001011234"
              required
            />
            <FieldDescription>请输入18位身份证号码。</FieldDescription>
          </Field>
          <Field>
            <FieldLabel for="password">
              密码
            </FieldLabel>
            <Input id="password" type="password" required />
            <FieldDescription>密码长度至少为8个字符。</FieldDescription>
          </Field>
          <Field>
            <FieldLabel for="confirm-password">
              确认密码
            </FieldLabel>
            <Input id="confirm-password" type="password" required />
            <FieldDescription>请再次输入密码进行确认。</FieldDescription>
          </Field>
          <FieldGroup>
            <Field>
              <Button type="submit">
                创建账户
              </Button>
              <Button variant="outline" type="button">
                使用 Google 注册
              </Button>
              <FieldDescription class="px-6 text-center">
                已有账户？
                <RouterLink :to="{ name: 'login'}">
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
