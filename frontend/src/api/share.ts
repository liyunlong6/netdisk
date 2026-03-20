import api from './index'

export interface Share {
  id: number
  fileId: number
  fileName: string
  token: string
  expiresAt: string | null
  downloadCount: number
  createdAt: string
}

export interface ShareListResponse {
  items: Share[]
}

export interface CreateShareRequest {
  fileId: number
  password?: string
  expiresAt?: string
  maxDownloads?: number
}

export interface CreateShareResponse {
  id: number
  token: string
  url: string
}

export const shareApi = {
  // 创建分享
  createShare: (data: CreateShareRequest) => {
    return api.post<CreateShareResponse>('/shares', data)
  },
  
  // 获取我的分享
  getMyShares: () => {
    return api.get<ShareListResponse>('/shares')
  },
  
  // 删除分享
  deleteShare: (id: number) => {
    return api.delete(`/shares/${id}`)
  },
  
  // 访问分享
  accessShare: (token: string, password?: string) => {
    return api.get(`/s/${token}`, { params: { password } })
  },
  
  // 下载分享文件
  downloadShare: (token: string, password?: string) => {
    return api.get(`/s/${token}/download`, { 
      params: { password },
      responseType: 'blob'
    })
  }
}
