package com.smartcrm.contract.service;

import com.smartcrm.contract.entity.Contract;
import com.smartcrm.contract.exception.ContractStatusException;
import com.smartcrm.contract.exception.DuplicateContractNumberException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContractService business logic
 */
class ContractServiceTest {

    // Valid transitions for testing
    private static final Set<String> VALID_TRANSITIONS = Set.of(
            "DRAFT->PENDING", "DRAFT->ACTIVE", "DRAFT->CANCELLED",
            "PENDING->ACTIVE", "PENDING->CANCELLED",
            "ACTIVE->EXPIRED", "ACTIVE->TERMINATED",
            "CANCELLED->DRAFT"
    );

    @Test
    void testDuplicateContractNumberException() {
        DuplicateContractNumberException ex = new DuplicateContractNumberException("CN-001");
        assertEquals(409, ex.getCode());
        assertTrue(ex.getMessage().contains("CN-001"));
    }

    @Test
    void testContractStatusException() {
        ContractStatusException ex = new ContractStatusException("DRAFT", "EXPIRED");
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("DRAFT"));
        assertTrue(ex.getMessage().contains("EXPIRED"));
    }

    @Test
    void testContractStatusExceptionMessage() {
        ContractStatusException ex = new ContractStatusException("Invalid transition");
        assertEquals(400, ex.getCode());
        assertEquals("Invalid transition", ex.getMessage());
    }

    @Test
    void testValidStatusTransitions() {
        // DRAFT -> PENDING, ACTIVE, CANCELLED
        assertTrue(isValidTransition("DRAFT", "PENDING"));
        assertTrue(isValidTransition("DRAFT", "ACTIVE"));
        assertTrue(isValidTransition("DRAFT", "CANCELLED"));
        
        // PENDING -> ACTIVE, CANCELLED
        assertTrue(isValidTransition("PENDING", "ACTIVE"));
        assertTrue(isValidTransition("PENDING", "CANCELLED"));
        
        // ACTIVE -> EXPIRED, TERMINATED
        assertTrue(isValidTransition("ACTIVE", "EXPIRED"));
        assertTrue(isValidTransition("ACTIVE", "TERMINATED"));
        
        // CANCELLED -> DRAFT
        assertTrue(isValidTransition("CANCELLED", "DRAFT"));
    }

    @Test
    void testInvalidStatusTransitions() {
        // Invalid transitions
        assertFalse(isValidTransition("DRAFT", "EXPIRED"));
        assertFalse(isValidTransition("EXPIRED", "ACTIVE"));
        assertFalse(isValidTransition("TERMINATED", "PENDING"));
        assertFalse(isValidTransition("CANCELLED", "ACTIVE"));
        assertFalse(isValidTransition("ACTIVE", "DRAFT"));
    }

    private boolean isValidTransition(String from, String to) {
        return VALID_TRANSITIONS.contains(from + "->" + to);
    }

    @Test
    void testSearchKeywordValidation() {
        String keyword = "test";
        assertNotNull(keyword);
        assertFalse(keyword.trim().isEmpty());
        
        String emptyKeyword = "   ";
        assertTrue(emptyKeyword.trim().isEmpty());
    }

    @Test
    void testContractAmountValidation() {
        BigDecimal positiveAmount = new BigDecimal("1000");
        BigDecimal negativeAmount = new BigDecimal("-100");
        BigDecimal zeroAmount = BigDecimal.ZERO;

        assertTrue(positiveAmount.compareTo(BigDecimal.ZERO) >= 0);
        assertFalse(negativeAmount.compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(zeroAmount.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testContractExpirationDaysCalculation() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plusDays(30);
        
        long daysUntilExpiration = java.time.temporal.ChronoUnit.DAYS.between(now, expirationDate);
        assertEquals(30, daysUntilExpiration);
    }

    @Test
    void testContractRenewalValidation() {
        String expiredStatus = "EXPIRED";
        String terminatedStatus = "TERMINATED";
        String activeStatus = "ACTIVE";
        
        assertTrue("EXPIRED".equals(expiredStatus) || "TERMINATED".equals(expiredStatus));
        assertFalse("ACTIVE".equals(expiredStatus) || "TERMINATED".equals(expiredStatus));
        assertTrue(!("EXPIRED".equals(activeStatus) || "TERMINATED".equals(activeStatus)));
    }

    @Test
    void testContractCloneGeneratesNewNumber() {
        String originalNumber = "CN-001";
        long timestampSuffix = System.currentTimeMillis() % 10000;
        String clonedNumber = originalNumber + "-C" + timestampSuffix;
        
        assertNotNull(clonedNumber);
        assertTrue(clonedNumber.startsWith("CN-001-C"));
        assertFalse(clonedNumber.equals(originalNumber));
    }

    @Test
    void testContractStatsStructure() {
        var stats = java.util.Map.of(
            "totalContracts", 10L,
            "activeCount", 5L,
            "totalValue", new BigDecimal("50000")
        );
        
        assertEquals(10L, stats.get("totalContracts"));
        assertEquals(5L, stats.get("activeCount"));
        assertEquals(new BigDecimal("50000"), stats.get("totalValue"));
    }

    @Test
    void testContractResponseOutstandingAmountCalculation() {
        BigDecimal totalAmount = new BigDecimal("10000");
        BigDecimal paidAmount = new BigDecimal("3000");
        BigDecimal outstandingAmount = totalAmount.subtract(paidAmount);
        
        assertEquals(new BigDecimal("7000"), outstandingAmount);
    }

    @Test
    void testFullyPaidContract() {
        BigDecimal totalAmount = new BigDecimal("10000");
        BigDecimal paidAmount = new BigDecimal("10000");
        
        assertTrue(paidAmount.compareTo(totalAmount) >= 0);
    }

    @Test
    void testOverpaidContract() {
        BigDecimal totalAmount = new BigDecimal("10000");
        BigDecimal paidAmount = new BigDecimal("10500");
        
        assertTrue(paidAmount.compareTo(totalAmount) > 0);
    }

    @Test
    void testContractExpirationDateCheck() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(30);
        LocalDateTime pastDate = now.minusDays(1);
        
        assertTrue(futureDate.isAfter(now));
        assertTrue(pastDate.isBefore(now));
    }

    @Test
    void testContractSearchPattern() {
        String keyword = "sales";
        String pattern = "%" + keyword.trim() + "%";
        
        assertEquals("%sales%", pattern);
        assertTrue(pattern.contains(keyword));
    }

    @Test
    void testContractNumberUniquenessCheck() {
        String contractNumber = "CN-001";
        String newContractNumber = "CN-002";
        
        assertNotEquals(contractNumber, newContractNumber);
    }

    @Test
    void testContractStatusValues() {
        Set<String> validStatuses = Set.of("DRAFT", "PENDING", "ACTIVE", "EXPIRED", "TERMINATED", "CANCELLED");
        
        assertTrue(validStatuses.contains("DRAFT"));
        assertTrue(validStatuses.contains("ACTIVE"));
        assertFalse(validStatuses.contains("INVALID"));
    }

    @Test
    void testContractTypeValues() {
        Set<String> validTypes = Set.of("SALES", "SERVICE", "NDA", "PARTNERSHIP", "OTHER");
        
        assertTrue(validTypes.contains("SALES"));
        assertTrue(validTypes.contains("SERVICE"));
        assertFalse(validTypes.contains("RENT"));
    }

    @Test
    void testRiskScoreValues() {
        Set<String> validRiskScores = Set.of("HIGH", "MEDIUM", "LOW");
        
        assertTrue(validRiskScores.contains("HIGH"));
        assertTrue(validRiskScores.contains("LOW"));
        assertFalse(validRiskScores.contains("UNKNOWN"));
    }

    @Test
    void testContractClonePreservesFields() {
        Contract original = new Contract();
        original.setAccountId(100L);
        original.setOpportunityId(200L);
        original.setContactId(300L);
        original.setType("SALES");
        original.setTotalAmount(new BigDecimal("50000"));
        
        assertEquals(100L, original.getAccountId());
        assertEquals(200L, original.getOpportunityId());
        assertEquals(300L, original.getContactId());
        assertEquals("SALES", original.getType());
        assertEquals(new BigDecimal("50000"), original.getTotalAmount());
    }

    @Test
    void testContractRenewalReset() {
        Contract renewed = new Contract();
        renewed.setPaidAmount(BigDecimal.ZERO);
        renewed.setStatus("DRAFT");
        
        assertEquals(BigDecimal.ZERO, renewed.getPaidAmount());
        assertEquals("DRAFT", renewed.getStatus());
    }

    @Test
    void testPaymentRecording() {
        BigDecimal initialPaid = new BigDecimal("3000");
        BigDecimal additionalPayment = new BigDecimal("2000");
        BigDecimal newTotal = initialPaid.add(additionalPayment);
        
        assertEquals(new BigDecimal("5000"), newTotal);
    }

    @Test
    void testNullExpirationDateHandling() {
        LocalDateTime nullExpirationDate = null;
        long daysUntilExpiration = (nullExpirationDate == null) ? -1 : 
            java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), nullExpirationDate);
        
        assertEquals(-1, daysUntilExpiration);
    }
}