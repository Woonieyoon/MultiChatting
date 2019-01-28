package chatting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class ServerClient {

	private Socket socket;
	private ExecutorService executorService;
	private List<ServerClient> connections = new ArrayList<ServerClient>();
	
	ServerClient(Socket socket,ExecutorService executorService,List<ServerClient> connections ) {
		this.socket = socket;
		this.executorService = executorService;
		this.connections = connections;
		receive();
	}

	void receive() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (true) {
					byte[] byteArr = new byte[1024];

					try{
						InputStream inputStream = socket.getInputStream();
						int readByteCount = inputStream.read(byteArr);
						if (readByteCount == -1) { // 클라이언트종료
							throw new IOException();
						}
						String message = "[요청 처리: " + socket.getRemoteSocketAddress() + " ]" + " / "
								+ Thread.currentThread().getName();
						System.out.println(message);

						String data = new String(byteArr, 0, readByteCount, "UTF-8");

						for (ServerClient client : connections) {
							if(client.socket != socket)
								client.send(data);
						}

					} catch (IOException e) {
						connections.remove(ServerClient.this);
						System.out.println("[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress()+" ]");
						try {
							socket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						return;
					}
				}
			}
		};
		executorService.submit(runnable);
	}

	void send(String data) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try{
					OutputStream outputStream = socket.getOutputStream();
					byte[] byteArr = data.getBytes("UTF-8");
					outputStream.write(byteArr);
					outputStream.flush();
				} catch (Exception e) {
					System.out.println("[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress());
					connections.remove(ServerClient.this);
					try {
						socket.close();
					} catch (IOException e1) {
					}
				}
			}
		};
		executorService.submit(runnable);
	}
	
	void close() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
