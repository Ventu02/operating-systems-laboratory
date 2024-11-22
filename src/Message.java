import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private final int id;
    private final String text;
    private final String publisherId; // Identificatore del publisher
    private final LocalDateTime timestamp;

    public Message(int id, String text, String publisherId) {
        this.id = id;
        this.text = text;
        this.publisherId = publisherId;
        this.timestamp = LocalDateTime.now();
        //System.out.println("Creato Messaggio con PublisherID: " + publisherId);
    }

    public int getId() {
        return id;
    }

    public String getPublisherId() {
        return publisherId;
    } //viene richiamato in handleListCommand per identificare un client

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        // Formatta la data e l'ora in: ore:minuti:secondi - giorno/mese/anno
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");
        String formattedDate = timestamp.format(formatter);
        return "ID: " + id + "\nTesto: " + text + "\nData: " + formattedDate;
    }
}
