package com.smartcrm.crm.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.crm.dto.AccountRequest;
import com.smartcrm.crm.entity.Account;
import com.smartcrm.crm.service.AccountServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Account REST controller.
 */
@Slf4j
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountServiceImpl accountService;

    public AccountController(AccountServiceImpl accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ApiResponse<Account> createAccount(@RequestBody AccountRequest request) {
        log.info("REST request to create account: {}", request.getAccountNumber());
        Account created = accountService.createAccount(request);
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}")
    public ApiResponse<Account> updateAccount(@PathVariable Long id, @RequestBody AccountRequest request) {
        log.info("REST request to update account: {}", id);
        Account updated = accountService.updateAccount(id, request);
        return ApiResponse.success(updated);
    }

    @GetMapping("/{id}")
    public ApiResponse<Account> getAccount(@PathVariable Long id) {
        log.info("REST request to get account: {}", id);
        Account account = accountService.getAccountById(id);
        return ApiResponse.success(account);
    }

    @GetMapping("/number/{accountNumber}")
    public ApiResponse<Account> getAccountByNumber(@PathVariable String accountNumber) {
        log.info("REST request to get account by number: {}", accountNumber);
        Account account = accountService.getAccountByAccountNumber(accountNumber);
        return ApiResponse.success(account);
    }

    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<Account>> getAccountsByCustomer(@PathVariable Long customerId) {
        log.info("REST request to get accounts for customer: {}", customerId);
        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);
        return ApiResponse.success(accounts);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAccount(@PathVariable Long id) {
        log.info("REST request to delete account: {}", id);
        accountService.deleteAccount(id);
        return ApiResponse.success("Account deleted successfully", null);
    }

    @PatchMapping("/{id}/suspend")
    public ApiResponse<Account> suspendAccount(@PathVariable Long id) {
        log.info("REST request to suspend account: {}", id);
        Account account = accountService.suspendAccount(id);
        return ApiResponse.success(account);
    }

    @GetMapping
    public ApiResponse<List<Account>> getAllAccounts() {
        log.info("REST request to get all accounts");
        List<Account> accounts = accountService.getAllAccounts();
        return ApiResponse.success(accounts);
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<Account>> getAccountsByStatus(@PathVariable String status) {
        log.info("REST request to get accounts by status: {}", status);
        List<Account> accounts = accountService.getAccountsByStatus(status);
        return ApiResponse.success(accounts);
    }

    @GetMapping("/search")
    public ApiResponse<List<Account>> searchAccounts(@RequestParam String name) {
        log.info("REST request to search accounts by name: {}", name);
        List<Account> accounts = accountService.searchAccountsByName(name);
        return ApiResponse.success(accounts);
    }

    @PatchMapping("/{id}/activate")
    public ApiResponse<Account> activateAccount(@PathVariable Long id) {
        log.info("REST request to activate account: {}", id);
        Account account = accountService.activateAccount(id);
        return ApiResponse.success(account);
    }

    @PatchMapping("/{id}/close")
    public ApiResponse<Account> closeAccount(@PathVariable Long id) {
        log.info("REST request to close account: {}", id);
        Account account = accountService.closeAccount(id);
        return ApiResponse.success(account);
    }
}