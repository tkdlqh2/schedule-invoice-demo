-- InvoiceSendRequestedEventHandler - handle_Success 테스트 데이터
-- 의존성이 높은 테이블부터 순서대로 삭제
TRUNCATE TABLE mock_notifications RESTART IDENTITY CASCADE;
TRUNCATE TABLE outbox_events RESTART IDENTITY CASCADE;
TRUNCATE TABLE invoice_schedules RESTART IDENTITY CASCADE;
TRUNCATE TABLE invoice_schedule_groups RESTART IDENTITY CASCADE;
TRUNCATE TABLE invoices RESTART IDENTITY CASCADE;
TRUNCATE TABLE wallet_transactions RESTART IDENTITY CASCADE;
TRUNCATE TABLE wallets RESTART IDENTITY CASCADE;
TRUNCATE TABLE corps RESTART IDENTITY CASCADE;

-- 테스트용 기관
INSERT INTO corps (id, name, business_number, contact_phone, created_at, updated_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '테스트 기관', '123-45-67890', '02-1234-5678', NOW(), NOW());

-- 테스트용 지갑 (잔액 100000원)
INSERT INTO wallets (id, corp_id, balance, version, created_at, updated_at)
VALUES (300, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 100000, 0, NOW(), NOW());

-- 테스트용 청구서 (발송 성공 케이스 - 010으로 시작하는 전화번호)
INSERT INTO invoices (id, corp_id, status, student_name, guardian_phone, amount, description, schedule_id, created_at, updated_at)
VALUES (1000, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'PENDING', '홍길동', '010-1234-5678', 10000, '테스트 발송', NULL, NOW(), NOW());

-- Auto increment 초기화
ALTER SEQUENCE IF EXISTS invoices_id_seq RESTART WITH 1001;