import java.util.*;

public class TopicManager {
    private final Map<String, List<Message>> topics = new HashMap<>(); // Mappa di topic e messaggi associati
    private static int messageIdCounter = 1; // Contatore per ID univoci dei messaggi

    // Metodo per aggiungere un messaggio a un topic e notificare i subscriber
    public synchronized void publishMessage(String topic, String text) {
        topics.putIfAbsent(topic, new ArrayList<>());
        Message message = new Message(messageIdCounter++, text); // Crea il messaggio con ID univoco (viene incrementato ad ogni nuovo messaggio)
        topics.get(topic).add(message);

        // Usa l'istanza singleton di SubscriberManager per notificare i subscriber
        SubscriberManager.getInstance().notifySubscribers(topic, text);
    }


    // Metodo che ritorna la lista di messaggi inviati su un topic specifico
    public synchronized List<Message> getMessagesByPublisher(String topic) {
        return topics.getOrDefault(topic, Collections.emptyList());
    }

    // Metodo per aggiungere un topic
    public synchronized void addTopic(String topic) {
        topics.putIfAbsent(topic, new ArrayList<>()); // Crea un topic se non esiste già
    }

    // Metodo per restituire la lista di tutti i topic
    public synchronized Set<String> getAllTopics() {
        return topics.keySet();
    }
}
