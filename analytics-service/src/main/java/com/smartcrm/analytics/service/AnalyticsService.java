package com.smartcrm.analytics.service;

import com.smartcrm.analytics.client.EmailFeignClient;
import com.smartcrm.analytics.client.LeadFeignClient;
import com.smartcrm.analytics.client.OpportunityFeignClient;
import com.smartcrm.analytics.dto.FunnelReportDto;
import com.smartcrm.analytics.dto.PerformanceReportDto;
import com.smartcrm.analytics.dto.RankingEntryDto;
import com.smartcrm.analytics.dto.SalesDashboardDto;
import com.smartcrm.analytics.dto.TrendPointDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
     * Get average time from lead creation to conversion in days.
     * Calculated from actual converted leads' createdAt and convertedAt timestamps.
     */
    public double getAverageTimeToConversion() {
        log.info("Calculating average time to conversion");
        try {
            var response = leadClient.getAllLeads();
            if (response != null && response.getData() != null) {
                List<Map<String, Object>> leads = response.getData();
                List<Long> conversionTimes = new ArrayList<>();

                for (Map<String, Object> lead : leads) {
                    String status = String.valueOf(lead.getOrDefault("status", ""));
                    if ("CONVERTED".equals(status)) {
                        Object createdAtObj = lead.get("createdAt");
                        Object convertedAtObj = lead.get("convertedAt");

                        if (createdAtObj != null && convertedAtObj != null) {
                            LocalDateTime created = parseDateTime(createdAtObj);
                            LocalDateTime converted = parseDateTime(convertedAtObj);
                            if (created != null && converted != null) {
                                long days = java.time.temporal.ChronoUnit.DAYS.between(created, converted);
                                conversionTimes.add(days);
                            }
                        }
                    }
                }

                if (!conversionTimes.isEmpty()) {
                    double avg = conversionTimes.stream()
                            .mapToLong(Long::longValue)
                            .average()
                            .orElse(0.0);
                    return Math.round(avg * 10) / 10.0;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to calculate average time to conversion: {}", e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get average sales cycle duration in days.
     * Calculated from won opportunities' createdAt to closedAt timestamps.
     */
    public double getAverageSalesCycleDuration() {
        log.info("Calculating average sales cycle duration");
        try {
            var response = opportunityClient.getAllOpportunities();
            if (response != null && response.getData() != null) {
                List<Map<String, Object>> opps = response.getData();
                List<Long> cycleTimes = new ArrayList<>();

                for (Map<String, Object> opp : opps) {
                    String stage = String.valueOf(opp.getOrDefault("stage", ""));
                    if ("CLOSED_WON".equals(stage)) {
                        Object createdAtObj = opp.get("createdAt");
                        Object closedAtObj = opp.get("closedAt");

                        if (createdAtObj != null && closedAtObj != null) {
                            LocalDateTime created = parseDateTime(createdAtObj);
                            LocalDateTime closed = parseDateTime(closedAtObj);
                            if (created != null && closed != null) {
                                long days = java.time.temporal.ChronoUnit.DAYS.between(created, closed);
                                cycleTimes.add(days);
                            }
                        }
                    }
                }

                if (!cycleTimes.isEmpty()) {
                    double avg = cycleTimes.stream()
                            .mapToLong(Long::longValue)
                            .average()
                            .orElse(0.0);
                    return Math.round(avg * 10) / 10.0;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to calculate average sales cycle duration: {}", e.getMessage());
        }
        return 0.0;
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

    // ==================== Business analytics: funnel, trend, ranking ====================

    /**
     * Build a sales-funnel report for the given period.
     *
     * <p>The funnel goes from {@code LEAD} (top) through {@code QUALIFIED_LEAD},
     * {@code OPPORTUNITY}, {@code PROPOSAL}, {@code NEGOTIATION} down to
     * {@code CLOSED_WON} (bottom). For each stage the report records:
     * <ul>
     *   <li>how many records entered the stage</li>
     *   <li>the conversion rate from the previous stage</li>
     *   <li>the conversion rate from the top of the funnel</li>
     *   <li>the total monetary value of opportunities at the stage</li>
     * </ul>
     *
     * <p>The method is defensive: if any of the downstream services fails
     * the entire report is still returned (with zeros) so dashboards can
     * still render rather than 500.
     */
    public FunnelReportDto getFunnelReport(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Building sales funnel report from {} to {}", startDate, endDate);
        FunnelReportDto funnel = new FunnelReportDto();
        funnel.setPeriodStart(startDate);
        funnel.setPeriodEnd(endDate);

        try {
            List<Map<String, Object>> leads = safeGetLeads();
            List<Map<String, Object>> opps = safeGetOpportunities();

            // Lead-side stages
            long totalLeads = leads.size();
            long qualifiedLeads = leads.stream()
                    .filter(l -> isQualifiedLeadStatus(String.valueOf(l.getOrDefault("status", ""))))
                    .count();

            // Opportunity-side stages
            Map<String, Long> oppStageCounts = new LinkedHashMap<>();
            BigDecimal proposalAmount = BigDecimal.ZERO;
            BigDecimal negotiationAmount = BigDecimal.ZERO;
            BigDecimal wonAmount = BigDecimal.ZERO;
            BigDecimal oppAmount = BigDecimal.ZERO;

            for (Map<String, Object> o : opps) {
                String stage = String.valueOf(o.getOrDefault("stage", ""));
                BigDecimal amount = parseAmount(o.get("amount"));
                oppStageCounts.merge(stage, 1L, Long::sum);
                oppAmount = oppAmount.add(amount);
                if ("PROPOSAL".equals(stage)) proposalAmount = proposalAmount.add(amount);
                else if ("NEGOTIATION".equals(stage)) negotiationAmount = negotiationAmount.add(amount);
                else if ("CLOSED_WON".equals(stage)) wonAmount = wonAmount.add(amount);
            }

            long totalOpps = opps.size();
            long proposalCount = oppStageCounts.getOrDefault("PROPOSAL", 0L);
            long negotiationCount = oppStageCounts.getOrDefault("NEGOTIATION", 0L);
            long wonCount = oppStageCounts.getOrDefault("CLOSED_WON", 0L);

            funnel.setTotalLeads(totalLeads);
            funnel.setTotalOpportunities(totalOpps);
            funnel.setWonDeals(wonCount);
            funnel.setTotalWonRevenue(wonAmount);
            funnel.setOverallConversionRate(percentage(wonCount, totalLeads));

            // Build the ordered stage list.
            List<FunnelReportDto.FunnelStage> stages = new ArrayList<>();
            long top = Math.max(totalLeads, 1L);

            stages.add(stage("LEAD", totalLeads, BigDecimal.ZERO, null, top));
            stages.add(stage("QUALIFIED_LEAD", qualifiedLeads, BigDecimal.ZERO, totalLeads, top));
            stages.add(stage("OPPORTUNITY", totalOpps, oppAmount, qualifiedLeads, top));
            stages.add(stage("PROPOSAL", proposalCount, proposalAmount, totalOpps, top));
            stages.add(stage("NEGOTIATION", negotiationCount, negotiationAmount, proposalCount, top));
            stages.add(stage("CLOSED_WON", wonCount, wonAmount, negotiationCount, top));

            funnel.setStages(stages);
        } catch (Exception e) {
            log.warn("Failed to build funnel report, returning empty result: {}", e.getMessage());
            // Defensive defaults — every numeric metric must be non-null
            funnel.setTotalLeads(0L);
            funnel.setTotalOpportunities(0L);
            funnel.setWonDeals(0L);
            funnel.setTotalWonRevenue(BigDecimal.ZERO);
            funnel.setOverallConversionRate(BigDecimal.ZERO);
            funnel.setStages(new ArrayList<>());
        }

        return funnel;
    }

    /**
     * Build a daily/monthly revenue trend series for the given period.
     *
     * <p>Won opportunities are bucketed by their {@code closedAt} date. The
     * resulting series is dense — every day/month in the period is included,
     * even if there were no wins (those entries will have value 0).
     *
     * @param granularity "DAY", "WEEK", or "MONTH"
     */
    public List<TrendPointDto> getRevenueTrend(LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        log.info("Building revenue trend from {} to {} ({})", startDate, endDate, granularity);
        String bucket = granularity == null ? "DAY" : granularity.toUpperCase(Locale.ROOT);

        try {
            List<Map<String, Object>> opps = safeGetOpportunities();
            // Build dense bucket list first
            List<LocalDate> periods = buildPeriods(startDate.toLocalDate(), endDate.toLocalDate(), bucket);

            Map<LocalDate, BigDecimal> revenueByPeriod = new HashMap<>();
            Map<LocalDate, Long> countByPeriod = new HashMap<>();
            for (LocalDate p : periods) {
                revenueByPeriod.put(p, BigDecimal.ZERO);
                countByPeriod.put(p, 0L);
            }

            for (Map<String, Object> o : opps) {
                if (!"CLOSED_WON".equals(String.valueOf(o.getOrDefault("stage", "")))) continue;
                LocalDate closed = extractDate(o.get("closedAt"));
                if (closed == null) continue;
                if (closed.isBefore(startDate.toLocalDate()) || closed.isAfter(endDate.toLocalDate())) continue;
                LocalDate bucketKey = roundDown(closed, bucket);
                revenueByPeriod.merge(bucketKey, parseAmount(o.get("amount")), BigDecimal::add);
                countByPeriod.merge(bucketKey, 1L, Long::sum);
            }

            List<TrendPointDto> result = new ArrayList<>(periods.size());
            for (LocalDate p : periods) {
                TrendPointDto point = new TrendPointDto();
                point.setPeriodStart(p);
                point.setValue(revenueByPeriod.getOrDefault(p, BigDecimal.ZERO));
                point.setCount(countByPeriod.getOrDefault(p, 0L));
                result.add(point);
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to build revenue trend: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Build a leaderboard of the top {@code limit} sales reps ranked by won
     * revenue within the given period.
     */
    public List<RankingEntryDto> getTopPerformers(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        log.info("Building top-performers ranking from {} to {} (limit={})", startDate, endDate, limit);
        if (limit <= 0) limit = 10;
        try {
            List<Map<String, Object>> opps = safeGetOpportunities();
            Map<Long, RankingAccumulator> byUser = new HashMap<>();

            for (Map<String, Object> o : opps) {
                if (!"CLOSED_WON".equals(String.valueOf(o.getOrDefault("stage", "")))) continue;
                LocalDate closed = extractDate(o.get("closedAt"));
                if (closed == null) continue;
                if (closed.isBefore(startDate.toLocalDate()) || closed.isAfter(endDate.toLocalDate())) continue;
                Long owner = parseLong(o.get("ownerId"));
                if (owner == null) continue;

                RankingAccumulator acc = byUser.computeIfAbsent(owner, k -> new RankingAccumulator());
                acc.userId = owner;
                acc.userName = String.valueOf(o.getOrDefault("ownerName", "User-" + owner));
                acc.wonCount++;
                acc.wonRevenue = acc.wonRevenue.add(parseAmount(o.get("amount")));
            }

            // Cross-reference with opportunities for total deal count / win rate
            for (Map<String, Object> o : opps) {
                Long owner = parseLong(o.get("ownerId"));
                if (owner == null) continue;
                RankingAccumulator acc = byUser.get(owner);
                if (acc == null) continue;
                acc.totalDeals++;
                String stage = String.valueOf(o.getOrDefault("stage", ""));
                if ("CLOSED_LOST".equals(stage)) acc.lostCount++;
            }

            return byUser.values().stream()
                    .sorted((a, b) -> b.wonRevenue.compareTo(a.wonRevenue))
                    .limit(limit)
                    .map(acc -> {
                        RankingEntryDto entry = new RankingEntryDto();
                        entry.setUserId(acc.userId);
                        entry.setUserName(acc.userName);
                        entry.setMetricValue(acc.wonRevenue);
                        entry.setSecondaryMetric(percentage(acc.wonCount, acc.wonCount + acc.lostCount));
                        entry.setContextCount(acc.totalDeals);
                        return entry;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to build top-performers ranking: {}", e.getMessage());
            return new ArrayList<>();
        }
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

    /**
     * Calculate conversion rate for a specific lead source based on actual data.
     * Conversion rate = (converted leads from source / total leads from source) * 100
     */
    private BigDecimal calculateConversionRateForSource(String source) {
        try {
            var response = leadClient.getAllLeads();
            if (response != null && response.getData() != null) {
                List<Map<String, Object>> leads = response.getData();

                long totalFromSource = leads.stream()
                        .filter(l -> source.equals(String.valueOf(l.getOrDefault("source", ""))))
                        .count();

                long convertedFromSource = leads.stream()
                        .filter(l -> source.equals(String.valueOf(l.getOrDefault("source", ""))))
                        .filter(l -> "CONVERTED".equals(String.valueOf(l.getOrDefault("status", ""))))
                        .count();

                if (totalFromSource > 0) {
                    return BigDecimal.valueOf(convertedFromSource)
                            .divide(BigDecimal.valueOf(totalFromSource), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to calculate conversion rate for source {}: {}", source, e.getMessage());
        }
        return BigDecimal.ZERO;
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

        try {
            var response = leadClient.getAllLeads();
            if (response != null && response.getData() != null) {
                List<Map<String, Object>> leads = response.getData();

                for (String source : sources) {
                    List<Map<String, Object>> sourceLeads = leads.stream()
                            .filter(l -> source.equals(String.valueOf(l.getOrDefault("source", ""))))
                            .toList();

                    long totalFromSource = sourceLeads.size();
                    long convertedFromSource = sourceLeads.stream()
                            .filter(l -> "CONVERTED".equals(String.valueOf(l.getOrDefault("status", ""))))
                            .count();

                    BigDecimal conversionRate = totalFromSource > 0
                            ? BigDecimal.valueOf(convertedFromSource)
                                    .divide(BigDecimal.valueOf(totalFromSource), 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    SalesDashboardDto.LeadSourceAnalysis analysis = new SalesDashboardDto.LeadSourceAnalysis();
                    analysis.setSource(source);
                    analysis.setLeadCount(totalFromSource);
                    analysis.setConversionCount(convertedFromSource);
                    analysis.setConversionRate(conversionRate);
                    analyses.add(analysis);
                }
                return analyses;
            }
        } catch (Exception e) {
            log.warn("Failed to analyze lead sources from actual data: {}", e.getMessage());
        }

        // Fallback: use estimates
        for (String source : sources) {
            SalesDashboardDto.LeadSourceAnalysis analysis = new SalesDashboardDto.LeadSourceAnalysis();
            analysis.setSource(source);
            analysis.setLeadCount(0L);
            analysis.setConversionCount(0L);
            analysis.setConversionRate(BigDecimal.ZERO);
            analyses.add(analysis);
        }
        return analyses;
    }

    /**
     * Parse datetime from various possible formats returned by Feign clients.
     */
    private LocalDateTime parseDateTime(Object dateObj) {
        if (dateObj == null) return null;
        try {
            if (dateObj instanceof String s) {
                // Handle ISO format: "2025-01-15T10:30:00" or "2025-01-15"
                if (s.length() >= 10) {
                    if (s.contains("T")) {
                        return LocalDateTime.parse(s);
                    } else {
                        return LocalDate.parse(s, DateTimeFormatter.ISO_DATE).atStartOfDay();
                    }
                }
            } else if (dateObj instanceof LocalDateTime ldt) {
                return ldt;
            } else if (dateObj instanceof LocalDate ld) {
                return ld.atStartOfDay();
            }
        } catch (Exception e) {
            log.trace("Failed to parse datetime: {}", dateObj);
        }
        return null;
    }

    // ==================== Helpers for the new analytics functions ====================

    private List<Map<String, Object>> safeGetLeads() {
        var response = leadClient.getAllLeads();
        if (response == null || response.getData() == null) return List.of();
        return response.getData();
    }

    private List<Map<String, Object>> safeGetOpportunities() {
        var response = opportunityClient.getAllOpportunities();
        if (response == null || response.getData() == null) return List.of();
        return response.getData();
    }

    private boolean isQualifiedLeadStatus(String status) {
        return "QUALIFIED".equals(status)
                || "QUALIFIED_LEAD".equals(status)
                || "CONVERTED".equals(status);
    }

    private BigDecimal parseAmount(Object raw) {
        if (raw == null) return BigDecimal.ZERO;
        try {
            if (raw instanceof BigDecimal bd) return bd;
            if (raw instanceof Number n) return new BigDecimal(n.toString());
            String s = String.valueOf(raw).trim();
            if (s.isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(s);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Long parseLong(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Number n) return n.longValue();
        try {
            String s = String.valueOf(raw).trim();
            if (s.isEmpty()) return null;
            return Long.parseLong(s);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate extractDate(Object raw) {
        if (raw == null) return null;
        if (raw instanceof LocalDate ld) return ld;
        if (raw instanceof LocalDateTime ldt) return ldt.toLocalDate();
        if (raw instanceof java.util.Date d) return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        if (raw instanceof String s && !s.isBlank()) {
            try {
                if (s.length() >= 10 && s.contains("T")) {
                    return LocalDateTime.parse(s.substring(0, 19)).toLocalDate();
                }
                return LocalDate.parse(s.substring(0, 10));
            } catch (Exception ignored) { }
        }
        return null;
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private FunnelReportDto.FunnelStage stage(String name, long count, BigDecimal amount,
                                              Long previousCount, long topCount) {
        FunnelReportDto.FunnelStage s = new FunnelReportDto.FunnelStage();
        s.setStage(name);
        s.setCount(count);
        s.setTotalAmount(amount);
        s.setConversionFromPrevious(previousCount == null ? null : percentage(count, previousCount));
        s.setConversionFromTop(percentage(count, topCount));
        return s;
    }

    /** Build dense list of period start dates between {@code start} and {@code end} inclusive. */
    private List<LocalDate> buildPeriods(LocalDate start, LocalDate end, String granularity) {
        List<LocalDate> out = new ArrayList<>();
        if (start == null || end == null || start.isAfter(end)) return out;
        LocalDate cursor = roundDown(start, granularity);
        while (!cursor.isAfter(end)) {
            out.add(cursor);
            cursor = switch (granularity) {
                case "WEEK" -> cursor.plusWeeks(1);
                case "MONTH" -> cursor.plusMonths(1);
                default -> cursor.plusDays(1);
            };
            // Safety net — guard against pathological huge ranges
            if (out.size() > 5000) break;
        }
        return out;
    }

    private LocalDate roundDown(LocalDate date, String granularity) {
        return switch (granularity) {
            case "WEEK" -> date.minusDays((long) date.getDayOfWeek().getValue() - 1);
            case "MONTH" -> date.withDayOfMonth(1);
            default -> date;
        };
    }

    /** Mutable accumulator used by {@link #getTopPerformers}. */
    private static class RankingAccumulator {
        Long userId;
        String userName;
        long wonCount;
        long lostCount;
        long totalDeals;
        BigDecimal wonRevenue = BigDecimal.ZERO;
    }
}
