import api from './index'

export interface FileItem {
  id: number
  fileName: string
  originalName: string
  fileSize: number
  contentType: string
  storagePath: string
  ownerId: number
  parentFolderId: number | null
  isFolder: boolean
  isFavorite: boolean
  createdAt: string
  updatedAt: string
}

export interface FileListResponse {
  items: FileItem[]
  total: number
  page: number
  size: number
}

export const fileApi = {
  // 获取文件列表
  getFileList: (params: { parentId?: number | null; page?: number; size?: number }) => {
    return api.get<FileListResponse>('/files', { params })
  },
  
  // 上传文件
  uploadFile: (file: File, parentId?: number | null) => {
    const formData = new FormData()
    formData.append('file', file)
    if (parentId) {
      formData.append('parentId', parentId.toString())
    }
    return api.post<FileItem>('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  // 获取文件信息
  getFile: (id: number) => api.get<FileItem>(`/files/${id}`),
  
  // 下载文件
  downloadFile: (id: number) => {
    return api.get(`/files/${id}/download`, { responseType: 'blob' })
  },
  
  // 删除文件
  deleteFile: (id: number) => api.delete(`/files/${id}`),
  
  // 重命名文件
  renameFile: (id: number, name: string) => {
    return api.put(`/files/${id}`, { name })
  },
  
  // 移动文件
  moveFile: (fileId: number, targetFolderId: number) => {
    return api.post('/files/move', { fileId, targetFolderId })
  },
  
  // 收藏文件
  toggleFavorite: (id: number) => api.post(`/files/${id}/favorite`),
  
  // 搜索文件
  searchFiles: (keyword: string, page = 1, size = 20) => {
    return api.get<FileListResponse>('/files/search', { params: { keyword, page, size } })
  },
  
  // 创建文件夹
  createFolder: (name: string, parentId?: number | null) => {
    return api.post('/folders', { name, parentId })
  },
  
  // 删除文件夹
  deleteFolder: (id: number) => api.delete(`/folders/${id}`),
  
  // 获取回收站
  getTrash: (page = 1, size = 20) => {
    return api.get<FileListResponse>('/trash', { params: { page, size } })
  },
  
  // 恢复文件
  restoreFile: (id: number) => api.post(`/trash/${id}/restore`),
  
  // 彻底删除
  permanentDelete: (id: number) => api.delete(`/trash/${id}`),
  
  // 清空回收站
  emptyTrash: () => api.delete('/trash/empty')
}
