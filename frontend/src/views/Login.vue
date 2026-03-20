<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, useMessage } from 'naive-ui'
import axios from 'axios'

const router = useRouter()
const message = useMessage()

const form = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    message.warning('请输入用户名和密码')
    return
  }
  
  try {
    const response = await axios.post('/api/auth/login', form.value)
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('user', JSON.stringify(response.data.user))
    message.success('登录成功')
    router.push('/files')
  } catch (error: any) {
    message.error(error.response?.data?.message || '登录失败，请检查用户名和密码')
  }
}
</script>

<template>
  <div class="login-container">
    <NCard title="登录 NetDisk" style="width: 400px">
      <NForm>
        <NFormItem label="用户名">
          <NInput v-model:value="form.username" placeholder="请输入用户名" />
        </NFormItem>
        <NFormItem label="密码">
          <NInput v-model:value="form.password" type="password" placeholder="请输入密码" />
        </NFormItem>
        <NSpace justify="end">
          <NButton @click="$router.push('/register')">注册</NButton>
          <NButton type="primary" @click="handleLogin">登录</NButton>
        </NSpace>
      </NForm>
    </NCard>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>
