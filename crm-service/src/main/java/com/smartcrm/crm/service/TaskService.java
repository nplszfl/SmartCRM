package com.smartcrm.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.crm.dto.TaskRequest;
import com.smartcrm.crm.entity.Task;
import com.smartcrm.crm.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Task service - manages CRM tasks and activities
 */
@Slf4j
@Service
public class TaskService extends ServiceImpl<TaskRepository, Task> {

    public Task createTask(TaskRequest request, Long createdById, String createdByName) {
        log.info("Creating task: {}", request.getSubject());

        Task task = new Task();
        task.setSubject(request.getSubject());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : "NOT_STARTED");
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setCategory(request.getCategory() != null ? request.getCategory() : "OTHER");
        task.setAccountId(request.getAccountId());
        task.setContactId(request.getContactId());
        task.setOpportunityId(request.getOpportunityId());
        task.setLeadId(request.getLeadId());
        task.setCampaignId(request.getCampaignId());
        task.setAssignedToId(request.getAssignedToId());
        task.setAssignedToName(request.getAssignedToName());
        task.setCreatedById(createdById);
        task.setCreatedByName(createdByName);
        task.setDueDate(request.getDueDate());
        task.setStartDate(request.getStartDate());
        task.setReminderDate(request.getReminderDate());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setIsRecurring(request.getIsRecurring());
        task.setRecurrencePattern(request.getRecurrencePattern());
        task.setRecurrenceInterval(request.getRecurrenceInterval());
        task.setLocation(request.getLocation());
        task.setNotes(request.getNotes());
        task.setCompletionPercentage(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        this.save(task);
        log.info("Task created with ID: {}", task.getId());
        return task;
    }

    public Task updateTask(Long id, TaskRequest request) {
        Task existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Task", id);
        }

        existing.setSubject(request.getSubject());
        existing.setDescription(request.getDescription());
        existing.setPriority(request.getPriority());
        existing.setCategory(request.getCategory());
        existing.setAccountId(request.getAccountId());
        existing.setContactId(request.getContactId());
        existing.setOpportunityId(request.getOpportunityId());
        existing.setLeadId(request.getLeadId());
        existing.setCampaignId(request.getCampaignId());
        existing.setAssignedToId(request.getAssignedToId());
        existing.setAssignedToName(request.getAssignedToName());
        existing.setDueDate(request.getDueDate());
        existing.setStartDate(request.getStartDate());
        existing.setReminderDate(request.getReminderDate());
        existing.setEstimatedHours(request.getEstimatedHours());
        existing.setIsRecurring(request.getIsRecurring());
        existing.setRecurrencePattern(request.getRecurrencePattern());
        existing.setRecurrenceInterval(request.getRecurrenceInterval());
        existing.setLocation(request.getLocation());
        existing.setNotes(request.getNotes());
        existing.setUpdatedAt(LocalDateTime.now());

