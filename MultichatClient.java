package multichat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class MultichatClient {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length != 1) {
			System.out.println("실행시 채팅 대화명이 필요합니다.");
			System.exit(0);
		}
		
		try {
			String serverip = "192.168.45.185";
			Socket socket = new Socket(serverip, 7777); // 소켓을 생성해서 연결을 요청
			System.out.println("[서버에 연결되었습니다.]");
			
			MultichatClient client = new MultichatClient();
			ClientSender sender  = client.new ClientSender(socket, args[0]);
			ClientReceiver receiver = new MultichatClient().new ClientReceiver(socket);
			sender.start();
			receiver.start();
			
		} catch(ConnectException ce) {
			System.out.println("서버 연결에 문제가 발생되었습니다.");
		} catch(Exception e) { }
	}
	
	// 클라이언트의 메시지를 보내는 스레드
	class ClientSender extends Thread {
		Socket socket = null;
		DataOutputStream out;
		String name;
		
		public ClientSender(Socket socket, String name) {
			this.socket = socket;
			try { 
				out = new DataOutputStream(socket.getOutputStream());
				this.name = name;
			} catch(Exception e) {
				System.out.println("채팅을 위한 출력 스트림 생성에 문제가 발생되었습니다.");
			}			
		}
		
		public void run() {
			Scanner sc = new Scanner(System.in);
			try {
				if(out != null) {
					out.writeUTF(name); // 제일 처음 대화명 보내기
				}
				
				String msg;
				while(!(msg=sc.nextLine()).equals("exit")) { // 콘솔 키보드를 통해 입력받은
					out.writeUTF("[" + name + "] " + msg); // 채팅 메시지 보내기
					out.flush();
				}
				out.close();
				System.exit(0); // exit를 입력한 경우 클라이언트 프로그램 종료
			} catch(IOException e) {
				
			}
		}
	}
	
	// 다른 클라이언트들의 메시지를 받는 스레드
	class ClientReceiver extends Thread {
		Socket socket = null;
		DataInputStream in = null;
		
		public ClientReceiver(Socket socket) {
			this.socket = socket;
			try { 
				in = new DataInputStream(socket.getInputStream());
			} catch(Exception e) {
				System.out.println("채팅을 위한 입력 스트림 생성에 문제가 발생되었습니다.");
			}
		}
		
		public void run() {			
			try {
				while(in != null) {
					System.out.println(in.readUTF());
				}
				in.close();
			} catch(IOException e) {
				System.out.println("채팅 내용을 입력받는 부분에서 문제가 발생되었습니다.");
			}		
		}
	}
}