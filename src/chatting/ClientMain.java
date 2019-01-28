package chatting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {
	String ip;
	int port;
	Socket socket;

	public ClientMain(String IP, int port) {
		this.ip = IP;
		this.port = port;
		startClient();
	}

	public void startClient() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress(ip, port));
					System.out.println("[연결 완료: " + socket.getRemoteSocketAddress() + " ]");
				} catch (IOException e) {
					if (!socket.isClosed()) {
						stopClient();
					}
					return;
				}
				receive();
			}
		};
		thread.start();
	}

	public void stopClient() {
		System.out.println("[연결끊기:" + socket.getRemoteSocketAddress() + "]");
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void receive() {
		while(true) {
			byte[] byteArr = new byte[1024];
			try {
				InputStream inputStream = socket.getInputStream();
				int readByteCount = inputStream.read(byteArr);
				if(readByteCount == -1) { //서버 정상 종료
					throw new IOException();
				}
				
				String data = new String(byteArr,0,readByteCount,"UTF-8");
				System.out.println("[받기 완료]" + data);
				
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("서버 통신 안됨");
				stopClient();
				break;
			}
		}
	}

	public void send(String data) {
		System.out.println("미쳤냐:   " + data);
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					OutputStream outputstream = socket.getOutputStream();
					byte[] byteArr = data.getBytes("UTF-8");
					outputstream.write(byteArr);
					outputstream.flush();
					System.out.println("[전송 완료]");
				} catch (Exception e) {
					System.out.println("서버 통신 안됨");
					stopClient();
				}
			}
		};
		thread.start();
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ClientMain client = new ClientMain("localhost", 10020);
		
		Scanner sc;
		while(true) {
			sc = new Scanner(System.in);
			String data = sc.nextLine();
			System.out.println("내가 입력한값: "+data);
			if(data.equalsIgnoreCase("Quit")) {
				client.stopClient();
			}
			client.send(data);
		}		
	}

}
