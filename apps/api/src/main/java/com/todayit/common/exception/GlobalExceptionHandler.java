package com.todayit.common.exception;

import com.todayit.common.response.ErrorResponse;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** 업무 예외와 MVC 입력 오류를 공통 API 오류 응답으로 변환합니다. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * MVC의 400 입력 오류를 공통 {@code INVALID_REQUEST} 응답으로 변환합니다.
   *
   * <p>그 밖의 상태는 Spring MVC의 기본 예외 처리를 따르며, 응답이 이미 전송된 경우에는 새 응답을 만들지 않습니다.
   *
   * @param exception 발생한 MVC 예외
   * @param body 기본 오류 응답 본문
   * @param headers 오류 응답 헤더
   * @param statusCode 예외에 대응하는 HTTP 상태 코드
   * @param request 오류가 발생한 요청
   * @return 오류 응답, 응답이 이미 전송된 경우 {@code null}
   */
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception exception,
      Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    if (statusCode.value() == HttpStatus.BAD_REQUEST.value()) {
      CommonErrorCode errorCode = CommonErrorCode.INVALID_REQUEST;
      return super.handleExceptionInternal(
          exception,
          ErrorResponse.of(errorCode, errorCode.message(), Map.of()),
          headers,
          statusCode,
          request);
    }
    return super.handleExceptionInternal(exception, body, headers, statusCode, request);
  }

  /**
   * 업무 예외를 오류 코드에 맞는 HTTP 응답으로 변환합니다.
   *
   * @param exception 발생한 업무 예외
   * @return 공통 오류 응답
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {

    ErrorResponse response =
        ErrorResponse.of(exception.getErrorCode(), exception.getMessage(), exception.getDetails());

    return ResponseEntity.status(toHttpStatus(exception.getErrorCode().status())).body(response);
  }

  /**
   * 업무 오류 상태를 HTTP 응답 상태로 변환합니다.
   *
   * @param status 업무 오류 상태
   * @return HTTP 응답 상태
   */
  private HttpStatus toHttpStatus(ErrorStatus status) {
    return switch (status) {
      case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
      case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
      case NOT_FOUND -> HttpStatus.NOT_FOUND;
      case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
      case CONFLICT -> HttpStatus.CONFLICT;
    };
  }
}
