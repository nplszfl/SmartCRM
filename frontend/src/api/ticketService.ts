import axios from 'axios'
import type { 
  Ticket, 
  TicketFilters, 
  PaginatedResponse, 
  AdminStats, 
  AIInsight,
  TicketComment 
} from '@/types/ticket'

const baseURL = '/api'

const client = axios.create({
  baseURL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

client.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message || error.message
    return Promise.reject(new Error(message))
  }
)

export const ticketApi = {
  // Ticket List
  async getTickets(
    page = 1, 
    pageSize = 20, 
    filters?: TicketFilters
  ): Promise<PaginatedResponse<Ticket>> {
    const params = { page, pageSize, ...filters }
    return client.get('/tickets', { params })
  },

  // Ticket Detail
  async getTicket(id: string): Promise<Ticket> {
    return client.get(`/tickets/${id}`)
  },

  // Create Ticket
  async createTicket(data: Partial<Ticket>): Promise<Ticket> {
    return client.post('/tickets', data)
  },

  // Update Ticket
  async updateTicket(id: string, data: Partial<Ticket>): Promise<Ticket> {
    return client.put(`/tickets/${id}`, data)
  },

  // Delete Ticket
  async deleteTicket(id: string): Promise<void> {
    return client.delete(`/tickets/${id}`)
  },

  // Add Comment
  async addComment(ticketId: string, content: string, isInternal = false): Promise<TicketComment> {
    return client.post(`/tickets/${ticketId}/comments`, { content, isInternal })
  },

  // Assign Ticket
  async assignTicket(ticketId: string, userId: string): Promise<Ticket> {
    return client.post(`/tickets/${ticketId}/assign`, { userId })
  },

  // Change Status
  async changeStatus(ticketId: string, status: string): Promise<Ticket> {
    return client.post(`/tickets/${ticketId}/status`, { status })
  },

  // Kanban Board
  async getKanbanBoard(): Promise<{ columns: Array<{ status: string; tickets: Ticket[] }> }> {
    return client.get('/tickets/kanban')
  }
}

export const adminApi = {
  // Admin Dashboard Stats
  async getStats(): Promise<AdminStats> {
    return client.get('/admin/stats')
  },

  // Get Users
  async getUsers(): Promise<{ data: Array<{ id: string; name: string; email: string; role: string }> }> {
    return client.get('/admin/users')
  },

  // Update User Role
  async updateUserRole(userId: string, role: string): Promise<void> {
    return client.put(`/admin/users/${userId}/role`, { role })
  },

  // Get System Settings
  async getSettings(): Promise<Record<string, unknown>> {
    return client.get('/admin/settings')
  },

  // Update System Settings
  async updateSettings(settings: Record<string, unknown>): Promise<void> {
    return client.put('/admin/settings', settings)
  }
}

export const aiInsightsApi = {
  // Get AI Insights
  async getInsights(ticketId?: string): Promise<AIInsight[]> {
    const params = ticketId ? { ticketId } : {}
    return client.get('/ai/insights', { params })
  },

  // Get Ticket AI Analysis
  async analyzeTicket(ticketId: string): Promise<{
    score: number
    confidence: number
    category: string
    suggestedActions: string[]
  }> {
    return client.get(`/ai/tickets/${ticketId}/analyze`)
  },

  // Get SLA Prediction
  async predictSLA(ticketId: string): Promise<{
    willMeetResponse: boolean
    willMeetResolution: boolean
    estimatedResponseTime: string
    estimatedResolutionTime: string
  }> {
    return client.get(`/ai/tickets/${ticketId}/sla-prediction`)
  }
}

export default { ticketApi, adminApi, aiInsightsApi }
