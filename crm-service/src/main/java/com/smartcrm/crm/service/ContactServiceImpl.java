package com.smartcrm.crm.service;

import com.smartcrm.crm.entity.Contact;
import com.smartcrm.crm.repository.ContactRepository;
import com.smartcrm.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * Contact service implementation.
 */
@Slf4j
@Service
public class ContactServiceImpl extends ServiceImpl<ContactRepository, Contact> {

    public Contact createContact(Contact contact) {
        log.info("Creating contact: {} {}", contact.getFirstName(), contact.getLastName());
        this.save(contact);
        return contact;
    }

    public Contact updateContact(Long id, Contact contact) {
        Contact existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Contact", id);
        }
        contact.setId(id);
        this.updateById(contact);
        return this.getById(id);
    }

    public Contact getContactById(Long id) {
        return this.getById(id);
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
}