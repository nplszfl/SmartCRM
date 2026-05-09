import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { 
  AIInsight, 
  AnalyticsData, 
  RevenueAnalytics, 
  PipelineAnalytics,
  TeamAnalytics,
  AIMetricsAnalytics,
  RepPerformance 
} from '@/types'
import { analyticsApi, aiInsightsApi, performanceApi } from '@/api/analyticsService'

export const useAIDashboardStore = defineStore('aiDashboard', () => {
  // State
  const dashboardData = ref<AnalyticsData | null>(null)
  const aiInsights = ref<AIInsight[]>([])
  const repPerformance = ref<RepPerformance[]>([])
  const loading = ref(false)
  const insightsLoading = ref(false)
  const lastRefresh = ref<string | null>(null)
  const autoRefresh = ref(false)
  const refreshInterval = ref<number | null>(null)

  // Filters
  const dateRange = ref<[string, string]>([
    new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
    new Date().toISOString()
  ])
  const selectedTeamId = ref<string | null>(null)
  const selectedRepId = ref<string | null>(null)

  // Getters
  const unreadInsights = computed(() => 
    aiInsights.value.filter(i => i.type === 'warning' || i.type === 'risk')
  )

  const highConfidenceInsights = computed(() =>
    aiInsights.value.filter(i => i.confidence >= 80)
  )

  const actionInsights = computed(() =>
    aiInsights.value.filter(i => i.action && i.action.length > 0)
  )

  const topRepPerformers = computed(() =>
    [...repPerformance.value]
      .sort((a, b) => b.revenue - a.revenue)
      .slice(0, 5)
  )

  const insightsByType = computed(() => {
    const grouped: Record<string, AIInsight[]> = {}
    aiInsights.value.forEach(insight => {
      if (!grouped[insight.type]) {
        grouped[insight.type] = []
      }
      grouped[insight.type].push(insight)
    })
    return grouped
  })

  const aiMetricsSummary = computed(() => {
    if (!dashboardData.value?.aiMetrics) return null
    return {
      predictionAccuracy: dashboardData.value.aiMetrics.predictionAccuracy,
      insightsGenerated: dashboardData.value.aiMetrics.insightsGenerated,
      revenueInfluence: dashboardData.value.aiMetrics.revenueInfluence
    }
  })

  // Actions
  async function fetchDashboard() {
    loading.value = true
    try {
      const response = await analyticsApi.getDashboard({
        dateRange: dateRange.value,
        teamId: selectedTeamId.value || undefined,
        repId: selectedRepId.value || undefined
      })
      dashboardData.value = response.data
      lastRefresh.value = new Date().toISOString()
    } finally {
      loading.value = false
    }
  }

  async function fetchRevenueAnalytics() {
    loading.value = true
    try {
      const response = await analyticsApi.getRevenueAnalytics({
        dateRange: dateRange.value
      })
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchPipelineAnalytics() {
    loading.value = true
    try {
      const response = await analyticsApi.getPipelineAnalytics({
        dateRange: dateRange.value
      })
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchTeamAnalytics() {
    loading.value = true
    try {
      const response = await analyticsApi.getTeamAnalytics({
        dateRange: dateRange.value,
        teamId: selectedTeamId.value || undefined
      })
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchAIMetrics() {
    loading.value = true
    try {
      const response = await analyticsApi.getAIMetrics({
        dateRange: dateRange.value
      })
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchInsights(filters?: {
    type?: AIInsight['type']
    source?: AIInsight['source']
    dateRange?: [string, string]
  }) {
    insightsLoading.value = true
    try {
      const response = await aiInsightsApi.getInsights({
        dateRange: dateRange.value,
        ...filters
      })
      aiInsights.value = response.data
      return response.data
    } finally {
      insightsLoading.value = false
    }
  }

  async function markInsightAsRead(id: string) {
    await aiInsightsApi.markAsRead(id)
    const insight = aiInsights.value.find(i => i.id === id)
    if (insight) {
      insight.confidence = insight.confidence // Trigger reactivity if needed
    }
  }

  async function dismissInsight(id: string) {
    await aiInsightsApi.dismissInsight(id)
    aiInsights.value = aiInsights.value.filter(i => i.id !== id)
  }

  async function provideInsightFeedback(id: string, helpful: boolean, comment?: string) {
    await aiInsightsApi.provideFeedback({ insightId: id, helpful, comment })
  }

  async function fetchCoachingInsights(repId: string) {
    insightsLoading.value = true
    try {
      const response = await aiInsightsApi.getCoachingInsights(repId)
      return response.data
    } finally {
      insightsLoading.value = false
    }
  }

  async function fetchRepPerformance(repId: string) {
    try {
      const response = await performanceApi.getRepPerformance(repId)
      return response.data
    } catch {
      return null
    }
  }

  async function fetchLeaderboard(metric: string = 'revenue') {
    try {
      const response = await performanceApi.getLeaderboard(metric, dateRange.value.join(','))
      return response.data
    } catch {
      return null
    }
  }

  function setDateRange(range: [string, string]) {
    dateRange.value = range
    fetchDashboard()
  }

  function setTeamFilter(teamId: string | null) {
    selectedTeamId.value = teamId
    fetchDashboard()
  }

  function setRepFilter(repId: string | null) {
    selectedRepId.value = repId
    fetchDashboard()
  }

  function startAutoRefresh(intervalMs: number = 60000) {
    if (refreshInterval.value) {
      clearInterval(refreshInterval.value)
    }
    autoRefresh.value = true
    refreshInterval.value = window.setInterval(() => {
      fetchDashboard()
      fetchInsights()
    }, intervalMs)
  }

  function stopAutoRefresh() {
    autoRefresh.value = false
    if (refreshInterval.value) {
      clearInterval(refreshInterval.value)
      refreshInterval.value = null
    }
  }

  function getInsightIcon(type: AIInsight['type']): string {
    switch (type) {
      case 'recommendation': return 'InfoFilled'
      case 'warning': return 'WarningFilled'
      case 'opportunity': return 'CircleCheckFilled'
      case 'risk': return 'Warning'
      default: return 'InfoFilled'
    }
  }

  function getInsightColor(type: AIInsight['type']): string {
    switch (type) {
      case 'recommendation': return 'primary'
      case 'warning': return 'warning'
      case 'opportunity': return 'success'
      case 'risk': return 'danger'
      default: return 'info'
    }
  }

  function formatInsightTime(dateStr: string): string {
    const date = new Date(dateStr)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMins / 60)
    const diffDays = Math.floor(diffHours / 24)

    if (diffMins < 60) return `${diffMins}m ago`
    if (diffHours < 24) return `${diffHours}h ago`
    if (diffDays < 7) return `${diffDays}d ago`
    return date.toLocaleDateString()
  }

  return {
    // State
    dashboardData,
    aiInsights,
    repPerformance,
    loading,
    insightsLoading,
    lastRefresh,
    autoRefresh,
    dateRange,
    selectedTeamId,
    selectedRepId,
    // Getters
    unreadInsights,
    highConfidenceInsights,
    actionInsights,
    topRepPerformers,
    insightsByType,
    aiMetricsSummary,
    // Actions
    fetchDashboard,
    fetchRevenueAnalytics,
    fetchPipelineAnalytics,
    fetchTeamAnalytics,
    fetchAIMetrics,
    fetchInsights,
    markInsightAsRead,
    dismissInsight,
    provideInsightFeedback,
    fetchCoachingInsights,
    fetchRepPerformance,
    fetchLeaderboard,
    setDateRange,
    setTeamFilter,
    setRepFilter,
    startAutoRefresh,
    stopAutoRefresh,
    getInsightIcon,
    getInsightColor,
    formatInsightTime
  }
})