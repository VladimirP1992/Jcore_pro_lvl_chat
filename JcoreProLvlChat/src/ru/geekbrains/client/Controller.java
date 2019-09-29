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

    public void clickConnect(ActionEvent event){
        if(loginField.getText().isEmpty() || passwordField.getText().isEmpty()){
            chatArea.appendText("Поля \"Логин\" и \"Пароль\" не должны быть пустыми!\n");
            return;
        }

        if(client == null || client.getState() == State.TERMINATED){
            try {
                client = new MyClient(loginField, passwordField, chatArea);
            } catch (IOException e) {
                e.printStackTrace();
                chatArea.appendText("Не удалось подключиться к серверу - вероятно он не запущен!\n");
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
            chatArea.appendText("Пустое сообщение не будет отправлено!\n");
        }
        else if(client != null && client.isAlive()){
            messageField.clear();
            client.sendMessage(message);
        }
        else{
            chatArea.appendText("Не удалось отправить сообщение - возможно соединение с сервером не установлено!\n");
        }
    }
}
