-- 기존 데이터 정리 후 삽입
DELETE FROM wallet_transactions;
DELETE FROM wallets;
DELETE FROM corps;

-- 테스트용 기관 데이터
INSERT INTO corps (id, name, business_number, contact_phone, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', '테스트 기관 A', '111-11-11111', '010-1111-1111', NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', '테스트 기관 B', '222-22-22222', '010-2222-2222', NOW(), NOW()),
    ('33333333-3333-3333-3333-333333333333', '테스트 기관 C', '333-33-33333', '010-3333-3333', NOW(), NOW());