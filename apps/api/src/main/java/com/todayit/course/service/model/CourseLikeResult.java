package com.todayit.course.service.model;

/**
 * 코스 좋아요 결과입니다.
 *
 * @param courseId 코스 식별자
 * @param liked 현재 회원의 좋아요 여부
 * @param likeCount 코스의 좋아요 수
 */
public record CourseLikeResult(int courseId, boolean liked, long likeCount) {}
