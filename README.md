# Perfume Backend

향수 서비스의 Spring Boot 백엔드입니다. 향수 목록, 리뷰, 위시리스트, 향 선호도 테스트, 레이어링 추천, 로컬/OAuth 인증, 프로필 이미지 업로드 API를 제공합니다.

## 기술 스택

- Java 25
- Spring Boot 4.1.0-M2
- Gradle Wrapper 9.3.1
- Spring MVC, Spring Security, OAuth2 Client, OAuth2 Resource Server
- Spring Data JPA, Hibernate ORM 7
- MySQL 8.0
- H2 Test DB
- Lombok, OpenCSV, AWS SDK S3 클라이언트
- Spotless, Error Prone

## 사전 준비

### 1. JDK 25 설치

이 프로젝트는 Gradle toolchain에서 Java 25를 요구합니다.

```bash
java -version
./gradlew -version
```

`./gradlew -version` 출력에서 JVM이 25로 잡히면 빌드 준비가 끝납니다. 여러 JDK를 같이 쓰는 환경에서는 `JAVA_HOME`을 JDK 25 경로로 지정합니다.

### 2. MySQL 준비

기본 설정은 MySQL을 사용합니다. `src/main/resources/application.properties`의 기본값은 다음 DB를 바라봅니다.

```properties
spring.datasource.url=jdbc:mysql://192.168.35.85:3309/perfume_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=root
```

팀 개발 DB를 쓰는 경우에는 별도 설정 없이 실행할 수 있습니다. 로컬 DB를 쓰려면 환경 변수로 datasource를 덮어씁니다.

```bash
docker run --name perfume-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=perfume_db \
  -p 3309:3306 \
  -d mysql:8.0

export SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3309/perfume_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false'
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root
```

`compose.yaml`을 사용할 수도 있습니다. 사용 전 `MYSQL_ROOT_PASSWORD`, 노출 포트, `spring.datasource.*` 값이 서로 맞는지 확인하세요.

## 설치

저장소를 받은 뒤 프로젝트 루트에서 Gradle Wrapper를 사용합니다.

```bash
cd /path/to/perfume-backend
./gradlew clean build
```

테스트만 실행하려면 다음 명령을 사용합니다.

```bash
./gradlew test
```

포맷 검사는 Spotless로 실행합니다.

```bash
./gradlew spotlessCheck
```

포맷을 자동 적용하려면 다음 명령을 사용합니다.

```bash
./gradlew spotlessApply
```

## 실행

로컬 실행에는 DB 설정과 JWT 시크릿이 필요합니다. `JWT_SECRET`은 HS256 서명에 쓰이므로 32바이트 이상이어야 합니다.

```bash
export JWT_SECRET='01234567890123456789012345678901'
export APP_AUTH_COOKIE_SECURE=false
export APP_AUTH_COOKIE_SAME_SITE=Lax
export APP_CORS_ALLOWED_ORIGINS='http://localhost:3000'

./gradlew bootRun
```

서버는 기본 포트 `8080`에서 실행됩니다.

```bash
curl http://localhost:8080/actuator/health
```

## 환경 변수

| 변수 | 필수 | 기본값 | 용도 |
| --- | --- | --- | --- |
| `SPRING_DATASOURCE_URL` | 로컬 DB 사용 시 권장 | `application.properties` 값 | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | 로컬 DB 사용 시 권장 | `root` | DB 사용자 |
| `SPRING_DATASOURCE_PASSWORD` | 로컬 DB 사용 시 권장 | `root` | DB 비밀번호 |
| `JWT_SECRET` | 실행 시 필수 | 빈 값 | JWT HS256 서명 키. 32바이트 이상 |
| `GOOGLE_CLIENT_ID` | OAuth 사용 시 필수 | 빈 값 | Google OAuth client id |
| `GOOGLE_CLIENT_SECRET` | OAuth 사용 시 필수 | 빈 값 | Google OAuth client secret |
| `NAVER_CLIENT_ID` | OAuth 사용 시 필수 | 빈 값 | Naver OAuth client id |
| `NAVER_CLIENT_SECRET` | OAuth 사용 시 필수 | 빈 값 | Naver OAuth client secret |
| `APP_AUTH_JWT_ACCESS_TOKEN_VALIDITY` | 선택 | `1h` | Access token 유효 시간 |
| `APP_AUTH_COOKIE_NAME` | 선택 | `PERFUME_ACCESS_TOKEN` | 인증 쿠키 이름 |
| `APP_AUTH_COOKIE_SECURE` | 로컬 HTTP 실행 시 설정 | `true` | 인증 쿠키 Secure 속성 |
| `APP_AUTH_COOKIE_SAME_SITE` | 로컬 HTTP 실행 시 설정 | `None` | 인증 쿠키 SameSite 속성 |
| `APP_OAUTH2_SUCCESS_REDIRECT_URI` | 선택 | `https://thescentlab.vercel.app/oauth2/success` | OAuth 성공 리다이렉트 |
| `APP_OAUTH2_FAILURE_REDIRECT_URI` | 선택 | `https://thescentlab.vercel.app/oauth2/failure` | OAuth 실패 리다이렉트 |
| `APP_CORS_ALLOWED_ORIGINS` | 프론트 주소가 다르면 필수 | `https://localhost:3000,https://thescentlab.vercel.app` | CORS 허용 origin 목록 |
| `APP_AUDIT_TRUSTED_PROXY_ADDRESSES` | 선택 | loopback 주소 | 감사 로그에서 신뢰할 프록시 주소 |
| `APP_AUDIT_RETENTION_DAYS` | 선택 | `180` | 감사 로그 보관일 |
| `APP_AUDIT_RETENTION_CLEANUP_CRON` | 선택 | `0 30 3 * * *` | 감사 로그 정리 cron |
| `APP_R2_ACCOUNT_ID` | 프로필 이미지 업로드 사용 시 필수 | 빈 값 | Cloudflare R2 account id |
| `APP_R2_ACCESS_KEY_ID` | 프로필 이미지 업로드 사용 시 필수 | 빈 값 | R2 access key id |
| `APP_R2_SECRET_ACCESS_KEY` | 프로필 이미지 업로드 사용 시 필수 | 빈 값 | R2 secret access key |
| `APP_R2_BUCKET` | 프로필 이미지 업로드 사용 시 필수 | 빈 값 | R2 bucket |
| `APP_R2_PUBLIC_BASE_URL` | 프로필 이미지 업로드 사용 시 필수 | 빈 값 | 업로드 이미지 공개 base URL |
| `APP_R2_KEY_PREFIX` | 선택 | `profile-images` | R2 object key prefix |
| `APP_R2_MAX_SIZE` | 선택 | `5MB` | 프로필 이미지 최대 크기 |

