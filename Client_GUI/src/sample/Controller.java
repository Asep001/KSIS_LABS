package sample;


import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Controller {

    public TextField nickName;
    public TextField message;
    public Button connect;
    public TextArea chat;
    public ComboBox<ComboBoxItem> selectPartner;
    public Button sendBtn;
    public Button disbtn;
    public ScrollPane mesObj;
    public VBox layout;
    public VBox files;
    public Button fileBtn;

    private Stage primaryStage;

    private Client client;
    private boolean isActive = true;
    private Long activeItem;

    private HttpStorage httpStorage;

    final int GLOBAL_MESSAGE = 0;

    final int CONNECT_MESSAGE_TYPE = 0;
    final int DISCONNECT_MESSAGE_TYPE = 1;
    final int IDNUMBER_MESSAGE_TYPE = 2;
    final int REGULAR_MESSAGE_TYPE = 3;
    final int REQUEST_STORY_TYPE = 4;
    final int REQUEST_FOR_USERS = 5;

    final int REGULAR_MESSAGE_WITH_FILE_TYPE = 6;

    final FileChooser fileChooser = new FileChooser();
    final DirectoryChooser directoryChooser = new DirectoryChooser();

    final int MAX_FILE_SIZE = 20971520;
    final int MAX_TOTAL_FILE_SIZE = 52428800;

    ArrayList<String > listofSharedFiles = new ArrayList<>();

    ArrayList<String> uploadFiles = new ArrayList<>();

    static int countOfFiles = 0;
    static long totalFileSize = 0;

    public void Connect(){
        client = new Client();
        try {
            client.start(nickName.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ComboBoxItem global =  new ComboBoxItem("Global", 0L);
        selectPartner.getItems().add( global);
        selectPartner.setValue(global);

        connect.setDisable(true);
        selectPartner.setDisable(false);
        disbtn.setDisable(false);
        sendBtn.setDisable(false);
        fileBtn.setDisable(false);

        if (isActive){
            activeItem = global.id;
            client.clientLogic.getStoryService(activeItem);
            comboAction();
        }

        httpStorage = new HttpStorage();

        new ReadMsg().start();
    }

    public void SetStage(Stage primaryStage){
        this.primaryStage = primaryStage;
    }

    public void SendMessage(){
        Long chatId = activeItem;
        if (uploadFiles.isEmpty())
            client.clientLogic.writeMsgService((long) REGULAR_MESSAGE_TYPE,chatId,
                    client.clientLogic.idNumder,message.getText());
        else
            client.clientLogic.writeMsgWithFiles((long) REGULAR_MESSAGE_WITH_FILE_TYPE,chatId,
                    client.clientLogic.idNumder,message.getText(),uploadFiles);
        message.clear();
        totalFileSize = 0;

        uploadFiles.clear();

        countOfFiles = 0;
        ObservableList<Node> children = files.getChildren();
        children.clear();
    }


    public void SendStopMessage(){
        client.clientLogic.writeStopMessageHandler();
        message.clear();
        connect.setDisable(false);
        selectPartner.setDisable(true);
        disbtn.setDisable(true);
        sendBtn.setDisable(true);
        fileBtn.setDisable(true);
        selectPartner.getItems().clear();
        ObservableList<Node> children = layout.getChildren();
        children.clear();
    }

    public void comboAction(){
        selectPartner.valueProperty().addListener(new ChangeListener<ComboBoxItem>() {
            @Override
            public void changed(ObservableValue<? extends ComboBoxItem> observableValue, ComboBoxItem comboBoxItem, ComboBoxItem t1) {

                ObservableList<Node> children = layout.getChildren();
                children.clear();

                isActive = false;
                boolean isChange = true;
                long chatId = selectPartner.getItems().indexOf(t1);
                isChange = renameComboBox(Math.toIntExact(chatId),t1.name,selectPartner);
                activeItem = chatId;
                if (isChange)
                    client.clientLogic.getStoryService(chatId);
                listofSharedFiles.clear();
            }
        });
    }

    public boolean renameComboBox(int chatId, String chatName,ComboBox<ComboBoxItem> selectPartner){
        if (chatName != null) {
            String []words = chatName.split(" ");
            if (words[words.length - 1].equals("+")) {
                selectPartner.getItems().add(chatId,new ComboBoxItem(chatName.substring(0,chatName.length()-2),(long)chatId));
                selectPartner.getItems().remove(chatId+1);
                return false;
            }
        }
        return true;
    }

    private void notifyOfNewMessage(String[] words){
        int chatId;
        if (Integer.parseInt(words[1].trim())==GLOBAL_MESSAGE)
            chatId = 0;
        else
            chatId = Integer.parseInt(words[2].trim());

        String chatName = selectPartner.getItems().get(chatId).name;

        String []tmpWords = chatName.split(" ");
        if (!tmpWords[tmpWords.length - 1].equals("+")) {
            try {
                selectPartner.getItems().remove(chatId);
                selectPartner.getItems().add(chatId,new ComboBoxItem(chatName + " +",(long)chatId));
            }
            catch (Exception ignored){ }
        }
    }

    private void updateMessageDialog(String message, String[] files){
        ObservableList<Node> children = layout.getChildren();
        children.add(createMessageItem(message, files));
    }

    private Node createMessageItem(String message, String[] files) {
        try {
            Node itemNode = FXMLLoader.load(Main.class.getResource("object_item.fxml"));
            ((Label) itemNode.lookup("#name")).textProperty().set(message);

            if (files !=null) {
                ScrollPane scrollPane = ((ScrollPane) itemNode.lookup("#csroll"));
                scrollPane.setPrefViewportHeight(360);
                VBox forFiles = (VBox) scrollPane.getContent().lookup("#forFiles");

                for (int i=1; i<=files.length-1; i++){
                    String fileId = files[i-1];
                    String fileName;
                    if (fileId!=null) {
                        fileName = httpStorage.sendHeadRequest(Long.valueOf(fileId))[0];
                        updateFileDialog(fileName, fileId, i, forFiles, false);

                        listofSharedFiles.add(fileName + "&" + fileId);
                    }
                }
            }
            return itemNode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateFileDialog(String fileName, String fileId, int number, VBox files, boolean isDelete){
        ObservableList<Node> children = files.getChildren();
        children.add(createFileItem(fileName, fileId, number, isDelete));
    }

    private Node createFileItem(String fileName, String fileId, int number, boolean isDelete) {
        try {
            Node itemNode = FXMLLoader.load(Main.class.getResource("fileItem.fxml"));
            ((Label) itemNode.lookup("#name")).textProperty().set(fileName);
            ((Label) itemNode.lookup("#num")).textProperty().set(String.valueOf(number));

            if (isDelete) {
                itemNode.setOnMouseClicked(mouseEvent -> {
                    try {
                        deleteFileFromMessage(Integer.parseInt(((Label) itemNode.lookup("#num")).textProperty().get()));
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                itemNode.setOnMouseClicked(mouseEvent -> {
                    String name = ((Label) itemNode.lookup("#name")).textProperty().get();
                    if (!name.equals("Файл отсутствует")) {
                        createAddMenu(name, fileId, ((Label) itemNode.lookup("#name")));
                    }

                });
            }
            return itemNode;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createAddMenu(String fileName, String fileId, Label label){
        Button downloadBtn = new Button();
        downloadBtn.setText("Download");
        downloadBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    downloadBtnClick(fileName);
                    label.textProperty().set(httpStorage.sendHeadRequest(Long.valueOf(fileId))[0]);
                    Stage stage = (Stage) downloadBtn.getScene().getWindow();
                    stage.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Button infoBtn = new Button();
        infoBtn.setText("info");
        infoBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    infoBtnClick(fileName);
                    label.textProperty().set(httpStorage.sendHeadRequest(Long.valueOf(fileId))[0]);
                    Stage stage = (Stage) infoBtn.getScene().getWindow();
                    stage.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Button deleteBtn = new Button();
        deleteBtn.setText("Delete");
        deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    deleteBtnClick(fileName);
                    label.textProperty().set(httpStorage.sendHeadRequest(Long.valueOf(fileId))[0]);
                    Stage stage = (Stage) deleteBtn.getScene().getWindow();
                    stage.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        StackPane secondaryLayout = new StackPane();
        secondaryLayout.setPadding(new Insets(0, 20, 10, 20));
        secondaryLayout.getChildren().add(downloadBtn);
        StackPane.setAlignment(downloadBtn, Pos.CENTER);
        secondaryLayout.getChildren().add(infoBtn);
        StackPane.setAlignment(infoBtn, Pos.CENTER_LEFT);
        secondaryLayout.getChildren().add(deleteBtn);
        StackPane.setAlignment(deleteBtn, Pos.CENTER_RIGHT);

        Scene secondScene = new Scene(secondaryLayout, 230, 100);

        Stage newWindow = new Stage();
        newWindow.setTitle(fileName);
        newWindow.setScene(secondScene);

        newWindow.initModality(Modality.WINDOW_MODAL);

        newWindow.initOwner(primaryStage);

        newWindow.setX(primaryStage.getX() + 200);
        newWindow.setY(primaryStage.getY() + 100);

        newWindow.show();
    }

    private void downloadBtnClick(String fileName){
        boolean isActiv =true;
        for (String item: listofSharedFiles ){
            if (item.split("&")[0].equals(fileName) && isActiv){
                try {
                    File dir;
                    String filePath;
                    isActiv = false;
                    if (!item.split("&")[0].equals("Файл отсутствует")) {
                        byte[] buf = httpStorage.sendGetRequest(item.split("&")[1]);
                        if (buf != null) {
                            dir = directoryChooser.showDialog(primaryStage);
                            if (dir != null) {
                                filePath = dir.getAbsolutePath() + File.separator + item.split("&")[0];
                                File file = new File(filePath);
                                if (file.createNewFile()){
                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                    fileOutputStream.write(buf);
                                    fileOutputStream.close();
                                }
                            }
                        } else {
                            createWindowWithLabel("Файл отсутствует на сервере");
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void infoBtnClick(String fileName){
        boolean isActiv =true;
        for (String item: listofSharedFiles ){
            if (item.split("&")[0].equals(fileName) && isActiv && !item.split("&")[0].equals("Файл отсутствует")){
                try {
                    String[] fileInfo = httpStorage.sendHeadRequest(Long.valueOf(item.split("&")[1]));

                    createWindowWithLabel("Name:"+fileInfo[0]+" Size:"+fileInfo[1]);

                    isActiv = false;
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createWindowWithLabel(String label){
        Label secondLabel = new Label(label);
        StackPane secondaryLayout = new StackPane();
        secondaryLayout.getChildren().add(secondLabel);
        Scene secondScene = new Scene(secondaryLayout, 230, 100);
        Stage newWindow = new Stage();
        newWindow.setTitle("INFO");
        newWindow.setScene(secondScene);
        newWindow.initModality(Modality.WINDOW_MODAL);
        newWindow.initOwner(primaryStage);
        newWindow.setX(primaryStage.getX() + 200);
        newWindow.setY(primaryStage.getY() + 100);
        newWindow.show();
    }

    private void deleteBtnClick(String fileName){
        boolean isActiv =true;
        for (String item: listofSharedFiles ){
            if (item.split("&")[0].equals(fileName) && isActiv && !item.split("&")[0].equals("Файл отсутствует")){
                try {
                    int statusCode = httpStorage.sendDeleteRequest(Long.valueOf(item.split("&")[1]));
                    if (statusCode == HttpURLConnection.HTTP_NOT_FOUND)
                        createWindowWithLabel("Файл отсутствует на сервере");
                    isActiv = false;
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void deleteFileFromMessage(int index) throws IOException, InterruptedException {

        httpStorage.sendDeleteRequest(Long.valueOf(uploadFiles.get(index - 1).split("&")[1]));
        totalFileSize -= Long.parseLong(uploadFiles.get(index - 1).split("&")[2]);

        uploadFiles.remove(index-1);

        files.getChildren().clear();
        countOfFiles--;
        int counter = 1;
        for (String file: uploadFiles) {
            String  fileName = file.split("&")[0];

            updateFileDialog(fileName, "",counter,files,true);
            counter++;
        }

    }






    public void uploadFile() throws IOException, InterruptedException{
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            Long fileId;
            String fileExtention = file.getName().split("\\.")[1];
            if (!fileExtention.equals("exe") && !fileExtention.equals("c") && !fileExtention.equals("com")) {
                if (file.length() <= MAX_FILE_SIZE && (totalFileSize + file.length()) <= MAX_TOTAL_FILE_SIZE) {
                    sendBtn.setDisable(true);
                    fileBtn.setDisable(true);

                    fileId = httpStorage.sendPostReuest(file.getPath());
                    uploadFiles.add(file.getName() + "&" + fileId + "&" + file.length());
                    countOfFiles++;
                    updateFileDialog(file.getName(), "",countOfFiles, files, true);
                    totalFileSize += file.length();
                    sendBtn.setDisable(false);
                    fileBtn.setDisable(false);
                } else {
                    createWindowWithLabel("Файл(ы) слишком большого размера!");
                }
            }else{
                createWindowWithLabel("Файл не поддерживаемого разширения");
            }
        }
    }

    private class ReadMsg extends Thread {
        @Override
        public void run() {
            String recvMessage;
            int counter = 1;
            try {
                client.clientLogic.in.readLine();
                while (true) {
                    recvMessage = client.clientLogic.in.readLine();
                    String[] words = recvMessage.split(" ",4);
                    String intdexOfChat = Long.toString(activeItem);
                    if (!words[0].equals("")) {
                        switch (Integer.parseInt(words[0].trim())) {
                            case CONNECT_MESSAGE_TYPE:
                                selectPartner.getItems().add(new ComboBoxItem(words[2],(long)counter));
                                break;
                            case REGULAR_MESSAGE_TYPE:
                                if ((words[1].equals(intdexOfChat)&&words[2].equals(intdexOfChat)) ||
                                        (words[2].equals(intdexOfChat)&&!words[1].equals("0"))||
                                        (words[1].equals("0")&&words[1].equals(intdexOfChat))||
                                        (words[1].equals(intdexOfChat)&&words[2].equals(Long.toString(client.clientLogic.idNumder)))) {
                                    Platform.runLater(()-> updateMessageDialog(words[3], null));
                                }
                                else
                                    notifyOfNewMessage(words);
                                break;

                            case DISCONNECT_MESSAGE_TYPE:
                                if (activeItem!=GLOBAL_MESSAGE)
                                    selectPartner.setValue(selectPartner.getItems().get((int) (activeItem-1)));
                                else
                                    selectPartner.setValue(selectPartner.getItems().get(Math.toIntExact(activeItem)));
                                selectPartner.getItems().remove(Integer.parseInt(words[2].trim()));
                                break;
                            case IDNUMBER_MESSAGE_TYPE:
                                System.out.println(words[2] + '\n');
                                client.clientLogic.idNumder = Long.valueOf(words[2].trim());
                                break;
                            case REQUEST_FOR_USERS:
                                selectPartner.getItems().add(new ComboBoxItem(words[1], (long) counter));
                                break;

                            case REGULAR_MESSAGE_WITH_FILE_TYPE:
                                if ((words[1].equals(intdexOfChat)&&words[2].equals(intdexOfChat)) ||
                                        (words[2].equals(intdexOfChat)&&!words[1].equals("0"))||
                                        (words[1].equals("0")&&words[1].equals(intdexOfChat))||
                                        (words[1].equals(intdexOfChat)&&words[2].equals(Long.toString(client.clientLogic.idNumder)))) {

                                    String[] wordsOfCountFiles = words[3].split(" ",2);
                                    int countOfFiles = Integer.parseInt(wordsOfCountFiles[0].trim());
                                    String[] wordsOfFiles = wordsOfCountFiles[1].split(" ",countOfFiles+1);

                                    Platform.runLater(()-> updateMessageDialog(wordsOfFiles[countOfFiles], wordsOfFiles));
                                }
                                else
                                    notifyOfNewMessage(words);
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
