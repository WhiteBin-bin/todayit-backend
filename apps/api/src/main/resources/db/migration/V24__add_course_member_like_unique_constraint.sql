ALTER TABLE course_member_like
    ADD CONSTRAINT uk_course_member_like_member_course UNIQUE (member_id, course_id);
