# Javadoc Rules

Javadoc은 코드 모양을 설명하는 주석이 아니라, 호출자가 구현을 읽지 않고도 사용할 수 있도록 공개 계약과 중요한 제약을 설명합니다.

## 필수 대상

- 운영 코드의 `public` 클래스, 인터페이스, Enum, Record와 Annotation
- 운영 코드의 `public` 메서드와 생성자
- Service의 업무 조건, 상태 변경, 트랜잭션과 주요 부수 효과
- Repository의 조회 조건, 정렬, 값이 없을 때의 동작
- DTO 필드와 Record Component의 단위, 형식, 허용 범위처럼 이름만으로 알 수 없는 제약
- 구체적인 업무 예외가 발생하는 조건

Checkstyle은 `public` 타입·메서드의 Javadoc 누락과 기본 형식을 검사합니다.

## 생략 가능 대상

- `private`와 Package-private 구현 세부사항
- 테스트 클래스와 테스트 메서드
- `@Override` 메서드에서 상위 계약을 그대로 따르는 경우
- 의미가 명확한 단순 Getter·Setter
- 컴파일러나 Annotation Processor가 생성하는 코드

생략 대상이라도 중요한 업무 판단, 보안 조건이나 일반적이지 않은 동작이 있으면 Javadoc을 작성합니다.

## 작성 내용

- 첫 문장에 타입 또는 메서드의 책임을 요약합니다.
- 구현 절차보다 호출 조건, 보장 결과와 주의할 부수 효과를 설명합니다.
- 파라미터의 의미가 이름만으로 충분하지 않으면 `@param`에 단위, 형식과 허용 범위를 기록합니다.
- 반환값과 값이 없을 수 있는 조건은 `@return`으로 설명합니다.
- 호출자가 처리해야 하는 예외는 `@throws`에 발생 조건을 기록합니다.
- 관련 타입과 메서드는 `{@link Type}`으로, 코드와 리터럴은 `{@code value}`로 표현합니다.
- 관련 명세 ID가 계약 이해에 필요하면 기록하되 변경 가능한 명세 내용을 Javadoc에 복제하지 않습니다.

## 금지 사항

- 클래스명이나 메서드명을 한국어로 그대로 반복하는 설명
- 코드와 쉽게 불일치하는 구현 순서와 내부 변수 설명
- 작성자, 작성일과 수정 이력. 이 정보는 Git이 관리합니다.
- 확정되지 않은 업무 규칙과 미래 구현 계획
- Javadoc 검사 통과만을 위한 `TODO`, `설명`, `처리한다` 같은 무의미한 문장

## 예시

```java
/** 회원의 공개 조회 계약을 제공합니다. */
public interface MemberQueryService {

  /**
   * 활성 상태인 회원을 조회합니다.
   *
   * @param memberId 조회할 회원 식별자
   * @return 다른 Aggregate에 공개할 회원 조회 모델
   * @throws MemberNotFoundException 회원이 없거나 조회할 수 없는 상태인 경우
   */
  MemberInfo getActiveMember(Long memberId);
}
```
