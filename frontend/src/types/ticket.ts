// Ticket Types for SmartITSM
export type TicketStatus = 'open' | 'in_progress' | 'pending' | 'resolved' | 'closed'
export type TicketPriority = 'low' | 'medium' | 'high' | 'critical'
export type TicketCategory = 'incident' | 'request' | 'problem' | 'change' | 'question'

export interface Ticket {
  id: string
  title: string
  description: string
  status: TicketStatus
  priority: TicketPriority
  category: TicketCategory
  createdBy: User
  assignedTo?: User
  affectedDevice?: string
  relatedCI?: string
  tags: string[]
  createdAt: string
  updatedAt: string
  resolvedAt?: string
  closedAt?: string
  dueDate?: string
  slaResponse?: string
  slaResolution?: string
  comments: TicketComment[]
  attachments: Attachment[]
  history: TicketHistory[]
  aiScore?: number
  aiConfidence?: number
  aiCategory?: string
}

export interface TicketComment {
  id: string
  ticketId: string
  author: User
  content: string
  isInternal: boolean
  createdAt: string
  updatedAt?: string
}

export interface Attachment {
  id: string
  ticketId: string
  fileName: string
  fileUrl: string
  fileSize: number
  mimeType: string
  uploadedBy: User
  uploadedAt: string
}

export interface TicketHistory {
  id: string
  ticketId: string
  field: string
  oldValue: string
  newValue: string
  changedBy: User
  changedAt: string
}

export interface User {
  id: string
  name: string
  email: string
  avatar?: string
  role: UserRole
  department?: string
}

export type UserRole = 'admin' | 'agent' | 'user' | 'technician' | 'manager'

export interface KanbanColumn {
  id: string
  title: string
  status: TicketStatus
  tickets: Ticket[]
  color: string
  order: number
}

export interface AdminStats {
  totalTickets: number
  openTickets: number
  resolvedTickets: number
  avgResolutionTime: number
  ticketsByPriority: Record<TicketPriority, number>
  ticketsByCategory: Record<TicketCategory, number>
  ticketsByStatus: Record<TicketStatus, number>
  topTechnicians: TechnicianPerformance[]
  recentActivity: Activity[]
}

export interface TechnicianPerformance {
  user: User
  ticketsResolved: number
  avgResponseTime: number
  satisfactionScore: number
}

export interface Activity {
  id: string
  type: 'created' | 'updated' | 'commented' | 'assigned' | 'resolved' | 'closed'
  ticketId: string
  ticketTitle: string
  user: User
  description: string
  timestamp: string
}

export interface AIInsight {
  id: string
  type: 'recommendation' | 'warning' | 'opportunity' | 'risk'
  title: string
  description: string
  confidence: number
  action?: string
  source: 'sla' | 'category' | 'sentiment' | 'pattern'
  relatedTicketId?: string
  createdAt: string
}

export interface PaginatedResponse<T> {
  data: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export interface TicketFilters {
  status?: TicketStatus
  priority?: TicketPriority
  category?: TicketCategory
  assignedTo?: string
  createdBy?: string
  search?: string
  dateFrom?: string
  dateTo?: string
}
