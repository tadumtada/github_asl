package middleware;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestAcceptor extends Thread{
	//one listener on one port. for every client uses a thread from the pool. the thread then handles request completely
	ServerSocket serverSocket;
	Socket clientSocket;
	long time;
	public RequestAcceptor(ServerSocket serverSocket){
		this.serverSocket = serverSocket;
	}
	
	public void run(){
		try {
			while(true){
				clientSocket = serverSocket.accept();
				time = System.nanoTime();
				Controller.threadPoolExecutor.execute(new Handler(clientSocket,time));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

}
