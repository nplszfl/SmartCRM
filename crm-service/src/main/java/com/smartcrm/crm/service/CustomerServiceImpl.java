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
        Customer customer = this.getById(id);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer", id);
        }
        return customer;
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

    public com.smartcrm.common.dto.PageResponse<Customer> getAllCustomers(int page, int size) {
        com.baomidou.mybatisplus.core.metadata.IPage<Customer> pageParam = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<Customer> result = this.page(pageParam);
        return com.smartcrm.common.dto.PageResponse.of(
            result.getTotal(), page, size, result.getRecords());
    }

    public List<Customer> getCustomersByStatus(String status) {
        return this.list(new LambdaQueryWrapper<Customer>().eq(Customer::getCustomerType, status));
    }

    public long countByStatus(String status) {
        return this.count(new LambdaQueryWrapper<Customer>().eq(Customer::getCustomerType, status));
    }

    public long countAll() {
        return this.count();
    }

    public long countByIndustry(String industry) {
        return this.count(new LambdaQueryWrapper<Customer>().eq(Customer::getIndustry, industry));
    }

    public List<Customer> searchByName(String name) {
        return this.list(new LambdaQueryWrapper<Customer>().like(Customer::getName, name));
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