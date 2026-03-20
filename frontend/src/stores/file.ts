import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { fileApi, type FileItem, type FileListResponse } from '@/api/file'
import { useAuthStore } from './auth'

export const useFileStore = defineStore('file', () => {
  const authStore = useAuthStore()
  
  const files = ref<FileItem[]>([])
  const currentFolderId = ref<number | null>(null)
  const breadcrumb = ref<{ id: number | null; name: string }[]>([{ id: null, name: '全部文件' }])
  const selectedFiles = ref<number[]>([])
  const isLoading = ref(false)
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(20)
  
  // 计算属性
  const currentPath = computed(() => {
    return breadcrumb.value.map(b => b.name).join(' / ')
  })
  
  // 获取文件列表
  async function fetchFiles() {
    isLoading.value = true
    try {
      const response = await fileApi.getFileList({
        parentId: currentFolderId.value,
        page: page.value,
        size: pageSize.value
      })
      const data = response.data
      files.value = data.items
      total.value = data.total
    } catch (error) {
      console.error('获取文件列表失败', error)
    } finally {
      isLoading.value = false
    }
  }
  
  // 进入文件夹
  function enterFolder(folder: FileItem) {
    if (!folder.isFolder) return
    
    currentFolderId.value = folder.id
    breadcrumb.value.push({ id: folder.id, name: folder.originalName })
    selectedFiles.value = []
    fetchFiles()
  }
  
  // 返回上级目录
  function navigateBack() {
    if (breadcrumb.value.length > 1) {
      breadcrumb.value.pop()
      currentFolderId.value = breadcrumb.value[breadcrumb.value.length - 1].id
      selectedFiles.value = []
      fetchFiles()
    }
  }
  
  // 导航到面包屑
  function navigateToBreadcrumb(index: number) {
    if (index < breadcrumb.value.length - 1) {
      breadcrumb.value = breadcrumb.value.slice(0, index + 1)
      currentFolderId.value = breadcrumb.value[index].id
      selectedFiles.value = []
      fetchFiles()
    }
  }
  
  // 上传文件
  async function uploadFile(file: File) {
    try {
      await fileApi.uploadFile(file, currentFolderId.value)
      await fetchFiles()
      // 更新存储使用量
      if (authStore.user) {
        authStore.updateStorageUsed(authStore.user.storageUsed + file.size)
      }
    } catch (error) {
      console.error('上传失败', error)
      throw error
    }
  }
  
  // 创建文件夹
  async function createFolder(name: string) {
    try {
      await fileApi.createFolder(name, currentFolderId.value)
      await fetchFiles()
    } catch (error) {
      console.error('创建文件夹失败', error)
      throw error
    }
  }
  
  // 删除文件
  async function deleteFile(id: number) {
    try {
      await fileApi.deleteFile(id)
      files.value = files.value.filter(f => f.id !== id)
    } catch (error) {
      console.error('删除失败', error)
      throw error
    }
  }
  
  // 重命名
  async function renameFile(id: number, newName: string) {
    try {
      await fileApi.renameFile(id, newName)
      await fetchFiles()
    } catch (error) {
      console.error('重命名失败', error)
      throw error
    }
  }
  
  // 移动文件
  async function moveFile(fileId: number, targetFolderId: number) {
    try {
      await fileApi.moveFile(fileId, targetFolderId)
      await fetchFiles()
    } catch (error) {
      console.error('移动失败', error)
      throw error
    }
  }
  
  // 搜索文件
  async function searchFiles(keyword: string) {
    isLoading.value = true
    try {
      const response = await fileApi.searchFiles(keyword)
      files.value = response.data.items
      total.value = response.data.total
    } catch (error) {
      console.error('搜索失败', error)
    } finally {
      isLoading.value = false
    }
  }
  
  // 切换选择
  function toggleSelect(id: number) {
    const index = selectedFiles.value.indexOf(id)
    if (index === -1) {
      selectedFiles.value.push(id)
    } else {
      selectedFiles.value.splice(index, 1)
    }
  }
  
  // 全选
  function selectAll() {
    selectedFiles.value = files.value.map(f => f.id)
  }
  
  // 取消选择
  function clearSelection() {
    selectedFiles.value = []
  }
  
  return {
    files,
    currentFolderId,
    breadcrumb,
    selectedFiles,
    isLoading,
    total,
    page,
    pageSize,
    currentPath,
    fetchFiles,
    enterFolder,
    navigateBack,
    navigateToBreadcrumb,
    uploadFile,
    createFolder,
    deleteFile,
    renameFile,
    moveFile,
    searchFiles,
    toggleSelect,
    selectAll,
    clearSelection
  }
})
