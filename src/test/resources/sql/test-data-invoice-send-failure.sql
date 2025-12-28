-- InvoiceSendRequestedEventHandler - handle_Failure 테스트 데이터
DELETE FROM mock_notifications;
DELETE FROM outbox_events;
DELETE FROM invoices;
DELETE FROM wallet_transactions;
DELETE FROM wallets;
DELETE FROM corps;

-- 테스트용 기관
INSERT INTO corps (id, name, business_number, contact_phone, created_at, updated_at)
VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '테스트 기관', '123-45-67890', '02-1234-5678', NOW(), NOW());

-- 테스트용 지갑 (잔액 100000원)
INSERT INTO wallets (id, corp_id, balance, version, created_at, updated_at)
VALUES (301, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 100000, 0, NOW(), NOW());

-- 테스트용 청구서 (발송 실패 케이스 - 000으로 시작하는 전화번호)
INSERT INTO invoices (id, corp_id, status, student_name, guardian_phone, amount, description, schedule_id, created_at, updated_at)
VALUES (1001, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'PENDING', '홍길동', '000-1234-5678', 10000, '테스트 발송', NULL, NOW(), NOW());

-- Auto increment 초기화
ALTER SEQUENCE IF EXISTS invoices_id_seq RESTART WITH 1002;