## 로컬 실행 예시

```bash
cd /path/to/perfume-backend

# DB가 없으면 먼저 실행합니다.
docker run --name perfume-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=perfume_db \
  -p 3309:3306 \
  -d mysql:8.0

export SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3309/perfume_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false'
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root
export JWT_SECRET='01234567890123456789012345678901'
export APP_AUTH_COOKIE_SECURE=false
export APP_AUTH_COOKIE_SAME_SITE=Lax
export APP_CORS_ALLOWED_ORIGINS='http://localhost:3000'

./gradlew bootRun
```

OAuth 로그인과 프로필 이미지 업로드를 실제로 호출하려면 OAuth client 값과 R2 값을 함께 설정합니다. 값을 비워 두면 관련 API 호출 시 외부 연동 단계에서 실패할 수 있습니다.

## API 문서

- 전체 REST API: [`docs/rest-api-spec.md`](docs/rest-api-spec.md)
- 프론트 인증 연동: [`docs/oauth-login.md`](docs/oauth-login.md)
- 향 선호도 API: [`docs/preference-api-spec.md`](docs/preference-api-spec.md)
- 마이페이지 API: [`docs/mypage-api.md`](docs/mypage-api.md)
- 레이어링 API: [`docs/layering-api/README.md`](docs/layering-api/README.md)

## 문제 해결

### `JWT_SECRET must be at least 32 bytes for HS256`

`JWT_SECRET`이 비어 있거나 32바이트보다 짧습니다. 로컬에서는 다음처럼 32자 이상 값을 지정합니다.

```bash
export JWT_SECRET='01234567890123456789012345678901'
```

### DB 연결 실패

로컬 MySQL을 쓴다면 컨테이너 포트와 datasource URL을 맞춥니다.

```bash
docker ps --filter name=perfume-mysql
```

컨테이너가 `0.0.0.0:3309->3306/tcp` 형태로 떠 있어야 위 예시 설정과 맞습니다.

### 쿠키가 로컬 브라우저에 저장되지 않음

HTTP 로컬 개발에서는 Secure 쿠키를 저장하지 않는 브라우저가 많습니다. 로컬 실행 중에는 다음 값을 사용합니다.

```bash
export APP_AUTH_COOKIE_SECURE=false
export APP_AUTH_COOKIE_SAME_SITE=Lax
```

배포 환경에서는 HTTPS를 쓰고 기본값인 `Secure=true`, `SameSite=None`을 유지합니다.

### CORS 오류

프론트엔드 주소를 `APP_CORS_ALLOWED_ORIGINS`에 추가합니다. 여러 origin은 쉼표로 구분합니다.

```bash
export APP_CORS_ALLOWED_ORIGINS='http://localhost:3000,https://thescentlab.vercel.app'
```

## 검증 체크리스트

문서나 설정을 바꾼 뒤 아래 항목을 확인합니다.

```bash
./gradlew test
./gradlew spotlessCheck
```

- `README.md`의 링크가 실제 파일을 가리키는지 확인합니다.
- 실행 예시의 환경 변수 이름이 `application.properties`와 일치하는지 확인합니다.
- 공개 API를 바꿨다면 `docs/` 아래 API 문서도 같이 갱신합니다.
