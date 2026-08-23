# Code Rules

이 디렉터리는 개발자와 Agent가 함께 따르는 코드 작성·리뷰 규칙의 단일 원본입니다.

## 읽기 순서

1. [명세 원본](specification.md)
2. [기술 기준](technology.md)
3. [코드 스타일](code-style.md)
4. [Javadoc](javadoc.md)
5. [아키텍처](architecture.md)
6. [DTO](dto.md)
7. [예외 처리](exception.md)
8. 작업 대상에 따라 [API](api.md) 또는 [데이터베이스](database.md)
9. [테스트](testing.md)
10. [보안](security.md)

## 적용 원칙

- Aggregate Root를 기준으로 최상위 업무 패키지를 나눕니다.
- 내부 패키지는 `controller`, `service`, `repository`, `entity`, `dto`, `exception` 명칭을 사용합니다.
- 코드 변경에는 관련 명세 ID와 검증 가능한 테스트를 함께 고려합니다.
- 결정되지 않은 기술과 업무 규칙을 임의로 확정하지 않습니다.
- 자동 포맷터와 정적 분석 설정이 도입되면 문서의 수동 선호보다 해당 설정을 우선합니다.

## 관련 문서

- [Git Convention](../CONVENTION.md)
- [Git Workflow](../WORKFLOW.md)
- [Agent 진입점](../../AGENTS.md)
