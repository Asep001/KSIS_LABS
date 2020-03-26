package sample;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class Controller {

    public TextField nickName;
    public TextField message;
    public Button connect;
    public TextArea chat;
    public ComboBox<String> selectPartner;
    public Button sendBtn;
    public Button disbtn;

    private Client client;

    final int GLOBAL_MESSAGE = 0;

    final int CONNECT_MESSAGE_TYPE = 0;
    final int DISCONNECT_MESSAGE_TYPE = 1;
    final int IDNUMBER_MESSAGE_TYPE = 2;
    final int REGULAR_MESSAGE_TYPE = 3;
    final int REQUEST_STORY_TYPE = 4;
    final int REQUEST_FOR_USERS = 5;

    public void Connect(){
        client = new Client();
        try {
            client.start(nickName.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        selectPartner.getItems().add("Global");
        selectPartner.setValue("Global");

        connect.setDisable(true);
        selectPartner.setDisable(false);
        disbtn.setDisable(false);
        sendBtn.setDisable(false);
        comboAction();
        new ReadMsg().start();
    }

    public void SendMessage(){
        client.clientLogic.writeMsgService(REGULAR_MESSAGE_TYPE,
                selectPartner.getItems().indexOf(selectPartner.getValue()),client.clientLogic.idNumder,message.getText());
        message.clear();
    }


    public void SendStopMessage(){
        client.clientLogic.writeStopMessageHandler();
        message.clear();
        connect.setDisable(false);
        selectPartner.setDisable(true);
        disbtn.setDisable(true);
        sendBtn.setDisable(true);
        selectPartner.getItems().clear();
        chat.clear();
    }

    public void comboAction(){
        selectPartner.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                chat.clear();
                boolean isChange = true;
                int chatId = selectPartner.getItems().indexOf(t1);
                isChange = renameComboBox(chatId,t1,selectPartner);

                if (isChange)
                    client.clientLogic.getStoryService(chatId);


            }
        });
        }

    public boolean renameComboBox(int chatId, String tmp, ComboBox<String> selectPartner){
        if (tmp != null) {
            String []tmpWords = tmp.split(" ");
            if (tmpWords[tmpWords.length - 1].equals("+")) {
                selectPartner.getItems().add(chatId,tmp.substring(0,tmp.length()-2));
                selectPartner.getItems().remove(chatId+1);
                return false;
            }
        }
        return true;
    }

    private class ReadMsg extends Thread {
        @Override
        public void run() {
            String recvMessage;
            try {
                client.clientLogic.in.readLine();
                while (true) {
                    recvMessage = client.clientLogic.in.readLine();
                    String[] words = recvMessage.split(" ",4);
                    String intdexOfChat = String.valueOf(selectPartner.getItems().indexOf(selectPartner.getValue()));
                    if (!words[0].equals("")) {
                        switch (Integer.parseInt(words[0].trim())) {
                            case CONNECT_MESSAGE_TYPE:
                                selectPartner.getItems().add(words[2]);
                                break;
                            case REGULAR_MESSAGE_TYPE:
                                if (words[1].equals(intdexOfChat) ||
                                        (words[2].equals(intdexOfChat)&&!words[1].equals("0")))
                                    chat.appendText(words[3] + '\n');
                                else {
                                    int chatId;
                                    if (Integer.parseInt(words[1].trim())==GLOBAL_MESSAGE)
                                        chatId = 0;
                                    else
                                        chatId = Integer.parseInt(words[2].trim());
                                    String tmp = selectPartner.getItems().get(chatId);
                                    System.out.println(tmp + " "+chatId);
                                    String []tmpWords = tmp.split(" ");
                                    if (!tmpWords[tmpWords.length - 1].equals("+")) {
                                        try {
                                            selectPartner.getItems().remove(chatId);
                                            selectPartner.getItems().add(chatId,tmp + " +");
                                        }
                                        catch (Exception ignored){ }
                                    }
                                }
                                break;
                            case DISCONNECT_MESSAGE_TYPE:
                                selectPartner.getItems().remove(words[2]);
                                break;
                            case IDNUMBER_MESSAGE_TYPE:
                                System.out.println(words[2] + '\n');
                                client.clientLogic.idNumder = Integer.parseInt(words[2].trim());
                                break;
                            case REQUEST_FOR_USERS:
                                selectPartner.getItems().add(words[1]);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                client.clientLogic.downServiceHandler();
            }
        }
    }
}
