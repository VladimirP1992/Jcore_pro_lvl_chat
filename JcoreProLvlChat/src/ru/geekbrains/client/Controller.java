package ru.geekbrains.client;

import java.io.IOException;
import java.lang.Thread.State;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class Controller {
    MyClient client = null;

    public TextField loginField;
    public TextField passwordField;
    public Button connectButton;
    public TextField messageField;
    public Button sendButton;
    public TextArea chatArea;


    public Button changeNickButton;
    public TextField myNickField;
    public TextField newNickField;

    public void clickConnect(ActionEvent event){
        if(loginField.getText().isEmpty() || passwordField.getText().isEmpty()){
            synchronized (chatArea){
                chatArea.appendText("Поля \"Логин\" и \"Пароль\" не должны быть пустыми!\n");
            }
            return;
        }

        if(client == null || client.getState() == State.TERMINATED){
            try {
                client = new MyClient(loginField, passwordField, chatArea, myNickField);
            } catch (IOException e) {
                e.printStackTrace();
                synchronized (chatArea) {
                    chatArea.appendText("Не удалось подключиться к серверу - вероятно он не запущен!\n");
                }
                client = null;
            }
        }
        if(client != null) {
            client.authorize();
        }
    }

    public void clickSend(ActionEvent event) {
        String message = messageField.getText().trim();
        if (message.isEmpty()){
            synchronized (chatArea) {
                chatArea.appendText("Пустое сообщение не будет отправлено!\n");
            }
        }
        else if(client != null && client.isAlive()){
            synchronized (messageField) {
                messageField.clear();
            }
            client.sendMessage(message);
        }
        else{
            synchronized (chatArea) {
                chatArea.appendText("Не удалось отправить сообщение - возможно соединение с сервером не установлено!\n");
            }
        }
    }

    public void clickChangeNick(ActionEvent event) {
        String newNickStr = newNickField.getText().trim();
        if(newNickStr.isEmpty()){
            synchronized (chatArea) {
                chatArea.appendText("Новый ник не должен быть пустым!\n");
            }
        }
        else if(client != null && client.isAlive()){
            if (!client.isAuthorized()){
                synchronized (chatArea) {
                    chatArea.appendText("Вы еще не авторизовались - смена ника невозможна!\n");
                }
                return;
            }
            synchronized (newNickField) {
                newNickField.clear();
            }
            client.sendMessage("/changemynick " + newNickStr);
        }
        else{
            synchronized (chatArea) {
                chatArea.appendText("Не сменить ник - возможно соединение с сервером не установлено!\n");
            }
        }
    }
}
