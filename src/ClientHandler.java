import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private static TopicManager topicManager = new TopicManager();
    private static SubscriberManager subscriberManager = new SubscriberManager();

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

    private void processCommand(String command) {
        String[] tokens = command.split(" ", 2);
        String action = tokens[0].toLowerCase();

        switch (action) {
            case "publish":
                if (tokens.length > 1) {
                    topicManager.addTopic(tokens[1]);
                    out.println("Topic '" + tokens[1] + "' creato e pronto per pubblicare.");
                } else {
                    out.println("Errore: specificare il nome del topic.");
                }
                break;

            case "subscribe":
                if (tokens.length > 1) {
                    String topic = tokens[1];
                    topicManager.addTopic(topic);
                    subscriberManager.addSubscriber(topic, out);
                    out.println("Iscritto al topic: " + topic);
                } else {
                    out.println("Errore: specificare il nome del topic.");
                }
                break;

            case "send":
                String[] sendTokens = tokens[1].split(" ", 2);
                if (sendTokens.length == 2) {
                    String topic = sendTokens[0];
                    String message = sendTokens[1];
                    topicManager.publishMessage(topic, message);
                    subscriberManager.notifySubscribers(topic, message);
                    out.println("Messaggio inviato al topic '" + topic + "': " + message);
                } else {
                    out.println("Errore: specificare il topic e il messaggio.");
                }
                break;

            case "listall":
                Set<String> topics = topicManager.getAllTopics();
                if (topics.isEmpty()) {
                    out.println("Nessun topic disponibile.");
                } else {
                    out.println("Topic disponibili: " + topics);
                }
                break;

            default:
                out.println("Comando non riconosciuto: " + action);
                break;
        }
    }
}
