-- Outbox Events 테이블
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    payload TEXT,
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retry_count INTEGER NOT NULL DEFAULT 3,
    processed_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_outbox_events_status ON outbox_events(status);
CREATE INDEX idx_outbox_events_status_created_at ON outbox_events(status, created_at);
CREATE INDEX idx_outbox_events_aggregate ON outbox_events(aggregate_type, aggregate_id);

-- 코멘트 추가
COMMENT ON TABLE outbox_events IS 'Transactional Outbox Pattern 이벤트 테이블';
COMMENT ON COLUMN outbox_events.event_type IS 'INVOICE_SEND_REQUESTED 등';
COMMENT ON COLUMN outbox_events.aggregate_type IS '집합 루트 타입 (예: INVOICE)';
COMMENT ON COLUMN outbox_events.aggregate_id IS '집합 루트 ID';
COMMENT ON COLUMN outbox_events.payload IS 'JSON 형식의 이벤트 페이로드 (선택)';
COMMENT ON COLUMN outbox_events.status IS 'PENDING, PROCESSING, COMPLETED, FAILED';
COMMENT ON COLUMN outbox_events.retry_count IS '재시도 횟수';
COMMENT ON COLUMN outbox_events.max_retry_count IS '최대 재시도 횟수';
COMMENT ON COLUMN outbox_events.processed_at IS '처리 완료 시각';
COMMENT ON COLUMN outbox_events.error_message IS '실패 시 에러 메시지';