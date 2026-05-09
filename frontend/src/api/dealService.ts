import apiClient from './client'
import type { Deal, DealStage, DealPrediction, ApiResponse, PaginatedResponse } from '@/types'

export interface DealFilters {
  stage?: DealStage
  assigneeId?: string
  minValue?: number
  maxValue?: number
  search?: string
  probabilityRange?: [number, number]
  dateRange?: [string, string]
}

export interface CreateDealDto {
  title: string
  value: number
  stage: DealStage
  leadId: string
  assigneeId: string
  predictedClose?: string
  notes?: string
}

export interface UpdateDealDto extends Partial<CreateDealDto> {
  probability?: number
  stage?: DealStage
}

// Deal API
export const dealApi = {
  // Get all deals with pagination and filters
  getDeals(params: {
    page?: number
    pageSize?: number
    filters?: DealFilters
    sortBy?: string
    sortOrder?: 'asc' | 'desc'
  }): Promise<PaginatedResponse<Deal>> {
    return apiClient.get('/deals', { params })
  },

  // Get single deal by ID
  getDeal(id: string): Promise<ApiResponse<Deal>> {
    return apiClient.get(`/deals/${id}`)
  },

  // Create new deal
  createDeal(data: CreateDealDto): Promise<ApiResponse<Deal>> {
    return apiClient.post('/deals', data)
  },

  // Update deal
  updateDeal(id: string, data: UpdateDealDto): Promise<ApiResponse<Deal>> {
    return apiClient.put(`/deals/${id}`, data)
  },

  // Delete deal
  deleteDeal(id: string): Promise<ApiResponse<void>> {
    return apiClient.delete(`/deals/${id}`)
  },

  // Move deal to stage
  moveToStage(id: string, stage: DealStage): Promise<ApiResponse<Deal>> {
    return apiClient.post(`/deals/${id}/stage`, { stage })
  },

  // Get AI prediction for deal
  getDealPrediction(id: string): Promise<ApiResponse<DealPrediction>> {
    return apiClient.get(`/deals/${id}/prediction`)
  },

  // Get pipeline overview
  getPipelineOverview(): Promise<ApiResponse<{
    stages: { stage: DealStage; count: number; value: number; conversionRate: number }[]
    totalValue: number
    weightedValue: number
  }>> {
    return apiClient.get('/deals/pipeline/overview')
  },

  // Close deal
  closeDeal(id: string, won: boolean, closeDate?: string): Promise<ApiResponse<Deal>> {
    return apiClient.post(`/deals/${id}/close`, { won, closeDate })
  },

  // Add activity to deal
  addActivity(id: string, activity: { type: string; description: string }): Promise<ApiResponse<Deal>> {
    return apiClient.post(`/deals/${id}/activities`, activity)
  },

  // Get deal history
  getDealHistory(id: string): Promise<ApiResponse<{ timestamp: string; action: string; user: string }[]>> {
    return apiClient.get(`/deals/${id}/history`)
  }
}

// AI Deal Prediction API
export const dealPredictionApi = {
  // Get prediction accuracy metrics
  getAccuracyMetrics(): Promise<ApiResponse<{
    overall: number
    byStage: { stage: DealStage; accuracy: number }[]
    trend: { date: string; accuracy: number }[]
  }>> {
    return apiClient.get('/ai/deals/prediction/accuracy')
  },

  // Get risk alerts
  getRiskAlerts(): Promise<ApiResponse<{ dealId: string; riskLevel: string; factors: string[] }[]>> {
    return apiClient.get('/ai/deals/prediction/risks')
  },

  // Get closing forecast
  getClosingForecast(months: number = 3): Promise<ApiResponse<{
    predicted: number
    confidence: number
    deals: { id: string; value: number; probability: number; closeDate: string }[]
  }>> {
    return apiClient.get('/ai/deals/prediction/forecast', { params: { months } })
  },

  // Get next best actions
  getNextActions(dealId: string): Promise<ApiResponse<{ action: string; reason: string; impact: number }[]>> {
    return apiClient.get(`/ai/deals/${dealId}/next-actions`)
  }
}

export default { dealApi, dealPredictionApi }