ALTER TABLE course_scrap
    ADD CONSTRAINT uk_course_scrap_member_course UNIQUE (member_id, course_id);
