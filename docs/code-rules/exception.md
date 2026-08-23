# Exception Rules

## 패키지와 계층

```text
common.exception
├─ BusinessException
├─ GlobalExceptionHandler
└─ ErrorResponse

member.exception
├─ MemberErrorCode
└─ MemberNotFoundException
```

- `BusinessException`은 업무 예외의 공통 상위 타입입니다.
- Aggregate별 구체적인 예외와 ErrorCode는 해당 Aggregate의 `exception`에 둡니다.
- ErrorCode는 안정적인 업무 코드와 기본 메시지를 표현하며 Spring의 `HttpStatus`에 직접 의존하지 않습니다.
- `GlobalExceptionHandler`가 업무 ErrorCode를 승인된 HTTP 상태와 오류 응답으로 매핑합니다.

## Service 책임

- Service와 Entity는 자신의 업무 규칙에 맞는 구체적인 예외를 던지는 것까지만 책임집니다.
- 업무 예외는 공통 `BusinessException` 계층과 안정적인 `ErrorCode`로 구분합니다.
- 처리하거나 복구할 수 없는 예외를 `catch`한 뒤 동일하게 다시 던지지 않습니다.
- Repository·외부 SDK의 기술 예외를 HTTP 응답 계약에 직접 노출하지 않습니다.
- 업무 예외는 호출자가 처리 가능한 충분한 문맥을 가지되 개인정보와 비밀정보를 포함하지 않습니다.

## 전역 HTTP 예외 처리

- `@RestControllerAdvice`는 Spring MVC 요청 처리 경계의 예외를 일관된 HTTP 응답으로 변환합니다.
- 업무 예외, 입력 검증, 인증·인가와 지원하지 않는 요청 예외를 명시적으로 매핑합니다.
- 예상하지 못한 예외는 내부 메시지와 Stack Trace를 노출하지 않고 일반적인 `500` 응답으로 변환합니다.
- 예상하지 못한 예외에는 서버 로그와 연결할 수 있는 추적 ID를 제공합니다.
- 오류 응답 필드는 승인된 API 오류 계약을 따릅니다. 명세에 없는 응답 필드를 임의로 확정하지 않습니다.
- 같은 예외를 여러 계층에서 중복 로깅하지 않고 처리 경계에서 한 번 기록합니다.

`@RestControllerAdvice`는 모든 Thread, Batch와 비동기 작업의 예외를 처리하는 전역 JVM Handler가 아닙니다. HTTP 밖의 실행 경계에는 별도의 실패 처리와 관측 규칙을 둡니다.
