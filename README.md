# GitHub Collaboration Template

GitHub 기반 팀 프로젝트에서 반복적으로 사용하는 Issue, Pull Request 및 Git 협업 규칙을 관리하기 위한 템플릿입니다.

## 제공 항목

- GitHub Issue Forms
- Pull Request Template
- Commit & Branch Convention
- GitHub Workflow

## 문서

프로젝트에서 사용하는 협업 정책은 `docs/`에서 확인할 수 있습니다.

- [Code Rules](docs/code-rules/README.md)
- [Convention](docs/CONVENTION.md)
- [Workflow](docs/WORKFLOW.md)

## Backend build

- 요구 환경: Java 21
- Windows 전체 검증: `.\gradlew.bat check`
- macOS·Linux 전체 검증: `./gradlew check`
- 로컬 포맷 적용: `gradlew.bat spotlessApply` 또는 `./gradlew spotlessApply`

전역 Gradle 설치 없이 저장소의 Gradle Wrapper를 사용합니다.
