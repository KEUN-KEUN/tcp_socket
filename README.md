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
  Prometheus exporter 연동 또는 Log/Alert 시스템 통합.
