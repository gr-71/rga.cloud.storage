package rga.cloud.storage.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import rga.cloud.storage.common.*;

import java.io.IOException;
import java.net.Socket;

class ClientNetwork {

    private static Socket socket;
    private static ObjectDecoderInputStream inputStream;
    private static ObjectEncoderOutputStream outputStream;

    static void start() {
        try {
            socket = new Socket("localhost", 8189);
            outputStream = new ObjectEncoderOutputStream(socket.getOutputStream());
            inputStream = new ObjectDecoderInputStream(socket.getInputStream(),
                    1000 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void stop() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendMessage(AbstractMessage msg) {
        try {
            outputStream.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendMessage(FileMessage msg) {
        try {
            outputStream.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendFileDeleteMessage(String fileForDeleting) {
        try {
            outputStream.writeObject(new DeleteMessage(fileForDeleting));
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static AbstractMessage readObject() throws ClassNotFoundException, IOException {
        Object obj = inputStream.readObject();
        return (AbstractMessage) obj;
    }

    static void sendAuthMessage(String login, String password) {
        try {
            outputStream.writeObject(new AuthorizationMessage(login, password));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendRegMessage(String login, String password) {
        try {
            outputStream.writeObject(new RegistrationMessage(login, password));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Object readInObject() throws ClassNotFoundException, IOException {
        return inputStream.readObject();
    }
}