package ru.geekbrains.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Thread.State;
import java.net.Socket;
import java.util.ArrayList;

import javafx.scene.control.*;

public class MyClient implements Runnable {
    private Thread t;

    final String SERVER_ADDR = "localhost";
    final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isAuthorized;

    private TextField loginField;
    private TextField passwordField;
    private TextArea chatArea;

    public MyClient(TextField loginField, TextField passwordField,TextArea chatArea) throws IOException {
        this.loginField = loginField;
        this.passwordField = passwordField;
        this.chatArea = chatArea;
        t = new Thread(this);

        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        setAuthorized(false);
        t.setDaemon(true);
        t.start();
        chatArea.appendText("Клиент запущен на порту " + SERVER_PORT + "\n");


    }

    @Override
    public void run() {
        try {
            while (true) {
                String strFromServer = in.readUTF();
                if(strFromServer.startsWith("/authok")) {
                    setAuthorized(true);
                    chatArea.appendText("Вы авторизованы! Для завершения сессии отправьте команду \"/end\".\n");
                    break;
                } else if (strFromServer.startsWith("##session##end##")) {
                    chatArea.appendText(strFromServer.replaceFirst("##session##end## ", "") + "\n");
                    break;
                }
                chatArea.appendText(strFromServer + "\n");
            }
            while (isAuthorized) {
                String strFromServer = in.readUTF();
                if (strFromServer.startsWith("##session##end##")) {
                    chatArea.appendText(strFromServer.replaceFirst("##session##end## ", ""));
                    break;
                }
                chatArea.appendText(strFromServer);
                chatArea.appendText("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }

    public State getState(){
        return t.getState();
    }

    public void authorize(){
        if(isAuthorized){
            chatArea.appendText("Вы уже авторизованы! Для завершения сессии отправьте команду \"/end\".\n");
            return;
        }

        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message){
        try {
            if(message.equalsIgnoreCase("/end"))
                setAuthorized(false);
            out.writeUTF(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void start(){
//        if(isAuthorized)
//            t.start();
//    }

    public boolean isAlive(){
        return t.isAlive();
    }
}
