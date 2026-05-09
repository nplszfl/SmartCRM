package com.smartcrm.crm.service;

import com.smartcrm.crm.entity.Account;
import com.smartcrm.crm.repository.AccountRepository;
import com.smartcrm.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * Account service implementation.
 */
@Slf4j
@Service
public class AccountServiceImpl extends ServiceImpl<AccountRepository, Account> {

    public Account createAccount(Account account) {
        log.info("Creating account: {} for customer: {}", account.getAccountNumber(), account.getCustomerId());
        account.setStatus("ACTIVE");
        this.save(account);
        return account;
    }

    public Account updateAccount(Long id, Account account) {
        Account existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Account", id);
        }
        account.setId(id);
        this.updateById(account);
        return this.getById(id);
    }

    public Account getAccountById(Long id) {
        return this.getById(id);
    }

    public Account getAccountByAccountNumber(String accountNumber) {
        return this.getOne(new LambdaQueryWrapper<Account>().eq(Account::getAccountNumber, accountNumber));
    }

    public List<Account> getAccountsByCustomerId(Long customerId) {
        return this.list(new LambdaQueryWrapper<Account>().eq(Account::getCustomerId, customerId));
    }

    public void deleteAccount(Long id) {
        this.removeById(id);
    }

    public Account suspendAccount(Long id) {
        Account account = this.getById(id);
        if (account != null) {
            account.setStatus("SUSPENDED");
            this.updateById(account);
        }
        return account;
    }
}