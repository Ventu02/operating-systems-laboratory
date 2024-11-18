import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private final int id;
    private final String text;
    private final LocalDateTime timestamp;

    public Message(int id, String text) {
        this.id = id;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        // Formatta la data e l'ora
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");
        String formattedDate = timestamp.format(formatter);

        return "ID: " + id + "\nTesto: " + text + "\nData: " + formattedDate;
    }
}
