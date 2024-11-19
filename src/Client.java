import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(new ServerListener(in)).start();
    }

    public void start() throws IOException {
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        String command;
        System.out.println("Inserisci un comando:");

        while ((command = userInput.readLine()) != null) {
            // Invia il comando al server
            out.println(command);
            out.flush();
        }
    }

    public static void main(String[] args) {
        try {
            Client client = new Client("127.0.0.1", 9000);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ServerListener implements Runnable {
        private BufferedReader in;

        public ServerListener(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.out.println("Connessione chiusa.");
            }
        }
    }
}
