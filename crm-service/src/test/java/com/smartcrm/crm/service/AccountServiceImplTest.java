package com.smartcrm.crm.service;

import com.smartcrm.crm.entity.Account;
import com.smartcrm.crm.repository.AccountRepository;
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
 * Unit tests for AccountServiceImpl.
 * Tests account management CRUD operations and business logic.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl();
        try {
            var field = ServiceImpl.class.getDeclaredField("baseMapper");
            field.setAccessible(true);
            field.set(accountService, accountRepository);
        } catch (Exception e) {
            // Fallback - the service uses this.baseMapper internally
        }
    }

    @Test
    void createAccount_withValidData_setsStatusToActiveAndSaves() {
        // Arrange
        Account account = new Account();
        account.setAccountNumber("ACC-001");
        account.setCustomerId(1L);
        account.setAccountName("Test Account");

        when(accountRepository.insert(any(Account.class))).thenReturn(1);
        when(accountRepository.selectById(any())).thenReturn(account);

        // Act
        Account result = accountService.createAccount(account);

        // Assert
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(accountRepository).insert(any(Account.class));
    }

    @Test
    void getAccountById_whenExists_returnsAccount() {
        // Arrange
        Long accountId = 1L;
        Account account = new Account();
        account.setId(accountId);
        account.setAccountNumber("ACC-001");
        account.setAccountName("Test Account");
        account.setStatus("ACTIVE");

        when(accountRepository.selectById(accountId)).thenReturn(account);

        // Act
        Account result = accountService.getAccountById(accountId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo("ACC-001");
    }

    @Test
    void getAccountById_whenNotExists_throwsResourceNotFoundException() {
        // Arrange
        Long accountId = 999L;
        when(accountRepository.selectById(accountId)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> accountService.getAccountById(accountId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account");
    }

    @Test
    void getAccountByAccountNumber_returnsMatchingAccount() {
        // Arrange
        String accountNumber = "ACC-001";
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber(accountNumber);
        account.setAccountName("Test Account");

        when(accountRepository.selectOne(any(LambdaQueryWrapper.class))).thenReturn(account);

        // Act
        Account result = accountService.getAccountByAccountNumber(accountNumber);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAccountNumber()).isEqualTo(accountNumber);
    }

    @Test
    void getAccountsByCustomerId_returnsFilteredList() {
        // Arrange
        Long customerId = 1L;
        Account a1 = new Account();
        a1.setId(1L);
        a1.setAccountNumber("ACC-001");
        a1.setCustomerId(customerId);
        
        Account a2 = new Account();
        a2.setId(2L);
        a2.setAccountNumber("ACC-002");
        a2.setCustomerId(customerId);

        when(accountRepository.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(a1, a2));

        // Act
        List<Account> result = accountService.getAccountsByCustomerId(customerId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getCustomerId().equals(customerId));
    }

    @Test
    void suspendAccount_whenAccountExists_setsStatusToSuspended() {
        // Arrange
        Long accountId = 1L;
        Account account = new Account();
        account.setId(accountId);
        account.setAccountNumber("ACC-001");
        account.setStatus("ACTIVE");

        when(accountRepository.selectById(accountId)).thenReturn(account);
        when(accountRepository.updateById(any(Account.class))).thenReturn(1);

        // Act
        Account result = accountService.suspendAccount(accountId);

        // Assert
        assertThat(result.getStatus()).isEqualTo("SUSPENDED");
        verify(accountRepository).updateById(any(Account.class));
    }

    @Test
    void suspendAccount_whenAccountNotExists_returnsNull() {
        // Arrange
        Long accountId = 999L;
        when(accountRepository.selectById(accountId)).thenReturn(null);

        // Act
        Account result = accountService.suspendAccount(accountId);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void deleteAccount_callsRemoveById() {
        // Arrange
        Long accountId = 1L;
        when(accountRepository.deleteById(accountId)).thenReturn(1);

        // Act
        accountService.deleteAccount(accountId);

        // Assert
        verify(accountRepository).deleteById(accountId);
    }
}
