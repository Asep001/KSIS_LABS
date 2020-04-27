package sample;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

class ClientLogic {

    private Socket socket;
    public BufferedReader in;
    private BufferedWriter out;
    private InetAddress userAddr;
    private int port;
    private String nickName;
    public Long idNumder;

    final Long GLOBAL_MESSAGE = 0l;


    final Long DISCONNECT_MESSAGE_TYPE = 1L;

    final Long REQUEST_STORY_TYPE = 4L;
    final Long REQUEST_FOR_USERS = 5l;


    public ClientLogic(InetAddress addr, int port, String nickname) {
        this.userAddr = addr;
        this.port = port;
        try {
            this.socket = new Socket(addr, port);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            assert socket != null;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.nickName = nickname;

            sendNickService();
            getUsersService();
        } catch (IOException e) {
            ClientLogic.this.downServiceHandler();
        }
    }

    private void sendNickService() {
        try {
            out.write(this.nickName + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }

    public void downServiceHandler() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {}
    }

    public void writeMsgService(Long typeOFMessage ,Long destination ,Long source,String userWord) {
        try {
            out.write(typeOFMessage + " " + destination + " " + source + " (" +
                    getDateHelper() + ") [" + userAddr + "] " + nickName + ": " + userWord + "\n");
            out.flush();
            } catch (IOException e) {
            ClientLogic.this.downServiceHandler();
        }
    }

    public void writeMsgWithFiles(Long typeOFMessage , Long destination , Long source, String userWord, ArrayList<String> files) {
        try {
            String fileList = "";
            for (String file: files) {
                fileList += file.split("&")[1] + " ";
            }

            System.out.println(fileList);
            out.write(typeOFMessage + " " + destination + " " + source +
                   " "  + files.size() + " " + fileList + " "
                   + " (" + getDateHelper() + ") [" + userAddr + "] " + nickName + ": " + userWord + "\n");
            out.flush();
        } catch (IOException e) {
            ClientLogic.this.downServiceHandler();
        }
    }

    private String getDateHelper(){
        Date time = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String date = simpleDateFormat.format(time);
        return date;
    }

    public void getStoryService(Long idNumberPartner){
        writeMsgService(REQUEST_STORY_TYPE,idNumberPartner,idNumder,"STORY");
    }

    private void getUsersService(){
        writeMsgService(REQUEST_FOR_USERS,GLOBAL_MESSAGE,idNumder,"USERS");
    }

    public void  writeStopMessageHandler(){
        writeMsgService(DISCONNECT_MESSAGE_TYPE, GLOBAL_MESSAGE,idNumder,"stop\n");
        ClientLogic.this.downServiceHandler();
    }
}
