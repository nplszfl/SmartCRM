package com.smartcrm.contract.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.contract.dto.ContractRequest;
import com.smartcrm.contract.entity.Contract;
import com.smartcrm.contract.repository.ContractRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Contract service - manages sales contracts
 */
@Slf4j
@Service
public class ContractService extends ServiceImpl<ContractRepository, Contract> {

    public Contract createContract(ContractRequest request) {
        log.info("Creating contract: {}", request.getContractNumber());

        Contract contract = new Contract();
        contract.setContractNumber(request.getContractNumber());
        contract.setTitle(request.getTitle());
        contract.setDescription(request.getDescription());
        contract.setAccountId(request.getAccountId());
        contract.setOpportunityId(request.getOpportunityId());
        contract.setContactId(request.getContactId());
        contract.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");
        contract.setType(request.getType() != null ? request.getType() : "SALES");
        contract.setTotalAmount(request.getTotalAmount());
        contract.setPaidAmount(request.getPaidAmount() != null ? request.getPaidAmount() : BigDecimal.ZERO);
        contract.setTaxAmount(request.getTaxAmount());
        contract.setPaymentTerms(request.getPaymentTerms());
        if (request.getPaymentDueDate() != null) {
            contract.setPaymentDueDate(LocalDateTime.parse(request.getPaymentDueDate() + "T00:00:00"));
        }
        if (request.getEffectiveDate() != null) {
            contract.setEffectiveDate(LocalDateTime.parse(request.getEffectiveDate() + "T00:00:00"));
        }
        if (request.getExpirationDate() != null) {
            contract.setExpirationDate(LocalDateTime.parse(request.getExpirationDate() + "T00:00:00"));
        }
        contract.setOwnerId(request.getOwnerId());
        contract.setCreatedAt(LocalDateTime.now());
        contract.setUpdatedAt(LocalDateTime.now());

        this.save(contract);
        log.info("Contract created with ID: {}", contract.getId());
        return contract;
    }

    public Contract updateContract(Long id, ContractRequest request) {
        Contract existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Contract", id);
        }

        existing.setContractNumber(request.getContractNumber());
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setAccountId(request.getAccountId());
        existing.setOpportunityId(request.getOpportunityId());
        existing.setContactId(request.getContactId());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setType(request.getType());
        existing.setTotalAmount(request.getTotalAmount());
        existing.setPaidAmount(request.getPaidAmount());
        existing.setTaxAmount(request.getTaxAmount());
        existing.setPaymentTerms(request.getPaymentTerms());
        if (request.getPaymentDueDate() != null) {
            existing.setPaymentDueDate(LocalDateTime.parse(request.getPaymentDueDate() + "T00:00:00"));
        }
        if (request.getEffectiveDate() != null) {
            existing.setEffectiveDate(LocalDateTime.parse(request.getEffectiveDate() + "T00:00:00"));
        }
        if (request.getExpirationDate() != null) {
            existing.setExpirationDate(LocalDateTime.parse(request.getExpirationDate() + "T00:00:00"));
        }
        existing.setOwnerId(request.getOwnerId());
        existing.setUpdatedAt(LocalDateTime.now());

