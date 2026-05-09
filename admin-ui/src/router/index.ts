import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import AdminLayout from '../layout/AdminLayout.vue'
import { Box, Present, Wallet, Van, ShoppingCart } from '@element-plus/icons-vue'

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
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('../views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'item',
        name: 'ItemManage',
        component: () => import('../views/item/index.vue'),
        meta: { title: '商品图鉴', icon: Box }
      },
      {
        path: 'gacha',
        name: 'GachaManage',
        component: () => import('../views/gacha/index.vue'),
        meta: { title: '抽赏配置', icon: Present }
      },
      {
        path: 'gacha/config/:poolId',
        name: 'GachaConfig',
        component: () => import('../views/gacha/config.vue'),
        meta: { title: '奖池配置', icon: Present }
      },
      {
        path: 'transaction',
        name: 'TransactionManage',
        component: () => import('../views/transaction/index.vue'),
        meta: { title: '交易监控', icon: Wallet }
      },
      {
        path: 'delivery',
        name: 'DeliveryManage',
        component: () => import('../views/delivery/index.vue'),
        meta: { title: '物流调度', icon: Van }
      },
      {
        path: 'redeem-product',
        name: 'RedeemProductManage',
        component: () => import('../views/redeem-product/index.vue'),
        meta: { title: '兑换商品管理', icon: ShoppingCart }
      },
      {
        path: 'risk-control',
        name: 'RiskControl',
        component: () => import('../views/risk-control/index.vue'),
        meta: { title: '风控中心', icon: 'Shield' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
