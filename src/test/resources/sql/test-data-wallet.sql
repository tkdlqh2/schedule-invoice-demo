-- 기존 데이터 정리 후 삽입
DELETE FROM wallet_transactions;
DELETE FROM wallets;
DELETE FROM corps;

-- 테스트용 기관 데이터
INSERT INTO corps (id, name, business_number, contact_phone, created_at, updated_at)
VALUES
    ('44444444-4444-4444-4444-444444444444', '지갑 테스트 기관 A', '444-44-44444', '010-4444-4444', NOW(), NOW()),
    ('55555555-5555-5555-5555-555555555555', '지갑 테스트 기관 B', '555-55-55555', '010-5555-5555', NOW(), NOW());

-- 테스트용 지갑 데이터 (id를 큰 값으로 설정하여 auto-increment와 충돌 방지)
INSERT INTO wallets (id, corp_id, balance, version, created_at, updated_at)
VALUES
    (100, '44444444-4444-4444-4444-444444444444', 10000, 0, NOW(), NOW());

-- 테스트용 지갑 트랜잭션 데이터
INSERT INTO wallet_transactions (id, wallet_id, type, amount, reason, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 100, 'FREE_CHARGE', 10000, '초기 포인트 지급', NOW(), NOW());