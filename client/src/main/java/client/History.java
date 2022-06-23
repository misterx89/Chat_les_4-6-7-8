package client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class History {
    private static PrintWriter out;

    private static String getHistoryFileNameByLogin(String login) {
        return "history/history_" + login + ".txt";
    }

    public static void start(String login) {
        try {
            out = new PrintWriter(new FileOutputStream(getHistoryFileNameByLogin(login), true), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (out != null) {
            out.close();
        }
    }

    public static void writeln(String msg) {
        out.println(msg);
    }

    public static String getLast100LinesOfHistory(String login){
        if (!Files.exists(Paths.get(getHistoryFileNameByLogin(login)))){
            return " ";
        }
        StringBuilder sb = new StringBuilder();
        try {
            List<String> histotyLines = Files.readAllLines(Paths.get(getHistoryFileNameByLogin(login)));
            int startPosition = 0;
            if (histotyLines.size() > 100) {
                startPosition = histotyLines.size() - 100;
            }
            for (int i = startPosition; i < histotyLines.size(); i++) {
                sb.append(histotyLines.get(i)).append(System.lineSeparator());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return sb.toString();
    }
}
