package multichat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;

public class MultichatServer {
	
	//클라이언트 접속 정보를 저장하는 Map컬렉션(key-접속 대화명, value-DataOutputStream)
	Hashtable<String, DataOutputStream> clients; 
	
	public MultichatServer() {
		clients = new Hashtable<String, DataOutputStream>();
	}
	
	//서버시작 초기화 메서드
	public void start() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("192.168.45.185", 7777));
			System.out.println("[서버가 시작되었습니다.]");
			while(true) {				
				socket = serverSocket.accept(); //클라이언트 요청 수락
				InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
				System.out.println("[" + isa.getHostName() + "] 에서 접속하였습니다.");
				ServerReceiver thread = new ServerReceiver(socket); //클라이언트와의 통신은 각각의 스레드가 담당
				thread.start();
			}			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// Map에 등록된 모든 클라이언트들에게 메시지 전송
	void sendToAll(String msg) {
		Iterator<String> it = clients.keySet().iterator();
		while(it.hasNext()) {
			try {
				DataOutputStream out = (DataOutputStream)clients.get(it.next());
				out.writeUTF(msg); // 클라이언트에게 메시지 전송
			} catch(IOException e) { 
				System.out.println("클라이언트 메시지 전송에 문제가 발생하였습니다.");
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new MultichatServer().start();
	}
	
	class ServerReceiver extends Thread {
		Socket socket;
		DataInputStream in;
		DataOutputStream out;
		
		ServerReceiver(Socket socket) { //클라이언트 요청 수락시에 생성된 socket을 전달 받음
			this.socket = socket;
			try {
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
			} catch(IOException e) {
				System.out.println("채팅을 위한 입출력 스트림 생성에 문제가 발생되었습니다.");
			}
		}
		
		public void run() {
			String name = "";
			try {
				name = in.readUTF();
				sendToAll("#" + name + "님이 들어오셨습니다.");
				clients.put(name, out); //Map에 클라이언트 접속 정보 추가
				System.out.println("현재 서버 접속자 수는 " + clients.size() + "입니다."); //서버 콘솔에 출력
				
				while(in!=null) {
					String msg = in.readUTF();
					sendToAll(msg); //모든 클라이언트에게 메시지 보내기
					System.out.println(msg); //서버 콘솔에 출력
				}
			} catch(IOException e) { 
				
			} finally { //클라이언트 접속 종료 후 처리될 기능들
				sendToAll("#" + name + "님이 나가셨습니다.");
				clients.remove(name); //Map에 클라이언트 접속 정보 제거
				InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
				System.out.println("[" + isa.getHostName() + "] 에서 접속을 종료하였습니다.");
				System.out.println("현재 서버 접속자 수는 " + clients.size() + "입니다.");
				
				try {
					in.close();
					out.close();
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
	}

}