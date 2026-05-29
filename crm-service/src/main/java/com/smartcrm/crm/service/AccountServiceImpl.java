package com.smartcrm.crm.service;

import com.smartcrm.crm.dto.AccountRequest;
import com.smartcrm.crm.entity.Account;
import com.smartcrm.crm.repository.AccountRepository;
import com.smartcrm.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * Account service implementation with DTO-based request handling.
 */
@Slf4j
@Service
public class AccountServiceImpl extends ServiceImpl<AccountRepository, Account> {

    public Account createAccount(AccountRequest request) {
        log.info("Creating account: {} for customer: {}", request.getAccountNumber(), request.getCustomerId());
        
        Account account = new Account();
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountName(request.getAccountName());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getBalance());
        account.setCreditLimit(request.getCreditLimit());
        account.setCurrency(request.getCurrency());
        account.setPaymentTerms(request.getPaymentTerms());
        account.setBillingAddress(request.getBillingAddress());
        account.setShippingAddress(request.getShippingAddress());
        account.setCustomerId(request.getCustomerId());
        account.setOwnerId(request.getOwnerId());
        account.setStatus("ACTIVE");
        
        this.save(account);
        log.info("Account created with ID: {}", account.getId());
        return account;
    }

    public Account updateAccount(Long id, AccountRequest request) {
        Account existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Account", id);
        }
        
        if (request.getAccountName() != null) existing.setAccountName(request.getAccountName());
        if (request.getAccountType() != null) existing.setAccountType(request.getAccountType());
        if (request.getBalance() != null) existing.setBalance(request.getBalance());
        if (request.getCreditLimit() != null) existing.setCreditLimit(request.getCreditLimit());
        if (request.getCurrency() != null) existing.setCurrency(request.getCurrency());
        if (request.getPaymentTerms() != null) existing.setPaymentTerms(request.getPaymentTerms());
        if (request.getBillingAddress() != null) existing.setBillingAddress(request.getBillingAddress());
        if (request.getShippingAddress() != null) existing.setShippingAddress(request.getShippingAddress());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());
        if (request.getOwnerId() != null) existing.setOwnerId(request.getOwnerId());
        
        this.updateById(existing);
        log.info("Account {} updated", id);
        return this.getById(id);
    }

    public Account getAccountById(Long id) {
        Account account = this.getById(id);
        if (account == null) {
            throw new ResourceNotFoundException("Account", id);
        }
        return account;
    }

    public Account getAccountByAccountNumber(String accountNumber) {
        return this.getOne(new LambdaQueryWrapper<Account>().eq(Account::getAccountNumber, accountNumber));
    }

    public List<Account> getAccountsByCustomerId(Long customerId) {
        return this.list(new LambdaQueryWrapper<Account>().eq(Account::getCustomerId, customerId));
    }

    public void deleteAccount(Long id) {
        baseMapper.deleteById(id);
    }

    public Account suspendAccount(Long id) {
        Account account = this.getById(id);
        if (account == null) {
            throw new ResourceNotFoundException("Account", id);
        }
        account.setStatus("SUSPENDED");
        this.updateById(account);
        return account;
    }

    public List<Account> getAllAccounts() {
        return this.list();
    }

    public List<Account> getAccountsByStatus(String status) {
        return this.list(new LambdaQueryWrapper<Account>().eq(Account::getStatus, status));
    }

    public long countByStatus(String status) {
        return this.count(new LambdaQueryWrapper<Account>().eq(Account::getStatus, status));
    }

    public long countAll() {
        return this.count();
    }

    public Account activateAccount(Long id) {
        Account account = this.getById(id);
        if (account == null) {
            throw new ResourceNotFoundException("Account", id);
        }
        account.setStatus("ACTIVE");
        this.updateById(account);
        return account;
    }

    public Account closeAccount(Long id) {
        Account account = this.getById(id);
        if (account == null) {
            throw new ResourceNotFoundException("Account", id);
        }
        account.setStatus("CLOSED");
        this.updateById(account);
        return account;
    }

    public List<Account> searchAccountsByName(String name) {
        return this.list(new LambdaQueryWrapper<Account>().like(Account::getAccountName, name));
    }
}