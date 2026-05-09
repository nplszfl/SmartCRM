import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AdminStats, User, Activity } from '@/types/ticket'
import { adminApi, ticketApi } from '@/api/ticketService'

export const useAdminStore = defineStore('admin', () => {
  // State
  const stats = ref<AdminStats | null>(null)
  const users = ref<User[]>([])
  const loading = ref(false)
  const lastRefresh = ref<string | null>(null)

  // Getters
  const activeUsers = computed(() => users.value.filter(u => u.role !== 'admin'))

  const technicians = computed(() => 
    users.value.filter(u => u.role === 'technician' || u.role === 'agent')
  )

  // Actions
  async function fetchStats() {
    loading.value = true
    try {
      stats.value = await adminApi.getStats()
      lastRefresh.value = new Date().toISOString()
      return stats.value
    } finally {
      loading.value = false
    }
  }

  async function fetchUsers() {
    try {
      const response = await adminApi.getUsers()
      users.value = response.data
      return users.value
    } catch {
      return []
    }
  }

  async function updateUserRole(userId: string, role: string) {
    await adminApi.updateUserRole(userId, role)
    const user = users.value.find(u => u.id === userId)
    if (user) {
      user.role = role as User['role']
    }
  }

  async function getSettings() {
    return adminApi.getSettings()
  }

  async function updateSettings(settings: Record<string, unknown>) {
    await adminApi.updateSettings(settings)
  }

  return {
    // State
    stats,
    users,
    loading,
    lastRefresh,
    // Getters
    activeUsers,
    technicians,
    // Actions
    fetchStats,
    fetchUsers,
    updateUserRole,
    getSettings,
    updateSettings
  }
})
