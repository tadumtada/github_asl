package client;


public class FillerClient extends ClientWorker{
	
	int maxQueues;
	int numbOfMesPerClient;
	int[] tempq;
	int[] tempc;
	
	public FillerClient(int serverPortNumber, String serverName,int clientId, int minPayload, int maxPayload, int maxQueues, int numbOfMesPerClient) {
		super(serverPortNumber, serverName, clientId, minPayload, maxPayload);
		this.maxQueues = maxQueues;
		this.numbOfMesPerClient = numbOfMesPerClient;
	}
	
	public void run(){
		registerNew();
		System.out.println("started client:"+ clientId);
		for(int i=0;i<maxQueues;i++){
			createNewQueue();
		}
		
		try {
			//enhance probability that all queues are generated befor filling them
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		queues = new int[]{1,2,3};
		clients = new int[]{1,2,3};
		for(int q=0;q<=numbOfMesPerClient;q++){
			if((tempq = getAllQueueIds())!= null){
				queues = tempq;
			}
			if((tempc = getAllActiveClients())!= null){
				clients = tempc;
			}
			int othersId = clients[(int)(Math.random()*clients.length)];
			int queueId = queues[(int)(Math.random()*queues.length)];
			write(othersId, queueId);
		}
		
		deregister(clientId);
	}

	

}
