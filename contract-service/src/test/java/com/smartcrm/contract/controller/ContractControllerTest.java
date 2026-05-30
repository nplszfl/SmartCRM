package com.smartcrm.contract.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.contract.dto.ContractRequest;
import com.smartcrm.contract.entity.Contract;
import com.smartcrm.contract.service.ContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContractController
 */
@ExtendWith(MockitoExtension.class)
class ContractControllerTest {

    @Mock
    private ContractService contractService;

    @InjectMocks
    private ContractController contractController;

    private Contract sampleContract;

    @BeforeEach
    void setUp() {
        sampleContract = new Contract();
        sampleContract.setId(1L);
        sampleContract.setContractNumber("CN-001");
        sampleContract.setTitle("Sample Contract");
        sampleContract.setStatus("DRAFT");
        sampleContract.setTotalAmount(new BigDecimal("10000"));
        sampleContract.setPaidAmount(BigDecimal.ZERO);
        sampleContract.setCreatedAt(LocalDateTime.now());
        sampleContract.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateContract() {
        ContractRequest request = new ContractRequest();
        request.setContractNumber("CN-001");
        request.setTitle("Sample Contract");
        request.setTotalAmount(new BigDecimal("10000"));

        when(contractService.createContract(any(ContractRequest.class))).thenReturn(sampleContract);

        ApiResponse<Contract> response = contractController.createContract(request);

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("CN-001", response.getData().getContractNumber());
        verify(contractService, times(1)).createContract(any(ContractRequest.class));
    }

    @Test
    void testGetContract() {
        when(contractService.getContractById(1L)).thenReturn(sampleContract);

        ApiResponse<Contract> response = contractController.getContract(1L);

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getId());
        verify(contractService, times(1)).getContractById(1L);
    }

    @Test
    void testGetAllContracts() {
        List<Contract> contracts = List.of(sampleContract);
        when(contractService.getAllContracts()).thenReturn(contracts);

        ApiResponse<List<Contract>> response = contractController.getAllContracts();

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        verify(contractService, times(1)).getAllContracts();
    }

    @Test
    void testGetContractsByStatus() {
        List<Contract> contracts = List.of(sampleContract);
        when(contractService.getContractsByStatus("DRAFT")).thenReturn(contracts);

        ApiResponse<List<Contract>> response = contractController.getByStatus("DRAFT");

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        verify(contractService, times(1)).getContractsByStatus("DRAFT");
    }

    @Test
    void testActivateContract() {
        Contract activated = new Contract();
        activated.setId(1L);
        activated.setStatus("ACTIVE");

        when(contractService.activateContract(1L)).thenReturn(activated);

        ApiResponse<Contract> response = contractController.activateContract(1L);

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("ACTIVE", response.getData().getStatus());
        verify(contractService, times(1)).activateContract(1L);
    }

    @Test
    void testRecordPayment() {
        BigDecimal amount = new BigDecimal("1000");
        Contract updated = new Contract();
        updated.setId(1L);
        updated.setPaidAmount(amount);
        updated.setStatus("ACTIVE");

        when(contractService.recordPayment(eq(1L), eq(amount))).thenReturn(updated);

        ApiResponse<Contract> response = contractController.recordPayment(1L, Map.of("amount", amount));

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        verify(contractService, times(1)).recordPayment(1L, amount);
    }

    @Test
    void testDeleteContract() {
        doNothing().when(contractService).deleteContract(1L);

        ApiResponse<Void> response = contractController.deleteContract(1L);

        assertEquals(200, response.getCode());
        verify(contractService, times(1)).deleteContract(1L);
    }

    @Test
    void testGetTotalContractValue() {
        BigDecimal totalValue = new BigDecimal("50000");
        when(contractService.getTotalContractValue()).thenReturn(totalValue);

        ApiResponse<BigDecimal> response = contractController.getTotalContractValue();

        assertEquals(200, response.getCode());
        assertEquals(totalValue, response.getData());
        verify(contractService, times(1)).getTotalContractValue();
    }

    @Test
    void testGetTotalOutstandingAmount() {
        BigDecimal outstanding = new BigDecimal("30000");
        when(contractService.getTotalOutstandingAmount()).thenReturn(outstanding);

        ApiResponse<BigDecimal> response = contractController.getTotalOutstandingAmount();

        assertEquals(200, response.getCode());
        assertEquals(outstanding, response.getData());
        verify(contractService, times(1)).getTotalOutstandingAmount();
    }

    @Test
    void testCountByStatus() {
        ApiResponse<Map<String, Long>> response = contractController.countByStatus();

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().containsKey("DRAFT"));
        assertTrue(response.getData().containsKey("ACTIVE"));
        verify(contractService, atLeast(1)).countByStatus(anyString());
    }

    @Test
    void testGetExpiringContracts() {
        List<Contract> contracts = List.of(sampleContract);
        when(contractService.getExpiringContracts(30)).thenReturn(contracts);

        ApiResponse<List<Contract>> response = contractController.getExpiringContracts(30);

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        verify(contractService, times(1)).getExpiringContracts(30);
    }

    @Test
    void testGetOverdueContracts() {
        List<Contract> contracts = List.of(sampleContract);
        when(contractService.getOverdueContracts()).thenReturn(contracts);

        ApiResponse<List<Contract>> response = contractController.getOverdueContracts();

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        verify(contractService, times(1)).getOverdueContracts();
    }

    @Test
    void testGetDaysUntilExpiration() {
        when(contractService.getDaysUntilExpiration(1L)).thenReturn(30L);

        ApiResponse<Long> response = contractController.getDaysUntilExpiration(1L);

        assertEquals(200, response.getCode());
        assertEquals(30L, response.getData());
        verify(contractService, times(1)).getDaysUntilExpiration(1L);
    }

    @Test
    void testUpdateAiRiskScore() {
        Contract updated = new Contract();
        updated.setId(1L);
        updated.setAiRiskScore("HIGH");
        updated.setAiRiskConfidence(0.85);

        when(contractService.updateAiRiskScore(eq(1L), eq("HIGH"), eq(0.85))).thenReturn(updated);

        ApiResponse<Contract> response = contractController.updateAiRiskScore(1L, Map.of(
            "riskScore", "HIGH",
            "confidence", 0.85
        ));

        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("HIGH", response.getData().getAiRiskScore());
        verify(contractService, times(1)).updateAiRiskScore(1L, "HIGH", 0.85);
    }
}