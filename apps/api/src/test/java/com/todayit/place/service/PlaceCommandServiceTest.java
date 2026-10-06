package com.todayit.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.todayit.place.config.PlaceShareProperties;
import com.todayit.place.entity.Place;
import com.todayit.place.exception.PlaceAlreadyLikedException;
import com.todayit.place.exception.PlaceAlreadyScrappedException;
import com.todayit.place.exception.PlaceNotFoundException;
import com.todayit.place.repository.PlaceMemberLikeRepository;
import com.todayit.place.repository.PlaceRepository;
import com.todayit.place.repository.PlaceScrapRepository;
import com.todayit.place.service.model.PlaceLikeResult;
import com.todayit.place.service.model.PlaceScrapResult;
import com.todayit.place.service.model.PlaceShareResult;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 장소 좋아요, 스크랩 변경 및 공유 업무를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class PlaceCommandServiceTest {

  @Mock private PlaceRepository placeRepository;

  @Mock private PlaceMemberLikeRepository placeMemberLikeRepository;

  @Mock private PlaceScrapRepository placeScrapRepository;

  @Mock private Place place;

  @Test
  @DisplayName("장소 좋아요를 생성하고 좋아요 수를 반환한다")
  void likesPlace() {
    // Given
    when(placeRepository.findByPlaceIdAndIsActiveTrueAndIsDeletedFalse(1))
        .thenReturn(Optional.of(place));
    when(placeMemberLikeRepository.insertIfAbsent("member-1", 1)).thenReturn(1);
    when(placeMemberLikeRepository.countByPlaceId(1)).thenReturn(3L);
    PlaceCommandService service =
        new PlaceCommandService(placeRepository, placeMemberLikeRepository, placeScrapRepository);

    // When
    PlaceLikeResult result = service.likePlace(1, "member-1");

    // Then
    assertThat(result).isEqualTo(new PlaceLikeResult(1, true, 3));
    verify(placeMemberLikeRepository).insertIfAbsent("member-1", 1);
  }

  @Test
  @DisplayName("이미 좋아요를 누른 장소는 중복 좋아요 오류를 반환한다")
  void rejectsDuplicateLike() {
    // Given
    when(placeRepository.findByPlaceIdAndIsActiveTrueAndIsDeletedFalse(1))
        .thenReturn(Optional.of(place));
    when(placeMemberLikeRepository.insertIfAbsent("member-1", 1)).thenReturn(0);
    PlaceCommandService service =
        new PlaceCommandService(placeRepository, placeMemberLikeRepository, placeScrapRepository);

    // When & Then
    assertThatThrownBy(() -> service.likePlace(1, "member-1"))
        .isInstanceOf(PlaceAlreadyLikedException.class)
        .hasMessage("이미 좋아요를 눌렀습니다.");
  }

  @Test
  @DisplayName("이미 스크랩한 장소는 중복 스크랩 오류를 반환한다")
  void rejectsDuplicateScrap() {
    // Given
    when(placeRepository.findByPlaceIdAndIsActiveTrueAndIsDeletedFalse(1))
        .thenReturn(Optional.of(place));
    when(placeScrapRepository.existsByMemberIdAndPlacePlaceIdAndIsDeletedFalse("member-1", 1))
        .thenReturn(true);
    PlaceCommandService service =
        new PlaceCommandService(placeRepository, placeMemberLikeRepository, placeScrapRepository);

    // When & Then
    assertThatThrownBy(() -> service.scrapPlace(1, "member-1"))
        .isInstanceOf(PlaceAlreadyScrappedException.class)
        .hasMessage("이미 스크랩되었습니다.");
  }

  @Test
  @DisplayName("동시 스크랩으로 upsert가 반영되지 않으면 중복 스크랩 오류를 반환한다")
  void rejectsConcurrentDuplicateScrap() {
    // Given
    when(placeRepository.findByPlaceIdAndIsActiveTrueAndIsDeletedFalse(1))
        .thenReturn(Optional.of(place));
    when(placeScrapRepository.existsByMemberIdAndPlacePlaceIdAndIsDeletedFalse("member-1", 1))
        .thenReturn(false);
    when(placeScrapRepository.upsertByMemberIdAndPlaceId("member-1", 1)).thenReturn(0);
    PlaceCommandService service =
        new PlaceCommandService(placeRepository, placeMemberLikeRepository, placeScrapRepository);

    // When & Then
    assertThatThrownBy(() -> service.scrapPlace(1, "member-1"))
        .isInstanceOf(PlaceAlreadyScrappedException.class)
        .hasMessage("이미 스크랩되었습니다.");
  }

  @Test
  @DisplayName("취소했던 스크랩을 다시 활성화하고 스크랩 수를 반환한다")
  void reactivatesCancelledScrap() {
    // Given
    when(placeRepository.findByPlaceIdAndIsActiveTrueAndIsDeletedFalse(1))
        .thenReturn(Optional.of(place));
    when(placeScrapRepository.upsertByMemberIdAndPlaceId("member-1", 1)).thenReturn(1);
    when(placeScrapRepository.countActiveByPlaceId(1)).thenReturn(3L);
    PlaceCommandService service =
        new PlaceCommandService(placeRepository, placeMemberLikeRepository, placeScrapRepository);

    // When
    PlaceScrapResult result = service.scrapPlace(1, "member-1");

    // Then
    assertThat(result).isEqualTo(new PlaceScrapResult(1, true, 3));
  }

  @Test
  @DisplayName("장소 스크랩을 취소하고 남은 스크랩 수를 반환한다")
  void cancelsScrap() {
    // Given
    when(placeRepository.findByPlaceIdAndIsActiveTrueAndIsDeletedFalse(1))
        .thenReturn(Optional.of(place));
    when(placeScrapRepository.countActiveByPlaceId(1)).thenReturn(2L);
    PlaceCommandService service =
        new PlaceCommandService(placeRepository, placeMemberLikeRepository, placeScrapRepository);

    // When
    PlaceScrapResult result = service.cancelPlaceScrap(1, "member-1");

    // Then
    assertThat(result).isEqualTo(new PlaceScrapResult(1, false, 2));
    verify(placeScrapRepository).cancelByMemberIdAndPlaceId("member-1", 1);
  }

  @Test
  @DisplayName("장소 식별자로 공유 링크와 만료 일시를 생성한다")
  void sharesPlaceWithExpiration() {
    // Given
    when(placeRepository.findByPlaceIdAndIsActiveTrueAndIsDeletedFalse(1))
        .thenReturn(Optional.of(place));
    PlaceShareProperties properties =
        new PlaceShareProperties("https://todayit.kr/places", Duration.ofDays(7));
    PlaceCommandService service =
        new PlaceCommandService(
            placeRepository, placeMemberLikeRepository, placeScrapRepository, properties);
    LocalDateTime now = LocalDateTime.of(2026, 10, 6, 14, 0, 0);

    // When
    PlaceShareResult result = service.sharePlace(1, now);

    // Then
    assertThat(result.placeId()).isEqualTo(1);
    assertThat(result.shareUrl()).isEqualTo("https://todayit.kr/places/1");
    assertThat(result.expiresAt()).isEqualTo(LocalDateTime.of(2026, 10, 13, 14, 0, 0));
  }

  @Test
  @DisplayName("만료 기간이 설정되지 않은 경우 만료 일시는 null이다")
  void sharesPlaceWithoutExpiration() {
    // Given
    when(placeRepository.findByPlaceIdAndIsActiveTrueAndIsDeletedFalse(1))
        .thenReturn(Optional.of(place));
    PlaceShareProperties properties =
        new PlaceShareProperties("https://todayit.kr/places/{placeId}", null);
    PlaceCommandService service =
        new PlaceCommandService(
            placeRepository, placeMemberLikeRepository, placeScrapRepository, properties);
    LocalDateTime now = LocalDateTime.of(2026, 10, 6, 14, 0, 0);

    // When
    PlaceShareResult result = service.sharePlace(1, now);

    // Then
    assertThat(result.placeId()).isEqualTo(1);
    assertThat(result.shareUrl()).isEqualTo("https://todayit.kr/places/1");
    assertThat(result.expiresAt()).isNull();
  }

  @Test
  @DisplayName("존재하지 않는 장소 공유 시 예외가 발생한다")
  void throwsExceptionWhenPlaceNotFound() {
    // Given
    when(placeRepository.findByPlaceIdAndIsActiveTrueAndIsDeletedFalse(999))
        .thenReturn(Optional.empty());
    PlaceCommandService service =
        new PlaceCommandService(placeRepository, placeMemberLikeRepository, placeScrapRepository);

    // When & Then
    assertThatThrownBy(() -> service.sharePlace(999))
        .isInstanceOf(PlaceNotFoundException.class)
        .hasMessage("장소를 찾을 수 없습니다.");
  }
}
