<template>
  <div class="admin-dashboard">
    <div class="header">
      <h2 class="title">Admin Dashboard</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="to"
          start-placeholder="Start date"
          end-placeholder="End date"
          @change="handleDateChange"
          style="margin-right: 12px"
        />
        <el-button @click="refresh" :loading="loading">
          <el-icon><Refresh /></el-icon> Refresh
        </el-button>
      </div>
    </div>

    <div class="stats-cards" v-loading="loading">
      <div class="stat-card">
        <div class="stat-icon" style="background: #409EFF20; color: #409EFF">
          <el-icon><Ticket /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats?.totalTickets || 0 }}</div>
          <div class="stat-label">Total Tickets</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #F56C6C20; color: #F56C6C">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats?.openTickets || 0 }}</div>
          <div class="stat-label">Open Tickets</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #67C23A20; color: #67C23A">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats?.resolvedTickets || 0 }}</div>
          <div class="stat-label">Resolved</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #E6A23C20; color: #E6A23C">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ formatAvgTime(stats?.avgResolutionTime || 0) }}</div>
          <div class="stat-label">Avg Resolution</div>
        </div>
      </div>
    </div>

    <div class="dashboard-grid">
      <el-card class="dashboard-card" header="Tickets by Status">
        <div ref="statusChartRef" class="chart-container"></div>
      </el-card>

      <el-card class="dashboard-card" header="Tickets by Priority">
        <div ref="priorityChartRef" class="chart-container"></div>
      </el-card>

      <el-card class="dashboard-card" header="Tickets by Category">
        <div ref="categoryChartRef" class="chart-container"></div>
      </el-card>

      <el-card class="dashboard-card" header="Top Technicians">
        <div class="technicians-list">
          <div v-for="tech in stats?.topTechnicians" :key="tech.user.id" class="tech-item">
            <div class="tech-info">
              <el-avatar :size="36">{{ tech.user.name?.charAt(0) || '?' }}</el-avatar>
              <div class="tech-details">
                <span class="tech-name">{{ tech.user.name }}</span>
                <span class="tech-dept">{{ tech.user.department || 'IT' }}</span>
              </div>
            </div>
            <div class="tech-stats">
              <div class="tech-stat">
                <span class="value">{{ tech.ticketsResolved }}</span>
                <span class="label">Resolved</span>
              </div>
              <div class="tech-stat">
                <span class="value">{{ formatMins(tech.avgResponseTime) }}</span>
                <span class="label">Avg Response</span>
              </div>
              <div class="tech-stat">
                <span class="value">{{ tech.satisfactionScore }}%</span>
                <span class="label">Satisfaction</span>
              </div>
            </div>
          </div>
          <div v-if="!stats?.topTechnicians?.length" class="empty-list">
            No data available
          </div>
        </div>
      </el-card>

      <el-card class="dashboard-card full-width" header="Recent Activity">
        <div class="activity-list">
          <div v-for="activity in stats?.recentActivity" :key="activity.id" class="activity-item">
            <div class="activity-icon" :class="activity.type">
              <el-icon><Document /></el-icon>
            </div>
            <div class="activity-content">
              <span class="activity-user">{{ activity.user?.name }}</span>
              <span class="activity-desc">{{ activity.description }}</span>
              <router-link v-if="activity.ticketId" :to="`/tickets/${activity.ticketId}`" class="activity-ticket">
                {{ activity.ticketTitle }}
              </router-link>
            </div>
            <span class="activity-time">{{ formatTime(activity.timestamp) }}</span>
          </div>
          <div v-if="!stats?.recentActivity?.length" class="empty-list">
            No recent activity
          </div>
        </div>
      </el-card>
    </div>

    <el-card class="users-card" header="User Management">
      <template #header>
        <div class="card-header">
          <span>User Management</span>
          <el-button type="primary" size="small">Add User</el-button>
        </div>
      </template>
      <el-table :data="users" v-loading="loading">
        <el-table-column prop="name" label="Name" width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32">{{ row.name?.charAt(0) || '?' }}</el-avatar>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="Email" width="220" />
        <el-table-column prop="role" label="Role" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="Department" width="140" />
        <el-table-column label="Actions" width="180">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editUser(row)">Edit</el-button>
            <el-button type="danger" link size="small" @click="deleteUser(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/adminStore'
import * as echarts from 'echarts'
import type { AdminStats, User } from '@/types/ticket'

const adminStore = useAdminStore()
const dateRange = ref<[Date, Date]>([
  new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
  new Date()
])

const statusChartRef = ref<HTMLDivElement>()
const priorityChartRef = ref<HTMLDivElement>()
const categoryChartRef = ref<HTMLDivElement>()
let statusChart: echarts.ECharts | null = null
let priorityChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null

const stats = computed(() => adminStore.stats)
const loading = computed(() => adminStore.loading)
const users = computed(() => adminStore.users)

