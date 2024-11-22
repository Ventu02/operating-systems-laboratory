import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import static java.lang.System.out;

public class Server {
    private static boolean running = true; // Controlla se il server è in esecuzione (viene settata su false per fermare il server nel quit)
    private static final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    static volatile boolean inInteractiveSession = false; // Flag per la sessione interattiva (evita che si possano utilizzare show e quit)
    static final Object sessionLock = new Object(); // Oggetto per sincronizzazione


    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(9000);
             Scanner scanner = new Scanner(System.in)) {

            // Thread per accettare connessioni dai client
            Thread connectionThread = new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept(); //accetta un nuovo client
                        ClientHandler clientHandler = new ClientHandler(clientSocket); //crea un clientHandler per gestire i client
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

                // Se è attiva una sessione interattiva allora blocca i comandi quit e inspect
                synchronized (sessionLock) {
                    if (inInteractiveSession && !command.equals(":end")) {
                        System.out.println("Durante la sessione interattiva è ammesso solo l'utilizzo dei comandi :listall, :delete <id> e :end. Riprova.");
                        continue;
                    }
                }

                if (command.startsWith("inspect")) {
                    String[] tokens = command.split(" ", 2);
                    if (tokens.length > 1) {
                        handleInspectCommand(tokens[1]);
                    } else {
                        System.out.println("Errore: specificare un topic. Sintassi: inspect <topic>");
                    }
                } else {
                    switch (command) {
                        case "quit":
                            handleQuitCommand();
                            break;

                        case "show":
                            handleShowCommandServer();
                            break;

                        default:
                            System.out.println("Comando non riconosciuto.");
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Errore nel server: " + e.getMessage());
        }
    }

    private static void handleQuitCommand() {
        out.println("Terminazione del server...");
        running = false;

        // ciclo che disconnette ciascun client connesso al server
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

    private static void handleInspectCommand(String topicName) {
        if (inInteractiveSession) {
            System.out.println("Errore: una sessione interattiva è già attiva.");
            return;
        }

        if (!ClientHandler.topicManager.getAllTopics().contains(topicName)) {
            System.out.println("Errore: il topic '" + topicName + "' non esiste.");
            return;
        }

        synchronized (sessionLock) {
            inInteractiveSession = true; // Imposta la sessione interattiva
        }

        System.out.println("Sessione interattiva avviata per il topic: " + topicName);
        Scanner scanner = new Scanner(System.in);
        boolean inSession = true;

        while (inSession) {
            System.out.print(topicName + ": ");
            String command = scanner.nextLine().toLowerCase();

            switch (command.split(" ")[0]) {
                case ":listall":
                    listAllMessages(topicName);
                    break;

                case ":delete":
                    deleteMessage(topicName, command);
                    break;

                case ":end":
                    inSession = false;
                    System.out.println("Sessione interattiva terminata per il topic: " + topicName);
                    break;

                default:
                    System.out.println("Comando non riconosciuto. Comandi disponibili: :listall, :delete <id>, :end");
            }
        }

        synchronized (sessionLock) {
            inInteractiveSession = false; // Libera la sessione interattiva
            sessionLock.notifyAll(); // Notifica i client in attesa, sbloccando i client e il server
        }
    }

    private static void listAllMessages(String topicName) {
        List<Message> messages = ClientHandler.topicManager.getMessagesByPublisher(topicName);
        if (messages.isEmpty()) {
            System.out.println("Nessun messaggio presente sul topic '" + topicName + "'.");
        } else {
            System.out.println("Messaggi nel topic '" + topicName + "':");
            for (Message message : messages) {
                System.out.println(message);
            }
        }
    }

    private static void deleteMessage(String topicName, String command) {
        try {
            int id = Integer.parseInt(command.split(" ")[1]); // Estrae l'ID dalla stringa del comando e lo parsa come int
            boolean success = ClientHandler.topicManager.deleteMessage(topicName, id);

            if (success) {
                System.out.println("Messaggio con ID " + id + " eliminato.");
            } else {
                System.out.println("Errore: nessun messaggio con ID " + id + " trovato nel topic '" + topicName + "'.");
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Errore: comando non valido. Sintassi corretta: :delete <id>");
        }
    }
}
