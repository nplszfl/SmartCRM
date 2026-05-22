package com.smartcrm.crm.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.crm.entity.Customer;
import com.smartcrm.crm.service.CustomerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer REST controller.
 */
@Slf4j
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerServiceImpl customerService;

    public CustomerController(CustomerServiceImpl customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ApiResponse<Customer> createCustomer(@RequestBody Customer customer) {
        log.info("REST request to create customer: {}", customer.getName());
        Customer created = customerService.createCustomer(customer);
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}")
    public ApiResponse<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        log.info("REST request to update customer: {}", id);
        Customer updated = customerService.updateCustomer(id, customer);
        return ApiResponse.success(updated);
    }

    @GetMapping("/{id}")
    public ApiResponse<Customer> getCustomer(@PathVariable Long id) {
        log.info("REST request to get customer: {}", id);
        Customer customer = customerService.getCustomerById(id);
        return ApiResponse.success(customer);
    }

    @GetMapping
    public ApiResponse<List<Customer>> getAllCustomers() {
        log.info("REST request to get all customers");
        List<Customer> customers = customerService.getAllCustomers();
        return ApiResponse.success(customers);
    }

    @GetMapping("/page")
    public ApiResponse<com.smartcrm.common.dto.PageResponse<Customer>> getCustomersPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get customers page: {}, size: {}", page, size);
        com.smartcrm.common.dto.PageResponse<Customer> pageResp = customerService.getAllCustomers(page, size);
        return ApiResponse.success(pageResp);
    }

    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Customer>> getCustomersByOwner(@PathVariable Long ownerId) {
        log.info("REST request to get customers by owner: {}", ownerId);
        List<Customer> customers = customerService.getCustomersByOwnerId(ownerId);
        return ApiResponse.success(customers);
    }

    @GetMapping("/type/{customerType}")
    public ApiResponse<List<Customer>> getCustomersByType(@PathVariable String customerType) {
        log.info("REST request to get customers by type: {}", customerType);
        List<Customer> customers = customerService.getCustomersByType(customerType);
        return ApiResponse.success(customers);
    }

    @GetMapping("/industry/{industry}")
    public ApiResponse<List<Customer>> getCustomersByIndustry(@PathVariable String industry) {
        log.info("REST request to get customers by industry: {}", industry);
        List<Customer> customers = customerService.getCustomersByIndustry(industry);
        return ApiResponse.success(customers);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCustomer(@PathVariable Long id) {
        log.info("REST request to delete customer: {}", id);
        customerService.deleteCustomer(id);
        return ApiResponse.success("Customer deleted successfully", null);
    }

    @PatchMapping("/{id}/activate")
    public ApiResponse<Customer> activateCustomer(@PathVariable Long id) {
        log.info("REST request to activate customer: {}", id);
        Customer customer = customerService.convertToActive(id);
        return ApiResponse.success(customer);
    }
}