        this.updateById(existing);
        return existing;
    }

    public Task getTaskById(Long id) {
        Task task = this.getById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task", id);
        }
        return task;
    }

    public List<Task> getAllTasks() {
        return this.list();
    }

    public List<Task> getTasksByStatus(String status) {
        return this.list(new LambdaQueryWrapper<Task>().eq(Task::getStatus, status));
    }

    public List<Task> getTasksByPriority(String priority) {
        return this.list(new LambdaQueryWrapper<Task>().eq(Task::getPriority, priority));
    }

    public List<Task> getTasksByAssignee(Long assigneeId) {
        return this.list(new LambdaQueryWrapper<Task>().eq(Task::getAssignedToId, assigneeId));
    }

    public List<Task> getTasksByAccount(Long accountId) {
        return this.list(new LambdaQueryWrapper<Task>().eq(Task::getAccountId, accountId));
    }

    public List<Task> getTasksByContact(Long contactId) {
        return this.list(new LambdaQueryWrapper<Task>().eq(Task::getContactId, contactId));
    }

    public List<Task> getTasksByOpportunity(Long opportunityId) {
        return this.list(new LambdaQueryWrapper<Task>().eq(Task::getOpportunityId, opportunityId));
    }

    public List<Task> getTasksByLead(Long leadId) {
        return this.list(new LambdaQueryWrapper<Task>().eq(Task::getLeadId, leadId));
    }

    public List<Task> getTasksByCampaign(Long campaignId) {
        return this.list(new LambdaQueryWrapper<Task>().eq(Task::getCampaignId, campaignId));
    }

    public List<Task> getOverdueTasks() {
        return this.list(new LambdaQueryWrapper<Task>()
                .eq(Task::getStatus, "NOT_STARTED")
                .or()
                .eq(Task::getStatus, "IN_PROGRESS")
                .lt(Task::getDueDate, LocalDateTime.now()));
    }

    public List<Task> getTasksDueToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return this.list(new LambdaQueryWrapper<Task>()
                .ge(Task::getDueDate, startOfDay)
                .lt(Task::getDueDate, endOfDay));
    }

    public List<Task> getTasksDueSoon(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);
        return this.list(new LambdaQueryWrapper<Task>()
                .in(Task::getStatus, "NOT_STARTED", "IN_PROGRESS")
                .ge(Task::getDueDate, now)
                .le(Task::getDueDate, future)
                .orderByAsc(Task::getDueDate));
    }

    public Task startTask(Long id) {
        Task task = this.getById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task", id);
        }
        task.setStatus("IN_PROGRESS");
        task.setStartDate(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        this.updateById(task);
        log.info("Task {} started", id);
        return task;
    }

    public Task completeTask(Long id, String outcome, Integer actualHours) {
        Task task = this.getById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task", id);
        }
        task.setStatus("COMPLETED");
        task.setCompletionPercentage(100);
        task.setCompletedDate(LocalDateTime.now());
        task.setOutcome(outcome);
        if (actualHours != null) {
            task.setActualHours(actualHours);
        }
        task.setUpdatedAt(LocalDateTime.now());
        this.updateById(task);
        log.info("Task {} completed", id);
        return task;
    }

    public Task deferTask(Long id, LocalDateTime newDueDate) {
        Task task = this.getById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task", id);
        }
        task.setStatus("DEFERRED");
        task.setDueDate(newDueDate);
        task.setUpdatedAt(LocalDateTime.now());
        this.updateById(task);
        log.info("Task {} deferred to {}", id, newDueDate);
        return task;
    }

    public Task updateCompletionPercentage(Long id, Integer percentage) {
        Task task = this.getById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task", id);
        }
        task.setCompletionPercentage(percentage);
        if (percentage == 100) {
            task.setStatus("COMPLETED");
            task.setCompletedDate(LocalDateTime.now());
        }
        task.setUpdatedAt(LocalDateTime.now());
        this.updateById(task);
        return task;
    }

    public Task reassignTask(Long id, Long newAssigneeId, String newAssigneeName) {
        Task task = this.getById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task", id);
        }
        task.setAssignedToId(newAssigneeId);
        task.setAssignedToName(newAssigneeName);
        task.setUpdatedAt(LocalDateTime.now());
        this.updateById(task);
        log.info("Task {} reassigned to {}", id, newAssigneeName);
        return task;
    }

    public void deleteTask(Long id) {
        this.removeById(id);
    }

    public long countByStatus(String status) {
        return this.count(new LambdaQueryWrapper<Task>().eq(Task::getStatus, status));
    }

    public long countOverdueTasks() {
        return this.count(new LambdaQueryWrapper<Task>()
                .in(Task::getStatus, "NOT_STARTED", "IN_PROGRESS")
                .lt(Task::getDueDate, LocalDateTime.now()));
    }

    public long countByAssignee(Long assigneeId) {
        return this.count(new LambdaQueryWrapper<Task>().eq(Task::getAssignedToId, assigneeId));
    }

    public Task addNote(Long id, String note) {
        Task task = this.getById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task", id);
        }
        String existingNotes = task.getNotes() != null ? task.getNotes() : "";
        task.setNotes(existingNotes + "\n[" + LocalDateTime.now() + "] " + note);
        task.setUpdatedAt(LocalDateTime.now());
        this.updateById(task);
        return task;
    }
}
