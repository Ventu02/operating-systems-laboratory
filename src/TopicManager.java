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

    // Metodo per ottenere tutti i messaggi inviati su un topic
    public synchronized List<Message> getMessagesByPublisher(String topic) {
        return topics.getOrDefault(topic, Collections.emptyList());
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

    //Elimina i messaggi specificando l'id
    public synchronized boolean deleteMessage(String topic, int id) {
        List<Message> messages = topics.get(topic);
        if (messages != null) {
            return messages.removeIf(message -> message.getId() == id); // Rimuove il messaggio con l'ID specificato
        }
        return false;
    }
}
