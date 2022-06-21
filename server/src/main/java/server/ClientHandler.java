package server;

import com.sun.security.ntlm.Client;
import constants.Command;
import javafx.stage.Modality;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;


public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticated;
    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

           // new Thread(() -> {
            server.getExecutorService().execute(() -> {
                try {
                    socket.setSoTimeout(5000);
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                sendMsg(Command.END);
                                break;
                            }

                            if (str.startsWith("/auth")) {
                                String[] token = str.split(" ", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                String newNick = server.getAuthService().getNicknameByLoginAndPassword(token[1], token[2]);
                                login = token[1];
                                if (newNick != null) {
                                    if (!server.isLoginAuthenticated(login)) {
                                        nickname = newNick;
                                        sendMsg("/auth_ok " + nickname);
                                        authenticated = true;
                                        server.subscribe(this);
                                        server.broadcastMsg(this, "connect");
                                        socket.setSoTimeout(0);
                                        break;
                                    } else {
                                        socket.close();
                                    }
                                } else {
                                    sendMsg("Password is incorrect");
                                }
                            }

                            if (str.startsWith("/reg")) {
                                String[] token = str.split(" ");
                                if (token.length < 4) {
                                    continue;
                                }
                                if (server.getAuthService().registration(token[1], token[2], token[3])) {
                                    sendMsg("/reg_ok");
                                } else {
                                    sendMsg("/reg_no");
                                }
                            }
                        }
                    }
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                sendMsg(Command.END);
                                break;
                            }
                            if (str.startsWith("/w ")) {
                                String[] token = str.split(" ", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                server.privateMsg(this, token[1], token[2]);
                            }
                            if (str.startsWith("/chnick")) {
                                String[] token = str.split("\\s+", 2);
                                if (token.length < 2) {
                                    continue;
                                }
                                if (token[1].contains(" ")) {
                                    sendMsg("? ?????? ?? ?????? ???? ???????!");
                                    continue;
                                }
                                if (server.getAuthService().changeNick(this.nickname, token[1])) {
                                    sendMsg("/yournickis " + token[1]);
                                    sendMsg("??? ??? ??????? ??: " + token[1]);
                                    this.nickname = token[1];
                                    server.broadcastClientList();
                                } else {
                                    sendMsg("?? ??????? ???????? ??? " + token[1] + " ??? ?????");
                                }
                            }

                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }

                } catch (SocketTimeoutException e) {
                    sendMsg(Command.END);

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    server.broadcastMsg(this, "disconnect");
                    System.out.println(" Client disconnected");

                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //}).start();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
