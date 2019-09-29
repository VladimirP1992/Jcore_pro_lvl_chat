package ru.geekbrains.lesson2.dbCreator;

import java.sql.*;

public class DbCreator {

    static Connection con;
    static Statement stmt;

    public static void main(String[] args) {
        try{
            //Базу данных MySQL заранее создал с помощью консольного клиента
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            System.out.println("Connection successful!");

            String url = "jdbc:mysql://localhost/chat_accounts?serverTimezone=Europe/Moscow&useSSL=false";
            String username = "TestName";
            String password ="TestPassword";
            con = DriverManager.getConnection(url, username, password);
            stmt = con.createStatement();

            //Создание БД для хранения учетных записей
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(" +
                    "id INT PRIMARY KEY AUTO_INCREMENT NOT NULL," +
                    "login TEXT NOT NULL," +
                    "password TEXT NOT NULL," +
                    "nick TEXT NOT NULL" +
                    ");");
            //Добавление нескольких пользователей
//            stmt.executeUpdate("INSERT INTO users (login, password, nick) VALUES ('login1', 'pass1', 'nick1')");
//            stmt.executeUpdate("INSERT INTO users (login, password, nick) VALUES ('login2', 'pass2', 'nick2')");
//            stmt.executeUpdate("INSERT INTO users (login, password, nick) VALUES ('login3', 'pass3', 'nick3')");
//            stmt.executeUpdate("INSERT INTO users (login, password, nick) VALUES ('login4', 'pass4', 'nick4')");
            con.close();
        }
        catch(Exception ex){
            System.out.println("Connection failed...");

            System.out.println(ex);
        }
    }
}
