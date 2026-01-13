package com.tcp.socket.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final String equipmentId;
    private PrintWriter out;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ClientHandler(Socket socket, String equipmentId) {
        this.socket = socket;
        this.equipmentId = equipmentId;
    }

    @Override
    public void run() {
        System.out.println("🟢 [" + equipmentId + "] 연결됨");

        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = writer;
            String line;

            while ((line = in.readLine()) != null) {
                logReceived(line);
                handleMessage(line);
            }

        } catch (IOException e) {
            System.err.println("❌ [" + equipmentId + "] 통신 오류: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * 클라이언트로부터 받은 메시지를 로그 출력
     */
    private void logReceived(String message) {
        System.out.println("📩 수신 [" + equipmentId + "]: " + message);
    }

    /**
     * 수신된 메시지를 파싱하고 처리 로직 분기
     */
    private void handleMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode header = root.path("header");
            String command = header.path("command").asText(null);

            if (command != null) {
                System.out.println("🔍 명령어: " + command);
                // MessageRouter.route(root, this); // 추후 연결 지점
            } else {
                System.out.println("⚠ command 필드 없음");
            }

        } catch (Exception e) {
            System.err.println("⚠ JSON 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * 서버에서 클라이언트로 메시지 전송
     */
    public void send(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("📤 송신 [" + equipmentId + "]: " + message);
        } else {
            System.err.println("⚠ 송신 실패 (out=null)");
        }
    }

    /**
     * 소켓 종료 처리
     */
    private void cleanup() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignore) {}
        System.out.println("🔌 연결 종료 [" + equipmentId + "]");
    }
}
