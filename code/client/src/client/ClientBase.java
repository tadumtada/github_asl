package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientBase implements Runnable{
	/*
	 * message format: read/update;action;senderId;receiverId;queueId,payload
	 * receiver == 0 -> broadcast queueId nonexisting -> create queue
	 * 
	 * actions:
	 * 
	 * dq: delete queue
	 * so: send  message to specific other client		
	 * sb: send broadcast		
	 * pq: pop message from queue		
	 * rq: read message from queue		
	 * ro: pop message from specific other client		
	 * gq: get queues with client's messages
	 * gaq: get all queueIds	
	 * cq: create new queue	
	 * 
	 * rnc: register new client
	 * roc:	register old client
	 * dc:	deregister client
	 * 
	 * 
	 */

	// needs to be configured the same as the server
	BufferedReader in;
	PrintWriter out;
	int serverPortNumber;
	String serverName;
	int clientId;
	int[] clients;
	int[] queues;
	int[] myQueues;
	int timer = 0;
	String payload = "";
	int minPayload;
	int maxPayload;
	
	
	//metadata
	String filePath;
	int version ;
	int arraySize;
	int clientMachine;
	
	//logging
	long start = 0;
	long stop = 0;
    String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    //for load balancing in ClientWorker
    int deleteFail = 0;

	
	public ClientBase(int serverPortNumber, String serverName, int clientId, int minPayload, int maxPayload){
		this.serverPortNumber = serverPortNumber;
		this.serverName = serverName;
		this.clientId = clientId;
		this.minPayload = minPayload;
		this.maxPayload = maxPayload;
	}

	public void run(){
		
		init();
		System.out.println("started client"+clientId);
	}
	 
	protected void init(){
		registerExisting(clientId);
	}

	protected void log(String logOperation, long logStart, long logStop, boolean logSuc){
		ClientMasterLogger.logOp(logOperation, logStart, logStop, logSuc);
		if(!logSuc){
			deleteFail++;
		}
	}
	
	protected String createPayload(int minPayload, int maxPayload){
		 int c = chars.length();
	      StringBuffer buffer = new StringBuffer();
	      double r = Math.random();
	      int length = minPayload + (int)(r*((double)(1+maxPayload-minPayload)));
	        for(int i =0; i < length; i++){
	            double pos = Math.random() * c;
	            buffer.append(chars.charAt((int) pos));
	        }
	        return buffer.toString();
	}
	
	
	protected void registerNew(){
		Message m = new Message(clientId);
		m.setAction("rnc");
		clientId = Integer.parseInt(genericReadQuery(m.toString()).trim());
	}
	
	protected void registerExisting(int clientId){
		start = System.nanoTime(); 
		Message m = new Message(clientId);
		m.setAction("roc");
		genericUpdateQuery(m.toString());
		stop = System.nanoTime();
		log("registerExisting", start, stop, true);
	}
	
	protected void deregister(int clientId){
		Message m = new Message(clientId);
		m.setAction("dc");
		genericUpdateQuery(m.toString());
	}


	protected boolean deleteQueue(int queueId) {
		start = System.nanoTime(); 
		Message m = new Message(clientId);
		m.setAction("dq");
		m.setQueueId(queueId);
		String suc = genericReadQuery(m.toString()).trim();
		stop = System.nanoTime();
		log("deleteQueue", start, stop, true);
		if(suc.equalsIgnoreCase("true")){
			return true;
		}else{																
			return false;
		}								
	}
	
	protected int[] getAllQueueIds(){
		start = System.nanoTime(); 
		Message m = new Message(clientId);
		m.setAction("gaq");
		String res = genericReadQuery(m.toString());
		stop = System.nanoTime();
		log("getAllQueueIds", start, stop, true);
		int[] queueIds = null;
		if(res.length() > 2){
			res = res.substring(2);
			String[] resSplit = res.split(";");
			queueIds = new int[resSplit.length];
			for(int i=0;i<resSplit.length;i++){
				queueIds[i] = Integer.parseInt(resSplit[i].trim());
			}
		}
		return queueIds;
	}
	
	protected int[] getAllActiveClients(){
		int[] clientIds = null;
		start = System.nanoTime(); 
		Message m = new Message(clientId);
		m.setAction("gac");
		String res = genericReadQuery(m.toString());
		stop = System.nanoTime();
		log("getAllActiveClients", start, stop, true);
		if(res.length()>2){
			res = res.substring(2);
			String[] resSplit = res.split(";");
			clientIds = new int[resSplit.length];
			for(int i=0;i<resSplit.length;i++){
				clientIds[i] = Integer.parseInt(resSplit[i].trim());
			}
		}
		return clientIds;
	}

	protected String popMessageFromSpecificOtherClient(int othersId) {
		start = System.nanoTime(); 
		Message m = new Message(clientId);
		m.setAction("ro");
		m.setOthersId(othersId);
		String res = genericReadQuery(m.toString());
		stop = System.nanoTime();
		if(res.equals("-1")|| res.equals("")){
			log("popMessageFromSpecific", start, stop, false);
		}else{
			log("popMessageFromSpecific", start, stop, true);
		}
		return res;
	}

	protected String readTopMostFromQueue(int queueId) {
		start = System.nanoTime(); 
		Message m = new Message(clientId);
		m.setAction("rq");
		m.setQueueId(queueId);
		String res = genericReadQuery(m.toString());
		stop = System.nanoTime();
		log("read", start, stop, true);
		return res;
	}

	protected String popFromQueue(int queueId) {
		start = System.nanoTime(); 
		Message m = new Message(clientId);
		m.setAction("pq");
		m.setQueueId(queueId);
		String res = genericReadQuery(m.toString());
		stop = System.nanoTime();
		if(res.equals("-1") || res.equals("")){
			log("pop", start, stop, false);
		}else{
			log("pop", start, stop, true);
		}
		return res;
	}

	protected int[] getQueuesWithMessagesWaiting() {
		start = System.nanoTime(); 
		int[] queueIds = null;
		Message m = new Message(clientId);
		m.setAction("gq");
		String res = genericReadQuery(m.toString());
		stop = System.nanoTime();
		log("getQueuesWithMessagesWaiting", start, stop, true);
		if(res.length()>2){
			res = res.substring(2);
			String[] resSplit = res.split(";");
			queueIds = new int[resSplit.length];
			for(int i=0;i<resSplit.length;i++){
				queueIds[i] = Integer.parseInt(resSplit[i].trim());
			}
		}
		return queueIds;
	}
	
	protected void sendMessageToSpecificOtherClient(int othersId, int queueId, String payload){
		start = System.nanoTime(); 
		Message m = new Message(clientId);
		m.setAction("so");
		m.setOthersId(othersId);
		m.setQueueId(queueId);
		m.setPayload(payload);
		genericUpdateQuery(m.toString());
		stop = System.nanoTime();
		log("writeToSpecific", start, stop, true);
	}
	
	protected void sendBroadcast(int queueId, String payload){
		start = System.nanoTime();
		Message m = new Message(clientId);
		m.setAction("sb");
		m.setOthersId(0);
		m.setQueueId(queueId);
		m.setPayload(payload);
		genericUpdateQuery(m.toString());
		stop = System.nanoTime();
		log("writeToAll", start, stop, true);
	}
	
	protected int createNewQueue(){
		start = System.nanoTime();
		Message m = new Message(clientId);
		m.setAction("cq");
		int i = Integer.parseInt(genericReadQuery(m.toString()).trim());
		stop = System.nanoTime();
		log("createNewQueue", start, stop, true);
		return i;
	}


	protected Socket getSocket(){
		Socket socket = null;
		try{ 
			socket = new Socket(serverName, serverPortNumber);
		}catch(Exception e){
			e.printStackTrace();
			socket = null;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		return socket;
	}

	protected void genericUpdateQuery(String message) {
		// connections & socket are closed automatically because they are in a
		// "try" block
		Socket socket = null;
		try {
			while(socket == null){
				socket = getSocket();
			}
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(message);
			out.flush();
			// BufferedReader in = new BufferedReader(new
			// InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				out.close();
				socket.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

	}

	protected String genericReadQuery(String message) {
		String res = "";
		StringBuilder builder = new StringBuilder();
		Socket socket = null;
		try {
			while(socket == null){
				socket = getSocket();
			}
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(message);
			out.flush();

			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				builder.append(line + "\n");
			}

			res = builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				out.close();
				in.close();
				socket.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		return res;
	}
	

}
