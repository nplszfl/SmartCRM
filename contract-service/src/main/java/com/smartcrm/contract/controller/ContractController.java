package com.smartcrm.contract.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.contract.dto.ContractRequest;
import com.smartcrm.contract.entity.Contract;
import com.smartcrm.contract.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Contract REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ApiResponse<Contract> createContract(@Valid @RequestBody ContractRequest request) {
        Contract contract = contractService.createContract(request);
        return ApiResponse.success(contract);
    }

    @PutMapping("/{id}")
    public ApiResponse<Contract> updateContract(@PathVariable Long id, @Valid @RequestBody ContractRequest request) {
        Contract contract = contractService.updateContract(id, request);
        return ApiResponse.success(contract);
    }

    @GetMapping("/{id}")
    public ApiResponse<Contract> getContract(@PathVariable Long id) {
        Contract contract = contractService.getContractById(id);
        return ApiResponse.success(contract);
    }

    @GetMapping
    public ApiResponse<List<Contract>> getAllContracts() {
        List<Contract> contracts = contractService.getAllContracts();
        return ApiResponse.success(contracts);
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<Contract>> getByStatus(@PathVariable String status) {
        List<Contract> contracts = contractService.getContractsByStatus(status);
        return ApiResponse.success(contracts);
    }

    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Contract>> getByOwner(@PathVariable Long ownerId) {
        List<Contract> contracts = contractService.getContractsByOwner(ownerId);
        return ApiResponse.success(contracts);
    }

    @GetMapping("/account/{accountId}")
    public ApiResponse<List<Contract>> getByAccount(@PathVariable Long accountId) {
        List<Contract> contracts = contractService.getContractsByAccount(accountId);
        return ApiResponse.success(contracts);
    }

    @GetMapping("/opportunity/{opportunityId}")
    public ApiResponse<List<Contract>> getByOpportunity(@PathVariable Long opportunityId) {
        List<Contract> contracts = contractService.getContractsByOpportunity(opportunityId);
        return ApiResponse.success(contracts);
    }

    @PostMapping("/{id}/activate")
    public ApiResponse<Contract> activateContract(@PathVariable Long id) {
        Contract contract = contractService.activateContract(id);
        return ApiResponse.success(contract);
    }

    @PostMapping("/{id}/expire")
    public ApiResponse<Contract> expireContract(@PathVariable Long id) {
        Contract contract = contractService.expireContract(id);
        return ApiResponse.success(contract);
    }

    @PostMapping("/{id}/terminate")
    public ApiResponse<Contract> terminateContract(@PathVariable Long id) {
        Contract contract = contractService.terminateContract(id);
        return ApiResponse.success(contract);
    }

    @PostMapping("/{id}/payment")
    public ApiResponse<Contract> recordPayment(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.get("amount");
        Contract contract = contractService.recordPayment(id, amount);
        return ApiResponse.success(contract);
    }

    @PatchMapping("/{id}/ai-risk")
    public ApiResponse<Contract> updateAiRiskScore(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String riskScore = (String) body.get("riskScore");
        Double confidence = ((Number) body.get("confidence")).doubleValue();
        Contract contract = contractService.updateAiRiskScore(id, riskScore, confidence);
        return ApiResponse.success(contract);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ApiResponse.success();
    }

    @GetMapping("/stats/total-value")
    public ApiResponse<BigDecimal> getTotalContractValue() {
        return ApiResponse.success(contractService.getTotalContractValue());
    }

    @GetMapping("/stats/outstanding-amount")
    public ApiResponse<BigDecimal> getTotalOutstandingAmount() {
        return ApiResponse.success(contractService.getTotalOutstandingAmount());
    }

    @GetMapping("/stats/count-by-status")
    public ApiResponse<Map<String, Long>> countByStatus() {
        return ApiResponse.success(Map.of(
                "DRAFT", contractService.countByStatus("DRAFT"),
                "PENDING", contractService.countByStatus("PENDING"),
                "ACTIVE", contractService.countByStatus("ACTIVE"),
                "EXPIRED", contractService.countByStatus("EXPIRED"),
                "TERMINATED", contractService.countByStatus("TERMINATED"),
                "CANCELLED", contractService.countByStatus("CANCELLED")
        ));
    }

    @GetMapping("/expiring/{daysThreshold}")
    public ApiResponse<List<Contract>> getExpiringContracts(@PathVariable int daysThreshold) {
        return ApiResponse.success(contractService.getExpiringContracts(daysThreshold));
    }

    @GetMapping("/overdue")
    public ApiResponse<List<Contract>> getOverdueContracts() {
        return ApiResponse.success(contractService.getOverdueContracts());
    }

    @GetMapping("/{id}/days-until-expiration")
    public ApiResponse<Long> getDaysUntilExpiration(@PathVariable Long id) {
        return ApiResponse.success(contractService.getDaysUntilExpiration(id));
    }
}
