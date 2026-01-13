# 🧩 프로젝트 개요: TCP 기반 장비 ↔ MES 통신 서버

이 프로젝트는 Java 기반 TCP Socket Server로, 제조 현장의 장비(Equipment)들과 양방향 메시지 통신을 처리합니다.
각 장비는 고유 ID(equipmentId)로 구분되며, JSON 기반 명령 수신 및 응답 구조를 따릅니다.


# ⚙️ 구성 요소 요약

1. EquipmentSocketServer
  장비 접속을 수락하는 TCP 서버.
  장비 ID별로 연결된 클라이언트를 관리 (ConcurrentHashMap).
  외부에서 특정 장비로 JSON 명령 전송 가능 (sendCommandTo()).

2. ClientHandler
  장비와의 1:1 연결을 처리하는 스레드 (Runnable).
  JSON 메시지를 수신 → 파싱 → 메시지 타입(type)에 따라 분기 처리.
  서버에서 장비로 응답 전송 (send()).
  메시지 종류: START, STATUS, ALARM, COMMANDRESPONSE.

3. ContextListener (SocketServerContextListener)
  WAS (Tomcat 등) 구동 시 web.xml에서 socket.port 설정을 읽어 소켓 서버 시작.
  웹 애플리케이션과 별개로 백그라운드 소켓 서버 동작 보장.

4. Python 테스트 클라이언트
  START 명령을 JSON 형식으로 전송하고 서버 응답 수신.
  로컬 개발환경에서 수신/응답 시나리오 검증에 활용.


# 📡 통신 메시지 구조 예시

{
  "header": {
    "equipmentId": "EQ001",
    "command": "INSPECT",
    "type": "START",
    "timestamp": "2026-01-13T13:40:21Z"
  },
  "body": {
    "operator": "operator1"
  }
}

→ 서버는 수신 후 아래 응답 전송:
{
  "type": "startAck",
  "equipmentId": "EQ001",
  "status": "received",
  "message": "START 명령 수신됨"
}


# 🧪 주요 기능 및 로그 예시

연결 수립:
  [ClientHandler] 연결 수립됨: /127.0.0.1:54321
  ✅ 장비 등록됨: EQ001

메시지 수신:
  📩 수신 [EQ001]: {...}
  [START][EQ001]: {...}

응답 송신:
  📤 송신 [EQ001]: {"type":"startAck",...}

연결 종료:
  🔌 연결 종료됨: EQ001


# 🛠 개선 방향 제안

✅ 기능적 개선
  Heartbeat / Ping 메시지 처리
  장비 생존 여부 판단을 위한 주기적 ping-pong 구조 도입.
  MessageRouter 클래스 도입
  handleMessage() 분기를 CommandRouter 등으로 위임해 SRP 원칙 강화.
  장비 상태 캐싱
  STATUS 메시지 처리 시 장비 상태를 메모리에 캐싱하거나 Redis 연동.
  메시지 유효성 검증 및 예외 처리 강화
  필드 누락, 잘못된 type 등 JSON Validation 도입.

✅ 구조 및 운영 개선
  비동기 로그 처리 도입 (예: Logback AsyncAppender)
  고속 처리 환경에서 I/O 병목 방지.
  Socket 연결 유지 기반 모드 지원
  클라이언트가 장시간 연결 유지하며 명령 수신하도록 변경 옵션 제공.
  TLS 통신 및 인증 체계 도입
  민감 통신 환경이라면 TLS 및 HMAC 인증 등 강화.
  모니터링/메트릭 통합
  Prometheus exporter 연동 또는 Log/Alert 시스템 통합..






# TCP Equipment Socket Server

Java 기반 장비 ↔ MES 간 TCP 통신을 위한 소켓 서버 애플리케이션입니다. 각 장비는 JSON 기반 명령으로 서버에 연결되며, 실시간 제어 및 모니터링 메시지를 주고받습니다.

---

## ✨ 프로젝트 개요

* **목적:** 장비와 MES 간의 양방향 통신을 위해 안정적인 TCP 서버 구현
* **형식:** 메시지는 JSON 형식으로 구성되며, command, status, alarm 등의 타입 구분 처리
* **연동:** Tomcat(Web App)과 함께 구동되며 Context Listener 기반으로 서버가 자동 시작

---

## ⚙️ 구성 요소

### 1. `EquipmentSocketServer`

* 지정 포트에서 장비 연결 수신
* 장비 ID별로 연결 관리 (ConcurrentMap)
* 외부에서 특정 장비로 메시지 전송 기능 제공

### 2. `ClientHandler`

* 장비 1대와의 연결을 독립적으로 관리 (Runnable)
* JSON 메시지 수신 → 파싱 → 메시지 타입 분기
* 서버 응답 전송 (`send()`)

### 3. `SocketServerContextListener`

* Tomcat 시작 시 소켓 서버 자동 기동
* `web.xml`에 포트 설정 (`context-param`)

### 4. Python 테스트 클라이언트

* 단일 메시지 테스트용 TCP 클라이언트
* START 명령 전송 및 서버 응답 확인 가능

---

## 📁 메시지 구조 예시

### ▶️ 요청 메시지 (START)

```json
{
  "header": {
    "equipmentId": "EQ001",
    "command": "INSPECT",
    "type": "START",
    "timestamp": "2026-01-13T13:40:21Z"
  },
  "body": {
    "operator": "operator1"
  }
}
```

### ▶️ 응답 메시지

```json
{
  "type": "startAck",
  "equipmentId": "EQ001",
  "status": "received",
  "message": "START 명령 수신되음"
}
```

---

## 📊 로그 예시

```text
[ClientHandler] 연결 수립됨: /127.0.0.1:54321
✅ 장비 등록됨: EQ001
📩 수신 [EQ001]: {...}
[START][EQ001]: {...}
📤 송신 [EQ001]: {"type":"startAck",...}
🔌 연결 종료됨: EQ001
```

---

## ✅ 개선 방향

### ① 기능 향상

* Heartbeat 또는 ping 명령 처리 기능
* MessageRouter 구현 및 handler 가능 사이드 정렬
* STATUS 명령 건너여 Redis 나 DB에 재고
* JSON 구조 검증 (검증 누락 시 fallback 처리)

### ② 운영 측면

* 비동기 로그 처리를 위해 Logback AsyncAppender 등 도입
* Socket 연결 유지 기반 Long-Session 모드 지원
* TLS + HMAC 그리고 client 인증 체계 도입
* Prometheus/Grafana 등을 통해 메트릭 확인

---

## 📝 web.xml 설정 예시

```xml
<context-param>
  <param-name>socket.port</param-name>
  <param-value>9100</param-value>
</context-param>
<listener>
  <listener-class>com.mycompany.ucp_socket.bootstrap.SocketServerContextListener</listener-class>
</listener>
```

---

## 💾 실행 방법 요약

1. `web.xml` 에서 `socket.port` 설정 (예: 9100)
2. Tomcat 서버 실행
3. Python 테스터 코드 실행
4. 서버 로그 및 클라이언트 응답 확인

---

## 🚀 목적에 부합하는 용도

* MES 건설 환경에서 지방 장비 및 사용자 반응 처리
* C/S 구조가 없는 경우, 여러 장비가 TCP 링크로 전달
* Web UI가 못 되는 통신과가 필요한 통신 목적

---

⚡️ **이 프로젝트는 실제 현장 테스트와 연동 테스트에서 사용 가능한 실용적인 소켓 서버 예제입니다.**

