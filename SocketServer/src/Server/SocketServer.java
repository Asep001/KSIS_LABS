package Server;

import java.net.*;

public class SocketServer
{
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        DatagramSocket datagramSocket = null;

        final int UDP_PORT = 9999;
        final int TCP_PORT = 9998;

        //UDP
        InetAddress ipAddr = null;
        try
        {
            ipAddr = InetAddress.getLocalHost();
            datagramSocket = new DatagramSocket(UDP_PORT);
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
            System.exit(0);
        }
        System.out.println("UDP Port: " + UDP_PORT+ "\nTCP Port: "+TCP_PORT+"\n"+ipAddr);


        UDPThead udpThead = new UDPThead(datagramSocket, TCP_PORT);
        udpThead.start();

        //TCP
        try
        {
            serverSocket = new ServerSocket(TCP_PORT);
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
            System.exit(0);
        }
        TCPConnection tcpConnection = new TCPConnection(serverSocket);
        tcpConnection.start();
        try
        {
            udpThead.join();
        }
        catch(InterruptedException ex)
        {
            System.out.println(ex.toString());
        }
        try
        {
            serverSocket.close();
            datagramSocket.close();
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
        }
        System.exit(0);
    }
}
