import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Deal, DealStage, DealPrediction, SalesRep } from '@/types'
import { dealApi, type DealFilters } from '@/api/dealService'
import dayjs from 'dayjs'

export const useDealStore = defineStore('deal', () => {
  // State
  const deals = ref<Deal[]>([])
  const currentDeal = ref<Deal | null>(null)
  const predictions = ref<Map<string, DealPrediction>>(new Map())
  const loading = ref(false)
  const pipelineLoading = ref(false)
  const pagination = ref({
    page: 1,
    pageSize: 50,
    total: 0
  })
  const filters = ref<DealFilters>({})
  const sortBy = ref<string>('updatedAt')
  const sortOrder = ref<'asc' | 'desc'>('desc')

  // Pipeline overview
  const pipelineOverview = ref<{
    stages: { stage: DealStage; count: number; value: number; conversionRate: number }[]
    totalValue: number
    weightedValue: number
  } | null>(null)

  // Getters
  const dealsByStage = computed(() => {
    const grouped: Record<DealStage, Deal[]> = {
      prospecting: [],
      qualification: [],
      proposal: [],
      negotiation: [],
      closed_won: [],
      closed_lost: []
    }
    deals.value.forEach(deal => {
      grouped[deal.stage].push(deal)
    })
    return grouped
  })

  const stageOrder: DealStage[] = ['prospecting', 'qualification', 'proposal', 'negotiation', 'closed_won']

  const sortedDealsByStage = computed(() => {
    const result: Record<DealStage, Deal[]> = {} as Record<DealStage, Deal[]>
    stageOrder.forEach(stage => {
      result[stage] = [...(dealsByStage.value[stage] || [])].sort((a, b) => 
        b.value * b.probability - a.value * a.probability
      )
    })
    return result
  })

  const totalPipelineValue = computed(() => {
    return deals.value
      .filter(d => !['closed_won', 'closed_lost'].includes(d.stage))
      .reduce((sum, d) => sum + d.value, 0)
  })

  const weightedPipelineValue = computed(() => {
    return deals.value
      .filter(d => !['closed_won', 'closed_lost'].includes(d.stage))
      .reduce((sum, d) => sum + (d.value * d.probability / 100), 0)
  })

  const predictedRevenue = computed(() => {
    return deals.value
      .filter(d => d.aiPrediction)
      .reduce((sum, d) => sum + (d.aiPrediction?.predictedAmount || 0), 0)
  })

  const dealsAtRisk = computed(() => {
    return deals.value.filter(d => 
      d.aiPrediction?.riskIndicators && 
      d.aiPrediction.riskIndicators.length > 0
    )
  })

  // Actions
  async function fetchDeals() {
    loading.value = true
    try {
      const response = await dealApi.getDeals({
        page: pagination.value.page,
        pageSize: pagination.value.pageSize,
        filters: filters.value,
        sortBy: sortBy.value,
        sortOrder: sortOrder.value
      })
      deals.value = response.data
      pagination.value.total = response.total
    } finally {
      loading.value = false
    }
  }

  async function fetchDeal(id: string) {
    loading.value = true
    try {
      const response = await dealApi.getDeal(id)
      currentDeal.value = response.data
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function fetchDealWithPrediction(id: string) {
    const deal = await fetchDeal(id)
    if (deal) {
      await fetchPrediction(id)
    }
    return deal
  }

  async function createDeal(data: Parameters<typeof dealApi.createDeal>[0]) {
    const response = await dealApi.createDeal(data)
    deals.value.unshift(response.data)
    pagination.value.total++
    return response.data
  }

  async function updateDeal(id: string, data: Parameters<typeof dealApi.updateDeal>[1]) {
    const response = await dealApi.updateDeal(id, data)
    const index = deals.value.findIndex(d => d.id === id)
    if (index !== -1) {
      deals.value[index] = response.data
    }
    if (currentDeal.value?.id === id) {
      currentDeal.value = response.data
    }
    return response.data
  }

  async function moveToStage(id: string, stage: DealStage) {
    const response = await dealApi.moveToStage(id, stage)
    const index = deals.value.findIndex(d => d.id === id)
    if (index !== -1) {
      deals.value[index] = response.data
    }
    return response.data
  }

  async function closeDeal(id: string, won: boolean) {
    const response = await dealApi.closeDeal(id, won)
    const index = deals.value.findIndex(d => d.id === id)
    if (index !== -1) {
      deals.value[index] = response.data
    }
    return response.data
  }

  async function fetchPrediction(dealId: string) {
    try {
      const response = await dealApi.getDealPrediction(dealId)
      predictions.value.set(dealId, response.data)
      return response.data
    } catch {
      return null
    }
  }

  async function fetchPipelineOverview() {
    pipelineLoading.value = true
    try {
      const response = await dealApi.getPipelineOverview()
      pipelineOverview.value = response.data
    } finally {
      pipelineLoading.value = false
    }
  }

  function setFilters(newFilters: DealFilters) {
    filters.value = { ...newFilters }
    pagination.value.page = 1
    fetchDeals()
  }

  function setSorting(field: string, order: 'asc' | 'desc') {
    sortBy.value = field
    sortOrder.value = order
    fetchDeals()
  }

  function setPage(page: number) {
    pagination.value.page = page
    fetchDeals()
  }

  function getConfidenceColor(confidence: number): string {
    if (confidence >= 80) return 'success'
    if (confidence >= 60) return 'warning'
    return 'danger'
  }

  function getConfidenceLabel(confidence: number): string {
    if (confidence >= 80) return 'High'
    if (confidence >= 60) return 'Medium'
    return 'Low'
  }

  function getStageColor(stage: DealStage): string {
    const colors: Record<DealStage, string> = {
      prospecting: 'info',
      qualification: 'primary',
      proposal: 'warning',
      negotiation: 'danger',
      closed_won: 'success',
      closed_lost: 'info'
    }
    return colors[stage] || 'info'
  }

  function getStageLabel(stage: DealStage): string {
    const labels: Record<DealStage, string> = {
      prospecting: 'Prospecting',
      qualification: 'Qualification',
      proposal: 'Proposal',
      negotiation: 'Negotiation',
      closed_won: 'Closed Won',
      closed_lost: 'Closed Lost'
    }
    return labels[stage] || stage
  }

  return {
    // State
    deals,
    currentDeal,
    predictions,
    loading,
    pipelineLoading,
    pagination,
    filters,
    sortBy,
    sortOrder,
    pipelineOverview,
    // Getters
    dealsByStage,
    sortedDealsByStage,
    totalPipelineValue,
    weightedPipelineValue,
    predictedRevenue,
    dealsAtRisk,
    stageOrder,
    // Actions
    fetchDeals,
    fetchDeal,
    fetchDealWithPrediction,
    createDeal,
    updateDeal,
    moveToStage,
    closeDeal,
    fetchPrediction,
    fetchPipelineOverview,
    setFilters,
    setSorting,
    setPage,
    getConfidenceColor,
    getConfidenceLabel,
    getStageColor,
    getStageLabel
  }
})