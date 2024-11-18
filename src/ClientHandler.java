import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Set;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private static TopicManager topicManager = new TopicManager(); // Gestore dei topic
    private static SubscriberManager subscriberManager = new SubscriberManager(); // Gestore dei subscriber
    private String clientRole = null; // Ruolo del client (publisher o subscriber)
    private String subscribedTopic = null; // Topic a cui il client è iscritto (se subscriber)

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Ricevuto comando dal client: " + request);
                processCommand(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Metodo per gestire i comandi inviati dal client
    private void processCommand(String command) {
        String[] tokens = command.split(" ", 2);
        String action = tokens[0].toLowerCase();

        switch (action) {
            case "publish":
                if (clientRole==null) {
                    clientRole = "publisher";
                    handlePublishCommand(tokens);
                } else {
                    out.println("Errore: il client è già registrato come " + clientRole);
                }
                break;

            case "subscribe":
                if (clientRole == null) {
                    clientRole = "subscriber";
                    handleSubscribeCommand(tokens);
                } else{
                    out.println("Errore: il client è già registrato come " + clientRole);
                }
                break;

            case "send":
                if ("publisher".equals(clientRole)) {
                    handleSendCommand(tokens);
                } else if ("subscriber".equals(clientRole)) {
                    out.println("Errore: il client è registrato come subscriber e non può inviare messaggi.");
                } else {
                    out.println("Errore: definire prima il ruolo con 'publish' o 'subscribe'.");
                }
                break;

            case "show":
                handleShowCommand();
                break;

            case "list":
                if ("publisher".equals(clientRole)) {
                    handleListCommand();
                } else {
                    out.println("Errore: solo i publisher possono utilizzare il comando 'list'.");
                }
                break;


            case "quit":
                handleQuitCommand();
                break;

            default:
                out.println("Comando non riconosciuto: " + action);
                break;
        }
    }

    private void handlePublishCommand(String[] tokens) {
        if (tokens.length > 1) {
            subscribedTopic = tokens[1]; // Associa il topic al publisher
            topicManager.addTopic(subscribedTopic);
            out.println("Topic '" + subscribedTopic + "' creato e pronto per pubblicare.");
        } else {
            out.println("Errore: specificare il nome del topic.");
        }
    }


    private void handleSubscribeCommand(String[] tokens) {
        if (tokens.length > 1) {
            subscribedTopic = tokens[1];
            topicManager.addTopic(subscribedTopic); // Crea il topic se non esiste
            SubscriberManager.getInstance().addSubscriber(subscribedTopic, out); // Aggiunge il client come subscriber
            out.println("Iscritto al topic: " + subscribedTopic);
        } else {
            out.println("Errore: specificare il nome del topic.");
        }
    }

    private void handleSendCommand(String[] tokens) {
        if (tokens.length > 1) {
            String message = tokens[1];
            if (subscribedTopic != null) { // Assicura che il publisher abbia un topic definito
                topicManager.publishMessage(subscribedTopic, message); // Pubblica il messaggio sul topic del publisher
                out.println("Messaggio inviato al topic '" + subscribedTopic + "': " + message);
            } else {
                out.println("Errore: nessun topic associato. Usa 'publish <topic>' per definire un topic.");
            }
        } else {
            out.println("Errore: specificare il messaggio da inviare. Sintassi: send <messaggio>");
        }
    }
    private void handleShowCommand() {
        Set<String> topics = topicManager.getAllTopics();
        if (topics.isEmpty()) {
            out.println("Nessun topic disponibile.");
        } else {
            out.println("Topic disponibili: " + topics);
        }
    }
    private void handleQuitCommand() {
        out.println("Connessione terminata.");
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }

    private void handleListCommand() {
        if (subscribedTopic != null) { //controllo se un client ha un topic associato
            List<Message> messages = topicManager.getMessagesByPublisher(subscribedTopic);
            if (messages.isEmpty()) {
                out.println("Nessun messaggio inviato su questo topic.");
            } else {
                out.println("Messaggi inviati:");
                for (Message message : messages) {
                    out.println(message); //stampa id,testo e data di ciascun messaggio
                }
            }
        } else {
            out.println("Errore: nessun topic associato. Usa 'publish <topic>' per definire un topic.");
        }
    }


}





