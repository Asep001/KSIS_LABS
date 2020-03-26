package Server;

import java.io.*;
import java.net.Socket;

class TCPLogic extends Thread {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private int idNumber;
    private String nickName;

    final int GLOBAL_SOURCE_MESSAGE = 0;
    final int GLOBAL_MESSAGE = 0;

    final int CONNECT_MESSAGE_TYPE = 0;
    final int DISCONNECT_MESSAGE_TYPE = 1;
    final int IDNUMBER_MESSAGE_TYPE = 2;
    final int REQUEST_STORY_TYPE = 4;
    final int REQUEST_FOR_USERS = 5;



    public TCPLogic(Socket socket, int number) throws IOException {
        this.socket = socket;
        this.idNumber = number;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        start();
    }

    @Override
    public void run() {
        String clientMessage;
        boolean isAlive = true;
        try {
            userConnectHendler(in.readLine());
            try {
                while (isAlive) {
                    clientMessage = in.readLine();
                    System.out.println(clientMessage);
                    String[] words = clientMessage.split(" ", 4);
                    if (!words[0].equals("")) {
                        switch (Integer.parseInt(words[0].trim())) {
                            case DISCONNECT_MESSAGE_TYPE:
                                removeUserHendler();
                                TCPConnection.globalStory.delStoryHelper(idNumber);
                                this.closeConnectionService();
                                isAlive = false;
                                break;
                            case REQUEST_STORY_TYPE:
                                TCPConnection.globalStory.printStoryHelper(out,words[2],words[1]);
                                break;
                            case REQUEST_FOR_USERS:
                                getUsersService();
                                break;
                            default:
                                for (TCPLogic vr : TCPConnection.serverList) {
                                    if (words[1].equals(Integer.toString(GLOBAL_MESSAGE))) {
                                        vr.sendMessageService(clientMessage);
                                    } else {
                                        if (vr.idNumber == (Integer.parseInt(words[1].trim())) || vr == this) {
                                            vr.sendMessageService(clientMessage);
                                        }
                                    }
                                }
                                TCPConnection.globalStory.addStoryMessageHelper(new StoredMessage(words[0], words[1], words[2], words[3]));
                        }
                    }
                }
            } catch (NullPointerException | IOException ignored) {}
        } catch (IOException e) {
            this.closeConnectionService();
        }
    }


    private void sendMessageService(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }

    private void closeConnectionService() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (TCPLogic item : TCPConnection.serverList) {
                    if(item == this)
                    TCPConnection.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {}
    }

    private void removeUserHendler(){
        System.out.println(this.nickName+" leave chat.");
        int counter = 1;
        for (TCPLogic item : TCPConnection.serverList) {
            item.idNumber=counter;
            item.sendMessageService(DISCONNECT_MESSAGE_TYPE+" "+GLOBAL_SOURCE_MESSAGE+" "+this.nickName+" leave chat.\n");
            item.sendMessageService(IDNUMBER_MESSAGE_TYPE+" "+this.nickName+" "+item.idNumber+" leave chat.\n");
            counter++;
            if (item == this) {
                counter--;
                TCPConnection.clientNumber--;
            }
        }
    }

    private void userConnectHendler(String clientMessage){
        this.nickName = clientMessage;
        String connect_message = CONNECT_MESSAGE_TYPE+" "+this.idNumber + " "+ this.nickName+ " connected to the server!\n";
        for (TCPLogic users : TCPConnection.serverList) {
            users.sendMessageService(connect_message);

            if (users == this) {
                users.sendMessageService(IDNUMBER_MESSAGE_TYPE + " " +this.nickName  + " "+ this.idNumber+ " connected to the server!\n");
            }
        }
        System.out.println(this.nickName+ " connected to the server!\n");
    }


    private void getUsersService(){
        for (TCPLogic users : TCPConnection.serverList) {
            if (users == this) {
                for (TCPLogic item : TCPConnection.serverList) {
                    users.sendMessageService(REQUEST_FOR_USERS + " " +item.nickName  + " "+item.idNumber+ " is connected\n");
                }
            }
        }
    }
}