package ru.geekbrains.server;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {
    static Connection con = null;
    static Statement stmt;

    private class Entry {
        private String login;
        private String pass;
        private String nick;

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }

//        @Override
//        public String toString() {
//            return ("login=" + login + "; pass=" + pass + "; nick=" + nick);
//        }
    }

    private List<Entry> entries;

    @Override
    public void start() throws Exception {

        //Базу данных MySQL заранее создал с помощью консольного клиента
        Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
        System.out.println("Connection successful!");

        String url = "jdbc:mysql://localhost/chat_accounts?serverTimezone=Europe/Moscow&useSSL=false&allowPublicKeyRetrieval=true";
        String username = "TestName";
        String password = "TestPassword";
        con = DriverManager.getConnection(url, username, password);
        stmt = con.createStatement();

        updateAccountList();
//        for (Entry e : entries){
//            System.out.println(e);
//        }

        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public void updateAccountList() throws Exception {
        entries = new ArrayList<>();
        ResultSet resultSet = stmt.executeQuery("SELECT * FROM users");
        if(!resultSet.first()){
            throw new Exception("Получен пустой список аккаунтов из БД");
        }

        Entry entry;
        do {
            entry = new Entry( resultSet.getString(2), resultSet.getString(3), resultSet.getString(4));
            entries.add(entry);
        } while (resultSet.next());
    }

    @Override
    public void stop() {
        if(con != null){
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии соединения с БД!");
            }finally {
                con = null;
            }
        }

        System.out.println("Сервис аутентификации остановлен");
    }


    public BaseAuthService() {

    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry o : entries) {
            if (o.login.equals(login) && o.pass.equals(pass)) return o.nick;
        }
        return null;
    }

    @Override
    public boolean isNickBusy(String nick) {
        for (Entry o : entries) {
            if (o.nick.equals(nick))
                return true;
        }
        return false;
    }

    @Override
    public boolean changeNick(String login, String newNick) {
        if(!isNickBusy(newNick)){
            try {
//                System.out.println("login = " + login + " newNick="+ newNick);
//                System.out.println("UPDATE users SET nick=" + newNick + " WHERE login=" + login + ";");
                stmt.executeUpdate("UPDATE users SET nick = '" + newNick + "' WHERE login = '" + login + "';");
                updateAccountList();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }
}
