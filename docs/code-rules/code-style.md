# Code Style Rules

## Java 명명 규칙

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 클래스·인터페이스·Enum | `PascalCase`, 역할을 나타내는 명사 | `MemberService`, `BoardRepository`, `GlobalExceptionHandler` |
| 메서드 | `camelCase`, 행위를 나타내는 동사로 시작 | `findMemberById`, `createBoard`, `printReport` |
| 변수·파라미터 | `camelCase`, 의미가 분명한 명사 | `request`, `currentMember`, `boardRepository` |
| 의미상 상수 | `SCREAMING_SNAKE_CASE` | `MAX_LOGIN_ATTEMPTS`, `DEFAULT_PAGE_SIZE` |
| 패키지 | 소문자, 대문자·언더스코어 금지 | `com.todayit.member`, `com.todayit.common.exception` |

- Controller는 `{Aggregate}Controller`, Service는 `{Aggregate}Service`, Repository는 `{Aggregate}Repository`로 명명합니다.
- DTO는 행위와 방향을 포함해 `CreateMemberRequest`, `MemberResponse`처럼 명명합니다.
- Service Interface가 실제로 필요하지 않으면 `MemberService`와 `MemberServiceImpl`을 기계적으로 분리하지 않습니다.
- `find...`는 값이 없을 수 있는 조회에 사용합니다.
- `get...`은 값이 반드시 있어야 하며 없으면 구체적인 예외가 발생하는 조회에 사용합니다.
- 존재 여부는 `exists...`, 개수는 `count...`로 표현합니다.
- Boolean은 `is...`, `has...`, `can...`처럼 의미가 드러나는 이름을 사용합니다.
- Collection은 복수 명사를 사용합니다.
- Java 약어도 일반 단어처럼 `ApiClient`, `UrlParser`로 표기합니다.
- `requestDto`, `data`, `info`처럼 타입이나 의미가 모호한 이름보다 `createMemberRequest`처럼 목적을 드러냅니다.
- 모든 `static final` 필드를 상수로 간주하지 않습니다. 의미상 변하지 않는 값에만 `SCREAMING_SNAKE_CASE`를 적용합니다.

## 가독성

- 이름은 역할과 의도를 드러내며 의미 없는 축약을 피합니다.
- 함수와 메서드는 한 가지 책임을 가지도록 작게 유지합니다.
- 중첩이 깊어지면 조기 반환, 작은 함수와 명확한 조건으로 분리합니다.
- 숫자·문자열 리터럴이 업무 의미를 가지면 이름 있는 상수나 타입으로 표현합니다.
- 주석은 코드가 무엇을 하는지가 아니라 제약, 이유와 선택 근거를 설명합니다.

## 변경 범위

- 요청과 직접 관련 없는 파일을 정리하거나 포맷하지 않습니다.
- 중복 제거는 의미와 변경 주기가 실제로 같을 때 수행합니다.
- 사용하지 않는 코드, 주석 처리한 코드와 임시 디버깅 출력을 남기지 않습니다.
- 하위 호환이 필요한 공개 계약은 명시적인 폐기 절차 없이 변경하지 않습니다.

## 오류와 경계

- 외부 입력은 시스템 경계에서 검증합니다.
- 예외를 무시하거나 성공 값으로 바꾸지 않습니다.
- 오류에는 원인 파악에 필요한 문맥을 포함하되 비밀정보와 개인정보를 포함하지 않습니다.
- 복구 가능한 오류와 프로그래밍 오류를 구분합니다.

들여쓰기, 줄바꿈과 import 정리는 Spotless의 Google Java Format을 기준으로 합니다. 형식이 다르면 수동으로 맞추지 말고 `gradlew.bat spotlessApply` 또는 `./gradlew spotlessApply`를 실행합니다.
