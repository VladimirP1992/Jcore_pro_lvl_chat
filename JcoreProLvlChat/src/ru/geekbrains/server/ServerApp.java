package ru.geekbrains.server;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class ServerApp {

    private static final Logger logger = Logger.getLogger(ServerApp.class.getName());

    public static void main(String[] args) {
        try {
            logger.addHandler(new FileHandler("log.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.getHandlers()[0].setFormatter(new SimpleFormatter());
        logger.info("Сервер запущен!");
        new MyServer(logger);
    }
}
