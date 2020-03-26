package sample;

import java.net.*;
import java.io.*;

public class Client {

    public ClientLogic clientLogic;

    public void start(String nickName) throws IOException {
        InetAddress ipAddr = InetAddress.getLocalHost();
        UDPConnection udpConnection = new UDPConnection();

        int serverPort = udpConnection.UDPConnectManager();

        clientLogic = new ClientLogic(ipAddr, serverPort, nickName);
    }
}