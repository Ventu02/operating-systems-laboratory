//import java.util.*;
//
//public class TopicManager {
//    private Map<String, List<String>> topics = new HashMap<>(); // Mappa di topic e messaggi associati
//    private Map<String, List<ClientHandler>> subscribers = new HashMap<>(); // Mappa dei topic e dei subscriber
//
//    // Metodo per aggiungere un messaggio a un topic e notificare i subscriber
//    public synchronized void publishMessage(String topic, String message) {
//        topics.putIfAbsent(topic, new ArrayList<>()); // Crea il topic se non esiste
//        topics.get(topic).add(message); // Aggiunge il messaggio alla lista del topic
//
//        // Notifica tutti i subscriber per questo topic
//        SubscriberManager.getInstance().notifySubscribers(topic, message);
//    }
//    // Metodo per aggiungere un nuovo subscriber a un topic
//    public synchronized void addSubscriber(String topic, ClientHandler subscriber) {
//        subscribers.putIfAbsent(topic, new ArrayList<>());
//        subscribers.get(topic).add(subscriber);
//    }
//
//    // Metodo per ottenere tutti i messaggi di un topic
//    public synchronized List<String> getMessages(String topic) {
//        return topics.getOrDefault(topic, Collections.emptyList());
//    }
//
//    // Metodo per aggiungere un topic
//    public synchronized void addTopic(String topic) {
//        topics.putIfAbsent(topic, new ArrayList<>()); // Crea un topic se non esiste già
//    }
//
//    // Metodo per restituire la lista di tutti i topic
//    public synchronized Set<String> getAllTopics() {
//        return topics.keySet();
//    }
//}
import java.util.*;
import java.util.stream.Collectors;

public class TopicManager {
    private final Map<String, List<Message>> topics = new HashMap<>(); // Mappa di topic e messaggi associati
    private static int messageIdCounter = 1; // Contatore per ID univoci dei messaggi

    // Metodo per aggiungere un messaggio a un topic e notificare i subscriber
    public synchronized void publishMessage(String topic, String text, String publisherId) {
        topics.putIfAbsent(topic, new ArrayList<>()); // Crea il topic se non esiste
        Message message = new Message(messageIdCounter++, text, publisherId); // Crea il messaggio con ID e publisher
        topics.get(topic).add(message); // Aggiunge il messaggio al topic

        System.out.println("Messaggio pubblicato da PublisherID: " + publisherId);

        // Usa l'istanza singleton di SubscriberManager per notificare i subscriber
        SubscriberManager.getInstance().notifySubscribers(topic, text);
    }

    // Metodo che ritorna la lista di tutti i messaggi di un topic specifico
    public synchronized List<Message> getMessagesByPublisher(String topic) {
        return topics.getOrDefault(topic, Collections.emptyList()); // Filtra i messaggi del publisher corrente
    }

    // Metodo per restituire la lista di tutti i topic
    public synchronized Set<String> getAllTopics() {
        return topics.keySet();
    }

    // Metodo per aggiungere un topic. Restituisce true se il topic è stato creato ex novo, false se esiste già.
    public synchronized boolean addTopic(String topic) {
        if (!topics.containsKey(topic)) {
            topics.put(topic, new ArrayList<>()); // Crea un nuovo topic
            return true; // Il topic è stato creato
        }
        return false; // Il topic esiste già
    }

    public synchronized List<Message> getMessagesByPublisher(String topic, String publisherId) {
        List<Message> allMessages = topics.getOrDefault(topic, Collections.emptyList());
        // Filtra i messaggi inviati dal publisher corrente
        return allMessages.stream()
                .filter(message -> publisherId.equals(message.getPublisherId())) // Confronto corretto
                .collect(Collectors.toList());
    }
}
