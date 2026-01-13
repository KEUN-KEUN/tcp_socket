package com.tcp.socket.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final EquipmentSocketServer server;

    private BufferedReader reader;
    private PrintWriter writer;

    private String equipmentId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClientHandler(Socket socket, EquipmentSocketServer server) throws IOException {
        this.socket = socket;
        this.server = server;

        this.reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    @Override
    public void run() {
        System.out.println(" [ClientHandler] 연결 수립됨: " + socket.getRemoteSocketAddress());

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(" 수신 [" + label() + "]: " + line);

                JsonNode json = objectMapper.readTree(line);
                handleMessage(json);
            }
        } catch (IOException e) {
            System.err.println(" [ClientHandler] 통신 오류 (" + label() + "): " + e.getMessage());
        } finally {
        	cleanup();
        }
    }

    private void handleMessage(JsonNode msg) {
        JsonNode header = msg.path("header");
        String type = header.path("type").asText(null);

        if (equipmentId == null && msg.has("equipmentId")) {
            equipmentId = msg.get("equipmentId").asText();
            server.registerHandler(equipmentId, this);
            System.out.println("✅ 장비 등록됨: " + equipmentId);
        }

        switch (type) {
        	case "START":
        		handleStart(msg);
        		break;
        	case "STATUS":
                handleStatus(msg);
                break;
            case "ALARM":
                handleAlarm(msg);
                break;
            case "COMMANDRESPONSE":
                handleCommandResponse(msg);
                break;
            default:
                System.err.println(" [ClientHandler] 알 수 없는 type (" + label() + "): " + type);
        }
    }

    private void handleStart(JsonNode msg) {
        System.out.println(" [START][" + label() + "]: " + msg.toString());

        // 예시: 서버에서 응답 메시지 전송
        String response = String.format(
            "{\"type\":\"startAck\",\"equipmentId\":\"%s\",\"status\":\"received\",\"message\":\"START 명령 수신됨\"}",
            equipmentId);
        
        send(response);
    }
    
    private void handleStatus(JsonNode msg) {
        System.out.println(" [Status][" + label() + "]: " + msg.toString());
    }

    private void handleAlarm(JsonNode msg) {
        System.out.println(" [Alarm][" + label() + "]: " + msg.toString());
    }

    private void handleCommandResponse(JsonNode msg) {
        System.out.println(" [CommandResponse][" + label() + "]: " + msg.toString());
    }

    /**
     * 서버 → 장비 메시지 전송
     */
    public void send(String message) {
        if (writer != null) {
            try {
                writer.println(message);
                writer.flush(); // 안전하게 보냄
                System.out.println(" 송신 [" + label() + "]: " + message);
                
                // 응답 전송 후 약간의 시간 대기 (클라이언트 수신 완료 보장용)
                try {
                    Thread.sleep(200); // 200ms 대기
                } catch (InterruptedException ignore) {}
                
            } catch (Exception e) {
                System.err.println(" [송신 실패][" + label() + "]: " + e.getMessage());
            }
        } else {
            System.err.println(" [송신 실패][" + label() + "]: writer가 null입니다.");
        }
    }


    /**
     * 연결 종료 및 리소스 해제
     */
    private void cleanup() {
        if (equipmentId != null) {
            server.unregisterHandler(equipmentId);
        }
        try {
            socket.close();
        } catch (IOException ignore) {}
        System.out.println(" 연결 종료됨: " + label());
    }

    /**
     * 장비 ID 또는 소켓 주소 표시용
     */
    private String label() {
        return (equipmentId != null) ? equipmentId : socket.getRemoteSocketAddress().toString();
    }
}

