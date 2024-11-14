import java.util.*;

public class TopicManager {
    private Map<String, List<String>> topics = new HashMap<>();
    private Map<String, List<ClientHandler>> subscribers = new HashMap<>();

    public synchronized void publishMessage(String topic, String message) {
        topics.putIfAbsent(topic, new ArrayList<>());
        topics.get(topic).add(message);

        SubscriberManager.getInstance().notifySubscribers(topic, message);
    }

    public synchronized void addTopic(String topic) {
        topics.putIfAbsent(topic, new ArrayList<>());
    }

    public synchronized Set<String> getAllTopics() {
        return topics.keySet();
    }
}
