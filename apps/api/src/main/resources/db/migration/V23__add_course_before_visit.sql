ALTER TABLE course ADD COLUMN before_visit JSONB;

COMMENT ON COLUMN course.before_visit IS 'AI 방문 전 유의사항 (JSON)';
