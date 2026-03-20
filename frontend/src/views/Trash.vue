<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NDataTable, NButton, NSpace, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import axios from 'axios'

interface TrashItem {
  id: number
  fileName: string
  originalName: string
  fileSize: number
  isFolder: boolean
  deletedAt: string
}

const message = useMessage()
const trashItems = ref<TrashItem[]>([])

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const columns: DataTableColumns<TrashItem> = [
  {
    title: '名称',
    key: 'name',
    render(row) {
      return row.originalName
    }
  },
  {
    title: '大小',
    key: 'size',
    render(row) {
      return row.isFolder ? '-' : formatFileSize(row.fileSize)
    }
  },
  {
    title: '删除时间',
    key: 'deletedAt',
    render(row) {
      return new Date(row.deletedAt).toLocaleString()
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row) {
      return h('div', { class: 'action-buttons' }, [
        h(NButton, { 
          size: 'small', 
          type: 'primary',
          onClick: () => handleRestore(row) 
        }, { default: () => '恢复' }),
        h(NButton, { 
          size: 'small', 
          type: 'error',
          onClick: () => handlePermanentDelete(row) 
        }, { default: () => '彻底删除' })
      ])
    }
  }
]

import { h } from 'vue'

const fetchTrash = async () => {
  try {
    const response = await axios.get('/api/trash')
    trashItems.value = response.data.items || []
  } catch (error) {
    message.error('获取回收站失败')
  }
}

const handleRestore = async (item: TrashItem) => {
  try {
    await axios.post(`/api/trash/${item.id}/restore`)
    message.success('已恢复')
    fetchTrash()
  } catch (error) {
    message.error('恢复失败')
  }
}

const handlePermanentDelete = async (item: TrashItem) => {
  try {
    await axios.delete(`/api/trash/${item.id}`)
    message.success('已彻底删除')
    fetchTrash()
  } catch (error) {
    message.error('删除失败')
  }
}

const handleEmptyTrash = async () => {
  try {
    await axios.delete('/api/trash/empty')
    message.success('回收站已清空')
    fetchTrash()
  } catch (error) {
    message.error('清空失败')
  }
}

onMounted(() => {
  fetchTrash()
})
</script>

<template>
  <div class="trash-container">
    <NCard title="回收站">
      <template #header-extra>
        <NSpace>
          <NButton size="small" @click="fetchTrash">刷新</NButton>
          <NButton size="small" type="error" @click="handleEmptyTrash">清空回收站</NButton>
        </NSpace>
      </template>
      <p v-if="trashItems.length === 0" style="text-align: center; color: #999; padding: 40px;">
        回收站是空的
      </p>
      <NDataTable
        v-else
        :columns="columns"
        :data="trashItems"
        :row-key="(row: TrashItem) => row.id"
      />
    </NCard>
  </div>
</template>

<style scoped>
.trash-container {
  padding: 16px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}
</style>
