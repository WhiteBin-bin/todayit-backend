package com.todayit.course.service.command;

/**
 * 코스 장소 추가 조건입니다.
 *
 * @param courseId 코스 식별자
 * @param placeId 장소 식별자
 * @param order 지정 순서, null 가능
 * @param stayMinute 체류 시간(분)
 */
public record AddCoursePlaceCommand(int courseId, int placeId, Integer order, int stayMinute) {}
