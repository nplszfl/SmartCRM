package com.smartcrm.customer.service;

import com.smartcrm.customer.dto.CustomerScoringRequest;
import com.smartcrm.customer.dto.CustomerScoringResponse;
import com.smartcrm.customer.entity.CustomerScore;
import com.smartcrm.customer.mapper.CustomerScoreMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerScoringServiceTest {

    @Mock
    private CustomerScoreMapper customerScoreMapper;

    @Mock
    private AiScoringService aiScoringService;

    @InjectMocks
    private CustomerScoringService customerScoringService;

    private CustomerScoringRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new CustomerScoringRequest();
        sampleRequest.setCustomerId(1L);
        sampleRequest.setCustomerName("Test Customer");
        sampleRequest.setTotalRevenue(new BigDecimal("500000"));
        sampleRequest.setTotalOrders(50);
        sampleRequest.setActiveOpportunities(5);
        sampleRequest.setDaysSinceLastContact(10);
        sampleRequest.setEmailResponseRate(70);
        sampleRequest.setMeetingCount(3);
    }

    @Test
    void testCalculateScore_withValidRequest() {
        // Given
        CustomerScoringResponse mockResponse = CustomerScoringResponse.builder()
                .customerId(1L)
                .customerName("Test Customer")
                .valueLevel(4)
                .engagementScore(75)
                .churnRisk("LOW")
                .build();

        when(aiScoringService.analyzeCustomer(any())).thenReturn(mockResponse);
        when(customerScoreMapper.insert(any())).thenReturn(1);

        // When
        CustomerScoringResponse result = customerScoringService.calculateScore(sampleRequest);

        // Then
        assertNotNull(result);
        assertEquals(4, result.getValueLevel());
        assertEquals(75, result.getEngagementScore());
        assertEquals("LOW", result.getChurnRisk());
        verify(customerScoreMapper, times(1)).insert(any(CustomerScore.class));
    }

    @Test
    void testGetScore_whenExists() {
        // Given
        CustomerScore existingScore = new CustomerScore();
        existingScore.setCustomerId(1L);
        existingScore.setCustomerName("Test Customer");
        existingScore.setValueLevel(4);
        existingScore.setEngagementScore(80);
        existingScore.setChurnRisk("LOW");

        when(customerScoreMapper.selectOne(any())).thenReturn(existingScore);

        // When
        CustomerScoringResponse result = customerScoringService.getScore(1L);

        // Then
        assertNotNull(result);
        assertEquals(4, result.getValueLevel());
    }

    @Test
    void testGetScore_whenNotExists() {
        // Given
        when(customerScoreMapper.selectOne(any())).thenReturn(null);

        // When
        CustomerScoringResponse result = customerScoringService.getScore(1L);

        // Then
        assertNull(result);
    }

    @Test
    void testGetValueLevelLabel() {
        assertEquals("LV1-潜在客户", CustomerScoringService.getValueLevelLabel(1));
        assertEquals("LV2-普通客户", CustomerScoringService.getValueLevelLabel(2));
        assertEquals("LV3-价值客户", CustomerScoringService.getValueLevelLabel(3));
        assertEquals("LV4-重要客户", CustomerScoringService.getValueLevelLabel(4));
        assertEquals("LV5-战略客户", CustomerScoringService.getValueLevelLabel(5));
        assertEquals("未知", CustomerScoringService.getValueLevelLabel(0));
    }

    @Test
    void testGetEngagementLevel() {
        assertEquals("高", CustomerScoringService.getEngagementLevel(85));
        assertEquals("中", CustomerScoringService.getEngagementLevel(65));
        assertEquals("低", CustomerScoringService.getEngagementLevel(45));
        assertEquals("极低", CustomerScoringService.getEngagementLevel(25));
    }
}