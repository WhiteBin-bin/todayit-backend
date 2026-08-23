# Claude Entry Point

Claude는 이 파일을 독립적인 규칙 원본으로 사용하지 않고 [AGENTS.md](AGENTS.md)를 공통 Agent 진입점으로 사용합니다.

## 필수 읽기 순서

1. [AGENTS.md](AGENTS.md)
2. [Agent 전용 행동 지침](.ai/rules/README.md)
3. [개발자·Agent 공통 코드 규칙](docs/code-rules/README.md)
4. [명세 원본 규칙](docs/code-rules/specification.md)
5. 현재 작업과 관련된 세부 코드 규칙 및 기존 코드·테스트

공통 규칙을 `CLAUDE.md`에 복사하지 않습니다. 충돌하면 `AGENTS.md`와 각 문서에 정의된 우선순위를 따릅니다.
