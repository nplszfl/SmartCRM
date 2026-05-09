import apiClient from './client'
import type { 
  AnalyticsData, 
  RevenueAnalytics, 
  PipelineAnalytics, 
  TeamAnalytics, 
  AIMetricsAnalytics,
  AIInsight,
  ApiResponse 
} from '@/types'

export interface DashboardFilters {
  dateRange?: [string, string]
  teamId?: string
  repId?: string
}

export interface AIFeedbackDto {
  insightId: string
  helpful: boolean
  comment?: string
}

// Analytics API
export const analyticsApi = {
  // Get main dashboard data
  getDashboard(filters?: DashboardFilters): Promise<ApiResponse<AnalyticsData>> {
    return apiClient.get('/analytics/dashboard', { params: filters })
  },

  // Get revenue analytics
  getRevenueAnalytics(filters?: DashboardFilters): Promise<ApiResponse<RevenueAnalytics>> {
    return apiClient.get('/analytics/revenue', { params: filters })
  },

  // Get pipeline analytics
  getPipelineAnalytics(filters?: DashboardFilters): Promise<ApiResponse<PipelineAnalytics>> {
    return apiClient.get('/analytics/pipeline', { params: filters })
  },

  // Get team analytics
  getTeamAnalytics(filters?: DashboardFilters): Promise<ApiResponse<TeamAnalytics>> {
    return apiClient.get('/analytics/team', { params: filters })
  },

  // Get AI metrics
  getAIMetrics(filters?: DashboardFilters): Promise<ApiResponse<AIMetricsAnalytics>> {
    return apiClient.get('/analytics/ai', { params: filters })
  },

  // Get trend data
  getTrends(metric: string, period: 'day' | 'week' | 'month' | 'quarter'): Promise<ApiResponse<{
    data: { date: string; value: number }[]
    change: number
    trend: 'up' | 'down' | 'stable'
  }>> {
    return apiClient.get('/analytics/trends', { params: { metric, period } })
  },

  // Export analytics data
  exportData(format: 'csv' | 'excel' | 'pdf', filters?: DashboardFilters): Promise<Blob> {
    return apiClient.get('/analytics/export', { 
      params: { format, ...filters },
      responseType: 'blob'
    })
  }
}

// AI Insights API
export const aiInsightsApi = {
  // Get all AI insights
  getInsights(filters?: {
    type?: AIInsight['type']
    source?: AIInsight['source']
    dateRange?: [string, string]
  }): Promise<ApiResponse<AIInsight[]>> {
    return apiClient.get('/ai/insights', { params: filters })
  },

  // Get insight by ID
  getInsight(id: string): Promise<ApiResponse<AIInsight>> {
    return apiClient.get(`/ai/insights/${id}`)
  },

  // Mark insight as read
  markAsRead(id: string): Promise<ApiResponse<void>> {
    return apiClient.post(`/ai/insights/${id}/read`)
  },

  // Dismiss insight
  dismissInsight(id: string): Promise<ApiResponse<void>> {
    return apiClient.post(`/ai/insights/${id}/dismiss`)
  },

  // Provide feedback on insight
  provideFeedback(data: AIFeedbackDto): Promise<ApiResponse<void>> {
    return apiClient.post('/ai/insights/feedback', data)
  },

  // Get AI recommendations
  getRecommendations(context: string): Promise<ApiResponse<{
    recommendations: { title: string; description: string; action: string; priority: number }[]
  }>> {
    return apiClient.post('/ai/recommendations', { context })
  },

  // Get coaching insights for rep
  getCoachingInsights(repId: string): Promise<ApiResponse<{
    strengths: string[]
    improvements: string[]
    personalizedTips: string[]
    recentPerformance: { metric: string; value: number; change: number }[]
  }>> {
    return apiClient.get(`/ai/coaching/${repId}`)
  }
}

// Sales Rep Performance API
export const performanceApi = {
  // Get rep performance metrics
  getRepPerformance(repId: string, period?: string): Promise<ApiResponse<{
    activities: number
    dealsClosed: number
    revenue: number
    quota: number
    attainment: number
    winRate: number
    averageDealSize: number
    responseTime: number
    customerSatisfaction: number
  }>> {
    return apiClient.get(`/performance/rep/${repId}`, { params: { period } })
  },

  // Get rep comparison
  compareReps(repIds: string[]): Promise<ApiResponse<{
    comparisons: { repId: string; metrics: Record<string, number> }[]
    rankings: { repId: string; rank: number; score: number }[]
  }>> {
    return apiClient.post('/performance/compare', { repIds })
  },

  // Get leaderboard
  getLeaderboard(metric: string = 'revenue', period?: string): Promise<ApiResponse<{
    leaders: { repId: string; repName: string; value: number; rank: number; trend: 'up' | 'down' | 'stable' }[]
  }>> {
    return apiClient.get('/performance/leaderboard', { params: { metric, period } })
  },

  // Get activity summary
  getActivitySummary(repId: string, days: number = 7): Promise<ApiResponse<{
    calls: number
    emails: number
    meetings: number
    tasks: number
    followUps: number
  }>> {
    return apiClient.get(`/performance/rep/${repId}/activities`, { params: { days } })
  }
}

export default { analyticsApi, aiInsightsApi, performanceApi }