import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/tickets'
  },
  {
    path: '/tickets',
    name: 'TicketList',
    component: () => import('@/views/ticket/TicketListView.vue')
  },
  {
    path: '/tickets/:id',
    name: 'TicketDetail',
    component: () => import('@/views/ticket/TicketDetailView.vue')
  },
  {
    path: '/kanban',
    name: 'Kanban',
    component: () => import('@/views/kanban/KanbanBoardView.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/AdminDashboardView.vue')
  },
  {
    path: '/ai-insights',
    name: 'AIInsights',
    component: () => import('@/views/ai/AIInsightsPanel.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
