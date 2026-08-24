# Technology Rules

이 저장소의 Backend 코드 기준은 Java 21, Spring Boot 4.1.1과 Gradle 9.7.1입니다. 주 웹 애플리케이션 스택은 Spring MVC이며 `@RestControllerAdvice`를 전역 HTTP 예외 변환 경계로 사용합니다.

## Web과 외부 연동

- HTTP 서버는 Spring MVC와 내장 Tomcat을 사용합니다.
- Spring WebFlux는 `WebClient`와 Reactor 기반 외부 연동에 사용하며 Reactive HTTP 서버 스택으로 사용하지 않습니다.
- MVC 요청 처리 흐름에서 블로킹 작업을 Reactor 스레드로 옮기거나, Reactive 타입을 사용한다는 이유만으로 전체 요청 처리를 비동기화하지 않습니다.

## 데이터와 배치

- 영속성 프레임워크는 Spring Data JPA, DBMS는 PostgreSQL을 사용합니다.
- PostgreSQL JDBC 드라이버는 런타임 의존성으로 사용하고 버전은 Spring Boot 의존성 관리에 맡깁니다.
- 스키마 변경은 Flyway Migration으로 관리하며 PostgreSQL 지원 모듈을 사용합니다.
- Spring Batch의 Job Repository는 PostgreSQL을 사용합니다. Batch 메타데이터 스키마의 생성과 변경도 재현 가능한 Migration으로 관리합니다.
- Redis 연동은 Spring Data Redis와 Lettuce를 사용합니다. 캐시, 세션 또는 업무 데이터 저장 용도는 기능별 승인 없이 임의로 확정하지 않습니다.
- DB와 Redis의 주소, 계정과 비밀번호는 실행환경 설정으로 주입합니다.

## 인증과 보안

- 인증·인가는 Spring Security를 사용하고 JWT 처리는 JJWT 0.13.0을 사용합니다.
- JWT 비밀키, 만료시간과 발급자 같은 환경별 값은 코드와 저장소에 저장하지 않습니다.
- Security 기본 자동 구성에 의존하지 않고 승인된 접근 정책을 `SecurityFilterChain`으로 명시합니다.

## 규칙과 AI

- 업무 규칙 엔진은 Drools 10.2.0의 Rule Unit 방식을 사용합니다. 전통적인 `kmodule.xml` 기반 `KieSession` 방식은 별도 요구가 승인된 경우에만 사용합니다.
- Spring AI 2.0.0의 제공자 중립 Model API를 사용합니다. 실제 AI 모델 제공자와 모델은 미확정이며 승인 전에는 제공자별 Starter와 접속 설정을 추가하지 않습니다.

## 관측성

- Spring Boot Actuator와 Micrometer Prometheus Registry를 사용합니다.
- 외부에 노출할 Actuator Endpoint와 접근 권한은 환경별로 명시하며 민감한 Endpoint를 기본 공개하지 않습니다.

## 공통 개발 기준

- Java 포맷과 import 정리는 Spotless의 Google Java Format을 사용합니다.
- 기본 정적 검사와 공개 계약의 Javadoc 누락 검사는 Checkstyle 14.0.0을 사용합니다.
- 패키지 의존 방향과 명칭 검사는 ArchUnit 1.5.0을 사용합니다.
- 의존성은 표준 기능으로 해결하기 어려운 명확한 이유가 있을 때만 추가합니다.
- 의존성을 추가하면 사용 목적, 적용 범위와 제거 가능성을 설명합니다.
- 버전은 기존 잠금 파일과 빌드 설정을 따르며 임의로 대규모 업그레이드하지 않습니다.
- 실행환경별 값과 비밀정보는 코드가 아닌 환경 설정으로 분리합니다.
- Spring Bean은 생성자 주입을 사용하고 필드 주입을 사용하지 않습니다.
- 순환 참조 허용 설정과 `@Lazy`로 구조 문제를 우회하지 않습니다.

## 로컬 검증

- 전체 검증: Windows는 `gradlew.bat check`, macOS·Linux는 `./gradlew check`
- 포맷 확인: `gradlew.bat spotlessCheck`
- 포맷 적용: `gradlew.bat spotlessApply`
- 아키텍처 검사는 전체 테스트에 포함됩니다.

`check`는 컴파일, 테스트, Spotless와 Checkstyle을 함께 실행합니다. 자동화하기 어려운 업무 의미와 Aggregate 소유권 규칙은 계속 코드 리뷰 기준으로 적용합니다.
