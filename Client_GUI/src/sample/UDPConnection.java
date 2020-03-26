package sample;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPConnection{
    private DatagramSocket udpSocket;
    final int UDP_SERVER_PORT = 9999;
    final String UDP_DEST_ADDR = "255.255.255.255";

    public int UDPConnectManager() throws IOException {
        try
        {
            udpSocket = new DatagramSocket();
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
        }
        InetAddress iaLocal = InetAddress.getLocalHost();

        int udpClientPort = udpSocket.getLocalPort();

        sendStringService(iaLocal.getHostAddress()+" "+udpClientPort);

        String receivedString = recvStringService(udpSocket);
        udpSocket.close();

        System.out.println("\n--->" + receivedString);
        String [] txt_out = receivedString.split(" ");

        int serverTCPPort = Integer.parseInt(txt_out[1].trim());
        return serverTCPPort;
    }
    void sendStringService(String string) throws IOException
    {
        byte[] buf = string.getBytes();

        InetAddress brouadcastAddr = InetAddress.getByName(UDP_DEST_ADDR);

        DatagramPacket datagramPacket;
        datagramPacket = new DatagramPacket(buf, buf.length, brouadcastAddr, UDP_SERVER_PORT);
        udpSocket.send(datagramPacket);
    }
    String recvStringService(DatagramSocket datagramSocket) throws IOException
    {
        DatagramPacket datagramPacket;
        byte[] buf = new byte[512];

        datagramPacket = new DatagramPacket(buf, 512);
        datagramSocket.receive(datagramPacket);

        return new String(buf);
    }
}