# Agent Entry Point

이 저장소의 Agent는 코드 변경 전에 [.ai/rules/README.md](.ai/rules/README.md)와 [공통 코드 규칙](docs/code-rules/README.md)을 읽습니다.

## 적용 범위

- 이 저장소에는 Cycle, Task 원장과 제품 설계 Workflow를 사용하지 않습니다.
- `.ai/rules/`는 코드 작성과 코드 리뷰 규칙만 관리합니다.
- Git 협업 방식은 기존 [Convention](docs/CONVENTION.md)과 [Workflow](docs/WORKFLOW.md)를 따릅니다.
- 더 하위 경로에 `AGENTS.md`가 생기면 해당 경로에서는 더 가까운 규칙을 우선합니다.

## 작업 전 확인

1. Git 변경과 현재 브랜치를 확인합니다.
2. [명세 원본 규칙](docs/code-rules/specification.md)에 따라 관련 요구사항·기능·API ID와 결정상태를 확인합니다.
3. 기술 스택과 기존 코드 구조를 확인합니다.
4. 관련 코드와 테스트를 읽은 후 최소 범위로 변경합니다.
5. 결정되지 않은 프레임워크·라이브러리·코드 스타일을 임의로 도입하지 않습니다.
