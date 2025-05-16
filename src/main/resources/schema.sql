-- Uid 테이블에 mfg_year 컬럼 추가 (VARCHAR 타입으로)
ALTER TABLE uid ADD COLUMN IF NOT EXISTS mfg_year VARCHAR(10);

-- 기존 데이터의 mfg_year 업데이트 (기본값: 현재 연도의 마지막 두 자리)
UPDATE uid SET mfg_year = CAST(YEAR(CURRENT_DATE()) % 100 AS CHAR) WHERE mfg_year IS NULL; 