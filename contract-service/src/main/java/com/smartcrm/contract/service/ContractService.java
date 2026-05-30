package com.smartcrm.contract.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.contract.dto.ContractRequest;
import com.smartcrm.contract.dto.ContractResponse;
import com.smartcrm.contract.entity.Contract;
import com.smartcrm.contract.exception.ContractStatusException;
import com.smartcrm.contract.exception.DuplicateContractNumberException;
import com.smartcrm.contract.repository.ContractRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contract service - manages sales contracts
 */
@Slf4j
@Service
public class ContractService extends ServiceImpl<ContractRepository, Contract> {

    private static final Set<String> VALID_TRANSITIONS = Set.of(
            "DRAFT->PENDING", "DRAFT->ACTIVE", "DRAFT->CANCELLED",
            "PENDING->ACTIVE", "PENDING->CANCELLED",
            "ACTIVE->EXPIRED", "ACTIVE->TERMINATED",
            "CANCELLED->DRAFT"
    );

    public Contract createContract(ContractRequest request) {
        log.info("Creating contract: {}", request.getContractNumber());

        // Check for duplicate contract number
        Long existingCount = this.count(new LambdaQueryWrapper<Contract>()
                .eq(Contract::getContractNumber, request.getContractNumber()));
        if (existingCount > 0) {
            throw new DuplicateContractNumberException(request.getContractNumber());
        }

        // Validate required fields
        validateRequiredFields(request);

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

    private void validateRequiredFields(ContractRequest request) {
        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount must be non-negative");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Contract title is required");
        }
        if (request.getContractNumber() == null || request.getContractNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Contract number is required");
        }
    }

    public Contract updateContract(Long id, ContractRequest request) {
        Contract existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Contract", id);
        }

        // Check for duplicate contract number (excluding current contract)
        if (request.getContractNumber() != null && !request.getContractNumber().equals(existing.getContractNumber())) {
            Long count = this.count(new LambdaQueryWrapper<Contract>()
                    .eq(Contract::getContractNumber, request.getContractNumber())
                    .ne(Contract::getId, id));
            if (count > 0) {
                throw new DuplicateContractNumberException(request.getContractNumber());
            }
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

    /**
     * Convert entity to response DTO
     */
    public ContractResponse toResponse(Contract contract) {
        ContractResponse response = new ContractResponse();
        response.setId(contract.getId());
        response.setContractNumber(contract.getContractNumber());
        response.setTitle(contract.getTitle());
        response.setDescription(contract.getDescription());
        response.setAccountId(contract.getAccountId());
        response.setOpportunityId(contract.getOpportunityId());
        response.setContactId(contract.getContactId());
        response.setStatus(contract.getStatus());
        response.setType(contract.getType());
        response.setTotalAmount(contract.getTotalAmount());
        response.setPaidAmount(contract.getPaidAmount());
        response.setTaxAmount(contract.getTaxAmount());
        response.setPaymentTerms(contract.getPaymentTerms());
        response.setPaymentDueDate(contract.getPaymentDueDate());
        response.setEffectiveDate(contract.getEffectiveDate());
        response.setExpirationDate(contract.getExpirationDate());
        response.setSignedDate(contract.getSignedDate());
        response.setOwnerId(contract.getOwnerId());
        response.setSignatureData(contract.getSignatureData());
        response.setAiRiskScore(contract.getAiRiskScore());
        response.setAiRiskConfidence(contract.getAiRiskConfidence());
        response.setCreatedAt(contract.getCreatedAt());
        response.setUpdatedAt(contract.getUpdatedAt());
        // Calculate outstanding amount
        if (contract.getTotalAmount() != null && contract.getPaidAmount() != null) {
            response.setOutstandingAmount(contract.getTotalAmount().subtract(contract.getPaidAmount()));
        }
        return response;
    }

    /**
     * Validate status transition
     */
    private void validateStatusTransition(String fromStatus, String toStatus) {
        String transition = fromStatus + "->" + toStatus;
        if (!VALID_TRANSITIONS.contains(transition)) {
            throw new ContractStatusException(fromStatus, toStatus);
        }
    }

    /**
     * Renew an expired or terminated contract - creates a new contract based on the old one
     */
    public Contract renewContract(Long id, LocalDateTime newExpirationDate) {
        Contract existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Contract", id);
        }

        // Only allow renewal from EXPIRED or TERMINATED status
        if (!"EXPIRED".equals(existing.getStatus()) && !"TERMINATED".equals(existing.getStatus())) {
            throw new ContractStatusException(existing.getStatus(), "DRAFT");
        }

        // Create a new contract based on the existing one
        Contract renewed = new Contract();
        renewed.setContractNumber(existing.getContractNumber() + "-R" + System.currentTimeMillis() % 10000);
        renewed.setTitle(existing.getTitle() + " (Renewed)");
        renewed.setDescription(existing.getDescription());
        renewed.setAccountId(existing.getAccountId());
        renewed.setOpportunityId(existing.getOpportunityId());
        renewed.setContactId(existing.getContactId());
        renewed.setStatus("DRAFT");
        renewed.setType(existing.getType());
        renewed.setTotalAmount(existing.getTotalAmount());
        renewed.setPaidAmount(BigDecimal.ZERO);
        renewed.setTaxAmount(existing.getTaxAmount());
        renewed.setPaymentTerms(existing.getPaymentTerms());
        renewed.setEffectiveDate(LocalDateTime.now());
        renewed.setExpirationDate(newExpirationDate);
        renewed.setOwnerId(existing.getOwnerId());
        renewed.setCreatedAt(LocalDateTime.now());
        renewed.setUpdatedAt(LocalDateTime.now());

        this.save(renewed);
        log.info("Contract {} renewed as new contract {}", id, renewed.getId());
        return renewed;
    }

    /**
     * Clone an existing contract as a new draft
     */
    public Contract cloneContract(Long id) {
        Contract existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Contract", id);
        }

        Contract cloned = new Contract();
        cloned.setContractNumber(existing.getContractNumber() + "-C" + System.currentTimeMillis() % 10000);
        cloned.setTitle(existing.getTitle() + " (Copy)");
        cloned.setDescription(existing.getDescription());
        cloned.setAccountId(existing.getAccountId());
        cloned.setOpportunityId(existing.getOpportunityId());
        cloned.setContactId(existing.getContactId());
        cloned.setStatus("DRAFT");
        cloned.setType(existing.getType());
        cloned.setTotalAmount(existing.getTotalAmount());
        cloned.setPaidAmount(BigDecimal.ZERO);
        cloned.setTaxAmount(existing.getTaxAmount());
        cloned.setPaymentTerms(existing.getPaymentTerms());
        cloned.setEffectiveDate(null);
        cloned.setExpirationDate(null);
        cloned.setOwnerId(existing.getOwnerId());
        cloned.setCreatedAt(LocalDateTime.now());
        cloned.setUpdatedAt(LocalDateTime.now());

        this.save(cloned);
        log.info("Contract {} cloned to new contract {}", id, cloned.getId());
        return cloned;
    }

    /**
     * Get contract statistics
     */
    public Map<String, Object> getContractStats() {
        long total = this.count();
        long draftCount = this.count(new LambdaQueryWrapper<Contract>().eq(Contract::getStatus, "DRAFT"));
        long pendingCount = this.count(new LambdaQueryWrapper<Contract>().eq(Contract::getStatus, "PENDING"));
        long activeCount = this.count(new LambdaQueryWrapper<Contract>().eq(Contract::getStatus, "ACTIVE"));
        long expiredCount = this.count(new LambdaQueryWrapper<Contract>().eq(Contract::getStatus, "EXPIRED"));
        long terminatedCount = this.count(new LambdaQueryWrapper<Contract>().eq(Contract::getStatus, "TERMINATED"));
        long cancelledCount = this.count(new LambdaQueryWrapper<Contract>().eq(Contract::getStatus, "CANCELLED"));

        BigDecimal totalValue = this.getTotalContractValue();
        BigDecimal outstandingValue = this.getTotalOutstandingAmount();

        return Map.of(
                "totalContracts", total,
                "draftCount", draftCount,
                "pendingCount", pendingCount,
                "activeCount", activeCount,
                "expiredCount", expiredCount,
                "terminatedCount", terminatedCount,
                "cancelledCount", cancelledCount,
                "totalValue", totalValue,
                "outstandingValue", outstandingValue
        );
    }

    /**
     * Search contracts by keyword in title or description
     */
    public List<Contract> searchContracts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        String pattern = "%" + keyword.trim() + "%";
        return this.list(new LambdaQueryWrapper<Contract>()
                .like(Contract::getTitle, pattern)
                .or()
                .like(Contract::getDescription, pattern));
    }
}
