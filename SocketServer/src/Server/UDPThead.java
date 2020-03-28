package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

class UDPThead extends Thread
{
    private DatagramSocket datagramSocket;
    private int tcpPort;

    final int BUF_SIZE = 512;

    public UDPThead(DatagramSocket datagramSocket, int tcpPort) {
        this.datagramSocket = datagramSocket;
        this.tcpPort = tcpPort;
    }


    public void run()
    {
        try
        {
            while(true)
            {
                String recvString = getStringService(datagramSocket);

                String [] words = recvString.split(" ");
                System.out.println("\n--->" + recvString);
                InetAddress iaLocal = InetAddress.getLocalHost();

                sendStringService(iaLocal.getHostAddress() + " " + tcpPort, words[0], words[1]);
            }
        }
        catch(SocketException se)
        {
            System.out.println("!Server socket could not be opened\n");
            datagramSocket.close();
        }
        catch(Exception ex)
        {
            datagramSocket.close();
        }
    }


    String getStringService(DatagramSocket datagramSocket) throws IOException
    {
        DatagramPacket datagramPacket;
        byte[] recvString = new byte[BUF_SIZE];

        datagramPacket = new DatagramPacket(recvString, BUF_SIZE);
        datagramSocket.receive(datagramPacket);
        return new String(recvString);
    }

    void sendStringService(String string,String destination, String port)
            throws IOException
    {
        byte[] sndString = string.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(sndString, sndString.length, InetAddress.getByName(destination),
                                                                                Integer.parseInt(port.trim()));

        datagramSocket.send(datagramPacket);
    }
}
