-- 기존 데이터 정리 후 삽입
DELETE FROM mock_notifications;
DELETE FROM invoices;
DELETE FROM wallet_transactions;
DELETE FROM wallets;
DELETE FROM corps;

-- 테스트용 기관 데이터
INSERT INTO corps (id, name, business_number, contact_phone, created_at, updated_at)
VALUES
    ('66666666-6666-6666-6666-666666666666', 'Invoice 테스트 기관 A', '666-66-66666', '010-6666-6666', NOW(), NOW()),
    ('77777777-7777-7777-7777-777777777777', 'Invoice 테스트 기관 B', '777-77-77777', '010-7777-7777', NOW(), NOW()),
    ('88888888-8888-8888-8888-888888888888', 'Invoice 테스트 기관 C (Wallet 없음)', '888-88-88888', '010-8888-8888', NOW(), NOW());

-- 테스트용 지갑 데이터 (id를 큰 값으로 설정하여 auto-increment와 충돌 방지)
INSERT INTO wallets (id, corp_id, balance, version, created_at, updated_at)
VALUES
    (200, '66666666-6666-6666-6666-666666666666', 50000, 0, NOW(), NOW()),  -- 충분한 잔액
    (201, '77777777-7777-7777-7777-777777777777', 100, 0, NOW(), NOW());    -- 부족한 잔액

-- 테스트용 지갑 트랜잭션 데이터
INSERT INTO wallet_transactions (id, wallet_id, type, amount, reason, created_at, updated_at)
VALUES
    ('22222222-2222-2222-2222-222222222222', 200, 'FREE_CHARGE', 50000, '초기 포인트 지급', NOW(), NOW()),
    ('33333333-3333-3333-3333-333333333333', 201, 'FREE_CHARGE', 100, '초기 포인트 지급', NOW(), NOW());