# Technology Rules

이 저장소의 Backend 코드 기준은 Java 21, Spring Boot 4.1.1과 Gradle 9.7.1입니다. Spring MVC의 `@RestControllerAdvice`를 전역 HTTP 예외 변환 경계로 사용합니다.

영속성 프레임워크와 데이터베이스는 **미확정**입니다.

- 승인된 Java·Spring 범위를 넘어 영속성 프레임워크와 데이터베이스를 임의로 선택하지 않습니다.
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
