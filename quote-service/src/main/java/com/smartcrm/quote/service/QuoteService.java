package com.smartcrm.quote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.quote.dto.*;
import com.smartcrm.quote.entity.Quote;
import com.smartcrm.quote.entity.QuoteLineItem;
import com.smartcrm.quote.repository.QuoteRepository;
import com.smartcrm.quote.repository.QuoteLineItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Quote service - manages quotes/proposals
 */
@Slf4j
@Service
public class QuoteService extends ServiceImpl<QuoteRepository, Quote> {

    private final QuoteLineItemRepository lineItemRepository;

    private static final AtomicInteger quoteSequence = new AtomicInteger(1000);

    public QuoteService(QuoteLineItemRepository lineItemRepository) {
        this.lineItemRepository = lineItemRepository;
    }

    @Transactional
    public Quote createQuote(QuoteRequest request) {
        log.info("Creating quote for opportunity: {}", request.getOpportunityId());

        Quote quote = new Quote();
        quote.setQuoteNumber(generateQuoteNumber());
        quote.setOpportunityId(request.getOpportunityId());
        quote.setAccountId(request.getAccountId());
        quote.setContactId(request.getContactId());
        quote.setTitle(request.getTitle());
        quote.setStatus("DRAFT");
        quote.setVersion(1);
        quote.setValidFrom(request.getValidFrom());
        quote.setValidUntil(request.getValidUntil());
        quote.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        quote.setPaymentTerms(request.getPaymentTerms());
        quote.setDeliveryTerms(request.getDeliveryTerms());
        quote.setNotes(request.getNotes());
        quote.setTermsAndConditions(request.getTermsAndConditions());
        quote.setInternalNotes(request.getInternalNotes());
        quote.setOwnerId(request.getOwnerId());
        quote.setOwnerName(request.getOwnerName());

        this.save(quote);

        if (request.getLineItems() != null && !request.getLineItems().isEmpty()) {
            for (int i = 0; i < request.getLineItems().size(); i++) {
                QuoteRequest.LineItemRequest itemReq = request.getLineItems().get(i);
                QuoteLineItem item = createLineItem(quote.getId(), i + 1, itemReq);
                lineItemRepository.insert(item);
            }
        }

        recalculateTotals(quote);
        this.updateById(quote);

        log.info("Quote created with ID: {} and number: {}", quote.getId(), quote.getQuoteNumber());
        return quote;
    }

    @Transactional
    public Quote updateQuote(Long id, QuoteRequest request) {
        Quote quote = this.getById(id);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote", id);
        }

        quote.setOpportunityId(request.getOpportunityId());
        quote.setAccountId(request.getAccountId());
        quote.setContactId(request.getContactId());
        quote.setTitle(request.getTitle());
        if (request.getStatus() != null) {
            quote.setStatus(request.getStatus());
        }
        quote.setValidFrom(request.getValidFrom());
        quote.setValidUntil(request.getValidUntil());
        if (request.getTaxRate() != null) {
            quote.setTaxRate(request.getTaxRate());
        }
        if (request.getDiscountPercent() != null) {
            quote.setDiscountPercent(request.getDiscountPercent());
        }
        if (request.getCurrency() != null) {
            quote.setCurrency(request.getCurrency());
        }
        quote.setPaymentTerms(request.getPaymentTerms());
        quote.setDeliveryTerms(request.getDeliveryTerms());
        quote.setNotes(request.getNotes());
        quote.setTermsAndConditions(request.getTermsAndConditions());
        quote.setInternalNotes(request.getInternalNotes());
        quote.setOwnerId(request.getOwnerId());
        quote.setOwnerName(request.getOwnerName());

        lineItemRepository.delete(new LambdaQueryWrapper<QuoteLineItem>().eq(QuoteLineItem::getQuoteId, id));

        if (request.getLineItems() != null && !request.getLineItems().isEmpty()) {
            for (int i = 0; i < request.getLineItems().size(); i++) {
                QuoteRequest.LineItemRequest itemReq = request.getLineItems().get(i);
                QuoteLineItem item = createLineItem(quote.getId(), i + 1, itemReq);
                lineItemRepository.insert(item);
            }
        }

        recalculateTotals(quote);
        this.updateById(quote);

