ALTER TABLE service_requests
  ADD COLUMN impact VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
  ADD COLUMN urgency VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
  ADD COLUMN priority_reason VARCHAR(255) NOT NULL DEFAULT 'Default priority based on impact and urgency',
  ADD COLUMN sla_due_at TIMESTAMP NULL,
  ADD COLUMN resolved_at TIMESTAMP NULL,
  ADD COLUMN resolution_seconds BIGINT NULL,
  ADD COLUMN resolved_on_time BOOLEAN NULL,
  ADD INDEX idx_request_sla_due_status (status, sla_due_at),
  ADD INDEX idx_request_priority_status (priority, status);

UPDATE service_requests
SET sla_due_at = CASE UPPER(priority)
  WHEN 'URGENT' THEN DATE_ADD(created_at, INTERVAL 4 HOUR)
  WHEN 'HIGH' THEN DATE_ADD(created_at, INTERVAL 8 HOUR)
  WHEN 'MEDIUM' THEN DATE_ADD(created_at, INTERVAL 24 HOUR)
  ELSE DATE_ADD(created_at, INTERVAL 72 HOUR)
END,
priority = CASE UPPER(priority)
  WHEN 'URGENT' THEN 'P1'
  WHEN 'HIGH' THEN 'P2'
  WHEN 'MEDIUM' THEN 'P3'
  ELSE 'P4'
END;

ALTER TABLE audit_logs
  ADD COLUMN old_value VARCHAR(255) NULL,
  ADD COLUMN new_value VARCHAR(255) NULL,
  ADD COLUMN reason VARCHAR(1000) NULL;

UPDATE service_requests
SET resolved_at = updated_at,
    resolution_seconds = GREATEST(0, TIMESTAMPDIFF(SECOND, created_at, updated_at)),
    resolved_on_time = (updated_at <= sla_due_at)
WHERE status = 'RESOLVED' AND resolved_at IS NULL;
