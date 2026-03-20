<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, useMessage } from 'naive-ui'
import axios from 'axios'

const router = useRouter()
const message = useMessage()

const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const handleRegister = async () => {
  if (!form.value.username || !form.value.email || !form.value.password) {
    message.warning('请填写所有必填项')
    return
  }
  
  if (form.value.password !== form.value.confirmPassword) {
    message.warning('两次密码输入不一致')
    return
  }
  
  try {
    await axios.post('/api/auth/register', {
      username: form.value.username,
      email: form.value.email,
      password: form.value.password
    })
    message.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    message.error(error.response?.data?.message || '注册失败')
  }
}
</script>

<template>
  <div class="register-container">
    <NCard title="注册 NetDisk" style="width: 400px">
      <NForm>
        <NFormItem label="用户名">
          <NInput v-model:value="form.username" placeholder="请输入用户名" />
        </NFormItem>
        <NFormItem label="邮箱">
          <NInput v-model:value="form.email" placeholder="请输入邮箱" />
        </NFormItem>
        <NFormItem label="密码">
          <NInput v-model:value="form.password" type="password" placeholder="请输入密码" />
        </NFormItem>
        <NFormItem label="确认密码">
          <NInput v-model:value="form.confirmPassword" type="password" placeholder="请确认密码" />
        </NFormItem>
        <NSpace justify="end">
          <NButton @click="$router.push('/login')">返回登录</NButton>
          <NButton type="primary" @click="handleRegister">注册</NButton>
        </NSpace>
      </NForm>
    </NCard>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>