        this.updateById(existing);
        return existing;
    }

    public Contract getContractById(Long id) {
        Contract contract = this.getById(id);
        if (contract == null) {
            throw new ResourceNotFoundException("Contract", id);
        }
        return contract;
    }

    public List<Contract> getAllContracts() {
        return this.list();
    }

    public List<Contract> getContractsByStatus(String status) {
        return this.list(new LambdaQueryWrapper<Contract>().eq(Contract::getStatus, status));
    }

    public List<Contract> getContractsByOwner(Long ownerId) {
        return this.list(new LambdaQueryWrapper<Contract>().eq(Contract::getOwnerId, ownerId));
    }

    public List<Contract> getContractsByAccount(Long accountId) {
        return this.list(new LambdaQueryWrapper<Contract>().eq(Contract::getAccountId, accountId));
    }

    public List<Contract> getContractsByOpportunity(Long opportunityId) {
        return this.list(new LambdaQueryWrapper<Contract>().eq(Contract::getOpportunityId, opportunityId));
    }

    public Contract activateContract(Long id) {
        Contract contract = this.getById(id);
        if (contract == null) {
            throw new ResourceNotFoundException("Contract", id);
        }
        contract.setStatus("ACTIVE");
        contract.setEffectiveDate(LocalDateTime.now());
        contract.setUpdatedAt(LocalDateTime.now());
        this.updateById(contract);
        log.info("Contract {} activated", id);
        return contract;
    }

    public Contract expireContract(Long id) {
        Contract contract = this.getById(id);
        if (contract == null) {
            throw new ResourceNotFoundException("Contract", id);
        }
        contract.setStatus("EXPIRED");
        contract.setUpdatedAt(LocalDateTime.now());
        this.updateById(contract);
        log.info("Contract {} expired", id);
        return contract;
    }

    public Contract terminateContract(Long id) {
        Contract contract = this.getById(id);
        if (contract == null) {
            throw new ResourceNotFoundException("Contract", id);
        }
        contract.setStatus("TERMINATED");
        contract.setUpdatedAt(LocalDateTime.now());
        this.updateById(contract);
        log.info("Contract {} terminated", id);
        return contract;
    }

    public Contract recordPayment(Long id, BigDecimal amount) {
        Contract contract = this.getById(id);
        if (contract == null) {
            throw new ResourceNotFoundException("Contract", id);
        }
        BigDecimal newPaid = contract.getPaidAmount().add(amount);
        contract.setPaidAmount(newPaid);
        
        // Auto-update status if fully paid
        if (newPaid.compareTo(contract.getTotalAmount()) >= 0) {
            contract.setStatus("ACTIVE");
        }
        contract.setUpdatedAt(LocalDateTime.now());
        this.updateById(contract);
        log.info("Payment recorded for contract {}: {} (total paid: {})", id, amount, newPaid);
        return contract;
    }

    public Contract updateAiRiskScore(Long id, String riskScore, Double confidence) {
        Contract contract = this.getById(id);
        if (contract == null) {
            throw new ResourceNotFoundException("Contract", id);
        }
        contract.setAiRiskScore(riskScore);
        contract.setAiRiskConfidence(confidence);
        contract.setUpdatedAt(LocalDateTime.now());
        this.updateById(contract);
        return contract;
    }

    public void deleteContract(Long id) {
        this.removeById(id);
    }

    public BigDecimal getTotalContractValue() {
        List<Contract> activeContracts = this.list(new LambdaQueryWrapper<Contract>()
                .in(Contract::getStatus, "ACTIVE", "PENDING"));
        return activeContracts.stream()
                .map(Contract::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalOutstandingAmount() {
        List<Contract> activeContracts = this.list(new LambdaQueryWrapper<Contract>()
                .in(Contract::getStatus, "ACTIVE", "PENDING"));
        return activeContracts.stream()
                .map(contract -> contract.getTotalAmount().subtract(contract.getPaidAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long countByStatus(String status) {
        return this.count(new LambdaQueryWrapper<Contract>().eq(Contract::getStatus, status));
    }

    public List<Contract> getExpiringContracts(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(daysThreshold);
        return this.list(new LambdaQueryWrapper<Contract>()
                .eq(Contract::getStatus, "ACTIVE")
                .lt(Contract::getExpirationDate, threshold)
                .gt(Contract::getExpirationDate, LocalDateTime.now()));
    }

    public List<Contract> getOverdueContracts() {
        return this.list(new LambdaQueryWrapper<Contract>()
                .in(Contract::getStatus, "ACTIVE", "PENDING")
                .lt(Contract::getPaymentDueDate, LocalDateTime.now()));
    }

    public long getDaysUntilExpiration(Long id) {
        Contract contract = this.getById(id);
        if (contract == null) {
            throw new ResourceNotFoundException("Contract", id);
        }
        if (contract.getExpirationDate() == null) {
            return -1;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), contract.getExpirationDate());
    }
}
