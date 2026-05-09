<template>
  <div class="ticket-detail-view" v-loading="loading">
    <div v-if="ticket" class="ticket-content">
      <div class="ticket-header">
        <div class="header-left">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon> Back
          </el-button>
          <h1 class="ticket-title">{{ ticket.title }}</h1>
          <el-tag :type="getStatusType(ticket.status)" size="large">{{ formatStatus(ticket.status) }}</el-tag>
          <el-tag :type="getPriorityType(ticket.priority)" size="large">{{ ticket.priority }}</el-tag>
        </div>
        <div class="header-actions">
          <el-button v-if="ticket.aiScore" type="info" plain @click="showAIAnalysis = true">
            AI Analysis: {{ ticket.aiScore }}%
          </el-button>
          <el-dropdown @command="handleAction">
            <el-button type="primary">Actions<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="assign">Assign</el-dropdown-item>
                <el-dropdown-item command="status">Change Status</el-dropdown-item>
                <el-dropdown-item command="edit">Edit</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="ticket-tabs">
        <el-tab-pane label="Details" name="details">
          <div class="details-grid">
            <div class="detail-item">
              <span class="label">Ticket ID</span>
              <span class="value">{{ ticket.id }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Category</span>
              <span class="value">{{ formatCategory(ticket.category) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Created By</span>
              <span class="value">{{ ticket.createdBy?.name || 'Unknown' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Assigned To</span>
              <span class="value">{{ ticket.assignedTo?.name || 'Unassigned' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Created At</span>
              <span class="value">{{ formatDate(ticket.createdAt) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Due Date</span>
              <span class="value" :class="{ overdue: isOverdue(ticket.dueDate) }">
                {{ ticket.dueDate ? formatDate(ticket.dueDate) : 'Not set' }}
              </span>
            </div>
            <div class="detail-item" v-if="ticket.affectedDevice">
              <span class="label">Affected Device</span>
              <span class="value">{{ ticket.affectedDevice }}</span>
            </div>
            <div class="detail-item" v-if="ticket.relatedCI">
              <span class="label">Related CI</span>
              <span class="value">{{ ticket.relatedCI }}</span>
            </div>
          </div>

          <div class="description-section">
            <h3>Description</h3>
            <div class="description-content">{{ ticket.description }}</div>
          </div>

          <div class="sla-section" v-if="ticket.slaResponse || ticket.slaResolution">
            <h3>SLA Information</h3>
            <div class="sla-grid">
              <div class="sla-item" v-if="ticket.slaResponse">
                <span class="label">Response SLA</span>
                <span class="value">{{ ticket.slaResponse }}</span>
              </div>
              <div class="sla-item" v-if="ticket.slaResolution">
                <span class="label">Resolution SLA</span>
                <span class="value">{{ ticket.slaResolution }}</span>
              </div>
            </div>
          </div>

          <div class="tags-section" v-if="ticket.tags.length">
            <h3>Tags</h3>
            <el-tag v-for="tag in ticket.tags" :key="tag" size="small" style="margin-right: 8px">
              {{ tag }}
            </el-tag>
          </div>
        </el-tab-pane>

        <el-tab-pane label="Comments" name="comments">
          <div class="comments-section">
            <div class="add-comment">
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="3"
                placeholder="Add a comment..."
              />
              <div class="comment-actions">
                <el-checkbox v-model="isInternalComment">Internal note</el-checkbox>
                <el-button type="primary" @click="submitComment" :loading="submittingComment">Submit</el-button>
              </div>
            </div>

            <div class="comments-list">
              <div v-for="comment in ticket.comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.author?.name || 'Unknown' }}</span>
                  <el-tag v-if="comment.isInternal" size="small" type="warning">Internal</el-tag>
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="History" name="history">
          <div class="history-list">
            <div v-for="entry in ticket.history" :key="entry.id" class="history-item">
              <div class="history-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="history-content">
                <span class="history-user">{{ entry.changedBy?.name }}</span>
                <span class="history-action">
                  changed <strong>{{ entry.field }}</strong> from "{{ entry.oldValue }}" to "{{ entry.newValue }}"
                </span>
                <span class="history-time">{{ formatTime(entry.changedAt) }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- AI Analysis Dialog -->
    <el-dialog v-model="showAIAnalysis" title="AI Analysis" width="500px">
      <div v-if="aiAnalysis" class="ai-analysis">
        <div class="ai-score">
          <el-progress type="circle" :percentage="aiAnalysis.score" :width="120" :color="getScoreColor(aiAnalysis.score)" />
          <p>AI Confidence: {{ aiAnalysis.confidence }}%</p>
        </div>
        <div class="ai-category">
          <strong>Suggested Category:</strong> {{ aiAnalysis.category }}
        </div>
        <div class="ai-actions" v-if="aiAnalysis.suggestedActions.length">
          <strong>Suggested Actions:</strong>
          <ul>
            <li v-for="(action, index) in aiAnalysis.suggestedActions" :key="index">{{ action }}</li>
          </ul>
        </div>
      </div>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog v-model="showEditDialog" title="Edit Ticket" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="Title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="editForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="Priority">
          <el-select v-model="editForm.priority" style="width: 100%">
            <el-option label="Low" value="low" />
            <el-option label="Medium" value="medium" />
            <el-option label="High" value="high" />
            <el-option label="Critical" value="critical" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">Cancel</el-button>
        <el-button type="primary" @click="submitEdit">Save</el-button>
      </template>
    </el-dialog>

    <!-- Assign Dialog -->
    <el-dialog v-model="showAssignDialog" title="Assign Ticket" width="400px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="Assign To">
          <el-select v-model="assignForm.userId" placeholder="Select user" style="width: 100%">
            <el-option v-for="user in users" :key="user.id" :label="user.name" :value="user.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAssignDialog = false">Cancel</el-button>
        <el-button type="primary" @click="submitAssign">Assign</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTicketStore } from '@/stores/ticketStore'
import { useAdminStore } from '@/stores/adminStore'
import { useAIInsightsStore } from '@/stores/aiInsightsStore'
import type { TicketStatus, TicketPriority } from '@/types/ticket'

const route = useRoute()
const router = useRouter()
const store = useTicketStore()
const adminStore = useAdminStore()
const aiStore = useAIInsightsStore()

const activeTab = ref('details')
const newComment = ref('')
const isInternalComment = ref(false)
const submittingComment = ref(false)
const showAIAnalysis = ref(false)
const showEditDialog = ref(false)
const showAssignDialog = ref(false)
const aiAnalysis = ref<{ score: number; confidence: number; category: string; suggestedActions: string[] } | null>(null)

const editForm = ref({ title: '', description: '', priority: 'medium' as TicketPriority })
const assignForm = ref({ userId: '' })
const users = ref<Array<{ id: string; name: string }>>([])

const ticket = computed(() => store.currentTicket)
const loading = computed(() => store.detailLoading)

onMounted(async () => {
  const id = route.params.id as string
  await store.fetchTicket(id)
  await adminStore.fetchUsers()
  users.value = adminStore.users
})

function goBack() {
  router.back()
}

async function submitComment() {
  if (!newComment.value.trim()) return
  submittingComment.value = true
  try {
    await store.addComment(ticket.value!.id, newComment.value, isInternalComment.value)
    newComment.value = ''
    ElMessage.success('Comment added')
  } catch {
    ElMessage.error('Failed to add comment')
  } finally {
    submittingComment.value = false
  }
}

async function submitEdit() {
  try {
    await store.updateTicket(ticket.value!.id, editForm.value)
    showEditDialog.value = false
    ElMessage.success('Ticket updated')
  } catch {
    ElMessage.error('Failed to update ticket')
  }
}

async function submitAssign() {
  try {
    await store.assignTicket(ticket.value!.id, assignForm.value.userId)
    showAssignDialog.value = false
    ElMessage.success('Ticket assigned')
  } catch {
    ElMessage.error('Failed to assign ticket')
  }
}

async function handleAction(command: string) {
  switch (command) {
    case 'edit':
      editForm.value = {
        title: ticket.value!.title,
        description: ticket.value!.description,
        priority: ticket.value!.priority
      }
      showEditDialog.value = true
      break
    case 'assign':
      assignForm.value = { userId: ticket.value!.assignedTo?.id || '' }
      showAssignDialog.value = true
      break
    case 'status':
      // Show status change dialog
      break
  }
}

function getStatusType(status: TicketStatus) {
  const types: Record<TicketStatus, string> = {
    open: 'primary', in_progress: 'warning', pending: 'info', resolved: 'success', closed: 'info'
  }
  return types[status] || 'info'
}

function getPriorityType(priority: TicketPriority) {
  const types: Record<TicketPriority, string> = {
    low: 'info', medium: 'warning', high: 'danger', critical: 'danger'
  }
  return types[priority] || 'info'
}

function formatStatus(status: TicketStatus) {
  const labels: Record<TicketStatus, string> = {
    open: 'Open', in_progress: 'In Progress', pending: 'Pending', resolved: 'Resolved', closed: 'Closed'
  }
  return labels[status] || status
}

function formatCategory(category: string) {
  return category.charAt(0).toUpperCase() + category.slice(1)
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMins / 60)
  if (diffMins < 60) return `${diffMins}m ago`
  if (diffHours < 24) return `${diffHours}h ago`
  return date.toLocaleDateString()
}

function isOverdue(dueDate?: string) {
  return dueDate ? new Date(dueDate) < new Date() : false
}

function getScoreColor(score: number) {
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}
</script>

<style scoped>
.ticket-detail-view { padding: 20px; }
.ticket-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.header-left { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.header-actions { display: flex; gap: 12px; }
.ticket-title { margin: 0; font-size: 24px; }
.ticket-tabs { background: #fff; padding: 20px; border-radius: 8px; }
.details-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }
.detail-item { display: flex; flex-direction: column; gap: 4px; }
.detail-item .label { font-size: 12px; color: #909399; }
.detail-item .value { font-size: 14px; font-weight: 500; }
.overdue { color: #F56C6C; }
.description-section h3, .sla-section h3, .tags-section h3 { margin: 0 0 12px; font-size: 16px; }
.description-content { background: #f5f7fa; padding: 12px; border-radius: 4px; white-space: pre-wrap; }
.sla-grid { display: flex; gap: 24px; }
.sla-item { display: flex; flex-direction: column; gap: 4px; }
.sla-item .label { font-size: 12px; color: #909399; }
.comments-section { display: flex; flex-direction: column; gap: 20px; }
.add-comment { display: flex; flex-direction: column; gap: 12px; }
.comment-actions { display: flex; justify-content: space-between; align-items: center; }
.comments-list { display: flex; flex-direction: column; gap: 16px; }
.comment-item { padding: 12px; background: #f5f7fa; border-radius: 8px; }
.comment-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.comment-author { font-weight: 600; }
.comment-time { color: #909399; font-size: 12px; margin-left: auto; }
.comment-content { white-space: pre-wrap; }
.history-list { display: flex; flex-direction: column; gap: 12px; }
.history-item { display: flex; gap: 12px; align-items: flex-start; }
.history-icon { width: 32px; height: 32px; border-radius: 50%; background: #f5f7fa; display: flex; align-items: center; justify-content: center; color: #909399; }
.history-content { flex: 1; font-size: 14px; }
.history-action { margin: 0 4px; }
.history-time { color: #909399; font-size: 12px; }
.ai-analysis { text-align: center; }
.ai-score { margin-bottom: 20px; }
.ai-score p { color: #909399; }
.ai-category, .ai-actions { text-align: left; margin-bottom: 16px; }
.ai-actions ul { margin: 8px 0 0 20px; }
</style>
