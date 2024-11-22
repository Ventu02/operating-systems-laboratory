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
    static TopicManager topicManager = new TopicManager(); // Gestore dei topic
    private static SubscriberManager subscriberManager = new SubscriberManager(); // Gestore dei subscriber
    private String clientRole = null; // Ruolo del client (publisher o subscriber)
    private String subscribedTopic = null; // Topic a cui il client è iscritto (se subscriber)

    private final String publisherId; // Identificatore unico del publisher

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.publisherId = clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort(); // Identificatore unico
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            String request;
            //Controllo che il socket non sia stato chiuso prima di leggere un nuovo comando
            while (!clientSocket.isClosed() && (request = in.readLine()) != null) {
                System.out.println("Ricevuto comando dal client: " + request);
                processCommand(request);
            }
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                System.err.println("Errore durante la gestione del client: " + e.getMessage());
            }
        } finally {
            try {
                disconnect(); // Chiude in sicurezza il client
            } catch (IOException e) {
                System.err.println("Errore durante la chiusura del client: " + e.getMessage());
            }
        }
    }

    // Metodo per gestire i comandi inviati dal client
    private void processCommand(String command) {
        synchronized (Server.sessionLock) {
            while (Server.inInteractiveSession) {
                try {
                    Server.sessionLock.wait(); // Mette il client in attesa, che verranno sbloccati quando verrà eseguito notifyAll
                } catch (InterruptedException e) {
                    System.err.println("Errore durante l'attesa: " + e.getMessage());
                }
            }
        }
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
                if (clientRole==null) {
                    handleShowCommand();
                } else {
                    out.println("Errore: comando non autorizzato'.");
                }
                break;

            case "list":
                if ("publisher".equals(clientRole)) {
                    handleListCommand();
                } else {
                    out.println("Errore: solo i publisher possono utilizzare il comando 'list'.");
                }
                break;

            case "listall":
                if (subscribedTopic != null) {
                    handleListAllCommand();
                } else {
                    out.println("Errore: nessun topic associato. Usa 'publish <topic>' o 'subscribe <topic>' per definirne uno.");
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
            String topic = tokens[1];
            if (topicManager.addTopic(topic)) {
                out.println("Topic '" + topic + "' creato.");
                System.out.println("Topic '" + topic + "' creato.");
            } else {
                out.println("Publisher aggiunto al topic '" + topic + "'.");
                System.out.println("Publisher aggiunto al topic '" + topic + "'.");
            }
            subscribedTopic = topic; // Assegna il topic al publisher
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
                topicManager.publishMessage(subscribedTopic, message, publisherId); // Passa il publisherId
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
        if (subscribedTopic != null) {
            // Debug: verifica il publisherId
            System.out.println("List command invocato da PublisherID: " + publisherId);

            List<Message> messages = topicManager.getMessagesByPublisher(subscribedTopic, publisherId); // Recupera i messaggi del publisher corrente
            if (messages.isEmpty()) {
                out.println("Non hai inviato alcun messaggio su questo topic.");
            } else {
                out.println("I tuoi messaggi inviati su '" + subscribedTopic + "':");
                for (Message message : messages) { //recupera i messaggi del publisher corrente
                    out.println(message); //stampa id, testo e data di ciascun messaggio inviato dal publisher corrente
                }
            }
        } else {
            out.println("Errore: nessun topic associato. Usa 'publish <topic>' per definirne uno.");
        }
    }

    private void handleListAllCommand() {
        List<Message> messages = topicManager.getMessagesByPublisher(subscribedTopic); // Recupera i messaggi
        if (messages.isEmpty()) {
            out.println("Nessun messaggio inviato su questo topic.");
        } else {
            out.println("Tutti i messaggi inviati su '" + subscribedTopic + "':");
            for (Message message : messages) {
                out.println(message); //stampa id, testo e data di ciascun messaggio presente nel topic
            }
        }
    }

    // Metodo per disconnettere i client
    // utilizzato nella classe Server per gestire la chiusura della connessione dal lato del client
    public void disconnect() throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
            System.out.println("Client disconnesso: " + clientSocket.getInetAddress());
        }
    }
}





