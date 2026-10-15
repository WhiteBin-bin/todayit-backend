package com.todayit.place.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/** 장소 API에서 전달받을 요청 값의 골격입니다. */
@Schema(description = "장소 요청")
public record PlaceRequest() {}
