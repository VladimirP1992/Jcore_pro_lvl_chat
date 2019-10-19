package ru.geekbrains.server;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public interface AuthService {
    void start() throws Exception;
    void updateAccountList() throws Exception;

    String getNickByLoginPass(String login, String pass);
    boolean isNickBusy(String nick);
    boolean changeNick(String login, String newNick);
    void stop();
}

