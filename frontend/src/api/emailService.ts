import apiClient from './client'
import type { Email, EmailTemplate, ApiResponse, PaginatedResponse } from '@/types'

export interface EmailFilters {
  status?: Email['status']
  leadId?: string
  dateRange?: [string, string]
}

export interface SendEmailDto {
  to: string[]
  cc?: string[]
  bcc?: string[]
  subject: string
  body: string
  leadId?: string
  templateId?: string
}

export interface AIGenerateEmailDto {
  leadId: string
  templateId?: string
  tone?: 'formal' | 'casual' | 'friendly'
  focusPoints?: string[]
  additionalContext?: string
}

export interface AIGenerateReplyDto {
  leadId: string
  originalEmail: string
  tone?: 'formal' | 'casual' | 'friendly'
  length?: 'short' | 'medium' | 'long'
}

// Email API
export const emailApi = {
  // Get all emails with pagination and filters
  getEmails(params: {
    page?: number
    pageSize?: number
    filters?: EmailFilters
  }): Promise<PaginatedResponse<Email>> {
    return apiClient.get('/emails', { params })
  },

  // Get single email by ID
  getEmail(id: string): Promise<ApiResponse<Email>> {
    return apiClient.get(`/emails/${id}`)
  },

  // Send email
  sendEmail(data: SendEmailDto): Promise<ApiResponse<Email>> {
    return apiClient.post('/emails/send', data)
  },

  // Save as draft
  saveDraft(data: Omit<SendEmailDto, 'to'> & { id?: string }): Promise<ApiResponse<Email>> {
    return apiClient.post('/emails/draft', data)
  },

  // Delete draft
  deleteDraft(id: string): Promise<ApiResponse<void>> {
    return apiClient.delete(`/emails/draft/${id}`)
  },

  // Get email templates
  getTemplates(): Promise<ApiResponse<EmailTemplate[]>> {
    return apiClient.get('/emails/templates')
  },

  // Create template
  createTemplate(data: Omit<EmailTemplate, 'id'>): Promise<ApiResponse<EmailTemplate>> {
    return apiClient.post('/emails/templates', data)
  },

  // Update template
  updateTemplate(id: string, data: Partial<EmailTemplate>): Promise<ApiResponse<EmailTemplate>> {
    return apiClient.put(`/emails/templates/${id}`, data)
  },

  // Delete template
  deleteTemplate(id: string): Promise<ApiResponse<void>> {
    return apiClient.delete(`/emails/templates/${id}`)
  },

  // Track email open
  trackOpen(id: string): Promise<void> {
    return apiClient.post(`/emails/${id}/track/open`)
  },

  // Get email analytics
  getEmailAnalytics(params?: { dateRange?: [string, string] }): Promise<ApiResponse<{
    sent: number
    delivered: number
    opened: number
    replied: number
    bounced: number
    openRate: number
    replyRate: number
  }>> {
    return apiClient.get('/emails/analytics', { params })
  }
}

// AI Email Generation API
export const aiEmailApi = {
  // Generate email from lead context
  generateEmail(data: AIGenerateEmailDto): Promise<ApiResponse<{
    subject: string
    body: string
    alternativeOptions: { subject: string; body: string }[]
    suggestions: string[]
  }>> {
    return apiClient.post('/ai/email/generate', data)
  },

  // Generate reply to incoming email
  generateReply(data: AIGenerateReplyDto): Promise<ApiResponse<{
    body: string
    alternativeOptions: string[]
  }>> {
    return apiClient.post('/ai/email/generate-reply', data)
  },

  // Improve existing email
  improveEmail(id: string, focus?: 'clarity' | 'persuasiveness' | 'tone'): Promise<ApiResponse<{
    original: string
    improved: string
    changes: string[]
  }>> {
    return apiClient.post(`/ai/email/${id}/improve`, { focus })
  },

  // Personalize email content
  personalizeEmail(id: string, leadId: string): Promise<ApiResponse<{
    subject: string
    body: string
    personalizationDetails: { field: string; original: string; personalized: string }[]
  }>> {
    return apiClient.post(`/ai/email/${id}/personalize`, { leadId })
  },

  // Get email subject suggestions
  suggestSubject(leadId: string, intent: string): Promise<ApiResponse<{
    suggestions: string[]
    reasons: string[]
  }>> {
    return apiClient.get('/ai/email/suggest-subject', { params: { leadId, intent } })
  },

  // Check email for issues
  checkEmail(id: string): Promise<ApiResponse<{
    issues: { type: 'grammar' | 'spelling' | 'tone' | 'length'; message: string; position?: number }[]
    score: number
    suggestions: string[]
  }>> {
    return apiClient.get(`/ai/email/${id}/check`)
  }
}

export default { emailApi, aiEmailApi }