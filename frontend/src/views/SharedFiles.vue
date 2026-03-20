<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NDataTable, NButton, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import axios from 'axios'

interface ShareItem {
  id: number
  fileId: number
  fileName: string
  token: string
  shareType: number
  passwordHash: string | null
  expiresAt: string | null
  downloadCount: number
  createdAt: string
}

const message = useMessage()
const shares = ref<ShareItem[]>([])

const columns: DataTableColumns<ShareItem> = [
  {
    title: '文件名',
    key: 'fileName'
  },
  {
    title: '分享链接',
    key: 'token',
    render(row) {
      const url = `${window.location.origin}/s/${row.token}`
      return url
    }
  },
  {
    title: '访问次数',
    key: 'downloadCount'
  },
  {
    title: '创建时间',
    key: 'createdAt',
    render(row) {
      return new Date(row.createdAt).toLocaleString()
    }
  },
  {
    title: '状态',
    key: 'status',
    render(row) {
      if (row.expiresAt && new Date(row.expiresAt) < new Date()) {
        return h(NTag, { type: 'error' }, { default: () => '已过期' })
      }
      return h(NTag, { type: 'success' }, { default: () => '有效' })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render(row) {
      return h('div', { class: 'action-buttons' }, [
        h(NButton, { 
          size: 'small', 
          text: true,
          onClick: () => handleCopyLink(row) 
        }, { default: () => '复制链接' }),
        h(NButton, { 
          size: 'small', 
          text: true,
          onClick: () => handleDelete(row) 
        }, { default: () => '取消分享' })
      ])
    }
  }
]

import { h } from 'vue'

const fetchShares = async () => {
  try {
    const response = await axios.get('/api/shares')
    shares.value = response.data.items || []
  } catch (error) {
    message.error('获取分享列表失败')
  }
}

const handleCopyLink = (share: ShareItem) => {
  const url = `${window.location.origin}/s/${share.token}`
  navigator.clipboard.writeText(url)
  message.success('链接已复制')
}

const handleDelete = async (share: ShareItem) => {
  try {
    await axios.delete(`/api/shares/${share.id}`)
    message.success('已取消分享')
    fetchShares()
  } catch (error) {
    message.error('操作失败')
  }
}

onMounted(() => {
  fetchShares()
})
</script>

<template>
  <div class="shared-container">
    <NCard title="我的分享">
      <template #header-extra>
        <NButton size="small" @click="fetchShares">刷新</NButton>
      </template>
      <NDataTable
        :columns="columns"
        :data="shares"
        :row-key="(row: ShareItem) => row.id"
      />
    </NCard>
  </div>
</template>

<style scoped>
.shared-container {
  padding: 16px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}
</style>
