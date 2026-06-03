package com.smartcrm.analytics.service;

import com.smartcrm.analytics.client.EmailFeignClient;
import com.smartcrm.analytics.client.LeadFeignClient;
import com.smartcrm.analytics.client.OpportunityFeignClient;
import com.smartcrm.analytics.dto.RankingEntryDto;
import com.smartcrm.analytics.dto.TrendPointDto;
import com.smartcrm.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the revenue-trend and top-performers business functions.
 */
@DisplayName("AnalyticsService trend + ranking")
class AnalyticsServiceTrendRankingTest {

    private LeadFeignClient leadClient;
    private OpportunityFeignClient opportunityClient;
    private EmailFeignClient emailClient;
    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        leadClient = mock(LeadFeignClient.class);
        opportunityClient = mock(OpportunityFeignClient.class);
        emailClient = mock(EmailFeignClient.class);
        service = new AnalyticsService(leadClient, opportunityClient, emailClient);
    }

    @Test
    @DisplayName("revenue trend: groups won opportunities by day and includes zero-value days")
    void revenueTrendIsDenseAndCorrect() {
        List<Map<String, Object>> opps = new ArrayList<>();
        opps.add(oppWithClose("O1", "CLOSED_WON", "1000", LocalDate.of(2025, 1, 1)));
        opps.add(oppWithClose("O2", "CLOSED_WON", "2000", LocalDate.of(2025, 1, 1)));
        opps.add(oppWithClose("O3", "CLOSED_WON", "500",  LocalDate.of(2025, 1, 3)));
        opps.add(oppWithClose("O4", "CLOSED_LOST", "9999", LocalDate.of(2025, 1, 2))); // ignored
        when(opportunityClient.getAllOpportunities()).thenReturn(ApiResponse.success(opps));

        List<TrendPointDto> trend = service.getRevenueTrend(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 3, 23, 59),
                "DAY");

        assertEquals(3, trend.size(), "all 3 days in range should be present");
        assertEquals(LocalDate.of(2025, 1, 1), trend.get(0).getPeriodStart());
        assertEquals(0, new BigDecimal("3000.00").compareTo(trend.get(0).getValue()));
        assertEquals(2L, trend.get(0).getCount());
        // Day 2 — no wins, must still be present
        assertEquals(LocalDate.of(2025, 1, 2), trend.get(1).getPeriodStart());
        assertEquals(0, BigDecimal.ZERO.compareTo(trend.get(1).getValue()));
        assertEquals(0L, trend.get(1).getCount());
        // Day 3
        assertEquals(0, new BigDecimal("500.00").compareTo(trend.get(2).getValue()));
    }

    @Test
    @DisplayName("revenue trend: monthly granularity buckets to first of month")
    void revenueTrendMonthlyGranularity() {
        List<Map<String, Object>> opps = new ArrayList<>();
        opps.add(oppWithClose("O1", "CLOSED_WON", "100", LocalDate.of(2025, 1, 5)));
        opps.add(oppWithClose("O2", "CLOSED_WON", "200", LocalDate.of(2025, 1, 20)));
        opps.add(oppWithClose("O3", "CLOSED_WON", "300", LocalDate.of(2025, 2, 14)));
        when(opportunityClient.getAllOpportunities()).thenReturn(ApiResponse.success(opps));

        List<TrendPointDto> trend = service.getRevenueTrend(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 2, 28, 23, 59),
                "MONTH");

        assertEquals(2, trend.size());
        assertEquals(LocalDate.of(2025, 1, 1), trend.get(0).getPeriodStart());
        assertEquals(0, new BigDecimal("300").compareTo(trend.get(0).getValue()));
        assertEquals(LocalDate.of(2025, 2, 1), trend.get(1).getPeriodStart());
        assertEquals(0, new BigDecimal("300").compareTo(trend.get(1).getValue()));
    }

    @Test
    @DisplayName("revenue trend: returns empty list when downstream fails")
    void revenueTrendEmptyOnFailure() {
        when(opportunityClient.getAllOpportunities())
                .thenThrow(new RuntimeException("kaboom"));
        List<TrendPointDto> trend = service.getRevenueTrend(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 5, 0, 0),
                "DAY");
        assertNotNull(trend);
        assertTrue(trend.isEmpty());
    }

    @Test
    @DisplayName("top performers: ranked by won revenue, includes win-rate secondary metric")
    void topPerformersRankedByRevenue() {
        List<Map<String, Object>> opps = new ArrayList<>();
        // Alice — high revenue, mixed win/loss
        opps.add(oppOwned("O1", 1L, "Alice", "CLOSED_WON", "10000", LocalDate.of(2025, 1, 10)));
        opps.add(oppOwned("O2", 1L, "Alice", "CLOSED_WON", "5000",  LocalDate.of(2025, 1, 15)));
        opps.add(oppOwned("O3", 1L, "Alice", "CLOSED_LOST", "2000", LocalDate.of(2025, 1, 12)));
        opps.add(oppOwned("O4", 1L, "Alice", "PROPOSAL", "1500", LocalDate.of(2025, 1, 18)));
        // Bob — smaller revenue, no losses
        opps.add(oppOwned("O5", 2L, "Bob", "CLOSED_WON", "6000", LocalDate.of(2025, 1, 11)));
        opps.add(oppOwned("O6", 2L, "Bob", "PROPOSAL", "4000", LocalDate.of(2025, 1, 19)));
        // Carol — only losses
        opps.add(oppOwned("O7", 3L, "Carol", "CLOSED_LOST", "1000", LocalDate.of(2025, 1, 14)));
        when(opportunityClient.getAllOpportunities()).thenReturn(ApiResponse.success(opps));

        List<RankingEntryDto> top = service.getTopPerformers(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 31, 23, 59),
                10);

        // Carol is excluded (zero won revenue)
        assertEquals(2, top.size());
        assertEquals(1L, top.get(0).getUserId());
        assertEquals("Alice", top.get(0).getUserName());
        assertEquals(0, new BigDecimal("15000").compareTo(top.get(0).getMetricValue()));
        // Alice win rate: 2 won / (2 won + 1 lost) = 66.67
        assertEquals(0, new BigDecimal("66.67").compareTo(top.get(0).getSecondaryMetric()));
        assertEquals(4L, top.get(0).getContextCount());

        assertEquals(2L, top.get(1).getUserId());
        assertEquals(0, new BigDecimal("6000").compareTo(top.get(1).getMetricValue()));
        // Bob: 1 won / (1 won + 0 lost) = 100.00
        assertEquals(0, new BigDecimal("100.00").compareTo(top.get(1).getSecondaryMetric()));
    }

    @Test
    @DisplayName("top performers: respects the limit")
    void topPerformersLimitIsRespected() {
        List<Map<String, Object>> opps = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            opps.add(oppOwned("O" + i, (long) i, "User-" + i, "CLOSED_WON",
                    String.valueOf((i + 1) * 1000), LocalDate.of(2025, 1, 10)));
        }
        when(opportunityClient.getAllOpportunities()).thenReturn(ApiResponse.success(opps));

        List<RankingEntryDto> top = service.getTopPerformers(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 31, 23, 59), 2);
        assertEquals(2, top.size());
    }

    @Test
    @DisplayName("top performers: returns empty list when downstream fails")
    void topPerformersEmptyOnFailure() {
        when(opportunityClient.getAllOpportunities())
                .thenThrow(new RuntimeException("kaboom"));
        List<RankingEntryDto> top = service.getTopPerformers(
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 31, 23, 59), 5);
        assertNotNull(top);
        assertTrue(top.isEmpty());
    }

    // ---- helpers ----

    private static Map<String, Object> oppWithClose(String id, String stage, String amount, LocalDate closed) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("stage", stage);
        m.put("amount", amount);
        m.put("closedAt", closed.toString());
        return m;
    }

    private static Map<String, Object> oppOwned(String id, Long ownerId, String ownerName, String stage,
                                                 String amount, LocalDate closed) {
        Map<String, Object> m = oppWithClose(id, stage, amount, closed);
        m.put("ownerId", ownerId);
        m.put("ownerName", ownerName);
        return m;
    }
}
