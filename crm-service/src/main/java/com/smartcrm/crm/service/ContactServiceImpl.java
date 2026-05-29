package com.smartcrm.crm.service;

import com.smartcrm.crm.dto.ContactRequest;
import com.smartcrm.crm.entity.Contact;
import com.smartcrm.crm.repository.ContactRepository;
import com.smartcrm.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;
import com.smartcrm.common.dto.PageResponse;

/**
 * Contact service implementation with DTO-based request handling.
 */
@Slf4j
@Service
public class ContactServiceImpl extends ServiceImpl<ContactRepository, Contact> {

    public Contact createContact(ContactRequest request) {
        log.info("Creating contact: {} {} for customer: {}", 
            request.getFirstName(), request.getLastName(), request.getCustomerId());
        
        Contact contact = new Contact();
        contact.setCustomerId(request.getCustomerId());
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setMobile(request.getMobile());
        contact.setJobTitle(request.getJobTitle());
        contact.setDepartment(request.getDepartment());
        contact.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        contact.setLinkedinUrl(request.getLinkedinUrl());
        contact.setTwitterHandle(request.getTwitterHandle());
        contact.setFacebookUrl(request.getFacebookUrl());
        contact.setDateOfBirth(request.getDateOfBirth());
        contact.setNotes(request.getNotes());
        contact.setOwnerId(request.getOwnerId());
        
        this.save(contact);
        log.info("Contact created with ID: {}", contact.getId());
        return contact;
    }

    public Contact updateContact(Long id, ContactRequest request) {
        Contact existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Contact", id);
        }
        
        if (request.getFirstName() != null) existing.setFirstName(request.getFirstName());
        if (request.getLastName() != null) existing.setLastName(request.getLastName());
        if (request.getEmail() != null) existing.setEmail(request.getEmail());
        if (request.getPhone() != null) existing.setPhone(request.getPhone());
        if (request.getMobile() != null) existing.setMobile(request.getMobile());
        if (request.getJobTitle() != null) existing.setJobTitle(request.getJobTitle());
        if (request.getDepartment() != null) existing.setDepartment(request.getDepartment());
        if (request.getIsPrimary() != null) existing.setIsPrimary(request.getIsPrimary());
        if (request.getLinkedinUrl() != null) existing.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getTwitterHandle() != null) existing.setTwitterHandle(request.getTwitterHandle());
        if (request.getFacebookUrl() != null) existing.setFacebookUrl(request.getFacebookUrl());
        if (request.getDateOfBirth() != null) existing.setDateOfBirth(request.getDateOfBirth());
        if (request.getNotes() != null) existing.setNotes(request.getNotes());
        if (request.getOwnerId() != null) existing.setOwnerId(request.getOwnerId());
        
        this.updateById(existing);
        log.info("Contact {} updated", id);
        return this.getById(id);
    }

    public Contact getContactById(Long id) {
        Contact contact = this.getById(id);
        if (contact == null) {
            throw new ResourceNotFoundException("Contact", id);
        }
        return contact;
    }

    public List<Contact> getContactsByCustomerId(Long customerId) {
        return this.list(new LambdaQueryWrapper<Contact>().eq(Contact::getCustomerId, customerId));
    }

    public Contact getPrimaryContact(Long customerId) {
        return this.getOne(new LambdaQueryWrapper<Contact>()
                .eq(Contact::getCustomerId, customerId)
                .eq(Contact::getIsPrimary, true));
    }

    public void deleteContact(Long id) {
        this.removeById(id);
    }

    public List<Contact> searchContactsByEmail(String email) {
        return this.list(new LambdaQueryWrapper<Contact>().like(Contact::getEmail, email));
    }

    public PageResponse<Contact> getAllContacts(int page, int size) {
        com.baomidou.mybatisplus.core.metadata.IPage<Contact> pageParam = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<Contact> result = this.page(pageParam);
        return PageResponse.of(result.getTotal(), page, size, result.getRecords());
    }

    public List<Contact> getAllContacts() {
        return this.list();
    }

    public long countByCustomer(Long customerId) {
        return this.count(new LambdaQueryWrapper<Contact>().eq(Contact::getCustomerId, customerId));
    }

    public long countAll() {
        return this.count();
    }
}