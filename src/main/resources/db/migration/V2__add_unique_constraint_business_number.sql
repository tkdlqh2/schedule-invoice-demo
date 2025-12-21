-- business_number 컬럼에 UNIQUE 제약 추가
-- Race condition 방지를 위해 DB 레벨에서 중복 방지

-- 기존 인덱스 제거 (UNIQUE 제약이 자동으로 UNIQUE 인덱스를 생성하므로)
DROP INDEX IF EXISTS idx_corps_business_number;

-- UNIQUE 제약 추가 (NULL 값은 허용, NULL이 아닌 값들은 중복 불가)
ALTER TABLE corps ADD CONSTRAINT uk_corps_business_number UNIQUE (business_number);

-- 코멘트 추가
COMMENT ON CONSTRAINT uk_corps_business_number ON corps IS '사업자 번호 중복 방지 (Race condition 방지)';