package com.example.chatclient;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientController {

    public ClientController() {

    }
    private String clientName;

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @FXML
    private VBox vbox_messages;

    @FXML
    private TextField tf_message;

    @FXML
    private AnchorPane ap_main;

    public Button button_emoji;

    private HBox emojiBox;

    private OutputStreamWriter bufferedWriter;


    @FXML
    public void initialize() {
        tf_message.setPromptText("Type your message...");
    }

    @FXML
    private void onSendButtonEmoji() {
        if (emojiBox != null && ap_main.getChildren().contains(emojiBox)) {
            ap_main.getChildren().remove(emojiBox);
        } else {
            setupEmojiBox();
        }
    }

    private void setupEmojiBox() {
        if (emojiBox == null) {
            emojiBox = new HBox(10);
            emojiBox.setAlignment(Pos.CENTER);
            emojiBox.getStyleClass().add("emoji-box");

            Button emoji1 = new Button("😊");
            Button emoji2 = new Button("😔");
            Button emoji3 = new Button("😎");
            Button emoji4 = new Button("🔥");
            Button emoji5 = new Button("👋");
            Button emoji6 = new Button("👍");
            Button emoji7 = new Button("👎");

            emojiBox.getChildren().addAll(emoji1, emoji2, emoji3, emoji4, emoji5, emoji6, emoji7);

            emoji1.setOnAction(e -> addEmojiToTextField("😊"));
            emoji2.setOnAction(e -> addEmojiToTextField("😔"));
            emoji3.setOnAction(e -> addEmojiToTextField("😎"));
            emoji4.setOnAction(e -> addEmojiToTextField("🔥"));
            emoji5.setOnAction(e -> addEmojiToTextField("👋"));
            emoji6.setOnAction(e -> addEmojiToTextField("👍"));
            emoji7.setOnAction(e -> addEmojiToTextField("👎"));
        }

        Bounds buttonBounds = button_emoji.localToScene(button_emoji.getBoundsInLocal());
        double buttonX = buttonBounds.getMinX();
        double buttonY = buttonBounds.getMinY();

        emojiBox.setLayoutX(buttonX + 15);
        emojiBox.setLayoutY(buttonY - 55);

        if (!ap_main.getChildren().contains(emojiBox)) {
            ap_main.getChildren().add(emojiBox);
        }
    }

    private void addEmojiToTextField(String emoji) {
        tf_message.appendText(emoji);
    }

    public VBox getVBoxMessages() {
        return vbox_messages;
    }

    public void setBufferedWriter(OutputStreamWriter writer) {
        this.bufferedWriter = writer;
    }

    public void addLabel(String message, VBox vBox, boolean isClientMessage) {

        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String timeString = currentTime.format(formatter);

        Text text = new Text(message);
        TextFlow messageFlow = new TextFlow(text);
        messageFlow.setStyle("-fx-background-color: " + (isClientMessage ? "PLUM" : "rgb(233, 233, 235)") + "; -fx-background-radius: 20px;");
        messageFlow.setPadding(new Insets(5, 10, 5, 10));

        Text timeText = new Text(timeString);
        timeText.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
        TextFlow timeFlow = new TextFlow(timeText);
        timeFlow.setPadding(new Insets(2, 10, 5, 10));

        VBox messageWithTime = new VBox(messageFlow, timeFlow);
        messageWithTime.setAlignment(isClientMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox hBox = new HBox();
        hBox.setAlignment(isClientMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));
        hBox.getChildren().add(messageWithTime);

        vBox.getChildren().add(hBox);
    }

    @FXML
    private void onSendButtonClick() {
        String message = tf_message.getText().trim();
        if (!message.isEmpty()) {
            sendMessageToServer(clientName +" " + message);
            addLabel(message, vbox_messages, true); // Display your message locally with blue background
            tf_message.clear();
        }
    }

    @FXML
    private void onEnterKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            onSendButtonClick();
        }
    }

    private void sendMessageToServer(String message) {
        try {
            if (bufferedWriter != null) {
                bufferedWriter.write(message + "\n");
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            System.out.println("Error sending message to server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
