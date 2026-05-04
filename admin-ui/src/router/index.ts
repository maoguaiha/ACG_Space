import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import AdminLayout from '../layout/AdminLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '数据大盘', icon: 'Odometer' }
      },
      {
        path: 'anime',
        name: 'AnimeManage',
        component: () => import('../views/anime/index.vue'),
        meta: { title: '番剧库管理', icon: 'VideoCamera' }
      },
      {
        path: 'article',
        name: 'ArticleManage',
        component: () => import('../views/article/index.vue'),
        meta: { title: '文章管理', icon: 'Document' }
      },
      {
        path: 'article/review',
        name: 'ArticleReview',
        component: () => import('../views/article/ReviewList.vue'),
        meta: { title: '文章审核', icon: 'Document' }
      },
      {
        path: 'comment',
        name: 'CommentManage',
        component: () => import('../views/comment/index.vue'),
        meta: { title: '评论审核', icon: 'ChatDotRound' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
