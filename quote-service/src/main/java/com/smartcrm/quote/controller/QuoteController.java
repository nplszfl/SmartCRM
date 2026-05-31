package com.smartcrm.quote.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.quote.dto.*;
import com.smartcrm.quote.entity.Quote;
import com.smartcrm.quote.entity.QuoteLineItem;
import com.smartcrm.quote.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Quote REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ApiResponse<Quote> createQuote(@RequestBody QuoteRequest request) {
        Quote quote = quoteService.createQuote(request);
        return ApiResponse.success(quote);
    }

    @PutMapping("/{id}")
    public ApiResponse<Quote> updateQuote(@PathVariable Long id, @RequestBody QuoteRequest request) {
        Quote quote = quoteService.updateQuote(id, request);
        return ApiResponse.success(quote);
    }

    @PatchMapping("/{id}/submit")
    public ApiResponse<Quote> submitQuote(@PathVariable Long id) {
        Quote quote = quoteService.submitQuote(id);
        return ApiResponse.success(quote);
    }

    @PatchMapping("/{id}/approve")
    public ApiResponse<Quote> approveQuote(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String approvedBy = body.get("approvedBy");
        Quote quote = quoteService.approveQuote(id, approvedBy);
        return ApiResponse.success(quote);
    }

    @PatchMapping("/{id}/reject")
    public ApiResponse<Quote> rejectQuote(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        Quote quote = quoteService.rejectQuote(id, reason);
        return ApiResponse.success(quote);
    }

    @PatchMapping("/{id}/accept")
    public ApiResponse<Quote> acceptQuote(@PathVariable Long id) {
        Quote quote = quoteService.acceptQuote(id);
        return ApiResponse.success(quote);
    }

    @PatchMapping("/{id}/expire")
    public ApiResponse<Quote> expireQuote(@PathVariable Long id) {
        Quote quote = quoteService.expireQuote(id);
        return ApiResponse.success(quote);
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<Quote> cancelQuote(@PathVariable Long id) {
        Quote quote = quoteService.cancelQuote(id);
        return ApiResponse.success(quote);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteQuote(@PathVariable Long id) {
        quoteService.deleteQuote(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<Quote> getQuote(@PathVariable Long id) {
        Quote quote = quoteService.getQuote(id);
        return ApiResponse.success(quote);
    }

    @GetMapping("/number/{quoteNumber}")
    public ApiResponse<Quote> getQuoteByNumber(@PathVariable String quoteNumber) {
        Quote quote = quoteService.getQuoteByNumber(quoteNumber);
        return ApiResponse.success(quote);
    }

    @GetMapping("/opportunity/{opportunityId}")
    public ApiResponse<List<Quote>> getQuotesByOpportunity(@PathVariable Long opportunityId) {
        List<Quote> quotes = quoteService.getQuotesByOpportunity(opportunityId);
        return ApiResponse.success(quotes);
    }

    @GetMapping("/account/{accountId}")
    public ApiResponse<List<Quote>> getQuotesByAccount(@PathVariable Long accountId) {
        List<Quote> quotes = quoteService.getQuotesByAccount(accountId);
        return ApiResponse.success(quotes);
    }

    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Quote>> getQuotesByOwner(@PathVariable Long ownerId) {
        List<Quote> quotes = quoteService.getQuotesByOwner(ownerId);
        return ApiResponse.success(quotes);
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<Quote>> getQuotesByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Quote> quotes = quoteService.getQuotesByStatus(status, page, size);
        return ApiResponse.success(quotes);
    }

    @GetMapping("/search")
    public ApiResponse<List<Quote>> searchQuotes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Quote> quotes = quoteService.searchQuotes(keyword, status, fromDate, toDate, page, size);
        return ApiResponse.success(quotes);
    }

    @GetMapping("/draft")
    public ApiResponse<List<Quote>> getDraftQuotes() {
        List<Quote> quotes = quoteService.getDraftQuotes();
        return ApiResponse.success(quotes);
    }

    @GetMapping("/expiring")
    public ApiResponse<List<Quote>> getExpiringQuotes(
            @RequestParam(defaultValue = "7") int days) {
        List<Quote> quotes = quoteService.getExpiringQuotes(days);
        return ApiResponse.success(quotes);
    }

    @PostMapping("/{id}/clone")
    public ApiResponse<Quote> cloneQuote(@PathVariable Long id) {
        Quote quote = quoteService.cloneQuote(id);
        return ApiResponse.success(quote);
    }

    @PostMapping("/{id}/line-items")
    public ApiResponse<QuoteLineItem> addLineItem(
            @PathVariable Long id,
            @RequestBody QuoteLineItemRequest request) {
        QuoteLineItem item = quoteService.addLineItem(id, request);
        return ApiResponse.success(item);
    }

    @PutMapping("/line-items/{lineItemId}")
    public ApiResponse<QuoteLineItem> updateLineItem(
            @PathVariable Long lineItemId,
            @RequestBody QuoteLineItemRequest request) {
        QuoteLineItem item = quoteService.updateLineItem(lineItemId, request);
        return ApiResponse.success(item);
    }

    @DeleteMapping("/line-items/{lineItemId}")
    public ApiResponse<Void> removeLineItem(@PathVariable Long lineItemId) {
        quoteService.removeLineItem(lineItemId);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/line-items")
    public ApiResponse<List<QuoteLineItem>> getLineItems(@PathVariable Long id) {
        List<QuoteLineItem> items = quoteService.getLineItems(id);
        return ApiResponse.success(items);
    }

    @GetMapping("/stats")
    public ApiResponse<QuoteStatsDTO> getStats() {
        QuoteStatsDTO stats = quoteService.getStats();
        return ApiResponse.success(stats);
    }
}