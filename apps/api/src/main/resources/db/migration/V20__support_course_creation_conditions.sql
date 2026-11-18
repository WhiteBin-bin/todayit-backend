ALTER TABLE course ALTER COLUMN end_at DROP NOT NULL;

INSERT INTO theme (name)
SELECT names.name
FROM (VALUES
    ('조용함'), ('로맨틱'), ('아늑함'), ('감성적'), ('활기참'),
    ('이색적'), ('여유로움'), ('고급스러움'), ('레트로'), ('자연친화적')
) AS names(name)
WHERE NOT EXISTS (SELECT 1 FROM theme WHERE theme.name = names.name);

COMMENT ON TABLE course IS '코스';
COMMENT ON COLUMN course.course_id IS '코스 식별자';
COMMENT ON COLUMN course.member_id IS '회원 식별자';
COMMENT ON COLUMN course.title IS '코스명';
COMMENT ON COLUMN course.course_summary IS '코스 요약';
COMMENT ON COLUMN course.transport IS '이동수단';
COMMENT ON COLUMN course.start_at IS '방문 시작 시간';
COMMENT ON COLUMN course.end_at IS '방문 종료 시간';
COMMENT ON COLUMN course.status IS '코스 상태';
COMMENT ON COLUMN course.visibility IS '공개 여부';
COMMENT ON COLUMN course.view_count IS '조회수';
COMMENT ON COLUMN course.created_at IS '생성 날짜';
COMMENT ON COLUMN course.updated_at IS '수정 날짜';
COMMENT ON COLUMN course.deleted_at IS '삭제 날짜';

COMMENT ON TABLE course_place IS '코스 장소';
COMMENT ON COLUMN course_place.course_place_id IS '코스 장소 식별자';
COMMENT ON COLUMN course_place.course_id IS '코스 식별자';
COMMENT ON COLUMN course_place.place_id IS '장소 식별자';
COMMENT ON COLUMN course_place."order" IS '방문 순서';
COMMENT ON COLUMN course_place.stay_minute IS '체류 시간(분)';
