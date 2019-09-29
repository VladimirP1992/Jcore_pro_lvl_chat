package ru.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final long TIME_OUT = 10000;
    private boolean isAuthorized;

    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";

            isAuthorized = false;
            Thread timer = new Thread(()-> {
                try {
                    Thread.sleep(TIME_OUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if (!getAuthorized()){
                        String message = "Время отведенное на аутентификацию ("+ (TIME_OUT / 1000) +" сек.) - истекло!";
                        out.writeUTF("##session##end## " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            timer.start();

            new Thread(() -> {
                try {
                    authentication();
                    setAuthorized(true);
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    private synchronized boolean getAuthorized(){
        return isAuthorized;
    }
    private synchronized void setAuthorized(boolean value){
        isAuthorized = value;
    }

    public void authentication() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                String[] parts = str.split("\\s");
                String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMsg("/authok " + nick);
                        name = nick;
                        myServer.broadcastMsg(name + " зашел в чат");
                        myServer.subscribe(this);
                        return;
                    } else {
                        sendMsg("Учетная запись уже используется");
                    }
                } else {
                    sendMsg("Неверные логин/пароль");
                }
            }
        }
    }

    public void readMessages() throws IOException {
        while (true) {
            String strFromClient = in.readUTF();
            System.out.println("от " + name + ": " + strFromClient);
            if (strFromClient.equalsIgnoreCase("/end")) {
                return;
            }
            if(strFromClient.startsWith("/w")){
                String[] parts = strFromClient.split("\\s");
                String destinationNick = parts[1];
                if(myServer.isNickBusy(destinationNick)){
                    String msgContent = strFromClient.replaceFirst(parts[0], "").replaceFirst(parts[1], "");
                    sendMsg("Вы написали " + destinationNick + ": " + msgContent);
                    myServer.sendPrivateMsg("От " + name + ": " + msgContent, destinationNick);
                }
                else{
                    sendMsg("В чате нет участника с ником " + destinationNick + "!");
                }
            }
            else{
                myServer.broadcastMsg(name + ": " + strFromClient);
            }

        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " вышел из чата");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
