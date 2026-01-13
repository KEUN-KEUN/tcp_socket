package com.tcp.socket.test;

import com.tcp.socket.server.EquipmentSocketServer;

import java.util.Scanner;

/**
 * Tomcat 없이 독립 실행 가능한 TCP 소켓 서버 테스트 클래스.
 * 테스트 포트에서 서버를 구동하고, 사용자 입력으로 종료 제어 가능.
 */
public class SocketServerTestMain {

    public static void main(String[] args) {
        int port = 9000; // 테스트용 포트
        EquipmentSocketServer server = new EquipmentSocketServer(port);

        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                System.err.println("[TestMain] 서버 실행 중 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }, "TestSocketServerThread");

        serverThread.start();
        System.out.println("[TestMain] TCP 소켓 서버 실행됨 (포트 " + port + ")");
        System.out.println("[TestMain] 종료하려면 'exit' 입력 후 Enter");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input.trim())) {
                    System.out.println("[TestMain] 서버 종료 요청 수신됨");
                    server.stop();
                    break;
                }
            }
        }

        try {
            serverThread.join(); // 서버 종료까지 대기
        } catch (InterruptedException e) {
            System.err.println("[TestMain] 종료 대기 중 인터럽트 발생");
        }

        System.out.println("[TestMain] 종료 완료");
    }
}
