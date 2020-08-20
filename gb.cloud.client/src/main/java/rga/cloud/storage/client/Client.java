package rga.cloud.storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {
    @Override
    public void start(Stage startStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/authorization.fxml"));
        Parent parentRoot = fxmlLoader.load();
        startStage.setTitle("GB CLOUD STORAGE");
        startStage.setScene(new Scene(parentRoot));
        startStage.show();
        System.out.println("Client is connected.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
