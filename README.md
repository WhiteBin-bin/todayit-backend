# Today-it Backend

## Module structure

```text
apps
├─ api                 # HTTP API 실행 애플리케이션
└─ data-worker         # 외부 데이터 처리 실행 애플리케이션
domains
└─ recommendation      # 추천 정책과 업무 규칙
integrations
└─ external-data       # 외부 API 데이터 수집과 변환
deploy                  # 서버 배포 설정, Gradle 모듈 아님
```

`apps`만 실행 및 배포할 수 있는 Spring Boot 모듈입니다. `recommendation`은 외부
통신 SDK에 의존하지 않으며, `external-data`가 추천 도메인이 정의한 후보 제공 계약을
구현합니다.

## 문서

프로젝트에서 사용하는 협업 정책은 `docs/`에서 확인할 수 있습니다.

- [Code Rules](docs/code-rules/README.md)
- [Convention](docs/CONVENTION.md)
- [Workflow](docs/WORKFLOW.md)

## 빠른 시작

- 요구 환경: Java 21
- 실행 방법 : `.\gradlew.bat :apps:api:bootRun --args="--spring.profiles.active=local"`
- Windows 전체 검증: `.\gradlew.bat check`
- macOS·Linux 전체 검증: `./gradlew check`

전역 Gradle 설치 없이 저장소의 Gradle Wrapper를 사용합니다.

포맷 적용과 검사 항목은 [코드 규칙의 검증 안내](docs/code-rules/README.md#검증)를 확인합니다.
