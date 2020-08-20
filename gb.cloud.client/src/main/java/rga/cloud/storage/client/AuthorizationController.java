package rga.cloud.storage.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;


public class AuthorizationController implements Initializable {

    private Object msgFromServer = null;
    private Alert alert;

    @FXML
    VBox authorization;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    VBox registration;
    @FXML
    TextField registrationLoginField;
    @FXML
    PasswordField registrationPasswordField;
    @FXML
    PasswordField registrationPasswordFieldAgain;
    @FXML
    Button loginButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientNetwork.start();
        Thread serverListener = new Thread(() -> {
            while (true){
                try {
                    msgFromServer = ClientNetwork.readInObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (Objects.requireNonNull(msgFromServer).toString().startsWith("This user is successfully checked.")) {
                    successfulEnterToStorageScene();

                } else if (msgFromServer.toString().startsWith("There is incorrect username or password. Please try again.")) {
                    Platform.runLater(() -> {
                        alert = new Alert(Alert.AlertType.ERROR, "There is incorrect username or password. Please try again.");
                        alert.showAndWait();
                    });

                } else if (msgFromServer.toString().startsWith("There is an already existing user. Please try again.")) {
                    Platform.runLater(() -> {
                        alert = new Alert(Alert.AlertType.ERROR, "There is an already existing user. Please try again.");
                        alert.showAndWait();
                        registrationLoginField.clear();
                        registrationPasswordField.clear();
                        registrationPasswordFieldAgain.clear();
                    });

                } else if (msgFromServer.toString().startsWith("Congratulations! Registration is successfully completed.")) {
                    Platform.runLater(() -> {
                        alert = new Alert(Alert.AlertType.INFORMATION, "Congratulations! Registration is successfully completed.");
                        alert.showAndWait();
                        exitRegistration();
                    });
                }
            }
        });
        serverListener.setDaemon(true);
        serverListener.start();
    }

    public void showRegistrationForm() {
        authorization.setVisible(false);
        registration.setVisible(true);
    }

    public void exitRegistration() {
        authorization.setVisible(true);
        registration.setVisible(false);
    }

    public void sendAuthorizationMessage() {
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            ClientNetwork.sendAuthMessage(loginField.getText(), passwordField.getText());
            loginField.clear();
            passwordField.clear();
        }
    }

    public void sendRegistrationMessage() {
        if (!registrationLoginField.getText().isEmpty() && !registrationPasswordField.getText().isEmpty()
                && !registrationPasswordFieldAgain.getText().isEmpty()) {
            if (registrationPasswordField.getText().equals(registrationPasswordFieldAgain.getText())) {
                ClientNetwork.sendRegMessage(registrationLoginField.getText(), registrationPasswordField.getText());
            } else {
                Platform.runLater(() -> {
                    alert = new Alert(Alert.AlertType.ERROR, "Passwords are not the same.");
                    alert.showAndWait();
                    registrationPasswordField.clear();
                    registrationPasswordFieldAgain.clear();
                });
            }
        }
    }

    private void switchToStorageScene() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        try {
            Parent parentRoot = FXMLLoader.load(getClass().getResource("/main_form.fxml"));
            stage.setTitle("GB CLOUD STORAGE");
            Scene scene = new Scene(parentRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void successfulEnterToStorageScene() {
        Platform.runLater(this::switchToStorageScene);
    }
}

