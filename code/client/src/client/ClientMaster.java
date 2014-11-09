package client;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ClientMaster {

	static int serverPortNumber = 9000;
	static String middlewareIP;
	static int clientId = 1;
	static int maxQueues = 100;
	static int clientOffSet;

	//used by all clients
	static ClientMasterLogger logger;

	// metadata
	public static String filePath = "../../logs/";
	public static String version;
	public static int arraySize = 250;
	public static String clientMachine;
	public static int numberOfClients = 5;
	public static Thread[] threads;
	public static String fill = "false";
	public static int numbOfMesPerClient = 100;
	public static int minPayload;
	public static int maxPayload;
	
	
	

	static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static void main(String[] args) {
		// middlewareIP
		middlewareIP = args[0];
		// max number of queues per client
		numberOfClients = Integer.parseInt(args[1]);
		clientOffSet = Integer.parseInt(args[2]);
		version = args[3];
		minPayload = Integer.parseInt(args[4]);
		maxPayload = Integer.parseInt(args[5]);
		//optional stuff for filling db
		if(args.length>6){
			fill = args[6];
			maxQueues = Integer.parseInt(args[7]);
			numbOfMesPerClient = Integer.parseInt(args[8]);
		}

		clientMachine = createName();
		
		 threads = new Thread[numberOfClients];

		logger = new ClientMasterLogger(version, arraySize, filePath,
				clientMachine, middlewareIP);
		
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new PeriodicLogger(), 100, 1000, TimeUnit.MILLISECONDS);
		
		System.out.println("started master");
		

		if (fill.equals("true")) {
			System.out.println("start fillerClients");
			fillDb();
		} else {
			for (int i = clientOffSet; i < clientOffSet+numberOfClients; i++) {
				runClient(i);
			}
		}

		System.out.println("finished startup");

	}

	public static void fillDb(){
		for(int i=0;i<numberOfClients;i++){
			threads[i] = new Thread(new FillerClient(serverPortNumber, middlewareIP, i, minPayload, maxPayload, maxQueues, numbOfMesPerClient));
			threads[i].start();
		}
		for(int i=0;i<numberOfClients;i++){
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void runClient(int clientId) {
		Thread t = new Thread(new ClientWorker(serverPortNumber, middlewareIP,
				clientId, minPayload, maxPayload));
		t.start();

	}

	protected static String createName() {
		int c = chars.length();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < 6; i++) {
			double pos = Math.random() * c;
			buffer.append(chars.charAt((int) pos));
		}
		return buffer.toString();
	}

}
