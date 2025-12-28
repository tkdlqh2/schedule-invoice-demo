-- 기존 데이터 정리 후 삽입
-- 의존성이 높은 테이블부터 순서대로 삭제
TRUNCATE TABLE mock_notifications RESTART IDENTITY CASCADE;
TRUNCATE TABLE outbox_events RESTART IDENTITY CASCADE;
TRUNCATE TABLE invoice_schedules RESTART IDENTITY CASCADE;
TRUNCATE TABLE invoice_schedule_groups RESTART IDENTITY CASCADE;
TRUNCATE TABLE invoices RESTART IDENTITY CASCADE;
TRUNCATE TABLE wallet_transactions RESTART IDENTITY CASCADE;
TRUNCATE TABLE wallets RESTART IDENTITY CASCADE;
TRUNCATE TABLE corps RESTART IDENTITY CASCADE;

-- 테스트용 기관 데이터
INSERT INTO corps (id, name, business_number, contact_phone, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', '잔액 부족 테스트 기관', '111-11-11111', '010-1111-1111', NOW(), NOW());

-- 테스트용 Wallet 데이터 (잔액 100원만 보유)
INSERT INTO wallets (corp_id, balance, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 100, NOW(), NOW());

-- 테스트용 InvoiceScheduleGroup 데이터 (50000원 청구, ID 명시)
INSERT INTO invoice_schedule_groups (id, corp_id, schedule_type, interval_unit, interval_value, student_name, guardian_phone, amount, description, created_at, updated_at)
VALUES
    (1, '11111111-1111-1111-1111-111111111111', 'ONCE', NULL, NULL, '홍길동', '010-1234-5678', 50000, '잔액 부족 테스트', NOW(), NOW());

-- Sequence 동기화
SELECT setval('invoice_schedule_groups_id_seq', (SELECT MAX(id) FROM invoice_schedule_groups));

-- 테스트용 InvoiceSchedule 데이터
INSERT INTO invoice_schedules (schedule_group_id, scheduled_at, status, created_at, updated_at)
VALUES
    (1, '2025-12-15 10:00:00', 'READY', NOW(), NOW());

-- Sequence 동기화
SELECT setval('invoice_schedules_id_seq', (SELECT MAX(id) FROM invoice_schedules));