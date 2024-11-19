import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.System.out;

public class Server {
    private static boolean running = true;
    private static final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(9000);
             Scanner scanner = new Scanner(System.in)) {

            // Thread per accettare connessioni dai client
            Thread connectionThread = new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clients.add(clientHandler);
                        new Thread(clientHandler).start();
                        out.println("Nuovo client connesso: " + clientSocket.getInetAddress());
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("Errore durante l'accettazione della connessione: " + e.getMessage());
                        }
                    }
                }
            });

            connectionThread.start();

            // Gestione dei comandi del server
            while (running) {
                String command = scanner.nextLine().toLowerCase();

                switch (command) {
                    case "quit":
                        handleQuitCommand();
                        break;

                    case "show":
                        handleShowCommandServer();
                        break;

                    default:
                        out.println("Comando non riconosciuto.");
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nel server: " + e.getMessage());
        }
    }

    private static void handleQuitCommand() {
        out.println("Terminazione del server...");
        running = false;

        // Disconnessione di tutti i client
        for (ClientHandler client : clients) {
            try {
                client.disconnect();
            } catch (IOException e) {
                System.err.println("Errore durante la disconnessione del client: " + e.getMessage());
            }
        }

        clients.clear();
        out.println("Server terminato.");
        System.exit(0); // Assicura la terminazione completa del server
    }

    private static void handleShowCommandServer() {
        Set<String> topics = ClientHandler.topicManager.getAllTopics();
        if (topics.isEmpty()) {
            System.out.println("Nessun topic disponibile.");
        } else {
            System.out.println("Topics disponibili:");
            for (String topic : topics) {
                System.out.println("- " + topic);
            }
        }
    }

}
