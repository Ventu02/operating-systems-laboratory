import java.util.*;

public class TopicManager {
    private Map<String, List<String>> topics = new HashMap<>(); // Utilizzo un hashmap per mappare i topic coi messaggi

    //Aggiungo un messaggio a un topic
    public synchronized void publishMessage(String topic, String message) {
        topics.putIfAbsent(topic, new ArrayList<>());
        topics.get(topic).add(message);
    }

    // Aggiungo un topic se non esiste già
    public synchronized void addTopic(String topic) {
        topics.putIfAbsent(topic, new ArrayList<>()); //
    }

    public synchronized Set<String> getAllTopics() {
        return topics.keySet();
    }
}
