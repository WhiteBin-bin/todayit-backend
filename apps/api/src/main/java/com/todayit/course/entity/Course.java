package com.todayit.course.entity;

import com.todayit.course.exception.InvalidCoursePlaceOrderException;
import com.todayit.course.exception.MaxPlaceDuplicationExceededException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 회원이 만든 코스의 기본 정보와 상태를 나타냅니다. */
@Entity
@Table(name = "course")
public class Course {

  /** 한 코스 내 동일한 장소의 최대 중복 허용 개수입니다. */
  public static final int MAX_PLACE_DUPLICATION_COUNT = 2;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_id", nullable = false)
  private int courseId;

  @Column(name = "member_id", nullable = false, length = 36)
  private String memberId;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "course_summary", length = 500)
  private String courseSummary;

  @Enumerated(EnumType.STRING)
  @Column(name = "transport", nullable = false, length = 30)
  private CourseTransport transport;

  @Column(name = "start_at", nullable = false)
  private LocalDateTime startAt;

  @Column(name = "end_at")
  private LocalDateTime endAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private CourseStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "visibility", nullable = false, length = 20)
  private CourseVisibility visibility;

  @Column(name = "view_count", nullable = false)
  private int viewCount;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CoursePlace> coursePlaces = new ArrayList<>();

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RegionCourse> regions = new ArrayList<>();

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CourseTheme> themes = new ArrayList<>();

  protected Course() {}

  /**
   * 입력 조건으로 비공개 임시 코스를 생성합니다.
   *
   * @param memberId 작성자 식별자
   * @param title 코스명
   * @param transport 이동수단
   * @param startAt 방문 시작 시각
   * @param endAt 방문 종료 시각, 미입력 시 null
   * @param selectedThemes 검증된 콘셉트 목록
   * @param createdAt 서버 생성 시각
   * @return 장소와 지역이 없는 비공개 DRAFT
   */
  public static Course createDraft(
      String memberId,
      String title,
      CourseTransport transport,
      LocalDateTime startAt,
      LocalDateTime endAt,
      List<Theme> selectedThemes,
      LocalDateTime createdAt) {
    Course course = new Course();
    course.memberId = memberId;
    course.title = title;
    course.transport = transport;
    course.startAt = startAt;
    course.endAt = endAt;
    course.viewCount = 0;
    course.status = CourseStatus.DRAFT;
    course.visibility = CourseVisibility.PRIVATE;
    course.createdAt = createdAt;
    selectedThemes.forEach(theme -> course.themes.add(new CourseTheme(course, theme)));
    return course;
  }

  /**
   * 특정 회원이 이 코스를 조회할 권한이 있는지 확인합니다. 공개 상태이면서 활성 상태인 코스는 누구나 조회할 수 있으며, 비공개 코스나 DRAFT 코스는 작성자 본인만
   * 조회할 수 있습니다.
   *
   * @param accessorMemberId 조회를 시도하는 회원 식별자, 비회원이면 null
   * @return 조회 가능 여부
   */
  public boolean canAccess(String accessorMemberId) {
    if (deletedAt != null || status == CourseStatus.DELETED) {
      return false;
    }
    if (visibility == CourseVisibility.PUBLIC && status == CourseStatus.ACTIVE) {
      return true;
    }
    return accessorMemberId != null && memberId.equals(accessorMemberId);
  }

  /**
   * 특정 회원이 이 코스의 작성자인지 확인합니다.
   *
   * @param accessorMemberId 확인할 회원 식별자, 비회원이면 null
   * @return 작성자 여부
   */
  public boolean isWriter(String accessorMemberId) {
    return accessorMemberId != null && memberId.equals(accessorMemberId);
  }

  public int getCourseId() {
    return courseId;
  }

  public String getMemberId() {
    return memberId;
  }

  public String getTitle() {
    return title;
  }

  public String getCourseSummary() {
    return courseSummary;
  }

  public CourseTransport getTransport() {
    return transport;
  }

  public LocalDateTime getStartAt() {
    return startAt;
  }

  public LocalDateTime getEndAt() {
    return endAt;
  }

  public CourseStatus getStatus() {
    return status;
  }

  public CourseVisibility getVisibility() {
    return visibility;
  }

  public int getViewCount() {
    return viewCount;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public List<RegionCourse> getRegions() {
    return Collections.unmodifiableList(regions);
  }

  public List<CourseTheme> getThemes() {
    return Collections.unmodifiableList(themes);
  }

  public List<CoursePlace> getCoursePlaces() {
    return Collections.unmodifiableList(coursePlaces);
  }

  /**
   * 코스에 장소를 추가합니다.
   *
   * @param coursePlace 추가할 코스 장소 엔티티
   */
  public void addCoursePlace(CoursePlace coursePlace) {
    this.coursePlaces.add(coursePlace);
  }

  /**
   * 코스에 특정 장소가 추가 가능한지(중복 허용 개수 미만인지) 확인합니다.
   *
   * @param placeId 확인할 장소 식별자
   * @return 추가 가능 여부
   */
  public boolean canAddPlace(int placeId) {
    return getPlaceCount(placeId) < MAX_PLACE_DUPLICATION_COUNT;
  }

  /**
   * 코스에 이미 포함된 특정 장소의 중복 개수를 반환합니다.
   *
   * @param placeId 확인할 장소 식별자
   * @return 장소의 개수
   */
  public long getPlaceCount(int placeId) {
    return coursePlaces.stream().filter(cp -> cp.getPlaceId() == placeId).count();
  }

  /**
   * 새 장소를 추가하기 위해 순서를 뒤로 밀어야 하는 대상 목록을 음수 임시 순서로 변경합니다.
   *
   * @param targetOrder 추가하려는 순서
   * @return 임시 순서로 변경된 장소 목록
   */
  public List<CoursePlace> shiftOrdersToNegative(int targetOrder) {
    int maxOrder = coursePlaces.stream().mapToInt(CoursePlace::getOrder).max().orElse(0);
    if (targetOrder > maxOrder) {
      return List.of();
    }
    int assignedOrder = Math.max(1, targetOrder);
    List<CoursePlace> targets =
        coursePlaces.stream().filter(cp -> cp.getOrder() >= assignedOrder).toList();
    for (CoursePlace cp : targets) {
      cp.setOrder(-cp.getOrder());
    }
    return targets;
  }

  /**
   * 음수 임시 순서로 변경된 장소들의 순서를 1씩 밀린 양수로 복원합니다.
   *
   * @param shiftedPlaces 임시 음수 순서로 변경된 장소 목록
   */
  public void restoreShiftedOrders(List<CoursePlace> shiftedPlaces) {
    for (CoursePlace cp : shiftedPlaces) {
      cp.setOrder(-cp.getOrder() + 1);
    }
  }

  /**
   * 코스에 새 장소를 추가합니다.
   *
   * @param placeId 추가할 장소 식별자
   * @param targetOrder 지정할 방문 순서, null이면 맨 뒤에 추가
   * @param stayMinute 체류 시간(분)
   * @return 생성된 CoursePlace 엔티티
   * @throws MaxPlaceDuplicationExceededException 이미 동일 장소가 최대 중복 개수(2개)만큼 존재하는 경우
   */
  public CoursePlace addPlace(int placeId, Integer targetOrder, int stayMinute) {
    if (!canAddPlace(placeId)) {
      throw new MaxPlaceDuplicationExceededException();
    }
    int maxOrder = coursePlaces.stream().mapToInt(CoursePlace::getOrder).max().orElse(0);
    int assignedOrder =
        (targetOrder == null || targetOrder > maxOrder) ? maxOrder + 1 : Math.max(1, targetOrder);
    CoursePlace newCoursePlace = new CoursePlace(this, placeId, assignedOrder, stayMinute);
    this.coursePlaces.add(newCoursePlace);
    return newCoursePlace;
  }

  /**
   * 코스에 포함된 장소들의 방문 순서를 새로운 순서대로 일괄 변경하기 위해 임시 음수 순서로 설정합니다.
   *
   * @param newOrderPlaceIds 새로운 방문 순서대로 나열된 코스 장소 식별자 목록
   * @throws InvalidCoursePlaceOrderException 목록이 비어 있거나 기존 장소 목록과 일치하지 않는 경우
   */
  public void reorderPlaces(List<Integer> newOrderPlaceIds) {
    if (newOrderPlaceIds == null
        || newOrderPlaceIds.isEmpty()
        || newOrderPlaceIds.size() != coursePlaces.size()) {
      throw new InvalidCoursePlaceOrderException();
    }
    Set<Integer> existingIds =
        coursePlaces.stream().map(CoursePlace::getCoursePlaceId).collect(Collectors.toSet());
    Set<Integer> newIds = new HashSet<>(newOrderPlaceIds);
    if (!existingIds.equals(newIds)) {
      throw new InvalidCoursePlaceOrderException();
    }
    for (int i = 0; i < coursePlaces.size(); i++) {
      coursePlaces.get(i).setOrder(-(i + 1));
    }
  }

  /**
   * 임시 음수 순서로 설정된 장소들을 새로운 방문 순서(1부터 시작)로 최종 확정합니다.
   *
   * @param newOrderPlaceIds 새로운 방문 순서대로 나열된 코스 장소 식별자 목록
   */
  public void applyNewOrders(List<Integer> newOrderPlaceIds) {
    Map<Integer, CoursePlace> map =
        coursePlaces.stream()
            .collect(Collectors.toMap(CoursePlace::getCoursePlaceId, Function.identity()));
    for (int order = 1; order <= newOrderPlaceIds.size(); order++) {
      int coursePlaceId = newOrderPlaceIds.get(order - 1);
      CoursePlace cp = map.get(coursePlaceId);
      if (cp != null) {
        cp.setOrder(order);
      }
    }
  }

  /**
   * 코스에 지역 관계를 추가합니다.
   *
   * @param regionId 연결할 지역 식별자
   */
  public void addRegion(int regionId) {
    this.regions.add(new RegionCourse(this, regionId));
  }

  /**
   * 코스의 조회 정보를 반환합니다.
   *
   * @return 코스 조회 정보
   */
  public CourseSnapshot getSnapshot() {
    return new CourseSnapshot(courseId, title, courseSummary, transport, startAt, endAt, viewCount);
  }

  /**
   * 코스 조회에 필요한 정보를 담습니다.
   *
   * @param courseId 코스 식별자
   * @param title 코스명
   * @param courseSummary 코스 요약
   * @param transport 이동수단
   * @param startAt 코스 시작 시간
   * @param endAt 코스 종료 시간
   * @param viewCount 조회수
   */
  public record CourseSnapshot(
      int courseId,
      String title,
      String courseSummary,
      CourseTransport transport,
      LocalDateTime startAt,
      LocalDateTime endAt,
      int viewCount) {}
}