        log.info("Quote {} updated", id);
        return quote;
    }

    public Quote submitQuote(Long id) {
        Quote quote = this.getById(id);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote", id);
        }
        quote.setStatus("SENT");
        quote.setSentAt(LocalDateTime.now());
        this.updateById(quote);
        log.info("Quote {} submitted", id);
        return quote;
    }

    public Quote approveQuote(Long id, String approvedBy) {
        Quote quote = this.getById(id);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote", id);
        }
        quote.setStatus("APPROVED");
        quote.setApprovedBy(approvedBy);
        quote.setApprovedAt(LocalDateTime.now());
        this.updateById(quote);
        log.info("Quote {} approved by {}", id, approvedBy);
        return quote;
    }

    public Quote rejectQuote(Long id, String reason) {
        Quote quote = this.getById(id);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote", id);
        }
        quote.setStatus("REJECTED");
        quote.setInternalNotes(quote.getInternalNotes() + "\n[Reject reason]: " + reason);
        this.updateById(quote);
        log.info("Quote {} rejected: {}", id, reason);
        return quote;
    }

    public Quote acceptQuote(Long id) {
        Quote quote = this.getById(id);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote", id);
        }
        quote.setStatus("ACCEPTED");
        this.updateById(quote);
        log.info("Quote {} accepted", id);
        return quote;
    }

    public Quote expireQuote(Long id) {
        Quote quote = this.getById(id);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote", id);
        }
        quote.setStatus("EXPIRED");
        this.updateById(quote);
        log.info("Quote {} expired", id);
        return quote;
    }

    public Quote cancelQuote(Long id) {
        Quote quote = this.getById(id);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote", id);
        }
        quote.setStatus("CANCELLED");
        this.updateById(quote);
        log.info("Quote {} cancelled", id);
        return quote;
    }

    public void deleteQuote(Long id) {
        this.removeById(id);
        lineItemRepository.delete(new LambdaQueryWrapper<QuoteLineItem>().eq(QuoteLineItem::getQuoteId, id));
        log.info("Quote {} deleted", id);
    }

    public Quote getQuote(Long id) {
        Quote quote = this.getById(id);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote", id);
        }
        return quote;
    }

    public Quote getQuoteByNumber(String quoteNumber) {
        Quote quote = this.getOne(new LambdaQueryWrapper<Quote>().eq(Quote::getQuoteNumber, quoteNumber));
        if (quote == null) {
            throw new ResourceNotFoundException("Quote with number: " + quoteNumber);
        }
        return quote;
    }

    public List<Quote> getQuotesByOpportunity(Long opportunityId) {
        return this.list(new LambdaQueryWrapper<Quote>().eq(Quote::getOpportunityId, opportunityId));
    }

    public List<Quote> getQuotesByAccount(Long accountId) {
        return this.list(new LambdaQueryWrapper<Quote>().eq(Quote::getAccountId, accountId));
    }

    public List<Quote> getQuotesByOwner(Long ownerId) {
        return this.list(new LambdaQueryWrapper<Quote>().eq(Quote::getOwnerId, ownerId));
    }

    public List<Quote> getQuotesByStatus(String status, int page, int size) {
        return this.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size),
                new LambdaQueryWrapper<Quote>().eq(Quote::getStatus, status)).getRecords();
    }

    public List<Quote> searchQuotes(String keyword, String status, LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {
        LambdaQueryWrapper<Quote> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Quote::getTitle, keyword).or().like(Quote::getQuoteNumber, keyword);
        }
        if (status != null) {
            wrapper.eq(Quote::getStatus, status);
        }
        if (fromDate != null) {
            wrapper.ge(Quote::getCreatedAt, fromDate);
        }
        if (toDate != null) {
            wrapper.le(Quote::getCreatedAt, toDate);
        }
        return this.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), wrapper).getRecords();
    }

    public List<Quote> getDraftQuotes() {
        return this.list(new LambdaQueryWrapper<Quote>().eq(Quote::getStatus, "DRAFT"));
    }

    public List<Quote> getExpiringQuotes(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(daysThreshold);
        return this.list(new LambdaQueryWrapper<Quote>()
                .eq(Quote::getStatus, "SENT")
                .lt(Quote::getValidUntil, threshold)
                .gt(Quote::getValidUntil, LocalDateTime.now()));
    }

    @Transactional
    public Quote cloneQuote(Long id) {
        Quote original = this.getById(id);
        if (original == null) {
            throw new ResourceNotFoundException("Quote", id);
        }

        Quote cloned = new Quote();
        cloned.setQuoteNumber(generateQuoteNumber());
        cloned.setOpportunityId(original.getOpportunityId());
        cloned.setAccountId(original.getAccountId());
        cloned.setContactId(original.getContactId());
        cloned.setTitle(original.getTitle() + " (Copy)");
        cloned.setStatus("DRAFT");
        cloned.setVersion(1);
        cloned.setValidFrom(LocalDateTime.now());
        cloned.setValidUntil(original.getValidUntil());
        cloned.setSubtotal(original.getSubtotal());
        cloned.setTaxRate(original.getTaxRate());
        cloned.setTaxAmount(original.getTaxAmount());
        cloned.setDiscountPercent(original.getDiscountPercent());
        cloned.setDiscountAmount(original.getDiscountAmount());
        cloned.setTotalAmount(original.getTotalAmount());
        cloned.setCurrency(original.getCurrency());
        cloned.setPaymentTerms(original.getPaymentTerms());
        cloned.setDeliveryTerms(original.getDeliveryTerms());
        cloned.setNotes(original.getNotes());
        cloned.setTermsAndConditions(original.getTermsAndConditions());
        cloned.setOwnerId(original.getOwnerId());
        cloned.setOwnerName(original.getOwnerName());

        this.save(cloned);

        List<QuoteLineItem> originalItems = lineItemRepository.selectList(
                new LambdaQueryWrapper<QuoteLineItem>().eq(QuoteLineItem::getQuoteId, id));
        for (QuoteLineItem originalItem : originalItems) {
            QuoteLineItem newItem = new QuoteLineItem();
            newItem.setQuoteId(cloned.getId());
            newItem.setLineNumber(originalItem.getLineNumber());
            newItem.setProductId(originalItem.getProductId());
            newItem.setProductName(originalItem.getProductName());
            newItem.setProductCode(originalItem.getProductCode());
            newItem.setDescription(originalItem.getDescription());
            newItem.setQuantity(originalItem.getQuantity());
            newItem.setUnitPrice(originalItem.getUnitPrice());
            newItem.setUnit(originalItem.getUnit());
            newItem.setDiscountPercent(originalItem.getDiscountPercent());
            newItem.setLineTotal(originalItem.getLineTotal());
            newItem.setSortOrder(originalItem.getSortOrder());
            lineItemRepository.insert(newItem);
        }

        log.info("Quote {} cloned to new quote {}", id, cloned.getId());
        return cloned;
    }

    public void recalculateTotals(Quote quote) {
        List<QuoteLineItem> items = lineItemRepository.selectList(
                new LambdaQueryWrapper<QuoteLineItem>().eq(QuoteLineItem::getQuoteId, quote.getId()));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (QuoteLineItem item : items) {
            BigDecimal lineTotal = item.getQuantity().multiply(item.getUnitPrice());
            if (item.getDiscountPercent() != null && item.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discount = lineTotal.multiply(item.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                lineTotal = lineTotal.subtract(discount);
            }
            item.setLineTotal(lineTotal);
            lineItemRepository.updateById(item);
            subtotal = subtotal.add(lineTotal);
        }

        quote.setSubtotal(subtotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (quote.getDiscountPercent() != null && quote.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = subtotal.multiply(quote.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        quote.setDiscountAmount(discountAmount);

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);

        BigDecimal taxAmount = BigDecimal.ZERO;
        if (quote.getTaxRate() != null && quote.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
            taxAmount = afterDiscount.multiply(quote.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        quote.setTaxAmount(taxAmount);

        BigDecimal total = afterDiscount.add(taxAmount);
        quote.setTotalAmount(total);
    }

    public String generateQuoteNumber() {
        String datePrefix = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        int sequence = quoteSequence.getAndIncrement() % 10000;
        return String.format("QT-%s-%04d", datePrefix, sequence);
    }

    @Transactional
    public QuoteLineItem addLineItem(Long quoteId, QuoteLineItemRequest request) {
        Quote quote = this.getById(quoteId);
        if (quote == null) {
            throw new ResourceNotFoundException("Quote", quoteId);
        }

        int maxLineNumber = lineItemRepository.selectList(
                        new LambdaQueryWrapper<QuoteLineItem>()
                                .eq(QuoteLineItem::getQuoteId, quoteId)
                                .orderByDesc(QuoteLineItem::getLineNumber))
                .stream()
                .mapToInt(QuoteLineItem::getLineNumber)
                .max()
                .orElse(0);

        QuoteLineItem item = new QuoteLineItem();
        item.setQuoteId(quoteId);
        item.setLineNumber(maxLineNumber + 1);
        item.setProductId(request.getProductId());
        item.setProductName(request.getProductName());
        item.setProductCode(request.getProductCode());
        item.setDescription(request.getDescription());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setUnit(request.getUnit() != null ? request.getUnit() : "UNIT");
        item.setDiscountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO);
        item.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : maxLineNumber + 1);

        lineItemRepository.insert(item);
        recalculateTotals(quote);
        this.updateById(quote);

        return item;
    }

    @Transactional
    public QuoteLineItem updateLineItem(Long lineItemId, QuoteLineItemRequest request) {
        QuoteLineItem item = lineItemRepository.selectById(lineItemId);
        if (item == null) {
            throw new ResourceNotFoundException("QuoteLineItem", lineItemId);
        }

        item.setProductId(request.getProductId());
        item.setProductName(request.getProductName());
        item.setProductCode(request.getProductCode());
        item.setDescription(request.getDescription());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        if (request.getUnit() != null) {
            item.setUnit(request.getUnit());
        }
        if (request.getDiscountPercent() != null) {
            item.setDiscountPercent(request.getDiscountPercent());
        }
        if (request.getSortOrder() != null) {
            item.setSortOrder(request.getSortOrder());
        }

        lineItemRepository.updateById(item);
        recalculateTotals(this.getById(item.getQuoteId()));
        this.updateById(this.getById(item.getQuoteId()));

        return item;
    }

    @Transactional
    public void removeLineItem(Long lineItemId) {
        QuoteLineItem item = lineItemRepository.selectById(lineItemId);
        if (item == null) {
            throw new ResourceNotFoundException("QuoteLineItem", lineItemId);
        }
        Long quoteId = item.getQuoteId();
        lineItemRepository.deleteById(lineItemId);
        recalculateTotals(this.getById(quoteId));
        this.updateById(this.getById(quoteId));
    }

    public List<QuoteLineItem> getLineItems(Long quoteId) {
        return lineItemRepository.selectList(
                new LambdaQueryWrapper<QuoteLineItem>()
                        .eq(QuoteLineItem::getQuoteId, quoteId)
                        .orderByAsc(QuoteLineItem::getSortOrder));
    }

    public QuoteStatsDTO getStats() {
        QuoteStatsDTO stats = new QuoteStatsDTO();

        long total = this.count();
        long draftCount = this.count(new LambdaQueryWrapper<Quote>().eq(Quote::getStatus, "DRAFT"));
        long sentCount = this.count(new LambdaQueryWrapper<Quote>().eq(Quote::getStatus, "SENT"));
        long approvedCount = this.count(new LambdaQueryWrapper<Quote>().eq(Quote::getStatus, "APPROVED"));
        long acceptedCount = this.count(new LambdaQueryWrapper<Quote>().eq(Quote::getStatus, "ACCEPTED"));
        long expiredCount = this.count(new LambdaQueryWrapper<Quote>().eq(Quote::getStatus, "EXPIRED"));
        long cancelledCount = this.count(new LambdaQueryWrapper<Quote>().eq(Quote::getStatus, "CANCELLED"));

        List<Quote> nonCancelledQuotes = this.list(
                new LambdaQueryWrapper<Quote>().ne(Quote::getStatus, "CANCELLED"));
        BigDecimal totalValue = nonCancelledQuotes.stream()
                .map(Quote::getTotalAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgAmount = total > 0 ? totalValue.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        List<Quote> approvedQuotes = this.list(new LambdaQueryWrapper<Quote>().eq(Quote::getStatus, "APPROVED"));
        Double avgApprovalTime = 0.0;
        if (!approvedQuotes.isEmpty()) {
            long totalApprovalHours = approvedQuotes.stream()
                    .filter(q -> q.getSentAt() != null && q.getApprovedAt() != null)
                    .mapToLong(q -> java.time.Duration.between(q.getSentAt(), q.getApprovedAt()).toHours())
                    .sum();
            long countWithDates = approvedQuotes.stream()
                    .filter(q -> q.getSentAt() != null && q.getApprovedAt() != null)
                    .count();
            avgApprovalTime = countWithDates > 0 ? (double) totalApprovalHours / countWithDates : 0.0;
        }

        double acceptanceRate = total > 0 ? (double) acceptedCount / total * 100 : 0.0;

        stats.setTotalQuotes(total);
        stats.setDraftCount(draftCount);
        stats.setSentCount(sentCount);
        stats.setApprovedCount(approvedCount);
        stats.setAcceptedCount(acceptedCount);
        stats.setExpiredCount(expiredCount);
        stats.setCancelledCount(cancelledCount);
        stats.setTotalValue(totalValue);
        stats.setAverageApprovalTime(avgApprovalTime);
        stats.setAcceptanceRate(acceptanceRate);
        stats.setAverageAmount(avgAmount);

        return stats;
    }

    private QuoteLineItem createLineItem(Long quoteId, int lineNumber, QuoteRequest.LineItemRequest request) {
        QuoteLineItem item = new QuoteLineItem();
        item.setQuoteId(quoteId);
        item.setLineNumber(lineNumber);
        item.setProductId(request.getProductId());
        item.setProductName(request.getProductName());
        item.setProductCode(request.getProductCode());
        item.setDescription(request.getDescription());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setUnit(request.getUnit() != null ? request.getUnit() : "UNIT");
        item.setDiscountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO);
        item.setSortOrder(lineNumber);
        return item;
    }

    public QuoteResponse toResponse(Quote quote) {
        QuoteResponse response = new QuoteResponse();
        response.setId(quote.getId());
        response.setQuoteNumber(quote.getQuoteNumber());
        response.setOpportunityId(quote.getOpportunityId());
        response.setAccountId(quote.getAccountId());
        response.setContactId(quote.getContactId());
        response.setTitle(quote.getTitle());
        response.setStatus(quote.getStatus());
        response.setVersion(quote.getVersion());
        response.setValidFrom(quote.getValidFrom());
        response.setValidUntil(quote.getValidUntil());
        response.setSubtotal(quote.getSubtotal());
        response.setTaxRate(quote.getTaxRate());
        response.setTaxAmount(quote.getTaxAmount());
        response.setDiscountPercent(quote.getDiscountPercent());
        response.setDiscountAmount(quote.getDiscountAmount());
        response.setTotalAmount(quote.getTotalAmount());
        response.setCurrency(quote.getCurrency());
        response.setPaymentTerms(quote.getPaymentTerms());
        response.setDeliveryTerms(quote.getDeliveryTerms());
        response.setNotes(quote.getNotes());
        response.setTermsAndConditions(quote.getTermsAndConditions());
        response.setInternalNotes(quote.getInternalNotes());
        response.setOwnerId(quote.getOwnerId());
        response.setOwnerName(quote.getOwnerName());
        response.setApprovedBy(quote.getApprovedBy());
        response.setApprovedAt(quote.getApprovedAt());
        response.setSentAt(quote.getSentAt());
        response.setViewedAt(quote.getViewedAt());
        response.setLastFollowupAt(quote.getLastFollowupAt());
        response.setAiGenerated(quote.getAiGenerated());
        response.setAiConfidenceScore(quote.getAiConfidenceScore());
        response.setCreatedAt(quote.getCreatedAt());
        response.setUpdatedAt(quote.getUpdatedAt());

        List<QuoteLineItem> items = getLineItems(quote.getId());
        response.setLineItems(items.stream().map(this::toLineItemResponse).collect(Collectors.toList()));

        return response;
    }

    public QuoteLineItemResponse toLineItemResponse(QuoteLineItem item) {
        QuoteLineItemResponse response = new QuoteLineItemResponse();
        response.setId(item.getId());
        response.setQuoteId(item.getQuoteId());
        response.setLineNumber(item.getLineNumber());
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setProductCode(item.getProductCode());
        response.setDescription(item.getDescription());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setUnit(item.getUnit());
        response.setDiscountPercent(item.getDiscountPercent());
        response.setLineTotal(item.getLineTotal());
        response.setSortOrder(item.getSortOrder());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}