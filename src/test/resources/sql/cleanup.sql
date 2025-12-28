-- 테스트 데이터 정리 (순서 중요: 외래키 제약조건 고려)
TRUNCATE TABLE mock_notifications, outbox_events, invoice_schedules, invoice_schedule_groups, invoices, wallet_transactions, wallets, corps RESTART IDENTITY CASCADE;