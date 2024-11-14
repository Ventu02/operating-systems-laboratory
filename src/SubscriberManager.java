import java.io.PrintWriter;
import java.util.*;

public class SubscriberManager {
    private static SubscriberManager instance; // Istanza singleton
    private final Map<String, List<PrintWriter>> subscribers = new HashMap<>();

    // Costruttore privato per il pattern singleton
    SubscriberManager() {}

    // Metodo per ottenere l'istanza singleton
    public static synchronized SubscriberManager getInstance() {
        if (instance == null) {
            instance = new SubscriberManager();
        }
        return instance;
    }

    // Aggiunge un client alla lista dei subscriber di un topic
    public synchronized void addSubscriber(String topic, PrintWriter clientOut) {
        subscribers.putIfAbsent(topic, new ArrayList<>()); // Crea la lista se non esiste
        subscribers.get(topic).add(clientOut); // Aggiunge il subscriber alla lista del topic
        System.out.println("Aggiunto subscriber al topic: " + topic);
    }

    // Invia un messaggio a tutti i subscriber di un topic
    public synchronized void notifySubscribers(String topic, String message) {
        List<PrintWriter> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null && !topicSubscribers.isEmpty()) {
            System.out.println("Notifica dei subscriber per il topic: " + topic);
            for (PrintWriter out : topicSubscribers) {
                out.println("Nuovo messaggio su " + topic + ": " + message);
                out.flush(); // Invio effettivo del messaggio
            }
        } else {
            System.out.println("Nessun subscriber trovato per il topic: " + topic);
        }
    }
}
