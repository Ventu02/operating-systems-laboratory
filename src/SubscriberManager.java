import java.io.PrintWriter;
import java.util.*;

public class SubscriberManager {
    private Map<String, List<PrintWriter>> subscribers = new HashMap<>();

    public synchronized void addSubscriber(String topic, PrintWriter clientOut) {
        subscribers.putIfAbsent(topic, new ArrayList<>());
        subscribers.get(topic).add(clientOut);
        System.out.println("Aggiunto subscriber al topic: " + topic);
    }

    public synchronized void notifySubscribers(String topic, String message) {
        List<PrintWriter> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null && !topicSubscribers.isEmpty()) {
            System.out.println("Notifica dei subscriber per il topic: " + topic);
            for (PrintWriter out : topicSubscribers) {
                out.println("Nuovo messaggio su " + topic + ": " + message);
                out.flush();
            }
        } else {
            System.out.println("Nessun subscriber trovato per il topic: " + topic);
        }
    }
}

