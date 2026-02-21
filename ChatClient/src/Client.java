package com.example.chatclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client extends Application {

    private Socket socket;
    private BufferedReader bufferedReader;
    private OutputStreamWriter bufferedWriter;
    private VBox vbox_messages;
    private ClientController clientController;

    @Override
    public void start(Stage primaryStage) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("client-view.fxml"));
            Parent root = loader.load();

            clientController = loader.getController();
            clientController.setClientName("Client 1:");

            vbox_messages = clientController.getVBoxMessages();


            primaryStage.setResizable(false);
            primaryStage.setIconified(false);


            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("Chat Application.png")));


            primaryStage.setTitle("Client1 Application");
            primaryStage.setScene(new Scene(root, 480, 400));
            primaryStage.show();


            connectToServer("localhost", 12345);

        } catch (IOException e) {
            System.out.println("Error loading FXML file: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void connectToServer(String host, int port) {
        try {
            socket = new Socket(host, port);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new OutputStreamWriter(socket.getOutputStream());
            clientController.setBufferedWriter(bufferedWriter);
            listenForMessages();
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                String messageFromServer;
                while ((messageFromServer = bufferedReader.readLine()) != null) {
                    final String finalMessage = messageFromServer;

                    Platform.runLater(() -> {
                        clientController.addLabel(finalMessage, vbox_messages, false);
                    });
                }
            } catch (IOException e) {
                System.out.println("Error reading messages: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
