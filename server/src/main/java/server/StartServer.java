package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

public class StartServer {
    public static void main(String[] args) {

        try {
            LogManager manager = LogManager.getLogManager();
            manager.readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Server();
    }
}
