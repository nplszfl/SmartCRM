package com.smartcrm.analytics.service;

import com.smartcrm.analytics.client.LeadFeignClient;
import com.smartcrm.analytics.client.OpportunityFeignClient;
import com.smartcrm.analytics.dto.FunnelReportDto;
import com.smartcrm.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the sales funnel business function.
 *
 * <p>RED: these tests assert the behaviour described in the new
 * {@code getFunnelReport(...)} method, which does not exist yet on
 * {@link AnalyticsService}. They must fail to compile (let alone run) until
 * the production code is added — proving the test describes the contract.
 */
@DisplayName("AnalyticsService.getFunnelReport")
class AnalyticsServiceFunnelTest {

    private LeadFeignClient leadClient;
    private OpportunityFeignClient opportunityClient;
    private com.smartcrm.analytics.client.EmailFeignClient emailClient;
    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        leadClient = mock(LeadFeignClient.class);
        opportunityClient = mock(OpportunityFeignClient.class);
        emailClient = mock(com.smartcrm.analytics.client.EmailFeignClient.class);
        service = new AnalyticsService(leadClient, opportunityClient, emailClient);
    }

    @Test
    @DisplayName("returns top-to-bottom funnel with conversion rates derived from real data")
    void buildsFunnelFromLeadAndOpportunityData() {
        // Arrange — 100 leads total, 20 of them qualified, 10 became opportunities,
        // 5 reached proposal, 3 negotiating, 1 closed-won.
        List<Map<String, Object>> leads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            leads.add(lead("L" + i, i < 20 ? "QUALIFIED" : "NEW", "0"));
        }
        when(leadClient.getAllLeads()).thenReturn(ApiResponse.success(leads));

        List<Map<String, Object>> opps = new ArrayList<>();
        opps.add(opp("O1", "PROPOSAL", "5000"));
        opps.add(opp("O2", "PROPOSAL", "3000"));
        opps.add(opp("O3", "PROPOSAL", "2000"));
        opps.add(opp("O4", "PROPOSAL", "1000"));
        opps.add(opp("O5", "PROPOSAL", "1000"));
        opps.add(opp("O6", "NEGOTIATION", "8000"));
        opps.add(opp("O7", "NEGOTIATION", "4000"));
        opps.add(opp("O8", "NEGOTIATION", "2000"));
        opps.add(opp("O9", "CLOSED_WON", "9000"));
        opps.add(opp("O10", "CLOSED_WON", "6000"));
        when(opportunityClient.getAllOpportunities()).thenReturn(ApiResponse.success(opps));

        // Act
        FunnelReportDto funnel = service.getFunnelReport(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 12, 31, 23, 59));

        // Assert
        assertNotNull(funnel, "funnel report must not be null");
        assertEquals(100L, funnel.getTotalLeads());
        assertEquals(10L, funnel.getTotalOpportunities());
        assertEquals(2L, funnel.getWonDeals());
        assertEquals(0, new BigDecimal("15000.00").compareTo(funnel.getTotalWonRevenue()),
                "won revenue should equal sum of closed-won amounts");

        // Top-to-bottom conversion: 2 / 100 * 100 = 2.00
        assertEquals(0, new BigDecimal("2.00").compareTo(funnel.getOverallConversionRate()));

        // 6 funnel stages defined: LEAD, QUALIFIED_LEAD, OPPORTUNITY, PROPOSAL, NEGOTIATION, CLOSED_WON
        assertEquals(6, funnel.getStages().size());

        // Stage[0] = LEAD — count=100, no conversion from previous
        FunnelReportDto.FunnelStage leadStage = funnel.getStages().get(0);
        assertEquals("LEAD", leadStage.getStage());
        assertEquals(100L, leadStage.getCount());
        assertNull(leadStage.getConversionFromPrevious());
        assertEquals(new BigDecimal("100.00"), leadStage.getConversionFromTop());

        // Stage[1] = QUALIFIED_LEAD — 20 leads, 20% of top
        FunnelReportDto.FunnelStage qualified = funnel.getStages().get(1);
        assertEquals(20L, qualified.getCount());
        assertEquals(0, new BigDecimal("20.00").compareTo(qualified.getConversionFromTop()));

        // Stage[4] = NEGOTIATION — count=3
        FunnelReportDto.FunnelStage negotiation = funnel.getStages().get(4);
        assertEquals(3L, negotiation.getCount());

        // Stage[5] = CLOSED_WON — count=2, 2% of top
        FunnelReportDto.FunnelStage won = funnel.getStages().get(5);
        assertEquals(2L, won.getCount());
        assertEquals(0, new BigDecimal("2.00").compareTo(won.getConversionFromTop()));
    }

    @Test
    @DisplayName("returns empty funnel with zero metrics when downstream services fail")
    void handlesDownstreamFailuresGracefully() {
        when(leadClient.getAllLeads()).thenThrow(new RuntimeException("boom"));
        when(opportunityClient.getAllOpportunities())
                .thenThrow(new RuntimeException("boom"));

        FunnelReportDto funnel = service.getFunnelReport(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 12, 31, 23, 59));

        assertNotNull(funnel);
        assertEquals(0L, funnel.getTotalLeads());
        assertEquals(0L, funnel.getTotalOpportunities());
        assertEquals(0L, funnel.getWonDeals());
        assertEquals(0, BigDecimal.ZERO.compareTo(funnel.getTotalWonRevenue()));
        assertNotNull(funnel.getStages());
        assertTrue(funnel.getStages().isEmpty(), "stages should be empty when no data is available");
    }

    @Test
    @DisplayName("counts only won opportunities as 'wonDeals' and excludes lost/closed_lost")
    void ignoresLostOpportunitiesInWonCount() {
        when(leadClient.getAllLeads()).thenReturn(ApiResponse.success(List.of()));
        List<Map<String, Object>> opps = new ArrayList<>();
        opps.add(opp("O1", "CLOSED_WON", "1000"));
        opps.add(opp("O2", "CLOSED_LOST", "500"));
        opps.add(opp("O3", "PROPOSAL", "750"));
        when(opportunityClient.getAllOpportunities()).thenReturn(ApiResponse.success(opps));

        FunnelReportDto funnel = service.getFunnelReport(
                LocalDateTime.now().minusDays(30), LocalDateTime.now());

        assertEquals(1L, funnel.getWonDeals());
        assertEquals(0, new BigDecimal("1000").compareTo(funnel.getTotalWonRevenue()));
    }

    // ---- helpers ----

    private static Map<String, Object> lead(String id, String status, String amount) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("status", status);
        m.put("source", "WEB");
        m.put("amount", amount);
        return m;
    }

    private static Map<String, Object> opp(String id, String stage, String amount) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("stage", stage);
        m.put("amount", amount);
        return m;
    }
}
