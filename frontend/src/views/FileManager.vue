<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NLayout, NLayoutSider, NLayoutContent, NMenu, NButton, NSpace, NDataTable, NUpload, useMessage, NModal, NInput, NIcon } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { FolderOutline, DocumentOutline, TrashOutline, ShareOutline, CloudUploadOutline, AddOutline } from '@vicons/ionicons5'
import axios from 'axios'

interface FileItem {
  id: number
  fileName: string
  originalName: string
  fileSize: number
  contentType: string
  isFolder: boolean
  isFavorite: boolean
  createdAt: string
  parentFolderId: number | null
}

const message = useMessage()
const files = ref<FileItem[]>([])
const currentFolderId = ref<number | null>(null)
const breadcrumb = ref<{ id: number | null; name: string }[]>([{ id: null, name: '全部文件' }])
const selectedKeys = ref<string[]>([])
const showNewFolderModal = ref(false)
const newFolderName = ref('')

const columns: DataTableColumns<FileItem> = [
  { type: 'selection' },
  {
    title: '名称',
    key: 'name',
    render(row) {
      return row.isFolder ? row.originalName : row.originalName
    }
  },
  {
    title: '大小',
    key: 'size',
    render(row) {
      if (row.isFolder) return '-'
      return formatFileSize(row.fileSize)
    }
  },
  {
    title: '修改时间',
    key: 'modified',
    render(row) {
      return new Date(row.createdAt).toLocaleString()
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render(row) {
      return h('div', { class: 'action-buttons' }, [
        !row.isFolder && h(NButton, { 
          size: 'small', 
          text: true,
          onClick: () => handleDownload(row) 
        }, { default: () => '下载' }),
        h(NButton, { 
          size: 'small', 
          text: true,
          onClick: () => handleShare(row) 
        }, { default: () => '分享' }),
        h(NButton, { 
          size: 'small', 
          text: true,
          onClick: () => handleDelete(row) 
        }, { default: () => '删除' })
      ])
    }
  }
]

import { h } from 'vue'

const menuOptions = [
  { label: '我的文件', key: '/files' },
  { label: '分享文件', key: '/shared' },
  { label: '回收站', key: '/trash' }
]

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const fetchFiles = async () => {
  try {
    const response = await axios.get('/api/files', {
      params: { parentId: currentFolderId.value }
    })
    files.value = response.data.items || []
  } catch (error) {
    message.error('获取文件列表失败')
  }
}

const handleUpload = async (options: { file: File }) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    if (currentFolderId.value) {
      formData.append('parentId', currentFolderId.value.toString())
    }
    
    await axios.post('/api/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    message.success('上传成功')
    fetchFiles()
  } catch (error) {
    message.error('上传失败')
  }
}

const handleNewFolder = async () => {
  if (!newFolderName.value.trim()) {
    message.warning('请输入文件夹名称')
    return
  }
  
  try {
    await axios.post('/api/folders', {
      name: newFolderName.value,
      parentId: currentFolderId.value
    })
    message.success('创建成功')
    showNewFolderModal.value = false
    newFolderName.value = ''
    fetchFiles()
  } catch (error) {
    message.error('创建失败')
  }
}

const handleDownload = (file: FileItem) => {
  window.open(`/api/files/${file.id}/download`, '_blank')
}

const handleShare = async (file: FileItem) => {
  try {
    const response = await axios.post('/api/shares', { fileId: file.id })
    const shareUrl = `${window.location.origin}/s/${response.data.token}`
    navigator.clipboard.writeText(shareUrl)
    message.success('分享链接已复制到剪贴板')
  } catch (error) {
    message.error('分享失败')
  }
}

const handleDelete = async (file: FileItem) => {
  try {
    await axios.delete(`/api/files/${file.id}`)
    message.success('删除成功')
    fetchFiles()
  } catch (error) {
    message.error('删除失败')
  }
}

const handleDoubleClick = (row: FileItem) => {
  if (row.isFolder) {
    currentFolderId.value = row.id
    breadcrumb.value.push({ id: row.id, name: row.originalName })
    fetchFiles()
  }
}

const navigateToBreadcrumb = (item: { id: number | null; name: string }, index: number) => {
  breadcrumb.value = breadcrumb.value.slice(0, index + 1)
  currentFolderId.value = item.id
  fetchFiles()
}

onMounted(() => {
  fetchFiles()
})
</script>

<template>
  <NLayout has-sider style="height: 100vh">
    <!-- 侧边栏 -->
    <NLayoutSider bordered :width="200" content-style="padding: 16px;">
      <h2 style="margin-bottom: 16px; color: #667eea;">NetDisk</h2>
      <NMenu :options="menuOptions" : inverted="true" />
    </NLayoutSider>
    
    <!-- 主内容区 -->
    <NLayoutContent content-style="padding: 16px;">
      <!-- 工具栏 -->
      <div class="toolbar">
        <NSpace>
          <NUpload
            :custom-request="handleUpload"
            :show-file-list="false"
            multiple
          >
            <NButton type="primary">
              <template #icon>
                <CloudUploadOutline />
              </template>
              上传文件
            </NButton>
          </NUpload>
          <NButton @click="showNewFolderModal = true">
            <template #icon>
              <AddOutline />
            </template>
            新建文件夹
          </NButton>
        </NSpace>
      </div>
      
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <span 
          v-for="(item, index) in breadcrumb" 
          :key="index"
          @click="navigateToBreadcrumb(item, index)"
          class="breadcrumb-item"
        >
          {{ item.name }}
          <span v-if="index < breadcrumb.length - 1"> / </span>
        </span>
      </div>
      
      <!-- 文件列表 -->
      <div class="file-list">
        <NDataTable
          :columns="columns"
          :data="files"
          :row-key="(row: FileItem) => row.id"
          @update:checked-row-keys="(keys: string[]) => selectedKeys = keys"
          @dblclick="(row: FileItem) => handleDoubleClick(row)"
        />
      </div>
    </NLayoutContent>
  </NLayout>
  
  <!-- 新建文件夹弹窗 -->
  <NModal v-model:show="showNewFolderModal" preset="dialog" title="新建文件夹">
    <NInput v-model:value="newFolderName" placeholder="请输入文件夹名称" />
    <template #action>
      <NButton @click="showNewFolderModal = false">取消</NButton>
      <NButton type="primary" @click="handleNewFolder">创建</NButton>
    </template>
  </NModal>
</template>

<style scoped>
.toolbar {
  margin-bottom: 16px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
}

.breadcrumb {
  margin-bottom: 16px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  font-size: 14px;
}

.breadcrumb-item {
  cursor: pointer;
  color: #666;
}

.breadcrumb-item:hover {
  color: #667eea;
}

.file-list {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  min-height: 400px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}
</style>
