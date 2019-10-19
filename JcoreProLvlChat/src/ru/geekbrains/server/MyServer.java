package ru.geekbrains.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MyServer {
    private final int PORT = 8189;

    private List<ClientHandler> clients;
    private AuthService authService;

    private Logger logger;

    public AuthService getAuthService() {
        return authService;
    }
    public Logger getLogger() { return logger; }

    public MyServer(Logger logger) {
        this.logger = logger;

        try (ServerSocket server = new ServerSocket(PORT)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                //System.out.println("Сервер ожидает подключения");
                logger.info("Сервер ожидает подключения");
                Socket socket = server.accept();
                //System.out.println("Клиент подключился");
                logger.info("Клиент подключился");
                new ClientHandler(this, socket);
            }
        }
        catch (SQLException e){
            //System.out.println("Ошибка при работе с БД");
            logger.warning("Ошибка при работе с БД");
            e.printStackTrace();
        } catch (IOException e) {
            //System.out.println("Ошибка в работе сервера");
            logger.warning("Ошибка в работе сервера");
        } catch (Exception e){
            //System.out.println("Ошибка!" + e);
            logger.warning("Ошибка!" + e);
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isNickChangeAllowed(String nick){
        return authService.isNickBusy(nick);
    }

    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);

            logger.info("Широковещательное сообщение: " + msg);
        }
    }

    public synchronized void sendPrivateMsg(String msg, String destinationNick){
        for (ClientHandler o : clients) {
            if (o.getName().equals(destinationNick)) {
                o.sendMsg(msg);
                 logger.info("Личное сообщение для " + destinationNick + ": " + msg);
                break;
            }
        }
    }

    public synchronized void broadcastClientsList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler o : clients) {
            sb.append(o.getName() + " ");
        }
        broadcastMsg(sb.toString());
    }

    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o);
        broadcastClientsList();
    }

    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
        broadcastClientsList();
    }
}
