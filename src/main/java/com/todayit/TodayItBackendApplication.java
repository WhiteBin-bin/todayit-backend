package com.todayit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Today-it Backend 애플리케이션의 실행 진입점입니다. */
@SpringBootApplication
public class TodayItBackendApplication {

  /**
   * Spring Boot 애플리케이션을 시작합니다.
   *
   * @param args 애플리케이션 실행 인자
   */
  public static void main(String[] args) {
    SpringApplication.run(TodayItBackendApplication.class, args);
  }
}
