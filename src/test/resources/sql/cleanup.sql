-- 테스트 데이터 정리 (순서 중요: 외래키 제약조건 고려)
DELETE FROM wallet_transactions;
DELETE FROM wallets;
DELETE FROM corps;

-- Auto increment 초기화 (PostgreSQL)
ALTER SEQUENCE IF EXISTS wallets_id_seq RESTART WITH 1;