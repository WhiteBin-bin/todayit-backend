package com.todayit.region;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todayit.region.exception.RegionErrorCode;
import com.todayit.region.exception.RegionException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 장소와 코스 목록의 공통 지역 입력을 해석하고 검증합니다. */
public final class RegionFilterParser {
  private static final int MAX_SIGUNGU_COUNT_PER_SIDO = 3;
  private static final ObjectMapper MAPPER =
      new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

  private RegionFilterParser() {}

  /**
   * 지역 JSON을 검증하고 입력 순서를 유지하면서 중복 시·군·구를 제거합니다.
   *
   * @param json 지역 필터 JSON 배열, 생략 시 null
   * @return 검증된 지역 필터 목록, 생략 시 빈 목록
   * @throws RegionException 입력 형식이나 코드가 잘못되었거나 시·도당 끝자리가 0인 시·군·구가 3개를 초과한 경우
   */
  public static List<RegionFilterValue> parse(String json) {
    if (json == null) {
      return List.of();
    }
    if (json.isBlank()) {
      throw new RegionException(RegionErrorCode.INVALID_PATH);
    }
    List<RegionFilterValue> requests;
    try {
      requests = MAPPER.readValue(json, new TypeReference<List<RegionFilterValue>>() {});
    } catch (JsonProcessingException exception) {
      throw new RegionException(RegionErrorCode.INVALID_PATH);
    }
    if (requests == null || requests.isEmpty()) {
      throw new RegionException(RegionErrorCode.INVALID_PATH);
    }
    Map<String, Set<String>> bySido = new LinkedHashMap<>();
    for (RegionFilterValue request : requests) {
      if (request == null
          || request.sidoCode() == null
          || !request.sidoCode().matches("[0-9]{2}")
          || bySido.containsKey(request.sidoCode())) {
        throw new RegionException(RegionErrorCode.INVALID_PATH);
      }
      Set<String> codes = new LinkedHashSet<>();
      for (String code : request.sigunguCodes()) {
        if (code == null || !code.matches("[0-9]{3}")) {
          throw new RegionException(RegionErrorCode.INVALID_PATH);
        }
        codes.add(code);
      }
      if (codes.stream().filter(code -> code.endsWith("0")).count() > MAX_SIGUNGU_COUNT_PER_SIDO) {
        throw new RegionException(RegionErrorCode.EXCEEDED_SIGUNGU_LIMIT);
      }
      bySido.put(request.sidoCode(), codes);
    }
    return bySido.entrySet().stream()
        .map(entry -> new RegionFilterValue(entry.getKey(), List.copyOf(entry.getValue())))
        .toList();
  }
}
