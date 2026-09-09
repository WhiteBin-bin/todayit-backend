# Deployment

API 서버와 외부 데이터 처리 서버의 배포 설정을 관리합니다.

- API 서버 빌드 대상: `:apps:api:bootJar`
- 데이터 처리 서버 빌드 대상: `:apps:data-worker:bootJar`
- 이 디렉터리는 Gradle 모듈이 아닙니다.

Docker, 실행환경과 CI/CD 설정은 배포 방식이 결정된 후 추가합니다.
