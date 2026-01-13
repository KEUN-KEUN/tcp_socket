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

