import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/files'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/files',
    name: 'Files',
    component: () => import('@/views/FileManager.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/shared',
    name: 'Shared',
    component: () => import('@/views/SharedFiles.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/trash',
    name: 'Trash',
    component: () => import('@/views/Trash.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.name === 'Login' || to.name === 'Register') && token) {
    next('/files')
  } else {
    next()
  }
})

export default router
