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
