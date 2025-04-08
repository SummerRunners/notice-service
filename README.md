# Notice Service

## 📋 프로젝트 소개
`Notice Service`는 MSA 구조에서 알림(Email, Slack)을 처리하는 알림 서비스 모듈입니다.  
**Spring Boot**를 기반으로 하며 이메일 전송, Slack 메시지 전송 이력 기록 등을 제공하며, 추후 다양한 알림 채널 확장을 계획하고 있습니다.

---

## 🚀 주요 기술 스택

### 1. **프로그래밍 언어**
- **Java 17**: JDK 17을 기반으로 빌드.

### 2. **백엔드 프레임워크**
- **Spring Boot 3.4.4**:
    - `spring-boot-starter-web`: REST API 설계 및 개발.
    - `spring-boot-starter-mail`: 이메일 전송 기능 지원.
    - `spring-boot-starter-thymeleaf`: 이메일 템플릿(HTML) 렌더링.
    - `spring-boot-starter-validation`: 입력값 검증(예: 이메일 주소의 유효성 검사).

### 3. **템플릿 엔진**
- **Thymeleaf**: HTML 기반 동적 템플릿 렌더링.

### 4. **데이터베이스**
- **MongoDB**:
    - 문서 기반의 NoSQL 데이터베이스로, 유연한 스키마와 높은 확장성 제공.
    - 알림 전송 내역 기록 및 이력 관리.

### 5. **빌드 도구**
- **Gradle**:
    - 플러그인 기반의 빌드 도구로, `org.springframework.boot`와 `io.spring.dependency-management` 사용.

---

## 🎯 주요 기능

### 1. **이메일 전송**
- HTML 템플릿 기반 이메일 발송 지원 (`Thymeleaf` 사용).
- 전송 성공 및 실패 결과를 MongoDB에 저장.

### 2. **Slack 메시지 전송**
- Slack 채널로 메시지 전송 (추후 구현 예정).

### 3. **알림 이력 관리**
- 알림 전송 결과(성공/실패)를 MongoDB에 저장하여 조회 가능.

---

## 🛠️ 개발 예정 기능
- 다양한 알림 채널 (ex. SMS, 푸시 알림 등) 지원.
- 알림 실패 재시도 기능.

---

## 📧 문의처
이 프로젝트와 관련된 질문 또는 협업 제안은 아래로 문의해주세요.
- Email: `srunners.owner@gmail.com`