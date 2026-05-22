package com.smartcrm.crm.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.common.dto.PageResponse;
import com.smartcrm.crm.entity.Contact;
import com.smartcrm.crm.service.ContactServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contact REST controller.
 */
@Slf4j
@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactServiceImpl contactService;

    public ContactController(ContactServiceImpl contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ApiResponse<Contact> createContact(@RequestBody Contact contact) {
        log.info("REST request to create contact: {} {}", contact.getFirstName(), contact.getLastName());
        Contact created = contactService.createContact(contact);
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}")
    public ApiResponse<Contact> updateContact(@PathVariable Long id, @RequestBody Contact contact) {
        log.info("REST request to update contact: {}", id);
        Contact updated = contactService.updateContact(id, contact);
        return ApiResponse.success(updated);
    }

    @GetMapping("/{id}")
    public ApiResponse<Contact> getContact(@PathVariable Long id) {
        log.info("REST request to get contact: {}", id);
        Contact contact = contactService.getContactById(id);
        return ApiResponse.success(contact);
    }

    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<Contact>> getContactsByCustomer(@PathVariable Long customerId) {
        log.info("REST request to get contacts for customer: {}", customerId);
        List<Contact> contacts = contactService.getContactsByCustomerId(customerId);
        return ApiResponse.success(contacts);
    }

    @GetMapping("/customer/{customerId}/primary")
    public ApiResponse<Contact> getPrimaryContact(@PathVariable Long customerId) {
        log.info("REST request to get primary contact for customer: {}", customerId);
        Contact contact = contactService.getPrimaryContact(customerId);
        return ApiResponse.success(contact);
    }

    @GetMapping("/search")
    public ApiResponse<List<Contact>> searchContactsByEmail(@RequestParam String email) {
        log.info("REST request to search contacts by email: {}", email);
        List<Contact> contacts = contactService.searchContactsByEmail(email);
        return ApiResponse.success(contacts);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteContact(@PathVariable Long id) {
        log.info("REST request to delete contact: {}", id);
        contactService.deleteContact(id);
        return ApiResponse.success("Contact deleted successfully", null);
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<Contact>> getContactsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get contacts page: {}, size: {}", page, size);
        PageResponse<Contact> pageResp = contactService.getAllContacts(page, size);
        return ApiResponse.success(pageResp);
    }

    @GetMapping
    public ApiResponse<List<Contact>> getAllContacts() {
        log.info("REST request to get all contacts");
        List<Contact> contacts = contactService.getAllContacts();
        return ApiResponse.success(contacts);
    }
}