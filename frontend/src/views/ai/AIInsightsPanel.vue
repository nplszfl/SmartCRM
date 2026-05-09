<template>
  <div class="ai-insights-panel">
    <div class="panel-header">
      <div class="header-left">
        <h2 class="title">AI Insights</h2>
        <el-badge :value="urgentCount" :hidden="urgentCount === 0" type="danger">
          <el-tag type="warning">Urgent</el-tag>
        </el-badge>
      </div>
      <div class="header-actions">
        <el-select v-model="filterType" placeholder="Filter by type" clearable size="small" style="width: 140px">
          <el-option label="All" value="" />
          <el-option label="Warning" value="warning" />
          <el-option label="Risk" value="risk" />
          <el-option label="Recommendation" value="recommendation" />
          <el-option label="Opportunity" value="opportunity" />
        </el-select>
        <el-button size="small" @click="refresh" :loading="loading">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <div class="insights-summary" v-if="summary">
      <div class="summary-card">
        <div class="summary-icon" style="background: #F56C6C20; color: #F56C6C">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="summary-content">
          <div class="summary-value">{{ warnings.length }}</div>
          <div class="summary-label">Warnings</div>
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-icon" style="background: #E6A23C20; color: #E6A23C">
          <el-icon><WarningFilled /></el-icon>
        </div>
        <div class="summary-content">
          <div class="summary-value">{{ risks.length }}</div>
          <div class="summary-label">Risks</div>
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-icon" style="background: #409EFF20; color: #409EFF">
          <el-icon><InfoFilled /></el-icon>
        </div>
        <div class="summary-content">
          <div class="summary-value">{{ recommendations.length }}</div>
          <div class="summary-label">Recommendations</div>
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-icon" style="background: #67C23A20; color: #67C23A">
          <el-icon><CircleCheckFilled /></el-icon>
        </div>
        <div class="summary-content">
          <div class="summary-value">{{ opportunities.length }}</div>
          <div class="summary-label">Opportunities</div>
        </div>
      </div>
    </div>

    <div class="insights-list" v-loading="loading">
      <TransitionGroup name="insight">
        <div
          v-for="insight in filteredInsights"
          :key="insight.id"
          class="insight-card"
          :class="insight.type"
        >
          <div class="insight-header">
            <div class="insight-icon" :style="{ background: getInsightColor(insight.type), color: '#fff' }">
              <el-icon><component :is="getInsightIcon(insight.type)" /></el-icon>
            </div>
            <div class="insight-meta">
              <span class="insight-type">{{ formatType(insight.type) }}</span>
              <span class="insight-time">{{ formatTime(insight.createdAt) }}</span>
            </div>
            <div class="insight-confidence">
              <el-progress
                type="circle"
                :percentage="Math.round(insight.confidence)"
                :width="36"
                :stroke-width="3"
                :color="getConfidenceColor(insight.confidence)"
              />
            </div>
          </div>

          <div class="insight-title">{{ insight.title }}</div>
          <div class="insight-description">{{ insight.description }}</div>

          <div class="insight-source">
            <el-tag size="small" type="info">{{ formatSource(insight.source) }}</el-tag>
            <span v-if="insight.relatedTicketId" class="related-ticket">
              Related to
              <router-link :to="`/tickets/${insight.relatedTicketId}`" class="ticket-link">
                #{{ insight.relatedTicketId.slice(0, 8) }}
              </router-link>
            </span>
          </div>

          <div class="insight-actions" v-if="insight.action">
            <el-button type="primary" size="small" @click="handleAction(insight)">
              {{ insight.action }}
            </el-button>
          </div>

          <div class="insight-footer">
            <el-button size="small" text @click="dismissInsight(insight.id)">
              <el-icon><Close /></el-icon> Dismiss
            </el-button>
            <el-button size="small" text @click="provideFeedback(insight.id, true)">
              <el-icon><CircleCheck /></el-icon> Helpful
            </el-button>
            <el-button size="small" text @click="provideFeedback(insight.id, false)">
              <el-icon><CircleClose /></el-icon> Not Helpful
            </el-button>
          </div>
        </div>
      </TransitionGroup>

      <div v-if="filteredInsights.length === 0" class="empty-state">
        <el-icon size="48"><MagicStick /></el-icon>
        <p>No insights available</p>
        <el-button type="primary" @click="refresh">Refresh</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAIInsightsStore } from '@/stores/aiInsightsStore'