onMounted(async () => {
  await adminStore.fetchStats()
  await adminStore.fetchUsers()
  initCharts()
})

watch(stats, () => {
  updateCharts()
}, { deep: true })

function refresh() {
  adminStore.fetchStats()
}

function handleDateChange() {
  // Filter by date range
}

function initCharts() {
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
  }
  if (priorityChartRef.value) {
    priorityChart = echarts.init(priorityChartRef.value)
  }
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
  }
  updateCharts()
}

function updateCharts() {
  if (!stats.value) return

  // Status Chart
  if (statusChart) {
    const statusData = stats.value.ticketsByStatus
    statusChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: statusData.open, name: 'Open' },
          { value: statusData.in_progress, name: 'In Progress' },
          { value: statusData.pending, name: 'Pending' },
          { value: statusData.resolved, name: 'Resolved' },
          { value: statusData.closed, name: 'Closed' }
        ]
      }]
    })
  }

  // Priority Chart
  if (priorityChart) {
    const priorityData = stats.value.ticketsByPriority
    priorityChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['Low', 'Medium', 'High', 'Critical'] },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: [
          priorityData.low,
          priorityData.medium,
          priorityData.high,
          priorityData.critical
        ],
        itemStyle: {
          color: (params: { dataIndex: number }) => ['#909399', '#E6A23C', '#F56C6C', '#F56C6C'][params.dataIndex]
        }
      }]
    })
  }

  // Category Chart
  if (categoryChart) {
    const categoryData = stats.value.ticketsByCategory
    categoryChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: Object.keys(categoryData) },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: Object.values(categoryData),
        itemStyle: { color: '#409EFF' }
      }]
    })
  }
}

function formatAvgTime(mins: number) {
  if (mins < 60) return `${Math.round(mins)}m`
  const hours = Math.floor(mins / 60)
  const remainingMins = Math.round(mins % 60)
  return `${hours}h ${remainingMins}m`
}

function formatMins(mins: number) {
  return `${Math.round(mins)}m`
}

function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMins / 60)
  const diffDays = Math.floor(diffHours / 24)
  if (diffMins < 60) return `${diffMins}m ago`
  if (diffHours < 24) return `${diffHours}h ago`
  return `${diffDays}d ago`
}

function getRoleType(role: string) {
  const types: Record<string, string> = {
    admin: 'danger', manager: 'warning', agent: 'primary', technician: 'success', user: 'info'
  }
  return types[role] || 'info'
}

function editUser(user: User) {
  ElMessage.info(`Edit user: ${user.name}`)
}

function deleteUser(user: User) {
  ElMessage.warning(`Delete user: ${user.name}`)
}
</script>

<style scoped>
.admin-dashboard { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.title { margin: 0; font-size: 24px; font-weight: 600; }
.header-actions { display: flex; align-items: center; }
.stats-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.stat-value { font-size: 28px; font-weight: 700; }
.stat-label { font-size: 14px; color: #909399; }
.dashboard-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 20px; }
.dashboard-card { background: #fff; }
.dashboard-card :deep(.el-card__header) { font-weight: 600; padding: 16px 20px; border-bottom: 1px solid #e4e7ed; }
.full-width { grid-column: span 3; }
.chart-container { height: 250px; }
.technicians-list { display: flex; flex-direction: column; gap: 16px; }
.tech-item { display: flex; justify-content: space-between; align-items: center; padding: 12px; background: #f5f7fa; border-radius: 8px; }
.tech-info { display: flex; align-items: center; gap: 12px; }
.tech-details { display: flex; flex-direction: column; }
.tech-name { font-weight: 600; }
.tech-dept { font-size: 12px; color: #909399; }
.tech-stats { display: flex; gap: 24px; }
.tech-stat { display: flex; flex-direction: column; align-items: center; }
.tech-stat .value { font-size: 18px; font-weight: 600; color: #409EFF; }
.tech-stat .label { font-size: 11px; color: #909399; }
.activity-list { display: flex; flex-direction: column; gap: 12px; max-height: 300px; overflow-y: auto; }
.activity-item { display: flex; align-items: flex-start; gap: 12px; padding: 12px; background: #f5f7fa; border-radius: 8px; }
.activity-icon { width: 36px; height: 36px; border-radius: 50%; background: #409EFF20; color: #409EFF; display: flex; align-items: center; justify-content: center; }
.activity-icon.created { background: #67C23A20; color: #67C23A; }
.activity-icon.resolved { background: #E6A23C20; color: #E6A23C; }
.activity-content { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.activity-user { font-weight: 600; }
.activity-ticket { color: #409EFF; font-size: 13px; }
.activity-time { color: #909399; font-size: 12px; }
.empty-list { text-align: center; color: #909399; padding: 20px; }
.users-card { margin-top: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.user-cell { display: flex; align-items: center; gap: 8px; }
</style>
