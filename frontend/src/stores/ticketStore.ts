import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { 
  Ticket, 
  TicketFilters, 
  KanbanColumn, 
  TicketStatus,
  TicketComment 
} from '@/types/ticket'
import { ticketApi } from '@/api/ticketService'

export const useTicketStore = defineStore('ticket', () => {
  // State
  const tickets = ref<Ticket[]>([])
  const currentTicket = ref<Ticket | null>(null)
  const kanbanColumns = ref<KanbanColumn[]>([])
  const loading = ref(false)
  const detailLoading = ref(false)
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(20)
  const filters = ref<TicketFilters>({})

  // Getters
  const openTickets = computed(() => 
    tickets.value.filter(t => t.status === 'open')
  )

  const criticalTickets = computed(() =>
    tickets.value.filter(t => t.priority === 'critical')
  )

  const ticketsByStatus = computed(() => {
    const grouped: Record<TicketStatus, Ticket[]> = {
      open: [],
      in_progress: [],
      pending: [],
      resolved: [],
      closed: []
    }
    tickets.value.forEach(ticket => {
      if (grouped[ticket.status]) {
        grouped[ticket.status].push(ticket)
      }
    })
    return grouped
  })

  // Actions
  async function fetchTickets(newFilters?: TicketFilters) {
    loading.value = true
    try {
      if (newFilters) {
        filters.value = { ...filters.value, ...newFilters }
      }
      const response = await ticketApi.getTickets(page.value, pageSize.value, filters.value)
      tickets.value = response.data
      total.value = response.total
      return response
    } finally {
      loading.value = false
    }
  }

  async function fetchTicket(id: string) {
    detailLoading.value = true
    try {
      currentTicket.value = await ticketApi.getTicket(id)
      return currentTicket.value
    } finally {
      detailLoading.value = false
    }
  }

  async function createTicket(data: Partial<Ticket>) {
    const ticket = await ticketApi.createTicket(data)
    tickets.value.unshift(ticket)
    return ticket
  }

  async function updateTicket(id: string, data: Partial<Ticket>) {
    const updated = await ticketApi.updateTicket(id, data)
    const index = tickets.value.findIndex(t => t.id === id)
    if (index !== -1) {
      tickets.value[index] = updated
    }
    if (currentTicket.value?.id === id) {
      currentTicket.value = updated
    }
    return updated
  }

  async function deleteTicket(id: string) {
    await ticketApi.deleteTicket(id)
    tickets.value = tickets.value.filter(t => t.id !== id)
    if (currentTicket.value?.id === id) {
      currentTicket.value = null
    }
  }

  async function addComment(ticketId: string, content: string, isInternal = false) {
    const comment = await ticketApi.addComment(ticketId, content, isInternal)
    if (currentTicket.value?.id === ticketId) {
      currentTicket.value.comments.push(comment)
    }
    return comment
  }

  async function assignTicket(ticketId: string, userId: string) {
    const updated = await ticketApi.assignTicket(ticketId, userId)
    const index = tickets.value.findIndex(t => t.id === ticketId)
    if (index !== -1) {
      tickets.value[index] = updated
    }
    if (currentTicket.value?.id === ticketId) {
      currentTicket.value = updated
    }
    return updated
  }

  async function changeStatus(ticketId: string, status: TicketStatus) {
    const updated = await ticketApi.changeStatus(ticketId, status)
    const index = tickets.value.findIndex(t => t.id === ticketId)
    if (index !== -1) {
      tickets.value[index] = updated
    }
    if (currentTicket.value?.id === ticketId) {
      currentTicket.value = updated
    }
    // Update kanban column if on kanban view
    updateKanbanTicket(updated)
    return updated
  }

  async function fetchKanbanBoard() {
    loading.value = true
    try {
      const response = await ticketApi.getKanbanBoard()
      const statusColors: Record<string, string> = {
        open: '#409EFF',
        in_progress: '#E6A23C',
        pending: '#909399',
        resolved: '#67C23A',
        closed: '#8C8C8C'
      }
      kanbanColumns.value = response.columns.map((col, index) => ({
        id: `col-${col.status}`,
        title: getStatusTitle(col.status as TicketStatus),
        status: col.status as TicketStatus,
        tickets: col.tickets,
        color: statusColors[col.status] || '#409EFF',
        order: index
      }))
      return kanbanColumns.value
    } finally {
      loading.value = false
    }
  }

  function updateKanbanTicket(ticket: Ticket) {
    const column = kanbanColumns.value.find(c => c.status === ticket.status)
    if (column) {
      const existingIndex = column.tickets.findIndex(t => t.id === ticket.id)
      if (existingIndex !== -1) {
        column.tickets[existingIndex] = ticket
      } else {
        column.tickets.unshift(ticket)
      }
    }
  }

  function setPage(newPage: number) {
    page.value = newPage
    fetchTickets()
  }

  function setFilters(newFilters: TicketFilters) {
    filters.value = newFilters
    page.value = 1
    fetchTickets()
  }

  function clearFilters() {
    filters.value = {}
    page.value = 1
    fetchTickets()
  }

  return {
    // State
    tickets,
    currentTicket,
    kanbanColumns,
    loading,
    detailLoading,
    total,
    page,
    pageSize,
    filters,
    // Getters
    openTickets,
    criticalTickets,
    ticketsByStatus,
    // Actions
    fetchTickets,
    fetchTicket,
    createTicket,
    updateTicket,
    deleteTicket,
    addComment,
    assignTicket,
    changeStatus,
    fetchKanbanBoard,
    setPage,
    setFilters,
    clearFilters
  }
})

function getStatusTitle(status: TicketStatus): string {
  const titles: Record<TicketStatus, string> = {
    open: 'Open',
    in_progress: 'In Progress',
    pending: 'Pending',
    resolved: 'Resolved',
    closed: 'Closed'
  }
  return titles[status] || status
}
