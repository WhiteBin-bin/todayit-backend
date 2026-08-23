# DTO Rules

DTO는 계층 경계의 데이터 전달과 단순 변환만 책임집니다.

## 허용

- Request DTO의 검증 Annotation
- 필요한 경우 Request DTO에서 Service 입력 Command로의 단순 필드 변환
- Response DTO의 `from(...)`, `of(...)` 정적 팩터리
- 이름·형식·기본 타입이 명확한 값의 기계적인 변환

```java
public record CreateMemberRequest(
    String email,
    String password
) {
    public CreateMemberCommand toCommand() {
        return new CreateMemberCommand(email, password);
    }
}
```

```java
public record MemberResponse(
    Long id,
    String email
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail());
    }
}
```

## 금지

- DTO에서 Repository, Service와 외부 API 호출
- DTO에서 권한, 중복, 상태 전이와 같은 업무 판단
- DTO 변환 과정에서 Entity 저장 또는 변경
- Entity가 `toResponseDto()`처럼 Controller DTO에 의존하는 구조
- 여러 Aggregate와 외부 결과를 조합하는 복잡한 변환

복잡한 조합은 Service 계층의 Assembler·Mapper 또는 전용 응답 생성 책임으로 분리합니다. 자동 매핑 라이브러리는 명시적인 변환보다 이점이 확인될 때만 도입합니다.

## Request와 Command 선택 기준

- 단순 CRUD이며 입력 경로가 HTTP 하나뿐이면 Service가 해당 Aggregate의 Request DTO를 직접 받을 수 있습니다.
- 같은 UseCase를 HTTP, Batch, Message 등 여러 입력 경로에서 사용하거나 업무 입력 의미를 분리해야 하면 `service.command`에 Command를 둡니다.
- 모든 Request마다 Command를 기계적으로 만들지 않습니다.
- 다른 Aggregate의 Service에 현재 Aggregate의 Request·Response DTO를 전달하지 않습니다.

## Aggregate 간 조회 모델

- 다른 Aggregate에는 Entity와 Controller DTO 대신 `service.model`의 불변 조회 모델을 공개합니다.
- 조회 모델은 필요한 필드만 포함하며 Setter와 업무 행위를 제공하지 않습니다.
- 조회 모델 이름은 `MemberInfo`, `MemberSummary`처럼 용도를 드러냅니다.
