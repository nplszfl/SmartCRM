package com.smartcrm.customer.service;

import com.smartcrm.customer.dto.CustomerScoringRequest;
import com.smartcrm.customer.dto.CustomerScoringResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AiScoringServiceTest {

    private AiScoringService aiScoringService;

    @BeforeEach
    void setUp() {
        aiScoringService = new AiScoringService();
    }

    @Test
    void testAnalyzeCustomer_withValidData() {
        // Given
        CustomerScoringRequest request = new CustomerScoringRequest();
        request.setCustomerId(1L);
        request.setCustomerName("Test Customer");
        request.setTotalRevenue(new BigDecimal("500000"));
        request.setTotalOrders(50);
        request.setActiveOpportunities(5);
        request.setDaysSinceLastContact(10);
        request.setEmailResponseRate(70);
        request.setMeetingCount(3);

        // When
        CustomerScoringResponse result = aiScoringService.analyzeCustomer(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getCustomerId());
        assertEquals("Test Customer", result.getCustomerName());
        assertNotNull(result.getValueLevel());
        assertTrue(result.getValueLevel() >= 1 && result.getValueLevel() <= 5);
        assertNotNull(result.getChurnRisk());
    }

    @Test
    void testAnalyzeCustomer_highRevenueCustomer() {
        // Given
        CustomerScoringRequest request = new CustomerScoringRequest();
        request.setCustomerId(1L);
        request.setCustomerName("High Revenue Customer");
        request.setTotalRevenue(new BigDecimal("2000000"));
        request.setTotalOrders(100);
        request.setActiveOpportunities(10);
        request.setDaysSinceLastContact(5);
        request.setEmailResponseRate(90);
        request.setMeetingCount(8);

        // When
        CustomerScoringResponse result = aiScoringService.analyzeCustomer(request);

        // Then
        assertNotNull(result);
        assertTrue(result.getValueLevel() >= 4, "High revenue customer should be LV4 or LV5");
        assertTrue(result.getChurnRisk().equals("LOW") || result.getChurnRisk().equals("MEDIUM"));
    }

    @Test
    void testAnalyzeCustomer_highChurnRisk() {
        // Given
        CustomerScoringRequest request = new CustomerScoringRequest();
        request.setCustomerId(1L);
        request.setCustomerName("Inactive Customer");
        request.setTotalRevenue(new BigDecimal("50000"));
        request.setTotalOrders(5);
        request.setActiveOpportunities(1);
        request.setDaysSinceLastContact(90);
        request.setEmailResponseRate(10);
        request.setMeetingCount(0);

        // When
        CustomerScoringResponse result = aiScoringService.analyzeCustomer(request);

        // Then
        assertNotNull(result);
        assertEquals("HIGH", result.getChurnRisk());
    }

    @Test
    void testAnalyzeCustomer_mediumEngagement() {
        // Given
        CustomerScoringRequest request = new CustomerScoringRequest();
        request.setCustomerId(1L);
        request.setCustomerName("Medium Engagement Customer");
        request.setTotalRevenue(new BigDecimal("150000"));
        request.setTotalOrders(20);
        request.setActiveOpportunities(2);
        request.setDaysSinceLastContact(45);
        request.setEmailResponseRate(50);
        request.setMeetingCount(2);

        // When
        CustomerScoringResponse result = aiScoringService.analyzeCustomer(request);

        // Then
        assertNotNull(result);
        assertTrue(result.getValueLevel() >= 2 && result.getValueLevel() <= 4);
        assertEquals("MEDIUM", result.getChurnRisk());
    }
}