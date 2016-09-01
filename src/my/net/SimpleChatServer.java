package my.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by off999555 on 17/11/2558 at 0:44.
 * Project Name: Test Java Network
 */
public class SimpleChatServer {

    private ServerSocket serverSocket;
    private List<PrintWriter> writers = new ArrayList<>(); // one writer will be created per one client

    public SimpleChatServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public static void main(String[] args) {
        try {
            SimpleChatServer server = new SimpleChatServer(5050);
            System.out.println("Enter something to broadcast to available chat participants!");
            new Thread(() -> readConsoleInput(server)).start();
            while (true) server.startAcceptingNewClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Setup Finished");
    }

    private static void readConsoleInput(SimpleChatServer server) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                String message = br.readLine();
                server.broadcastMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAcceptingNewClient() throws IOException {
        System.out.println("SimpleChatServer.startAcceptingNewClient");
        Socket socket = serverSocket.accept();
        // this listener will wait for incoming messages and invoke broadcast
        new Thread(new ClientListener(socket)).start();
    }

    private void broadcastMessage(String message) {
        System.out.println("SimpleChatServer.broadcastMessage");
        writers.forEach(writer -> {
            writer.println(message);
            writer.flush();
        });
    }


    private class ClientListener implements Runnable {
        BufferedReader reader;

        public ClientListener(Socket socket) {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writers.add(new PrintWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                reader.lines().forEach(SimpleChatServer.this::broadcastMessage);
            } catch (UncheckedIOException e) {
                System.err.println("Disconnected from " + Thread.currentThread().getName());
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
