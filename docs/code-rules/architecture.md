# Architecture Rules

## Aggregate Root 중심 패키지

최상위 업무 패키지는 DB 테이블이 아니라 Aggregate Root와 업무 책임을 기준으로 나눕니다.

```text
com.todayit
├─ member
│  ├─ controller
│  ├─ service
│  │  ├─ command
│  │  └─ model
│  ├─ repository
│  ├─ entity
│  ├─ dto
│  │  ├─ request
│  │  └─ response
│  └─ exception
├─ couple
│  ├─ controller
│  ├─ service
│  ├─ repository
│  ├─ entity
│  ├─ dto
│  └─ exception
└─ common
   ├─ config
   ├─ exception
   └─ client
```

- `controller`: HTTP 요청 수신, 검증과 응답 반환
- `service`: UseCase 조정, 트랜잭션 경계와 업무 흐름
- `service.command`: 여러 입력 경로에서 재사용하는 Service 입력 모델
- `service.model`: 다른 Aggregate에 공개할 불변 조회 모델
- `repository`: 해당 Aggregate Root의 Repository Interface와 필요한 조회 구현
- `entity`: Aggregate Root, Entity, Value Object와 내부 업무 규칙
- `dto`: Controller의 요청·응답 모델. 규모가 커지면 `request`, `response`로 분리
- `exception`: 해당 Aggregate에서 발생하는 구체적인 업무 예외
- `client`: 외부 API, 메시징과 다른 외부 시스템 연계
- `common`: 실제로 여러 Aggregate가 공유하는 기술 요소만 포함
- 모든 Controller·Service·Repository를 한곳에 모으는 전역 수평 패키지를 만들지 않습니다.

## 의존성 방향

- 업무 규칙은 Web, 데이터베이스, 메시징과 외부 SDK 세부사항에 직접 의존하지 않도록 합니다.
- 의존성은 외부 계층에서 핵심 로직 방향으로 향하게 합니다.
- Controller·Handler는 입력 변환과 응답 조립에 집중하고 업무 판단을 직접 구현하지 않습니다.
- 외부 시스템과 영속성 접근은 명확한 인터페이스 경계를 둡니다.
- 계층 간 순환 의존성을 만들지 않습니다.
- 트랜잭션 범위는 하나의 업무 단위에 맞추고 네트워크 호출을 불필요하게 포함하지 않습니다.
- 공유 유틸리티는 책임과 소유자가 명확할 때만 만들며 도메인 로직의 우회 장소로 사용하지 않습니다.
- 확장 가능성을 이유로 현재 필요하지 않은 추상화와 계층을 미리 만들지 않습니다.

## Repository와 Aggregate 경계

- 각 Aggregate는 자신이 소유한 Repository Interface만 사용합니다.
- Aggregate Root의 Repository Interface는 해당 Aggregate의 `repository`에 둡니다.
- 외부 시스템 연계 구현은 소유 Aggregate의 `client` 또는 공통 기술 연계인 경우 `common.client`에 둡니다.
- `Service A`는 다른 Aggregate인 `Entity B`를 조회하기 위해 `Repository B`를 직접 주입하지 않습니다. Interface여도 동일합니다.
- 다른 Aggregate의 정보는 해당 Aggregate가 공개한 Service 조회 메서드 또는 Facade를 통해 사용합니다.
- 다른 Aggregate 조회에는 `{Aggregate}QueryService`를 사용하고 `Entity` 대신 필요한 값만 가진 `{Aggregate}Info`·`{Aggregate}Summary` 같은 불변 조회 모델을 반환합니다.
- 공개 조회 모델은 제공 Aggregate의 `service.model`에 두며 업무 행위와 상태 변경 메서드를 포함하지 않습니다.
- 두 Aggregate를 함께 조정해야 하면 한쪽 Service가 다른 쪽을 연쇄 호출하게 하지 않고 별도 `Facade`·`Coordinator`가 흐름을 소유합니다.
- 비동기 일관성이 허용되는 후속 처리는 도메인 이벤트를 고려합니다.
- 중간 테이블의 관계가 독립적인 상태와 생명주기를 가지면 별도 Aggregate로 만들고 전용 Service·Repository를 둡니다.
- 단순 조회 조합은 Aggregate의 쓰기 Repository를 우회하지 말고 전용 조회 Service 또는 Read Model로 분리합니다.

## Service 순환 의존성

- `Service A → Service B → Service A` 형태의 양방향 의존을 금지합니다.
- `@Lazy`, Setter 주입과 Spring 순환 참조 허용 설정으로 문제를 숨기지 않습니다.
- 순환이 생기면 책임을 별도 Facade·Coordinator로 이동하거나 조회 책임을 분리합니다.
- Service는 다른 Aggregate의 DTO, Repository와 내부 Entity 구현에 의존하지 않습니다.

## Facade·Coordinator

- 여러 Aggregate를 함께 변경하는 UseCase만 Facade·Coordinator로 분리합니다.
- Facade는 해당 UseCase의 결과를 소유하는 대표 Aggregate의 `service`에 둡니다.
- Facade는 하위 Service를 조정하며 Repository를 직접 사용하지 않습니다.
- 단일 Aggregate만 다루는 기능에 Facade를 기계적으로 만들지 않습니다.

## Transaction

- 상태 변경 트랜잭션은 Service 또는 Facade의 `public` UseCase 메서드에서 시작합니다.
- 단순 조회는 `@Transactional(readOnly = true)`를 사용합니다.
- Controller와 DTO에 `@Transactional`을 선언하지 않습니다.
- `private` 메서드와 같은 클래스의 내부 호출에 선언한 `@Transactional`이 별도 Proxy 경계를 만든다고 기대하지 않습니다.
- DB 트랜잭션 안에 느린 외부 API 호출을 불필요하게 포함하지 않습니다.
- 외부 연계와 DB 변경의 일관성이 필요하면 Timeout, 재시도, 멱등성, 보상 처리 또는 Outbox 적용 여부를 명시적으로 결정합니다.
