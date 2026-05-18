package com.smartcrm.crm.service;

import com.smartcrm.crm.entity.Customer;
import com.smartcrm.crm.repository.CustomerRepository;
import com.smartcrm.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerServiceImpl.
 * Tests customer CRUD operations, filtering, and business logic.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl();
        try {
            var field = ServiceImpl.class.getDeclaredField("baseMapper");
            field.setAccessible(true);
            field.set(customerService, customerRepository);
        } catch (Exception e) {
            // Fallback - the service uses this.baseMapper internally
        }
    }

    @Test
    void createCustomer_withValidData_setsDefaultTypeAndSaves() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("Test Company");
        customer.setEmail("test@company.com");
        customer.setPhone("1234567890");
        customer.setIndustry("Technology");

        when(customerRepository.insert(any(Customer.class))).thenReturn(1);

        // Act
        Customer result = customerService.createCustomer(customer);

        // Assert
        assertThat(result.getCustomerType()).isEqualTo("PROSPECT");
        verify(customerRepository).insert(any(Customer.class));
    }

    @Test
    void getCustomerById_whenExists_returnsCustomer() {
        // Arrange
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName("Test Company");
        customer.setCustomerType("ACTIVE");

        when(customerRepository.selectById(customerId)).thenReturn(customer);

        // Act
        Customer result = customerService.getCustomerById(customerId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Company");
    }

    @Test
    void getCustomerById_whenNotExists_throwsResourceNotFoundException() {
        // Arrange
        Long customerId = 999L;
        when(customerRepository.selectById(customerId)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> customerService.getCustomerById(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer");
    }

    @Test
    void getCustomersByOwnerId_returnsFilteredList() {
        // Arrange
        Long ownerId = 1L;
        Customer c1 = new Customer();
        c1.setId(1L);
        c1.setName("Company A");
        c1.setOwnerId(ownerId);
        
        Customer c2 = new Customer();
        c2.setId(2L);
        c2.setName("Company B");
        c2.setOwnerId(ownerId);

        when(customerRepository.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c1, c2));

        // Act
        List<Customer> result = customerService.getCustomersByOwnerId(ownerId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getOwnerId().equals(ownerId));
    }

    @Test
    void getCustomersByType_returnsFilteredList() {
        // Arrange
        String customerType = "ACTIVE";
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Company A");
        customer.setCustomerType("ACTIVE");

        when(customerRepository.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(customer));

        // Act
        List<Customer> result = customerService.getCustomersByType(customerType);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerType()).isEqualTo("ACTIVE");
    }

    @Test
    void convertToActive_changesCustomerTypeToActive() {
        // Arrange
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName("Test Company");
        customer.setCustomerType("PROSPECT");

        when(customerRepository.selectById(customerId)).thenReturn(customer);
        when(customerRepository.updateById(any(Customer.class))).thenReturn(1);

        // Act
        Customer result = customerService.convertToActive(customerId);

        // Assert
        assertThat(result.getCustomerType()).isEqualTo("ACTIVE");
        verify(customerRepository).updateById(any(Customer.class));
    }

    @Test
    void deleteCustomer_callsRemoveById() {
        // Arrange
        Long customerId = 1L;
        when(customerRepository.deleteById(customerId)).thenReturn(1);

        // Act
        customerService.deleteCustomer(customerId);

        // Assert
        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void getAllCustomers_returnsAllCustomers() {
        // Arrange
        Customer c1 = new Customer();
        c1.setId(1L);
        c1.setName("Company A");
        
        Customer c2 = new Customer();
        c2.setId(2L);
        c2.setName("Company B");

        when(customerRepository.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c1, c2));

        // Act
        List<Customer> result = customerService.getAllCustomers();

        // Assert
        assertThat(result).hasSize(2);
    }
}
