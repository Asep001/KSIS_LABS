package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResource("sample.fxml").openStream());
        primaryStage.setTitle("Client");
        primaryStage.setScene(new Scene(root, 770, 400));
        primaryStage.show();
        Controller controller = loader.getController();
        controller.SetStage(primaryStage);

    }


    public static void main(String[] args) {
        launch(args);
    }
}
