package com.tcp.socket.message;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 수신된 메시지를 type에 따라 적절한 처리기로 라우팅.
 * 단순 분기 방식이며, 확장 시 핸들러 인터페이스 기반으로 개선 가능.
 */
public class MessageRouter {

    public void route(JsonNode rootJson) {
        JsonNode headerNode = rootJson.path("header");
        JsonNode bodyNode = rootJson.path("body");

        String type = headerNode.path("type").asText(null);
        String equipmentId = headerNode.path("equipmentId").asText(null);

        if (type == null) {
            System.err.println("[Router] type 없음. 메시지 무시.");
            return;
        }

        switch (type) {
            case "status":
                handleStatus(equipmentId, bodyNode);
                break;

            case "alarm":
                handleAlarm(equipmentId, bodyNode);
                break;

            case "commandResponse":
                handleCommandResponse(equipmentId, bodyNode);
                break;

            default:
                System.err.println("[Router] 알 수 없는 type: " + type);
        }
    }

    private void handleStatus(String equipmentId, JsonNode body) {
        System.out.printf("[Status] [%s] 데이터 수신: %s%n", equipmentId, body);
        // TODO: DB 저장, 모니터링 업데이트 등
    }

    private void handleAlarm(String equipmentId, JsonNode body) {
        System.out.printf("[Alarm] [%s] 알람 수신: %s%n", equipmentId, body);
        // TODO: 알람 로그, 사용자 알림 등
    }

    private void handleCommandResponse(String equipmentId, JsonNode body) {
        System.out.printf("[CommandResponse] [%s] 응답 수신: %s%n", equipmentId, body);
        // TODO: 명령 처리 결과 반영
    }
}
