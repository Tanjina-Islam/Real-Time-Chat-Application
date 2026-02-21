package com.example.chatserver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class Server extends Application {
    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private ServerController serverController;

    private List<ConnectionHandler> connectionHandlerList = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("server-view.fxml"));
            Parent root = fxmlLoader.load();

            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("Chat Application.png"))); // Adjust path if needed

            primaryStage.setTitle("Server Application");
            primaryStage.setScene(new Scene(root, 480, 400));
            primaryStage.show();

            serverController = fxmlLoader.getController();
            serverController.setServer(this);

            clientThreadPool = Executors.newCachedThreadPool();

            primaryStage.setResizable(false);
            primaryStage.setIconified(false);

            new Thread(this::setupServerConnection).start();

        } catch (IOException e) {
            System.out.println("Error loading FXML file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setServerController(ServerController serverController) {
        this.serverController = serverController;
    }

    public void listenForMessages(ConnectionHandler handler) {
        try {
            String messageFromClient;
            while ((messageFromClient = handler.bufferedReader.readLine()) != null) {
                final String finalMessage = messageFromClient;

                Platform.runLater(() -> serverController.addMessageToVBox(finalMessage, false));

                sendMessageToClient(finalMessage, handler);
            }
        } catch (IOException e) {
            Platform.runLater(() -> serverController.addMessageToVBox("Client disconnected.", false));
            e.printStackTrace();
        } finally {
            closeResources(handler);
        }
    }

    public void broadcastMessage(String message, ConnectionHandler sender) {
        for (ConnectionHandler handler : connectionHandlerList) {
            if (handler != sender) {
                handler.sendMessage(message);
            }
        }
    }

    public void sendMessageToClient(String message, ConnectionHandler sender) {
        broadcastMessage(message, sender);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void setupServerConnection() {
        try {
            serverSocket = new ServerSocket(12345);
            System.out.println("Server started. Waiting for client...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");

                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStreamWriter writer = new OutputStreamWriter(clientSocket.getOutputStream());

                ConnectionHandler handler = new ConnectionHandler(clientSocket, reader, writer);

                connectionHandlerList.add(handler);

                handler.sendMessage("Hello, Client! You are connected.");

                clientThreadPool.submit(() -> listenForMessages(handler));
            }
        } catch (IOException e) {
            System.out.println("Error while setting up server connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeResources(ConnectionHandler handler) {
        try {
            if (handler.bufferedReader != null) {
                handler.bufferedReader.close();
            }
            if (handler.bufferedWriter != null) {
                handler.bufferedWriter.close();
            }
            if (handler.clientSocket != null) {
                handler.clientSocket.close();
            }
            connectionHandlerList.remove(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ConnectionHandler {
        private Socket clientSocket;
        private BufferedReader bufferedReader;
        private OutputStreamWriter bufferedWriter;

        public ConnectionHandler(Socket clientSocket, BufferedReader bufferedReader, OutputStreamWriter bufferedWriter) {
            this.clientSocket = clientSocket;
            this.bufferedReader = bufferedReader;
            this.bufferedWriter = bufferedWriter;
        }

        public void sendMessage(String message) {
            try {
                bufferedWriter.write(message + "\n");
                bufferedWriter.flush(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
