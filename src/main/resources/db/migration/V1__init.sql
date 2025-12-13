-- Corps 테이블
CREATE TABLE corps (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    business_number VARCHAR(20),
    contact_phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_corps_name ON corps(name);
CREATE INDEX idx_corps_business_number ON corps(business_number);

-- Wallets 테이블
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    corp_id UUID NOT NULL UNIQUE,
    balance BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_wallets_corp FOREIGN KEY (corp_id) REFERENCES corps(id)
);

CREATE INDEX idx_wallets_corp_id ON wallets(corp_id);

-- Wallet Transactions 테이블
CREATE TABLE wallet_transactions (
    id UUID PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    invoice_id BIGINT,
    related_transaction_id UUID,
    reason VARCHAR(200),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_wallet_transactions_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    CONSTRAINT fk_wallet_transactions_related FOREIGN KEY (related_transaction_id) REFERENCES wallet_transactions(id)
);

CREATE INDEX idx_wallet_transactions_wallet_id ON wallet_transactions(wallet_id);
CREATE INDEX idx_wallet_transactions_invoice_id ON wallet_transactions(invoice_id);
CREATE INDEX idx_wallet_transactions_type ON wallet_transactions(type);
CREATE INDEX idx_wallet_transactions_created_at ON wallet_transactions(created_at DESC);

-- Invoices 테이블
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    corp_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    student_name VARCHAR(50) NOT NULL,
    guardian_phone VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    description VARCHAR(200),
    schedule_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_invoices_corp FOREIGN KEY (corp_id) REFERENCES corps(id)
);

CREATE INDEX idx_invoices_corp_id ON invoices(corp_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_schedule_id ON invoices(schedule_id);
CREATE INDEX idx_invoices_created_at ON invoices(created_at DESC);

-- Invoice Schedule Groups 테이블
CREATE TABLE invoice_schedule_groups (
    id BIGSERIAL PRIMARY KEY,
    corp_id UUID NOT NULL,
    schedule_type VARCHAR(20) NOT NULL,
    interval_unit VARCHAR(10),
    interval_value INTEGER,
    student_name VARCHAR(50) NOT NULL,
    guardian_phone VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_invoice_schedule_groups_corp FOREIGN KEY (corp_id) REFERENCES corps(id)
);

CREATE INDEX idx_invoice_schedule_groups_corp_id ON invoice_schedule_groups(corp_id);
CREATE INDEX idx_invoice_schedule_groups_schedule_type ON invoice_schedule_groups(schedule_type);
CREATE INDEX idx_invoice_schedule_groups_created_at ON invoice_schedule_groups(created_at DESC);

-- Invoice Schedules 테이블
CREATE TABLE invoice_schedules (
    id BIGSERIAL PRIMARY KEY,
    schedule_group_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    executed_at TIMESTAMP,
    invoice_id BIGINT,
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_invoice_schedules_group FOREIGN KEY (schedule_group_id) REFERENCES invoice_schedule_groups(id)
);

CREATE INDEX idx_invoice_schedules_group_id ON invoice_schedules(schedule_group_id);
CREATE INDEX idx_invoice_schedules_status_scheduled_at ON invoice_schedules(status, scheduled_at);
CREATE INDEX idx_invoice_schedules_invoice_id ON invoice_schedules(invoice_id);

-- Mock Notifications 테이블
CREATE TABLE mock_notifications (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    corp_id UUID NOT NULL,
    student_name VARCHAR(50) NOT NULL,
    guardian_phone VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    description VARCHAR(200),
    sent_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_mock_notifications_invoice_id ON mock_notifications(invoice_id);
CREATE INDEX idx_mock_notifications_corp_id ON mock_notifications(corp_id);
CREATE INDEX idx_mock_notifications_sent_at ON mock_notifications(sent_at DESC);

-- 코멘트 추가
COMMENT ON TABLE corps IS '기관(학원) 테이블';
COMMENT ON TABLE wallets IS '지갑 테이블 - 기관당 1개';
COMMENT ON TABLE wallet_transactions IS '지갑 트랜잭션 (불변 원장)';
COMMENT ON TABLE invoices IS '청구서 테이블';
COMMENT ON TABLE invoice_schedule_groups IS '청구서 스케줄 그룹 (템플릿 정보 포함) - 수정 불가, 삭제 및 생성만 가능';
COMMENT ON TABLE invoice_schedules IS '청구서 스케줄 (실행 단위)';
COMMENT ON TABLE mock_notifications IS 'Mock 발송 로그';

COMMENT ON COLUMN wallet_transactions.type IS 'FREE_CHARGE, INVOICE_USE, INVOICE_REFUND';
COMMENT ON COLUMN wallet_transactions.amount IS '트랜잭션 금액 (부호 포함)';
COMMENT ON COLUMN wallet_transactions.related_transaction_id IS '환불의 경우 원본 사용 트랜잭션 ID';

COMMENT ON COLUMN invoices.status IS 'PENDING, SENT, FAILED';
COMMENT ON COLUMN invoices.schedule_id IS '스케줄로부터 생성된 경우 스케줄 ID';

COMMENT ON COLUMN invoice_schedule_groups.schedule_type IS 'ONCE, RECURRING';
COMMENT ON COLUMN invoice_schedule_groups.interval_unit IS 'DAY, WEEK, MONTH, YEAR (RECURRING인 경우 필수)';
COMMENT ON COLUMN invoice_schedule_groups.interval_value IS '반복 주기 값 (RECURRING인 경우 필수)';

COMMENT ON COLUMN invoice_schedules.status IS 'READY, PROCESSING, COMPLETED, FAILED';
