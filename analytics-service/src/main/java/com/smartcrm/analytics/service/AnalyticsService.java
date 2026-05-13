package com.smartcrm.analytics.service;

import com.smartcrm.analytics.client.EmailFeignClient;
import com.smartcrm.analytics.client.LeadFeignClient;
import com.smartcrm.analytics.client.OpportunityFeignClient;
import com.smartcrm.analytics.dto.PerformanceReportDto;
import com.smartcrm.analytics.dto.SalesDashboardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Analytics Service - Provides comprehensive sales analytics and reporting
 * 
 * This service aggregates data from:
 * - Lead management (leads, sources, conversions) via LeadFeignClient
 * - Opportunity management (deals, pipeline, stages) via OpportunityFeignClient
 * - Email tracking (sent, opened, replied) via EmailFeignClient
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LeadFeignClient leadClient;
    private final OpportunityFeignClient opportunityClient;
    private final EmailFeignClient emailClient;

    /**
     * Get comprehensive sales dashboard overview by aggregating data from all services
     */
    public SalesDashboardDto getSalesDashboard() {
        log.info("Generating comprehensive sales dashboard");
        SalesDashboardDto dashboard = new SalesDashboardDto();

        try {
            // Lead metrics - fetch from lead service
            Map<String, Long> leadCounts = fetchLeadCounts();
            dashboard.setTotalLeads(sumAllLeads(leadCounts));
            dashboard.setHotLeads(leadCounts.getOrDefault("hotCount", 0L));
            dashboard.setConvertedLeads(leadCounts.getOrDefault("CONVERTED", 0L));
            dashboard.setLeadsByStatus(leadCounts);

            // Opportunity metrics - fetch from opportunity service
            Map<String, Long> oppCounts = fetchOpportunityCounts();
            dashboard.setTotalOpportunities(sumAllOpportunities(oppCounts));
            dashboard.setOpenOpportunities(oppCounts.getOrDefault("openCount", 0L));
            dashboard.setWonOpportunities(oppCounts.getOrDefault("CLOSED_WON", 0L));
            dashboard.setLostOpportunities(oppCounts.getOrDefault("CLOSED_LOST", 0L));
            dashboard.setOpportunitiesByStage(oppCounts);

            // Financial metrics
            BigDecimal totalPipeline = fetchTotalPipelineValue();
            BigDecimal weightedPipeline = fetchWeightedPipelineValue();
            dashboard.setTotalPipelineValue(totalPipeline);
            dashboard.setWeightedPipelineValue(weightedPipeline);
            dashboard.setWonRevenue(fetchWonRevenue());
            dashboard.setLostRevenue(fetchLostRevenue());
            dashboard.setAverageDealSize(calculateAverageDealSize(dashboard.getWonOpportunities(), dashboard.getWonRevenue()));

            // Win rate calculation
            dashboard.setWinRate(calculateWinRate(dashboard.getWonOpportunities(), dashboard.getLostOpportunities()));

            // Stage distribution analysis
            dashboard.setStageDistribution(analyzeStageDistribution(oppCounts, totalPipeline));

            // Lead source effectiveness
            dashboard.setLeadSourceAnalysis(analyzeLeadSources(leadCounts));

        } catch (Exception e) {
            log.warn("Failed to fetch some analytics data, using partial results: {}", e.getMessage());
            initializeEmptyDashboard(dashboard);
        }

        return dashboard;
    }

    /**
     * Get detailed performance report for a sales rep
     */
    public PerformanceReportDto getUserPerformance(Long userId, String userName,
                                                    LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating performance report for user {} from {} to {}", userId, startDate, endDate);

        PerformanceReportDto report = new PerformanceReportDto();
        report.setUserId(userId);
        report.setUserName(userName);
        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);

        try {
            // Lead ownership metrics
            report.setLeadsOwned(fetchLeadsOwnedByUser(userId));

            // Opportunity metrics
            Map<String, Object> oppMetrics = fetchOpportunityMetricsByUser(userId);
            report.setOpportunitiesOwned(((Number) oppMetrics.getOrDefault("owned", 0)).longValue());
            report.setDealsWon(((Number) oppMetrics.getOrDefault("won", 0)).longValue());
            report.setDealsLost(((Number) oppMetrics.getOrDefault("lost", 0)).longValue());
            report.setTotalSalesValue(new BigDecimal(oppMetrics.getOrDefault("totalValue", "0").toString()));

            // Deal metrics
            report.setWinRate(calculateWinRate(report.getDealsWon(), report.getDealsLost()));
            report.setAverageDealSize(calculateAverageDealSize(report.getDealsWon(), report.getTotalSalesValue()));

            // Email engagement metrics
            Map<String, Long> emailStats = fetchEmailStatsByUser(userId);
            report.setEmailsSent(emailStats.getOrDefault("sent", 0L));
            report.setEmailsOpened(emailStats.getOrDefault("opened", 0L));
            report.setEmailsReplied(emailStats.getOrDefault("replied", 0L));
            
            report.setEmailOpenRate(calculateEmailRate(report.getEmailsOpened(), report.getEmailsSent()));
            report.setEmailReplyRate(calculateEmailRate(report.getEmailsReplied(), report.getEmailsSent()));

        } catch (Exception e) {
            log.warn("Failed to fetch performance data for user {}, using defaults: {}", userId, e.getMessage());
        }

        return report;
    }

    /**
     * Get pipeline forecast for upcoming months
     */
    public BigDecimal getPipelineForecast(int monthsAhead) {
        log.info("Calculating pipeline forecast for {} months ahead", monthsAhead);
        
        try {
            BigDecimal currentPipeline = fetchWeightedPipelineValue();
            BigDecimal winRate = calculateWinRate(fetchWonCount(), fetchLostCount());
            BigDecimal historicalWinRate = winRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            double avgCycle = getAverageSalesCycleDuration();
            
            if (avgCycle > 0) {
                return currentPipeline.multiply(historicalWinRate)
                        .multiply(BigDecimal.valueOf(monthsAhead))
                        .divide(BigDecimal.valueOf(avgCycle), 2, RoundingMode.HALF_UP);
            }
            return currentPipeline.multiply(historicalWinRate);
        } catch (Exception e) {
            log.warn("Failed to calculate forecast: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get conversion rates broken down by lead source
     */
    public Map<String, BigDecimal> getConversionRatesBySource() {
        log.info("Calculating conversion rates by source");
        
        Map<String, BigDecimal> conversionRates = new HashMap<>();
        String[] sources = {"WEB", "REFERRAL", "LINKEDIN", "TRADE_SHOW", "COLD_OUTREACH", "PARTNER"};
        
        try {
            for (String source : sources) {
                BigDecimal rate = calculateConversionRateForSource(source);
                conversionRates.put(source, rate);
            }
        } catch (Exception e) {
            log.warn("Failed to calculate conversion rates: {}", e.getMessage());
            for (String source : sources) {
                conversionRates.put(source, BigDecimal.ZERO);
            }
        }
        
        return conversionRates;
    }

    /**
     * Get average time from lead creation to conversion
     */
    public double getAverageTimeToConversion() {
        log.info("Calculating average time to conversion");
        // Would calculate from lead created_at to converted_at timestamps
        return 14.5; // days - estimated based on typical sales cycle
    }

    /**
     * Get average sales cycle duration
     */
    public double getAverageSalesCycleDuration() {
        log.info("Calculating average sales cycle duration");
        return 45.0; // days - default based on typical B2B sales cycle
    }

    /**
     * Get activity summary for a user in date range
     */
    public Map<String, Long> getActivitySummary(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting activity summary for user {} from {} to {}", userId, startDate, endDate);
        
        Map<String, Long> summary = new LinkedHashMap<>();
        
        try {
            Map<String, Long> emailStats = fetchEmailStatsByUser(userId);
            summary.put("emailsSent", emailStats.getOrDefault("sent", 0L));
            summary.put("emailsOpened", emailStats.getOrDefault("opened", 0L));
            summary.put("emailsReplied", emailStats.getOrDefault("replied", 0L));
        } catch (Exception e) {
            summary.put("emailsSent", 0L);
            summary.put("emailsOpened", 0L);
            summary.put("emailsReplied", 0L);
        }
        
        summary.put("callsMade", 0L);    // Would integrate with activity/call service
        summary.put("meetingsHeld", 0L);  // Would integrate with calendar service
        summary.put("proposalsSent", 0L); // Would integrate with document service
        summary.put("dealsClosed", fetchDealsClosedByUser(userId));
        
        return summary;
    }

    // ==================== Private helper methods ====================

    private Map<String, Long> fetchLeadCounts() {
        Map<String, Long> counts = new HashMap<>();
        try {
            var response = leadClient.countByStatus();
            if (response != null && response.getData() != null) {
                counts.putAll(response.getData());
            }
            var hotResponse = leadClient.countHotLeads();
            if (hotResponse != null && hotResponse.getData() != null) {
                counts.put("hotCount", hotResponse.getData());
            }
        } catch (Exception e) {
            log.debug("Could not fetch lead counts: {}", e.getMessage());
        }
        return counts;
    }

    private Map<String, Long> fetchOpportunityCounts() {
        Map<String, Long> counts = new HashMap<>();
        try {
            var response = opportunityClient.countByStage();
            if (response != null && response.getData() != null) {
                counts.putAll(response.getData());
                // Calculate open opportunities (non-closed)
                long open = counts.values().stream()
                        .filter(c -> !counts.isEmpty())
                        .mapToLong(Long::longValue)
                        .sum();
                counts.put("openCount", counts.getOrDefault("PROSPECTING", 0L) 
                        + counts.getOrDefault("QUALIFICATION", 0L)
                        + counts.getOrDefault("PROPOSAL", 0L)
                        + counts.getOrDefault("NEGOTIATION", 0L));
            }
        } catch (Exception e) {
            log.debug("Could not fetch opportunity counts: {}", e.getMessage());
        }
        return counts;
    }

    private BigDecimal fetchTotalPipelineValue() {
        try {
            var response = opportunityClient.getTotalPipelineValue();
            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.debug("Could not fetch total pipeline value: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal fetchWeightedPipelineValue() {
        try {
            var response = opportunityClient.getWeightedPipelineValue();
            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.debug("Could not fetch weighted pipeline value: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal fetchWonRevenue() {
        try {
            var response = opportunityClient.getAllOpportunities();
            if (response != null && response.getData() != null) {
                return response.getData().stream()
                        .filter(m -> "CLOSED_WON".equals(String.valueOf(m.getOrDefault("stage", ""))))
                        .map(m -> new BigDecimal(String.valueOf(m.getOrDefault("amount", "0"))))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } catch (Exception e) {
            log.debug("Could not fetch won revenue: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal fetchLostRevenue() {
        try {
            var response = opportunityClient.getAllOpportunities();
            if (response != null && response.getData() != null) {
                return response.getData().stream()
                        .filter(m -> "CLOSED_LOST".equals(String.valueOf(m.getOrDefault("stage", ""))))
                        .map(m -> new BigDecimal(String.valueOf(m.getOrDefault("amount", "0"))))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } catch (Exception e) {
            log.debug("Could not fetch lost revenue: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private long sumAllLeads(Map<String, Long> leadCounts) {
        return leadCounts.values().stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    private long sumAllOpportunities(Map<String, Long> oppCounts) {
        return oppCounts.values().stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    private BigDecimal calculateWinRate(long won, long lost) {
        if (won + lost == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(won)
                .divide(BigDecimal.valueOf(won + lost), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageDealSize(long dealsWon, BigDecimal totalRevenue) {
        if (dealsWon == 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(BigDecimal.valueOf(dealsWon), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEmailRate(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateConversionRateForSource(String source) {
        // Simplified: would need source tracking in lead entity
        return BigDecimal.valueOf(15.0); // Default 15% conversion rate
    }

    private long fetchWonCount() {
        try {
            var response = opportunityClient.countByStage();
            if (response != null && response.getData() != null) {
                return response.getData().getOrDefault("CLOSED_WON", 0L);
            }
        } catch (Exception e) {
            log.debug("Could not fetch won count: {}", e.getMessage());
        }
        return 0L;
    }

    private long fetchLostCount() {
        try {
            var response = opportunityClient.countByStage();
            if (response != null && response.getData() != null) {
                return response.getData().getOrDefault("CLOSED_LOST", 0L);
            }
        } catch (Exception e) {
            log.debug("Could not fetch lost count: {}", e.getMessage());
        }
        return 0L;
    }

    private long fetchLeadsOwnedByUser(Long userId) {
        try {
            var response = leadClient.getLeadsByOwner(userId);
            if (response != null && response.getData() != null) {
                return response.getData().size();
            }
        } catch (Exception e) {
            log.debug("Could not fetch leads owned by user {}: {}", userId, e.getMessage());
        }
        return 0L;
    }

    private Map<String, Object> fetchOpportunityMetricsByUser(Long userId) {
        Map<String, Object> metrics = new HashMap<>();
        try {
            var response = opportunityClient.getOpportunitiesByOwner(userId);
            if (response != null && response.getData() != null) {
                metrics.put("owned", response.getData().size());
                metrics.put("won", response.getData().stream()
                        .filter(m -> "CLOSED_WON".equals(String.valueOf(m.getOrDefault("stage", ""))))
                        .count());
                metrics.put("lost", response.getData().stream()
                        .filter(m -> "CLOSED_LOST".equals(String.valueOf(m.getOrDefault("stage", ""))))
                        .count());
                metrics.put("totalValue", response.getData().stream()
                        .filter(m -> "CLOSED_WON".equals(String.valueOf(m.getOrDefault("stage", ""))))
                        .map(m -> new BigDecimal(String.valueOf(m.getOrDefault("amount", "0"))))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .toString());
            }
        } catch (Exception e) {
            log.debug("Could not fetch opportunity metrics for user {}: {}", userId, e.getMessage());
        }
        return metrics;
    }

    private Map<String, Long> fetchEmailStatsByUser(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        try {
            var response = emailClient.countByStatus();
            if (response != null && response.getData() != null) {
                stats.put("sent", response.getData().getOrDefault("SENT", 0L));
                stats.put("opened", response.getData().getOrDefault("OPENED", 0L));
                stats.put("replied", response.getData().getOrDefault("REPLIED", 0L));
            }
        } catch (Exception e) {
            log.debug("Could not fetch email stats: {}", e.getMessage());
        }
        return stats;
    }

    private long fetchDealsClosedByUser(Long userId) {
        try {
            var response = opportunityClient.getOpportunitiesByOwner(userId);
            if (response != null && response.getData() != null) {
                return response.getData().stream()
                        .filter(m -> "CLOSED_WON".equals(String.valueOf(m.getOrDefault("stage", ""))))
                        .count();
            }
        } catch (Exception e) {
            log.debug("Could not fetch deals closed by user {}: {}", userId, e.getMessage());
        }
        return 0L;
    }

    private void initializeEmptyDashboard(SalesDashboardDto dashboard) {
        dashboard.setTotalLeads(0L);
        dashboard.setHotLeads(0L);
        dashboard.setConvertedLeads(0L);
        dashboard.setTotalOpportunities(0L);
        dashboard.setOpenOpportunities(0L);
        dashboard.setWonOpportunities(0L);
        dashboard.setLostOpportunities(0L);
        dashboard.setTotalPipelineValue(BigDecimal.ZERO);
        dashboard.setWeightedPipelineValue(BigDecimal.ZERO);
        dashboard.setWonRevenue(BigDecimal.ZERO);
        dashboard.setLostRevenue(BigDecimal.ZERO);
        dashboard.setAverageDealSize(BigDecimal.ZERO);
        dashboard.setWinRate(BigDecimal.ZERO);
        dashboard.setStageDistribution(new ArrayList<>());
        dashboard.setLeadSourceAnalysis(new ArrayList<>());
        dashboard.setLeadsByStatus(new HashMap<>());
        dashboard.setOpportunitiesByStage(new HashMap<>());
    }

    private List<SalesDashboardDto.StageDistribution> analyzeStageDistribution(Map<String, Long> oppCounts, BigDecimal totalPipeline) {
        List<SalesDashboardDto.StageDistribution> distribution = new ArrayList<>();
        String[] stages = {"PROSPECTING", "QUALIFICATION", "PROPOSAL", "NEGOTIATION", "CLOSED_WON", "CLOSED_LOST"};
        
        long totalCount = oppCounts.values().stream().mapToLong(Long::longValue).sum();
        
        for (String stage : stages) {
            SalesDashboardDto.StageDistribution dist = new SalesDashboardDto.StageDistribution();
            dist.setStage(stage);
            dist.setCount(oppCounts.getOrDefault(stage, 0L));
            
            try {
                var response = opportunityClient.getOpportunitiesByStage(stage);
                if (response != null && response.getData() != null) {
                    BigDecimal stageAmount = response.getData().stream()
                            .map(m -> new BigDecimal(String.valueOf(m.getOrDefault("amount", "0"))))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    dist.setTotalAmount(stageAmount);
                } else {
                    dist.setTotalAmount(BigDecimal.ZERO);
                }
            } catch (Exception e) {
                dist.setTotalAmount(BigDecimal.ZERO);
            }
            
            if (totalCount > 0) {
                dist.setPercentage(BigDecimal.valueOf(dist.getCount())
                        .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP));
            } else {
                dist.setPercentage(BigDecimal.ZERO);
            }
            
            distribution.add(dist);
        }
        
        return distribution;
    }

    private List<SalesDashboardDto.LeadSourceAnalysis> analyzeLeadSources(Map<String, Long> leadCounts) {
        List<SalesDashboardDto.LeadSourceAnalysis> analyses = new ArrayList<>();
        String[] sources = {"WEB", "REFERRAL", "LINKEDIN", "TRADE_SHOW", "COLD_OUTREACH", "PARTNER"};
        
        for (String source : sources) {
            SalesDashboardDto.LeadSourceAnalysis analysis = new SalesDashboardDto.LeadSourceAnalysis();
            analysis.setSource(source);
            // In real implementation, would group leads by source
            // For now, estimate based on total leads
            long totalLeads = leadCounts.values().stream().mapToLong(Long::longValue).sum();
            long sourceCount = totalLeads > 0 ? Math.round(totalLeads * 0.15) : 0; // Estimate 15% per source
            analysis.setLeadCount(sourceCount);
            analysis.setConversionCount((long) (sourceCount * 0.2)); // Estimate 20% conversion
            analysis.setConversionRate(BigDecimal.valueOf(20.0));
            analyses.add(analysis);
        }
        
        return analyses;
    }
}
