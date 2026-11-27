package com.todayit.course.service.model;

/**
 * 코스 스크랩 및 스크랩 취소 결과입니다.
 *
 * @param courseId 코스 식별자
 * @param scrapped 현재 회원의 스크랩 활성 여부
 * @param scrapCount 코스의 활성 스크랩 수
 */
public record CourseScrapResult(int courseId, boolean scrapped, long scrapCount) {}
