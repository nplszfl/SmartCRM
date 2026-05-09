import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Email, EmailTemplate } from '@/types'
import { emailApi, aiEmailApi, type SendEmailDto } from '@/api/emailService'

export const useEmailStore = defineStore('email', () => {
  // State
  const emails = ref<Email[]>([])
  const templates = ref<EmailTemplate[]>([])
  const currentDraft = ref<Partial<Email>>({
    to: [],
    cc: [],
    bcc: [],
    subject: '',
    body: ''
  })
  const loading = ref(false)
  const sending = ref(false)
  const generatingAI = ref(false)
  const aiSuggestions = ref<{
    subject?: string
    body?: string
    alternatives?: { subject: string; body: string }[]
    suggestions?: string[]
  } | null>(null)

  // Getters
  const draftSubject = computed(() => currentDraft.value.subject || '')
  const draftBody = computed(() => currentDraft.value.body || '')
  const hasRecipients = computed(() => (currentDraft.value.to?.length || 0) > 0)
  const isValid = computed(() => 
    hasRecipients.value && 
    currentDraft.value.subject?.trim() && 
    currentDraft.value.body?.trim()
  )

  // Actions
  async function fetchEmails(params?: { page?: number; pageSize?: number }) {
    loading.value = true
    try {
      const response = await emailApi.getEmails(params || { page: 1, pageSize: 20 })
      emails.value = response.data
      return response
    } finally {
      loading.value = false
    }
  }

  async function fetchTemplates() {
    const response = await emailApi.getTemplates()
    templates.value = response.data
    return response.data
  }

  function setDraft(field: keyof Email, value: unknown) {
    currentDraft.value[field] = value as never
  }

  function addRecipient(email: string) {
    if (!currentDraft.value.to) {
      currentDraft.value.to = []
    }
    if (!currentDraft.value.to.includes(email)) {
      currentDraft.value.to.push(email)
    }
  }

  function removeRecipient(email: string) {
    currentDraft.value.to = currentDraft.value.to?.filter(e => e !== email)
  }

  async function saveDraft() {
    if (currentDraft.value.id) {
      await emailApi.saveDraft({ ...currentDraft.value, id: currentDraft.value.id } as SendEmailDto)
    } else {
      const response = await emailApi.saveDraft(currentDraft.value as SendEmailDto)
      currentDraft.value.id = response.data.id
    }
  }

  async function sendEmail() {
    if (!isValid.value) return
    sending.value = true
    try {
      await emailApi.sendEmail(currentDraft.value as SendEmailDto)
      clearDraft()
      return true
    } finally {
      sending.value = false
    }
  }

  async function generateWithAI(leadId: string, options?: {
    tone?: 'formal' | 'casual' | 'friendly'
    focusPoints?: string[]
    additionalContext?: string
    templateId?: string
  }) {
    generatingAI.value = true
    aiSuggestions.value = null
    try {
      const response = await aiEmailApi.generateEmail({ leadId, ...options })
      aiSuggestions.value = response.data
      return response.data
    } finally {
      generatingAI.value = false
    }
  }

  async function generateReply(leadId: string, originalEmail: string, options?: {
    tone?: 'formal' | 'casual' | 'friendly'
    length?: 'short' | 'medium' | 'long'
  }) {
    generatingAI.value = true
    aiSuggestions.value = null
    try {
      const response = await aiEmailApi.generateReply({ leadId, originalEmail, ...options })
      aiSuggestions.value = response.data
      return response.data
    } finally {
      generatingAI.value = false
    }
  }

  function applySuggestion(subject?: string, body?: string) {
    if (subject) currentDraft.value.subject = subject
    if (body) currentDraft.value.body = body
  }

  function clearDraft() {
    currentDraft.value = {
      to: [],
      cc: [],
      bcc: [],
      subject: '',
      body: ''
    }
    aiSuggestions.value = null
  }

  function loadTemplate(template: EmailTemplate) {
    currentDraft.value.subject = template.subject
    currentDraft.value.body = template.body
  }

  async function createTemplate(data: Omit<EmailTemplate, 'id'>) {
    const response = await emailApi.createTemplate(data)
    templates.value.push(response.data)
    return response.data
  }

  async function deleteTemplate(id: string) {
    await emailApi.deleteTemplate(id)
    templates.value = templates.value.filter(t => t.id !== id)
  }

  function replaceTemplateVariables(text: string, variables: Record<string, string>) {
    let result = text
    Object.entries(variables).forEach(([key, value]) => {
      result = result.replace(new RegExp(`{{${key}}}`, 'g'), value)
    })
    return result
  }

  return {
    // State
    emails,
    templates,
    currentDraft,
    loading,
    sending,
    generatingAI,
    aiSuggestions,
    // Getters
    draftSubject,
    draftBody,
    hasRecipients,
    isValid,
    // Actions
    fetchEmails,
    fetchTemplates,
    setDraft,
    addRecipient,
    removeRecipient,
    saveDraft,
    sendEmail,
    generateWithAI,
    generateReply,
    applySuggestion,
    clearDraft,
    loadTemplate,
    createTemplate,
    deleteTemplate,
    replaceTemplateVariables
  }
})