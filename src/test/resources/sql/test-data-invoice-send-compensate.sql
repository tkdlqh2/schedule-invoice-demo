-- InvoiceSendRequestedEventHandler - compensate_RefundWallet 테스트 데이터
DELETE FROM mock_notifications;
DELETE FROM outbox_events;
DELETE FROM invoices;
DELETE FROM wallet_transactions;
DELETE FROM wallets;
DELETE FROM corps;

-- 테스트용 기관
INSERT INTO corps (id, name, business_number, contact_phone, created_at, updated_at)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccccc', '테스트 기관', '123-45-67890', '02-1234-5678', NOW(), NOW());

-- 테스트용 지갑 (이미 10000원 차감된 상태 - 잔액 90000원)
INSERT INTO wallets (id, corp_id, balance, version, created_at, updated_at)
VALUES (302, 'cccccccc-cccc-cccc-cccc-cccccccccccc', 90000, 0, NOW(), NOW());

-- 테스트용 청구서 (환불 테스트용)
INSERT INTO invoices (id, corp_id, status, student_name, guardian_phone, amount, description, schedule_id, created_at, updated_at)
VALUES (1002, 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'PENDING', '홍길동', '000-1234-5678', 10000, '환불 테스트', NULL, NOW(), NOW());

-- 원본 사용 트랜잭션 (INVOICE_USE)
INSERT INTO wallet_transactions (id, wallet_id, type, amount, reason, invoice_id, related_transaction_id, created_at, updated_at)
VALUES ('dddddddd-dddd-dddd-dddd-dddddddddddd', 302, 'INVOICE_USE', 10000, '청구서 발송 비용', 1002, NULL, NOW(), NOW());

-- Auto increment 초기화
ALTER SEQUENCE IF EXISTS invoices_id_seq RESTART WITH 1003;