package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

class TCPConnection {

    private ServerSocket serverSocket;
    public static LinkedList<TCPLogic> serverList = new LinkedList<>();
    public static Story globalStory;
    public static int clientNumber = 1;

    public TCPConnection(ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
    }

    public void start(){
        globalStory = new Story();
        System.out.println("Server Started");

        try {
            while(true) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    assert socket != null;
                    serverList.add(new TCPLogic(socket,clientNumber));
                    clientNumber++;
                } catch (IOException e) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } finally {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}