import type { AIInsight } from '@/types/ticket'

const aiStore = useAIInsightsStore()
const filterType = ref('')

const loading = computed(() => aiStore.loading)
const insights = computed(() => aiStore.insights)
const warnings = computed(() => aiStore.warnings)
const risks = computed(() => aiStore.risks)
const recommendations = computed(() => aiStore.recommendations)
const opportunities = computed(() => aiStore.opportunities)
const urgentCount = computed(() => aiStore.urgentInsights.length)
const summary = computed(() => insights.value.length > 0)

const filteredInsights = computed(() => {
  if (!filterType.value) return insights.value
  return insights.value.filter(i => i.type === filterType.value)
})

onMounted(() => {
  aiStore.fetchInsights()
})

function refresh() {
  aiStore.fetchInsights()
}

function getInsightIcon(type: AIInsight['type']) {
  switch (type) {
    case 'warning': return 'WarningFilled'
    case 'risk': return 'Warning'
    case 'recommendation': return 'InfoFilled'
    case 'opportunity': return 'CircleCheckFilled'
    default: return 'InfoFilled'
  }
}

function getInsightColor(type: AIInsight['type']) {
  switch (type) {
    case 'warning': return '#E6A23C'
    case 'risk': return '#F56C6C'
    case 'recommendation': return '#409EFF'
    case 'opportunity': return '#67C23A'
    default: return '#909399'
  }
}

function getConfidenceColor(confidence: number) {
  if (confidence >= 80) return '#67C23A'
  if (confidence >= 60) return '#E6A23C'
  return '#F56C6C'
}

function formatType(type: string) {
  return type.charAt(0).toUpperCase() + type.slice(1)
}

function formatSource(source: string) {
  const sources: Record<string, string> = {
    sla: 'SLA Analysis',
    category: 'Category Prediction',
    sentiment: 'Sentiment Analysis',
    pattern: 'Pattern Detection'
  }
  return sources[source] || source
}

function formatTime(dateStr: string) {
  return aiStore.formatTime(dateStr)
}

function handleAction(insight: AIInsight) {
  if (insight.relatedTicketId) {
    // Navigate to ticket or perform action
  }
  ElMessage.info(`Action: ${insight.action}`)
}

function dismissInsight(id: string) {
  aiStore.insights = aiStore.insights.filter(i => i.id !== id)
  ElMessage.success('Insight dismissed')
}

function provideFeedback(id: string, helpful: boolean) {
  ElMessage.success(helpful ? 'Marked as helpful' : 'Marked as not helpful')
}
</script>

<style scoped>
.ai-insights-panel {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.panel-header {
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
.title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.insights-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.summary-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.summary-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.summary-value {
  font-size: 24px;
  font-weight: 700;
}
.summary-label {
  font-size: 13px;
  color: #909399;
}
.insights-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.insight-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #909399;
}
.insight-card.warning { border-left-color: #E6A23C; }
.insight-card.risk { border-left-color: #F56C6C; }
.insight-card.recommendation { border-left-color: #409EFF; }
.insight-card.opportunity { border-left-color: #67C23A; }
.insight-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.insight-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.insight-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.insight-type {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}
.insight-time {
  font-size: 12px;
  color: #909399;
}
.insight-confidence {
  display: flex;
  align-items: center;
}
.insight-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}
.insight-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
  line-height: 1.5;
}
.insight-source {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.related-ticket {
  font-size: 13px;
  color: #909399;
}
.ticket-link {
  color: #409EFF;
  text-decoration: none;
}
.ticket-link:hover {
  text-decoration: underline;
}
.insight-actions {
  margin-bottom: 12px;
}
.insight-footer {
  display: flex;
  gap: 12px;
  border-top: 1px solid #f5f7fa;
  padding-top: 12px;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
  gap: 16px;
}
.empty-state p {
  margin: 0;
  font-size: 16px;
}
.insight-enter-active, .insight-leave-active {
  transition: all 0.3s ease;
}
.insight-enter-from, .insight-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
