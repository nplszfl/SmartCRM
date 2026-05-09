import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Lead, AIInsight, AIScore, SalesRep } from '@/types'
import { leadApi, type LeadFilters } from '@/api/leadService'
import dayjs from 'dayjs'

export const useLeadStore = defineStore('lead', () => {
  // State
  const leads = ref<Lead[]>([])
  const currentLead = ref<Lead | null>(null)
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    pageSize: 20,
    total: 0
  })
  const filters = ref<LeadFilters>({})
  const sortBy = ref<string>('createdAt')
  const sortOrder = ref<'asc' | 'desc'>('desc')
  const selectedLeadIds = ref<string[]>([])

  // Getters
  const sortedLeads = computed(() => {
    return [...leads.value].sort((a, b) => {
      const aVal = getLeadSortValue(a, sortBy.value)
      const bVal = getLeadSortValue(b, sortBy.value)
      const modifier = sortOrder.value === 'asc' ? 1 : -1
      return aVal > bVal ? modifier : -modifier
    })
  })

  const highPriorityLeads = computed(() => {
    return leads.value.filter(lead => lead.score.score >= 80)
  })

  const leadsNeedingFollowUp = computed(() => {
    const threeDaysAgo = dayjs().subtract(3, 'day').toISOString()
    return leads.value.filter(lead => 
      lead.status !== 'converted' && 
      lead.lastActivityAt && 
      lead.lastActivityAt < threeDaysAgo
    )
  })

  const averageScore = computed(() => {
    if (leads.value.length === 0) return 0
    const total = leads.value.reduce((sum, lead) => sum + lead.score.score, 0)
    return Math.round(total / leads.value.length)
  })

  // Actions
  async function fetchLeads() {
    loading.value = true
    try {
      const response = await leadApi.getLeads({
        page: pagination.value.page,
        pageSize: pagination.value.pageSize,
        filters: filters.value,
        sortBy: sortBy.value,
        sortOrder: sortOrder.value
      })
      leads.value = response.data
      pagination.value.total = response.total
    } finally {
      loading.value = false
    }
  }

  async function fetchLead(id: string) {
    loading.value = true
    try {
      const response = await leadApi.getLead(id)
      currentLead.value = response.data
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function createLead(data: Parameters<typeof leadApi.createLead>[0]) {
    const response = await leadApi.createLead(data)
    leads.value.unshift(response.data)
    pagination.value.total++
    return response.data
  }

  async function updateLead(id: string, data: Parameters<typeof leadApi.updateLead>[1]) {
    const response = await leadApi.updateLead(id, data)
    const index = leads.value.findIndex(l => l.id === id)
    if (index !== -1) {
      leads.value[index] = response.data
    }
    if (currentLead.value?.id === id) {
      currentLead.value = response.data
    }
    return response.data
  }

  async function deleteLead(id: string) {
    await leadApi.deleteLead(id)
    leads.value = leads.value.filter(l => l.id !== id)
    pagination.value.total--
    if (currentLead.value?.id === id) {
      currentLead.value = null
    }
  }

  async function assignLead(id: string, assigneeId: string) {
    const response = await leadApi.assignLead(id, assigneeId)
    const index = leads.value.findIndex(l => l.id === id)
    if (index !== -1) {
      leads.value[index] = response.data
    }
    return response.data
  }

  function setFilters(newFilters: LeadFilters) {
    filters.value = { ...newFilters }
    pagination.value.page = 1
    fetchLeads()
  }

  function setSorting(field: string, order: 'asc' | 'desc') {
    sortBy.value = field
    sortOrder.value = order
    fetchLeads()
  }

  function setPage(page: number) {
    pagination.value.page = page
    fetchLeads()
  }

  function setPageSize(size: number) {
    pagination.value.pageSize = size
    pagination.value.page = 1
    fetchLeads()
  }

  function toggleLeadSelection(id: string) {
    const index = selectedLeadIds.value.indexOf(id)
    if (index === -1) {
      selectedLeadIds.value.push(id)
    } else {
      selectedLeadIds.value.splice(index, 1)
    }
  }

  function selectAllLeads() {
    selectedLeadIds.value = leads.value.map(l => l.id)
  }

  function clearSelection() {
    selectedLeadIds.value = []
  }

  function getScoreColor(score: number): string {
    if (score >= 80) return 'success'
    if (score >= 60) return 'warning'
    return 'danger'
  }

  function getScoreLabel(score: number): string {
    if (score >= 80) return 'Hot'
    if (score >= 60) return 'Warm'
    if (score >= 40) return 'Cool'
    return 'Cold'
  }

  return {
    // State
    leads,
    currentLead,
    loading,
    pagination,
    filters,
    sortBy,
    sortOrder,
    selectedLeadIds,
    // Getters
    sortedLeads,
    highPriorityLeads,
    leadsNeedingFollowUp,
    averageScore,
    // Actions
    fetchLeads,
    fetchLead,
    createLead,
    updateLead,
    deleteLead,
    assignLead,
    setFilters,
    setSorting,
    setPage,
    setPageSize,
    toggleLeadSelection,
    selectAllLeads,
    clearSelection,
    getScoreColor,
    getScoreLabel
  }
})

// Helper function
function getLeadSortValue(lead: Lead, field: string): unknown {
  switch (field) {
    case 'score':
      return lead.score.score
    case 'name':
      return `${lead.firstName} ${lead.lastName}`.toLowerCase()
    case 'company':
      return lead.company.toLowerCase()
    case 'createdAt':
      return lead.createdAt
    case 'lastActivityAt':
      return lead.lastActivityAt || ''
    default:
      return ''
  }
}