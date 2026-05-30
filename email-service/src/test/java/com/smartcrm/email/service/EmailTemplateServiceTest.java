package com.smartcrm.email.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailTemplateService - template variable substitution logic
 * These tests focus on the business logic without requiring MyBatis Plus infrastructure.
 */
class EmailTemplateServiceTest {

    private EmailTemplateService emailTemplateService = new EmailTemplateService();

    @Test
    void testApplyTemplate_WithVariables() {
        String content = "Hello {{name}}";
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "John");

        String result = emailTemplateService.applyTemplate(content, variables);

        assertEquals("Hello John", result);
    }

    @Test
    void testApplyTemplate_WithMultipleVariables() {
        String content = "Dear {{name}},\n\nYour order #{{orderId}} is ready.";
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "John");
        variables.put("orderId", "12345");

        String result = emailTemplateService.applyTemplate(content, variables);

        assertEquals("Dear John,\n\nYour order #12345 is ready.", result);
    }

    @Test
    void testApplyTemplate_WithMissingVariables() {
        String content = "Dear {{name}},\n\nYour order #{{orderId}} is ready.";
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "John");
        // orderId is missing

        String result = emailTemplateService.applyTemplate(content, variables);

        assertEquals("Dear John,\n\nYour order #{{orderId}} is ready.", result);
    }

    @Test
    void testApplyTemplate_WithNullVariables() {
        String content = "Hello {{name}}";

        String result = emailTemplateService.applyTemplate(content, null);

        assertEquals("Hello {{name}}", result);
    }

    @Test
    void testApplyTemplate_WithEmptyVariables() {
        String content = "Hello {{name}}";

        String result = emailTemplateService.applyTemplate(content, new HashMap<>());

        assertEquals("Hello {{name}}", result);
    }

    @Test
    void testApplyTemplate_WithNullContent() {
        String result = emailTemplateService.applyTemplate(null, Map.of("name", "John"));

        assertNull(result);
    }

    @Test
    void testApplyTemplate_WithEmptyContent() {
        String result = emailTemplateService.applyTemplate("", Map.of("name", "John"));

        assertEquals("", result);
    }

    @Test
    void testApplyTemplate_WithNoPlaceholders() {
        String content = "Hello John";

        String result = emailTemplateService.applyTemplate(content, Map.of("name", "Jane"));

        assertEquals("Hello John", result);
    }

    @Test
    void testApplyTemplate_ReplacesMultipleSameVariable() {
        String content = "{{name}} sent a message to {{name}}";
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "Alice");

        String result = emailTemplateService.applyTemplate(content, variables);

        assertEquals("Alice sent a message to Alice", result);
    }

    @Test
    void testApplyTemplate_WithNumericVariables() {
        String content = "Order amount: ${{amount}}";
        Map<String, String> variables = new HashMap<>();
        variables.put("amount", "99.99");

        String result = emailTemplateService.applyTemplate(content, variables);

        assertEquals("Order amount: $99.99", result);
    }
}