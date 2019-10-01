package ru.geekbrains.client;

import java.io.*;
import java.lang.Thread.State;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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

    private String currentLogin;
    private TextField loginField;
    private TextField passwordField;
    private TextArea chatArea;
    private TextField nickField;

    private String logFileName;

    public MyClient(TextField loginField, TextField passwordField,TextArea chatArea, TextField nickField) throws IOException {
        this.loginField = loginField;
        this.passwordField = passwordField;
        this.chatArea = chatArea;
        this.nickField = nickField;

        t = new Thread(this);

        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        setAuthorized(false);
        t.setDaemon(true);
        t.start();
        synchronized (chatArea) {
            chatArea.appendText("Клиент запущен на порту " + SERVER_PORT + "\n");
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String strFromServer = in.readUTF();
                if(strFromServer.startsWith("/authok")) {
                    String[] parts = strFromServer.split("\\s");
                    logFileName = "history_".concat(currentLogin).concat(".txt");

                    synchronized (nickField) {
                        nickField.setText(parts[1]);
                    }
                    setAuthorized(true);
                    synchronized (chatArea) {
                        chatArea.appendText("Вы авторизованы! Для завершения сессии отправьте команду \"/end\".\n");
                    }
                    getHistory(logFileName, chatArea);
                    break;
                }
                else if (strFromServer.startsWith("##session##end##")) {
                    synchronized (chatArea) {
                        chatArea.appendText(strFromServer.replaceFirst("##session##end## ", "") + "\n");
                    }
                    break;
                }
                synchronized (chatArea) {
                    chatArea.appendText(strFromServer + "\n");
                }
            }
            while (isAuthorized) {
                String strFromServer = in.readUTF();
                if (strFromServer.startsWith("##session##end##")) {
                    synchronized (chatArea) {
                        chatArea.appendText(strFromServer.replaceFirst("##session##end## ", ""));
                    }
                    break;
                }
                else if(strFromServer.startsWith("/changemynickok")){
                    String[] parts = strFromServer.split("\\s");
                    synchronized (nickField) {
                        nickField.setText(parts[1]);
                    }
                }
                synchronized (chatArea) {
                    chatArea.appendText(strFromServer + "\n");
                }
                logMessage(logFileName, strFromServer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }

    public boolean isAuthorized(){
        return isAuthorized;
    }

    public State getState(){
        return t.getState();
    }

    public void authorize(){
        if(isAuthorized){
            synchronized (chatArea) {
                chatArea.appendText("Вы уже авторизованы! Для завершения сессии отправьте команду \"/end\".\n");
            }
            return;
        }

        try {
            String currentPassword;
            synchronized (loginField){
                currentLogin = loginField.getText();
            }
            synchronized (passwordField){
                currentPassword = passwordField.getText();
            }

            out.writeUTF("/auth " + currentLogin + " " + currentPassword);
            synchronized (loginField) {
                loginField.clear();
            }
            synchronized (passwordField) {
                passwordField.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message){
        try {
            if(message.equalsIgnoreCase("/end")) {
                setAuthorized(false);
                nickField.clear();
            }
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

    private void getHistory(String logFileName, TextArea chatArea) {
        if(new File(logFileName).exists()) {

        }
    }

    private void logMessage(String logFileName, String message){
        try(OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(logFileName, true), StandardCharsets.UTF_8)) {
            osw.write(message.concat("\n"));
        } catch (IOException e){
            System.out.println("Ошибка записи в log-файл");
            e.printStackTrace();
        }
    }
}
