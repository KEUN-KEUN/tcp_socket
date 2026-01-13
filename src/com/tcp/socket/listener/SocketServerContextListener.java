package com.tcp.socket.listener;

import com.tcp.socket.server.EquipmentSocketServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Tomcat 구동 시 Socket 서버를 자동으로 시작하고,
 * 종료 시 안전하게 중단하는 Context 리스너.
 * 톰캣 기동 시 TCP 소켓 서버 자동 실행을 위한 리스너.
 * web.xml 또는 @WebListener를 통해 등록됨.
 * 
 * 정리
 * 1. Socket 서버의 정의
 * 2. ContextListener 저의 
 */
@WebListener
public class SocketServerContextListener implements ServletContextListener {

    private EquipmentSocketServer socketServer;
    private Thread serverThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	
    	String portStr = sce.getServletContext().getInitParameter("socket.port");
        int port = Integer.parseInt(portStr);

        socketServer = new EquipmentSocketServer(port);
        
        System.out.println("**********************************************************");
        System.out.println("[Listener] TCP 소켓 서버 시작됨 (port: " + port + ")");
        System.out.println("**********************************************************");
        
        serverThread = new Thread(() -> {
            try {
                socketServer.start();
            } catch (Exception e) {
                System.err.println("[SocketServer] 실행 중 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }, "SocketServerThread");

        serverThread.setDaemon(true); // 톰캣 종료 시 자동 정리
        serverThread.start();

        System.out.println("[SocketServer] 포트 " + port + "에서 서버 시작됨");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (socketServer != null) {
                socketServer.stop(); // ServerSocket 닫기
                System.out.println("[SocketServer] 정상 종료됨");
            }
        } catch (Exception e) {
            System.err.println("[SocketServer] 종료 중 오류: " + e.getMessage());
            e.printStackTrace();
        }

        if (serverThread != null && serverThread.isAlive()) {
            try {
                serverThread.interrupt(); // 필요시 인터럽트
            } catch (Exception ignore) {}
        }
    }
}
