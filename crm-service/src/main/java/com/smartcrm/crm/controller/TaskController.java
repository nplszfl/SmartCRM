package com.smartcrm.crm.controller;

import com.smartcrm.crm.dto.TaskRequest;
import com.smartcrm.crm.entity.Task;
import com.smartcrm.crm.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Task controller - manages CRM tasks and activities
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskRequest request,
                                           @RequestHeader(value = "X-User-Id", defaultValue = "1") Long createdById,
                                           @RequestHeader(value = "X-User-Name", defaultValue = "System") String createdByName) {
        return ResponseEntity.ok(taskService.createTask(request, createdById, createdByName));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable String status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Task>> getTasksByPriority(@PathVariable String priority) {
        return ResponseEntity.ok(taskService.getTasksByPriority(priority));
    }

    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<Task>> getTasksByAssignee(@PathVariable Long assigneeId) {
        return ResponseEntity.ok(taskService.getTasksByAssignee(assigneeId));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Task>> getTasksByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(taskService.getTasksByAccount(accountId));
    }

    @GetMapping("/contact/{contactId}")
    public ResponseEntity<List<Task>> getTasksByContact(@PathVariable Long contactId) {
        return ResponseEntity.ok(taskService.getTasksByContact(contactId));
    }

    @GetMapping("/opportunity/{opportunityId}")
    public ResponseEntity<List<Task>> getTasksByOpportunity(@PathVariable Long opportunityId) {
        return ResponseEntity.ok(taskService.getTasksByOpportunity(opportunityId));
    }

    @GetMapping("/lead/{leadId}")
    public ResponseEntity<List<Task>> getTasksByLead(@PathVariable Long leadId) {
        return ResponseEntity.ok(taskService.getTasksByLead(leadId));
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<Task>> getTasksByCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(taskService.getTasksByCampaign(campaignId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        return ResponseEntity.ok(taskService.getOverdueTasks());
    }

    @GetMapping("/due-today")
    public ResponseEntity<List<Task>> getTasksDueToday() {
        return ResponseEntity.ok(taskService.getTasksDueToday());
    }

    @GetMapping("/due-soon")
    public ResponseEntity<List<Task>> getTasksDueSoon(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(taskService.getTasksDueSoon(days));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Task> startTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.startTask(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable Long id,
                                            @RequestParam(required = false) String outcome,
                                            @RequestParam(required = false) Integer actualHours) {
        return ResponseEntity.ok(taskService.completeTask(id, outcome, actualHours));
    }

    @PostMapping("/{id}/defer")
    public ResponseEntity<Task> deferTask(@PathVariable Long id, @RequestParam LocalDateTime newDueDate) {
        return ResponseEntity.ok(taskService.deferTask(id, newDueDate));
    }

    @PutMapping("/{id}/completion")
    public ResponseEntity<Task> updateCompletionPercentage(@PathVariable Long id, @RequestParam Integer percentage) {
        return ResponseEntity.ok(taskService.updateCompletionPercentage(id, percentage));
    }

    @PostMapping("/{id}/reassign")
    public ResponseEntity<Task> reassignTask(@PathVariable Long id,
                                             @RequestParam Long newAssigneeId,
                                             @RequestParam String newAssigneeName) {
        return ResponseEntity.ok(taskService.reassignTask(id, newAssigneeId, newAssigneeName));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<Task> addNote(@PathVariable Long id, @RequestParam String note) {
        return ResponseEntity.ok(taskService.addNote(id, note));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
