import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AIInsight } from '@/types/ticket'
import { aiInsightsApi } from '@/api/ticketService'

export const useAIInsightsStore = defineStore('aiInsights', () => {
  // State
  const insights = ref<AIInsight[]>([])
  const ticketInsights = ref<Map<string, AIInsight[]>>(new Map())
  const loading = ref(false)
  const currentAnalysis = ref<{
    score: number
    confidence: number
    category: string
    suggestedActions: string[]
  } | null>(null)
  const slaPrediction = ref<{
    willMeetResponse: boolean
    willMeetResolution: boolean
    estimatedResponseTime: string
    estimatedResolutionTime: string
  } | null>(null)

  // Getters
  const warnings = computed(() => insights.value.filter(i => i.type === 'warning'))
  const risks = computed(() => insights.value.filter(i => i.type === 'risk'))
  const recommendations = computed(() => insights.value.filter(i => i.type === 'recommendation'))
  const opportunities = computed(() => insights.value.filter(i => i.type === 'opportunity'))

  const urgentInsights = computed(() =>
    insights.value.filter(i => i.type === 'warning' || i.type === 'risk')
  )

  // Actions
  async function fetchInsights() {
    loading.value = true
    try {
      insights.value = await aiInsightsApi.getInsights()
      return insights.value
    } finally {
      loading.value = false
    }
  }

  async function fetchTicketInsights(ticketId: string) {
    loading.value = true
    try {
      const ticketInsightsList = await aiInsightsApi.getInsights(ticketId)
      ticketInsights.value.set(ticketId, ticketInsightsList)
      return ticketInsightsList
    } finally {
      loading.value = false
    }
  }

  async function analyzeTicket(ticketId: string) {
    loading.value = true
    try {
      currentAnalysis.value = await aiInsightsApi.analyzeTicket(ticketId)
      return currentAnalysis.value
    } finally {
      loading.value = false
    }
  }

  async function predictSLA(ticketId: string) {
    loading.value = true
    try {
      slaPrediction.value = await aiInsightsApi.predictSLA(ticketId)
      return slaPrediction.value
    } finally {
      loading.value = false
    }
  }

  function clearAnalysis() {
    currentAnalysis.value = null
    slaPrediction.value = null
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
      case 'recommendation': return '#409EFF'
      case 'warning': return '#E6A23C'
      case 'opportunity': return '#67C23A'
      case 'risk': return '#F56C6C'
      default: return '#909399'
    }
  }

  function formatTime(dateStr: string): string {
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
    insights,
    ticketInsights,
    loading,
    currentAnalysis,
    slaPrediction,
    // Getters
    warnings,
    risks,
    recommendations,
    opportunities,
    urgentInsights,
    // Actions
    fetchInsights,
    fetchTicketInsights,
    analyzeTicket,
    predictSLA,
    clearAnalysis,
    getInsightIcon,
    getInsightColor,
    formatTime
  }
})
