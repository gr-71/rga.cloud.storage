package rga.cloud.storage.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import rga.cloud.storage.common.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class Controller implements Initializable {

    @FXML
    ListView<String> clientFilesList;
    @FXML
    ListView<String> clientFilesSizeList;
    @FXML
    ListView<String> serverFilesList;
    @FXML
    ListView<String> serverFilesSizeList;
    @FXML
    Button exitButton;

    private final String CLIENT_STORAGE_PATH = "client_storage/";
    private final Desktop desktop = Desktop.getDesktop();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ClientNetwork.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage abstractMessage = ClientNetwork.readObject();
                    if (abstractMessage instanceof FileMessage) {
                        FileMessage fileMessage = (FileMessage) abstractMessage;
                        Files.write(Paths.get(CLIENT_STORAGE_PATH + fileMessage.getFileName()),
                                fileMessage.getFileData(), StandardOpenOption.CREATE);
                        updateClientFilesList();
                    }
                    if (abstractMessage instanceof UpdateServerStorageMessage) {
                        UpdateServerStorageMessage updateServerStorageMessage =
                                (UpdateServerStorageMessage) abstractMessage;

                        TreeMap<String, Long> map = updateServerStorageMessage.getTreeMap();
                        Platform.runLater(() -> {
                            for (Map.Entry<String, Long> entry : map.entrySet()) {
                                serverFilesList.getItems().add(entry.getKey());
                                serverFilesSizeList.getItems().add(entry.getValue().toString());
                            }
                        });
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                ClientNetwork.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        updateClientFilesList();
        updateServerFilesList();
    }

    public void clickOnUploadButton() throws IOException {
        ClientNetwork.sendMessage(new FileMessage(Paths.get(CLIENT_STORAGE_PATH + clientFilesList.getSelectionModel().getSelectedItem())));
        updateServerFilesList();
    }

    public void clickOnDownloadButton() {
        ClientNetwork.sendMessage(new FileRequest(serverFilesList.getSelectionModel().getSelectedItem()));
    }

    public void clickOnClientDeleteButton() throws IOException {
        Files.delete(Paths.get(CLIENT_STORAGE_PATH + clientFilesList.getSelectionModel().getSelectedItem()));
        updateClientFilesList();
    }

    public void clickOnClientOpenButton() throws IOException {
        File file = Paths.get(CLIENT_STORAGE_PATH +
                clientFilesList.getSelectionModel().getSelectedItem()).toFile();
        this.desktop.open(file);
    }

    public void clickOnServerDeleteButton() {
        ClientNetwork.sendFileDeleteMessage(serverFilesList.getSelectionModel().getSelectedItem());
        updateServerFilesList();
    }

    public void clickOnAddFileToClientListButton() {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        try {
            Files.copy(file.toPath(), Paths.get(CLIENT_STORAGE_PATH + file.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateClientFilesList();
    }

    public void clickOnExitButton() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        try {
            Parent parentRoot = FXMLLoader.load(getClass().getResource("/authorization.fxml"));
            stage.setTitle("GB CLOUD STORAGE");
            Scene scene = new Scene(parentRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateClientFilesList() {
        updateUI(() -> {
            try {
                clientFilesList.getItems().clear();
                clientFilesSizeList.getItems().clear();
                Files.walkFileTree(Paths.get(CLIENT_STORAGE_PATH), new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) {
                        TreeMap<String, Long> findFiles = new TreeMap<>();
                        String fileName = file.getFileName().toString();
                        Long fileSize = (long) Math.ceil(file.toFile().length() / 1024.0);
                        findFiles.put(fileName, fileSize);
                        for (Map.Entry<String, Long> entry : findFiles.entrySet()) {
                            clientFilesList.getItems().add(entry.getKey());
                            clientFilesSizeList.getItems().add(entry.getValue().toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateServerFilesList() {
        serverFilesList.getItems().clear();
        serverFilesSizeList.getItems().clear();
        ClientNetwork.sendMessage(new UpdateMessage());
    }

    private static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}
