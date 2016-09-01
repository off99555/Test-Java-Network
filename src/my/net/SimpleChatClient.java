package my.net;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class SimpleChatClient {
    private JTextArea incoming;
    private JTextField messageBox;
    private JButton sendButton;
    private BufferedReader reader;
    private PrintWriter writer;

    public static void main(String[] args) {
        SimpleChatClient client = new SimpleChatClient();
        client.setUpGUI();
        client.establishConnection(5050);
        client.listenToIncomingMessages();
        System.out.println("Setup Finished");
    }

    private void listenToIncomingMessages() {
        System.out.println("SimpleChatClient.listenToIncomingMessages");
        Thread listener = new Thread(new IncomingReader());
        listener.start();
    }

    private void establishConnection(int port) {
        System.out.println("SimpleChatClient.establishConnection");
        try {
            Socket socket = new Socket("localhost", port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void setUpGUI() {
        System.out.println("SimpleChatClient.setUpGUI");
        JFrame frame = new JFrame();
        incoming = new JTextArea(15, 50);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(incoming);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        messageBox = new JTextField(20);
        sendButton = new JButton("Send");
        JPanel mainPanel = new JPanel();
        mainPanel.add(scrollPane);
        mainPanel.add(messageBox);
        mainPanel.add(sendButton);
        sendButton.addActionListener(new SendButtonActivationListener());
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    private class SendButtonActivationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String text = messageBox.getText();
            if (text.length() > 0) {
                System.out.println("messageBox.getText() = " + text);
                writer.println(text);
                writer.flush();
                messageBox.setText("");
            }
            messageBox.requestFocus();
        }
    }

    private class IncomingReader implements Runnable {
        @Override
        public void run() {
            System.out.println("IncomingReader.run");
            try {
                reader.lines().forEach(message -> incoming.append(message + "\n"));
            } catch (UncheckedIOException e) {
                System.err.println("Connection lost!");
                System.err.println(e);
            }
        }
    }
}
