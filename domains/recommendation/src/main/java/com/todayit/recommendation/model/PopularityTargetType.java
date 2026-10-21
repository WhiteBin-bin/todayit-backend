package com.todayit.recommendation.model;

/** 인기순 집계 대상 유형을 나타냅니다. 코스와 장소는 별도로 집계하며 순위를 섞지 않습니다. */
public enum PopularityTargetType {
  /** 공개·확정 코스. */
  COURSE,

  /** 서비스 노출 가능 장소. */
  PLACE
}
