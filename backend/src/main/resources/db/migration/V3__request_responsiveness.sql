ALTER TABLE service_requests
  ADD INDEX idx_request_status_sla (status, resolved_at, sla_due_at),
  ADD INDEX idx_request_requester_created (requester_id, created_at),
  ADD INDEX idx_request_assignee_created (assignee_id, created_at);
CREATE TABLE sla_breach_alerts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  request_id BIGINT NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sla_alert_request FOREIGN KEY (request_id) REFERENCES service_requests(id) ON DELETE CASCADE
);
ALTER TABLE service_requests ADD COLUMN idempotency_key VARCHAR(180) NULL UNIQUE;
