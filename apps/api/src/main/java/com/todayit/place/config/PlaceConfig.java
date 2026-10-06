package com.todayit.place.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 장소 도메인 설정입니다. */
@Configuration
@EnableConfigurationProperties(PlaceShareProperties.class)
public class PlaceConfig {}
