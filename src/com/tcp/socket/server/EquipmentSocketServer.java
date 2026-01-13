package com.tcp.socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

import com.tcp.socket.server.ClientHandler;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 장비 ↔ MES 소켓 통신을 위한 TCP 서버.
 * 장비 ID별 연결 관리 및 명령 전송 기능 포함.
 * 
 * 정리 
 * 1. Socket 서버와 TCP 서버의 역할 
 *   
 */
public class EquipmentSocketServer {

    private final int port;
    private volatile boolean running = false;

    private ServerSocket serverSocket;
    private ExecutorService threadPool;

    // 장비 ID ↔ 클라이언트 핸들러 매핑
    private final ConcurrentMap<String, ClientHandler> clientMap = new ConcurrentHashMap<>();

    public EquipmentSocketServer(int port) {
        this.port = port;
    }

    /**
     * 서버 실행: 소켓 열고 연결 수락 루프 진입
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newCachedThreadPool();
        running = true;

        System.out.println("[SocketServer] 포트 " + port + "에서 수신 대기 중...");

        while (running) {
            try {
                Socket socket = serverSocket.accept(); // blocking
                ClientHandler handler = new ClientHandler(socket, this);
                threadPool.submit(handler);
            } catch (IOException e) {
                if (running) {
                    System.err.println("[SocketServer] 연결 수락 중 오류: " + e.getMessage());
                }
            }
        }

        shutdownResources();
    }

    
    /**
     * 장비 ID 기준으로 핸들러 등록
     */
    public void registerHandler(String equipmentId, ClientHandler handler) {
        clientMap.put(equipmentId, handler);
        System.out.println("[SocketServer] 장비 등록됨: " + equipmentId);
    }

    /**
     * 장비 ID 기준으로 핸들러 제거
     */
    public void unregisterHandler(String equipmentId) {
        clientMap.remove(equipmentId);
        System.out.println("[SocketServer] 장비 해제됨: " + equipmentId);
    }

    /**
     * 특정 장비로 명령(JSON) 전송
     */
    public boolean sendCommandTo(String equipmentId, JsonNode commandJson) {
        ClientHandler handler = clientMap.get(equipmentId);
        if (handler != null) {
            handler.send(commandJson.toString());
            return true;
        } else {
            System.err.println("[SocketServer] 장비 없음: " + equipmentId);
            return false;
        }
    }
    

    /**
     * 서버 종료
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignore) {}
        shutdownResources();
    }

    private void shutdownResources() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }
        System.out.println("[SocketServer] 서버 종료 완료");
    }
}

