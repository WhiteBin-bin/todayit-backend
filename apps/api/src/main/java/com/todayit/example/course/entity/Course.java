package com.todayit.example.course.entity;

/** 상태와 생성 조건을 소유하는 업무 객체입니다. JPA Entity는 아닙니다. */
public final class Course {
  private final String id;
  private final String name;

  private Course(String id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * HTTP 밖에서 호출해도 생성 조건을 지킵니다.
   *
   * @param id 외부에서 생성한 식별자
   * @param name 비어 있지 않은 코스 이름
   * @return 생성된 코스
   * @throws IllegalArgumentException 이름이 없거나 공백일 때
   */
  public static Course create(String id, String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("코스 이름은 필수입니다.");
    }
    return new Course(id, name);
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
