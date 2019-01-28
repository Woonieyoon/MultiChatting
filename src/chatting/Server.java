package chatting;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	private final String ip;
	private final int port;
	
	private ExecutorService executorService;
	private List<ServerClient> connections = new ArrayList<ServerClient>();
	private ServerSocket serverSocket;
	
	public Server(String ip,int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public void initialize() {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(ip, port));
		} catch (Exception e) {
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
	}
	

	public void execute() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				System.out.println("[서버 시작]");

				while (true) {
					try {
						Socket socket = serverSocket.accept();
						String message = "[연결 수락: " + socket.getRemoteSocketAddress() + " ]" + "  /  "
								+ Thread.currentThread().getName();
						System.out.println(message);

						ServerClient client = new ServerClient(socket,executorService,connections);
						connections.add(client);
						System.out.println("[클라이언트 접속자수: " + connections.size() + "]");
					} catch (IOException e) {
						if (!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		executorService.submit(runnable);
	}

	void stopServer() {
		try {
			Iterator<ServerClient> iterator = connections.iterator();
			while (iterator.hasNext()) {
				ServerClient client = iterator.next();
				client.close();
				iterator.remove();
			}
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			if (executorService != null && !executorService.isShutdown()) {
				executorService.shutdown();
			}
			System.out.println("[서버 종료]");
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {
		Server server = new Server("localhost", 10020);
		server.initialize();
		server.execute();
	}

}
