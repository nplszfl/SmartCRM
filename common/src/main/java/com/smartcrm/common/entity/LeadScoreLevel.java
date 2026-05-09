package com.smartcrm.common.entity;

/**
 * Enum for lead scoring levels.
 */
public enum LeadScoreLevel {
    HOT,      // Score >= 80
    WARM,     // Score >= 60
    COOL,     // Score >= 40
    COLD      // Score < 40
}