package com.todayit.example.course.service.command;

/**
 * HTTP 타입에 의존하지 않는 코스 생성 입력입니다.
 *
 * @param memberId 서버에서 인증한 회원 식별자
 * @param name 코스 이름
 */
public record CreateCourseCommand(String memberId, String name) {}
