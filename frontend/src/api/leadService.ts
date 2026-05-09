import apiClient from './client'
import type { Lead, AIScore, AIInsight, ApiResponse, PaginatedResponse } from '@/types'

export interface LeadFilters {
  status?: Lead['status']
  source?: Lead['source']
  assigneeId?: string
  minScore?: number
  maxScore?: number
  search?: string
  tags?: string[]
  dateRange?: [string, string]
}

export interface CreateLeadDto {
  firstName: string
  lastName: string
  email: string
  phone?: string
  company: string
  title?: string
  source: Lead['source']
  tags?: string[]
  customFields?: Record<string, unknown>
}

export interface UpdateLeadDto extends Partial<CreateLeadDto> {
  status?: Lead['status']
  assigneeId?: string
}

// Lead API
export const leadApi = {
  // Get all leads with pagination and filters
  getLeads(params: {
    page?: number
    pageSize?: number
    filters?: LeadFilters
    sortBy?: string
    sortOrder?: 'asc' | 'desc'
  }): Promise<PaginatedResponse<Lead>> {
    return apiClient.get('/leads', { params })
  },

  // Get single lead by ID
  getLead(id: string): Promise<ApiResponse<Lead>> {
    return apiClient.get(`/leads/${id}`)
  },

  // Create new lead
  createLead(data: CreateLeadDto): Promise<ApiResponse<Lead>> {
    return apiClient.post('/leads', data)
  },

  // Update lead
  updateLead(id: string, data: UpdateLeadDto): Promise<ApiResponse<Lead>> {
    return apiClient.put(`/leads/${id}`, data)
  },

  // Delete lead
  deleteLead(id: string): Promise<ApiResponse<void>> {
    return apiClient.delete(`/leads/${id}`)
  },

  // Get AI score for lead
  getLeadScore(id: string): Promise<ApiResponse<AIScore>> {
    return apiClient.get(`/leads/${id}/score`)
  },

  // Get AI insights for lead
  getLeadInsights(id: string): Promise<ApiResponse<AIInsight[]>> {
    return apiClient.get(`/leads/${id}/insights`)
  },

  // Bulk update leads
  bulkUpdate(ids: string[], data: UpdateLeadDto): Promise<ApiResponse<{ updated: number }>> {
    return apiClient.put('/leads/bulk', { ids, data })
  },

  // Assign lead to rep
  assignLead(id: string, assigneeId: string): Promise<ApiResponse<Lead>> {
    return apiClient.post(`/leads/${id}/assign`, { assigneeId })
  },

  // Add tag to lead
  addTag(id: string, tag: string): Promise<ApiResponse<Lead>> {
    return apiClient.post(`/leads/${id}/tags`, { tag })
  },

  // Remove tag from lead
  removeTag(id: string, tag: string): Promise<ApiResponse<Lead>> {
    return apiClient.delete(`/leads/${id}/tags/${tag}`)
  }
}

// AI Scoring API
export const aiScoreApi = {
  // Get scoring model info
  getModelInfo(): Promise<ApiResponse<{ name: string; version: string; accuracy: number }>> {
    return apiClient.get('/ai/scoring/model')
  },

  // Recalculate scores for all leads
  recalculateScores(): Promise<ApiResponse<{ triggered: boolean }>> {
    return apiClient.post('/ai/scoring/recalculate')
  },

  // Get score factors explanation
  explainScore(leadId: string): Promise<ApiResponse<{ factors: AIScoreFactor[] }>> {
    return apiClient.get(`/ai/scoring/${leadId}/explain`)
  }
}

export default { leadApi, aiScoreApi }