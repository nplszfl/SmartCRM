package com.smartcrm.crm.service;

import com.smartcrm.crm.entity.Customer;
import com.smartcrm.crm.repository.CustomerRepository;
import com.smartcrm.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * Customer service implementation.
 */
@Slf4j
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerRepository, Customer> {

    public Customer createCustomer(Customer customer) {
        log.info("Creating customer: {}", customer.getName());
        customer.setCustomerType("PROSPECT");
        this.save(customer);
        return customer;
    }

    public Customer updateCustomer(Long id, Customer customer) {
        Customer existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Customer", id);
        }
        customer.setId(id);
        this.updateById(customer);
        return this.getById(id);
    }

    public Customer getCustomerById(Long id) {
        return this.getById(id);
    }

    public List<Customer> getAllCustomers() {
        return this.list();
    }

    public List<Customer> getCustomersByOwnerId(Long ownerId) {
        return this.list(new LambdaQueryWrapper<Customer>().eq(Customer::getOwnerId, ownerId));
    }

    public List<Customer> getCustomersByType(String customerType) {
        return this.list(new LambdaQueryWrapper<Customer>().eq(Customer::getCustomerType, customerType));
    }

    public List<Customer> getCustomersByIndustry(String industry) {
        return this.list(new LambdaQueryWrapper<Customer>().eq(Customer::getIndustry, industry));
    }

    public void deleteCustomer(Long id) {
        this.removeById(id);
    }

    public Customer convertToActive(Long id) {
        Customer customer = this.getById(id);
        if (customer != null) {
            customer.setCustomerType("ACTIVE");
            this.updateById(customer);
        }
        return customer;
    }
}