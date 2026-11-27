package com.todayit.course.service;

import com.todayit.common.pagination.PageResult;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.service.model.CourseScrapSort;
import com.todayit.course.service.model.MyCourseCardResult;
import com.todayit.course.service.model.MyCourseSort;

/** 회원의 본인 작성 코스 및 스크랩 코스 조회에 필요한 코스 서비스 계약입니다. */
public interface MemberCourseQueryService {

  /**
   * 회원이 작성한 코스 목록을 상태 조건 및 페이징에 맞게 조회합니다.
   *
   * @param memberId 회원 식별자
   * @param status 코스 상태 필터, null이면 DELETED 제외 전체 조회
   * @param page 페이지 번호
   * @param size 페이지 크기
   * @param sort 정렬 기준
   * @return 내 코스 카드 페이지
   */
  PageResult<MyCourseCardResult> findMyCourses(
      String memberId, CourseStatus status, int page, int size, MyCourseSort sort);

  /**
   * 회원이 스크랩한 코스 목록을 정렬 기준 및 페이징에 맞게 조회합니다.
   *
   * @param memberId 회원 식별자
   * @param page 페이지 번호
   * @param size 페이지 크기
   * @param sort 스크랩 정렬 기준
   * @return 스크랩 코스 카드 페이지
   */
  PageResult<MyCourseCardResult> findScrappedCourses(
      String memberId, int page, int size, CourseScrapSort sort);
}
