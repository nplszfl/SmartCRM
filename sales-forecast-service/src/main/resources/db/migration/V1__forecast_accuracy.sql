-- Sales Forecast Accuracy Tracking
-- Tracks actual vs predicted values, accuracy score, and deviation for sales forecasts.
CREATE TABLE IF NOT EXISTS forecast_accuracy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    forecast_id BIGINT NOT NULL COMMENT 'Foreign key to sales_forecasts.id (logical)',
    period VARCHAR(20) NOT NULL COMMENT 'Period identifier, e.g. 2025-06',
    predicted DECIMAL(15, 2) NOT NULL COMMENT 'Predicted value at forecast time',
    actual DECIMAL(15, 2) NOT NULL COMMENT 'Actual realized value',
    deviation DECIMAL(15, 2) NOT NULL COMMENT 'Absolute deviation actual - predicted',
    accuracy DOUBLE NOT NULL COMMENT 'Accuracy score in [0, 1] (1 = perfect, 0 = worst)',
    alert_level VARCHAR(16) NOT NULL DEFAULT 'OK' COMMENT 'Alert level: OK, WARN, CRITICAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_forecast_id (forecast_id),
    INDEX idx_period (period),
    INDEX idx_accuracy (accuracy),
    INDEX idx_alert_level (alert_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
