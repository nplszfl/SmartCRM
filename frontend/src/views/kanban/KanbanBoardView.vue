<template>
  <div class="kanban-view">
    <div class="header">
      <h2 class="title">Kanban Board</h2>
      <div class="header-actions">
        <el-button @click="refreshBoard" :loading="loading">
          <el-icon><Refresh /></el-icon> Refresh
        </el-button>
      </div>
    </div>

    <div class="kanban-board" v-loading="loading">
      <div
        v-for="column in columns"
        :key="column.id"
        class="kanban-column"
        :style="{ borderTopColor: column.color }"
      >
        <div class="column-header">
          <div class="column-title">
            <span class="status-dot" :style="{ background: column.color }"></span>
            {{ column.title }}
          </div>
          <el-badge :value="column.tickets.length" :max="99" class="ticket-count" />
        </div>

        <div class="column-content" 
          @dragover.prevent="handleDragOver($event, column.status)"
          @drop="handleDrop($event, column.status)"
        >
          <div
            v-for="ticket in column.tickets"
            :key="ticket.id"
            class="kanban-card"
            draggable="true"
            @dragstart="handleDragStart($event, ticket)"
            @click="goToDetail(ticket.id)"
          >
            <div class="card-header">
              <span class="ticket-id">#{{ ticket.id.slice(0, 8) }}</span>
              <el-tag :type="getPriorityType(ticket.priority)" size="small">{{ ticket.priority }}</el-tag>
            </div>
            <div class="card-title">{{ ticket.title }}</div>
            <div class="card-meta">
              <span v-if="ticket.aiScore" class="ai-score">
                <el-icon><MagicStick /></el-icon> {{ ticket.aiScore }}%
              </span>
              <span class="category">{{ formatCategory(ticket.category) }}</span>
            </div>
            <div class="card-footer">
              <span v-if="ticket.assignedTo" class="assignee">
                <el-icon><User /></el-icon> {{ ticket.assignedTo.name }}
              </span>
              <span v-else class="unassigned">Unassigned</span>
              <span v-if="ticket.dueDate" class="due-date" :class="{ overdue: isOverdue(ticket.dueDate) }">
                {{ formatDueDate(ticket.dueDate) }}
              </span>
            </div>
          </div>

          <div v-if="column.tickets.length === 0" class="empty-column">
            <el-icon><Document /></el-icon>
            <span>No tickets</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTicketStore } from '@/stores/ticketStore'
import type { TicketStatus, Ticket, KanbanColumn } from '@/types/ticket'

const router = useRouter()
const store = useTicketStore()

let draggedTicket: Ticket | null = null

const columns = computed(() => store.kanbanColumns)
const loading = computed(() => store.loading)

onMounted(() => {
  store.fetchKanbanBoard()
})

function refreshBoard() {
  store.fetchKanbanBoard()
}

function handleDragStart(event: DragEvent, ticket: Ticket) {
  draggedTicket = ticket
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', ticket.id)
  }
}

function handleDragOver(event: DragEvent, _status: TicketStatus) {
  event.preventDefault()
}

async function handleDrop(event: DragEvent, newStatus: TicketStatus) {
  event.preventDefault()
  if (draggedTicket && draggedTicket.status !== newStatus) {
    await store.changeStatus(draggedTicket.id, newStatus)
    await store.fetchKanbanBoard()
  }
  draggedTicket = null
}

function goToDetail(id: string) {
  router.push(`/tickets/${id}`)
}

function getPriorityType(priority: string) {
  const types: Record<string, string> = {
    low: 'info', medium: 'warning', high: 'danger', critical: 'danger'
  }
  return types[priority] || 'info'
}

function formatCategory(category: string) {
  return category.charAt(0).toUpperCase() + category.slice(1)
}

function formatDueDate(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diffDays = Math.floor((date.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
  if (diffDays < 0) return `${Math.abs(diffDays)}d overdue`
  if (diffDays === 0) return 'Today'
  if (diffDays === 1) return 'Tomorrow'
  return `in ${diffDays}d`
}

function isOverdue(dueDate: string) {
  return new Date(dueDate) < new Date()
}
</script>

<style scoped>
.kanban-view {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}
.header-actions {
  display: flex;
  gap: 12px;
}
.kanban-board {
  display: flex;
  gap: 16px;
  flex: 1;
  overflow-x: auto;
  padding-bottom: 20px;
}
.kanban-column {
  flex: 0 0 300px;
  background: #f5f7fa;
  border-radius: 8px;
  border-top: 4px solid #409EFF;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 160px);
}
.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}
.column-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
}
.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.ticket-count {
  font-size: 12px;
}
.column-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 100px;
}
.kanban-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  transition: box-shadow 0.2s, transform 0.2s;
}
.kanban-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.ticket-id {
  font-size: 12px;
  color: #909399;
}
.card-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #606266;
}
.ai-score {
  display: flex;
  align-items: center;
  gap: 2px;
  color: #409EFF;
}
.category {
  color: #909399;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}
.unassigned {
  color: #C0C4CC;
}
.overdue {
  color: #F56C6C;
}
.empty-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
  gap: 8px;
}
</style>
