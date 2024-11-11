import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService pool;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        pool = Executors.newFixedThreadPool(4); // Limita a N thread simultanei
    }

    public void start() throws IOException {
        System.out.println("Server in ascolto sulla porta " + serverSocket.getLocalPort());

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Nuovo client connesso: " + clientSocket.getInetAddress());

            pool.execute(new ClientHandler(clientSocket)); // Esegue il client su un nuovo thread utilizzando il ClientHandler
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(9000);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
