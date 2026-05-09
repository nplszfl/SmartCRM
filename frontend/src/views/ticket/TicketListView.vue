<template>
  <div class="ticket-list-view">
    <div class="header">
      <div class="header-left">
        <h2 class="title">Tickets</h2>
        <el-tag v-if="total > 0" type="info" size="large">{{ total }} tickets</el-tag>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchQuery"
          placeholder="Search tickets..."
          prefix-icon="Search"
          clearable
          @input="handleSearch"
          style="width: 240px"
        />
        <el-select v-model="filterStatus" placeholder="Status" clearable @change="applyFilters" style="width: 140px">
          <el-option label="Open" value="open" />
          <el-option label="In Progress" value="in_progress" />
          <el-option label="Pending" value="pending" />
          <el-option label="Resolved" value="resolved" />
          <el-option label="Closed" value="closed" />
        </el-select>
        <el-select v-model="filterPriority" placeholder="Priority" clearable @change="applyFilters" style="width: 140px">
          <el-option label="Low" value="low" />
          <el-option label="Medium" value="medium" />
          <el-option label="High" value="high" />
          <el-option label="Critical" value="critical" />
        </el-select>
        <el-select v-model="filterCategory" placeholder="Category" clearable @change="applyFilters" style="width: 140px">
          <el-option label="Incident" value="incident" />
          <el-option label="Request" value="request" />
          <el-option label="Problem" value="problem" />
          <el-option label="Change" value="change" />
          <el-option label="Question" value="question" />
        </el-select>
        <el-button type="primary" @click="handleCreate">Create Ticket</el-button>
      </div>
    </div>

    <el-table :data="tickets" v-loading="loading" stripe @row-click="handleRowClick">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="Title" min-width="200">
        <template #default="{ row }">
          <div class="ticket-title-cell">
            <span class="ticket-title">{{ row.title }}</span>
            <el-tag v-if="row.aiScore" size="small" type="info">AI: {{ row.aiScore }}%</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Status" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">{{ formatStatus(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Priority" width="100">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)" size="small">{{ row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Category" width="100">
        <template #default="{ row }">
          <span>{{ formatCategory(row.category) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Assigned To" width="150">
        <template #default="{ row }">
          <span v-if="row.assignedTo">{{ row.assignedTo.name }}</span>
          <span v-else class="text-muted">Unassigned</span>
        </template>
      </el-table-column>
      <el-table-column label="Created" width="150">
        <template #default="{ row }">
          <span>{{ formatDate(row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Due Date" width="120">
        <template #default="{ row }">
          <span :class="{ 'overdue': isOverdue(row.dueDate) }">{{ row.dueDate ? formatDate(row.dueDate) : '-' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-count="totalPages"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- Create Ticket Dialog -->
    <el-dialog v-model="showCreateDialog" title="Create Ticket" width="600px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="Title" required>
          <el-input v-model="createForm.title" placeholder="Enter ticket title" />
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="createForm.description" type="textarea" :rows="4" placeholder="Enter description" />
        </el-form-item>
        <el-form-item label="Priority">
          <el-select v-model="createForm.priority" style="width: 100%">
            <el-option label="Low" value="low" />
            <el-option label="Medium" value="medium" />
            <el-option label="High" value="high" />
            <el-option label="Critical" value="critical" />
          </el-select>
        </el-form-item>
        <el-form-item label="Category">
          <el-select v-model="createForm.category" style="width: 100%">
            <el-option label="Incident" value="incident" />
            <el-option label="Request" value="request" />
            <el-option label="Problem" value="problem" />
            <el-option label="Change" value="change" />
            <el-option label="Question" value="question" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">Cancel</el-button>
        <el-button type="primary" @click="submitCreate" :loading="submitting">Create</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTicketStore } from '@/stores/ticketStore'
import type { Ticket, TicketStatus, TicketPriority, TicketCategory } from '@/types/ticket'

const router = useRouter()
const store = useTicketStore()

const searchQuery = ref('')
const filterStatus = ref<TicketStatus | ''>('')
const filterPriority = ref<TicketPriority | ''>('')
const filterCategory = ref<TicketCategory | ''>('')
const currentPage = ref(1)
const pageSize = ref(20)
const showCreateDialog = ref(false)
const submitting = ref(false)

const createForm = ref({
  title: '',
  description: '',
  priority: 'medium' as TicketPriority,
  category: 'request' as TicketCategory
})

const tickets = computed(() => store.tickets)
const loading = computed(() => store.loading)
const total = computed(() => store.total)
const totalPages = computed(() => Math.ceil(store.total / store.pageSize))

onMounted(() => {
  store.fetchTickets()
})

function handleSearch() {
  store.setFilters({
    search: searchQuery.value || undefined,
    status: filterStatus.value || undefined,
    priority: filterPriority.value || undefined,
    category: filterCategory.value || undefined
  })
}

function applyFilters() {
  handleSearch()
}

function handlePageChange(page: number) {
  store.setPage(page)
}

function handleRowClick(row: Ticket) {
  router.push(`/tickets/${row.id}`)
}

function handleCreate() {
  showCreateDialog.value = true
  createForm.value = {
    title: '',
    description: '',
    priority: 'medium',
    category: 'request'
  }
}

async function submitCreate() {
  if (!createForm.value.title.trim()) {
    ElMessage.warning('Title is required')
    return
  }
  submitting.value = true
  try {
    await store.createTicket(createForm.value)
    ElMessage.success('Ticket created successfully')
    showCreateDialog.value = false
  } catch {
    ElMessage.error('Failed to create ticket')
  } finally {
    submitting.value = false
  }
}

function getStatusType(status: TicketStatus) {
  const types: Record<TicketStatus, string> = {
    open: 'primary',
    in_progress: 'warning',
    pending: 'info',
    resolved: 'success',
    closed: 'info'
  }
  return types[status] || 'info'
}

function getPriorityType(priority: TicketPriority) {
  const types: Record<TicketPriority, string> = {
    low: 'info',
    medium: 'warning',
    high: 'danger',
    critical: 'danger'
  }
  return types[priority] || 'info'
}

function formatStatus(status: TicketStatus) {
  const labels: Record<TicketStatus, string> = {
    open: 'Open',
    in_progress: 'In Progress',
    pending: 'Pending',
    resolved: 'Resolved',
    closed: 'Closed'
  }
  return labels[status] || status
}

function formatCategory(category: TicketCategory) {
  return category.charAt(0).toUpperCase() + category.slice(1)
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function isOverdue(dueDate?: string) {
  if (!dueDate) return false
  return new Date(dueDate) < new Date()
}
</script>

<style scoped>
.ticket-list-view {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}
.ticket-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ticket-title {
  font-weight: 500;
}
.text-muted {
  color: #909399;
}
.overdue {
  color: #F56C6C;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
