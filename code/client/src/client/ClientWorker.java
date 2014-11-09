package client;

public class ClientWorker extends ClientBase{
	
	double rand = 0;
	int counter = 0;
	double msize = 0;
	
	int[] clientsTemp;
	int[] queuesTemp;
	int[] myQueuesTemp;
	long start = 0;
	long stop = 0;
	
	//for load balancing
	//deleteFail is stored in the ClientBase
	int write;
	int pop; 
	
	double pWrite = 0.345;
	double pRead = 0.3;
	double restProb;
	
	
	
	
	boolean queueDelete = false;
	public ClientWorker(int serverPortNumber, String serverName, int clientId, int minPayload, int maxPayload) {
		super(serverPortNumber, serverName, clientId, minPayload, maxPayload);
		restProb = 1.0-pRead;
	}
	
	public void run(){
		System.out.println("started client: "+ clientId);
		registerExisting(clientId);
		queues = getAllQueueIds();
		clients = getAllActiveClients();
		myQueues = getQueuesWithMessagesWaiting();
		while(true){
			rand = Math.random();
			int othersId = clients[(int)(Math.random()*clients.length)];
			int queueId = queues[(int)(Math.random()*queues.length)];
			int myQueue = myQueues[(int)(Math.random()*myQueues.length)];
			counter++;
			
			//periodic queue & clientupdate
			if(counter > 500){
				if((queuesTemp = getAllQueueIds()) != null){
					ClientMasterLogger.others.incrementAndGet();
					queues = queuesTemp;
				}
				if((clientsTemp = getAllActiveClients()) != null){
					ClientMasterLogger.others.incrementAndGet();
					clients = clientsTemp;
				}
				if((myQueuesTemp = getQueuesWithMessagesWaiting()) != null){
					ClientMasterLogger.others.incrementAndGet();
					myQueues = myQueuesTemp;
				}
				counter = 0;
				
			}
			
			if( (write) > (pop-deleteFail) ){
				//more pop
				pWrite = pWrite - 0.07;
			}
			
			if( (pop - deleteFail) > write ){
				//more write
				pWrite = pWrite + 0.07;
			}
			
			if(rand > (1.0 -pRead)){
				//read
				read(myQueue);
			}else{ 
				//read or write
				if(rand > pWrite){
					//pop
					pop(myQueue, queueId, othersId);
				}else{
					//write
					write(othersId, queueId);
				}
				
			}
		}
		
	}
	
	private void pop(int myQueue, int deleteQueue,int othersId){
		double ar = Math.random();
		if(ar < 0.001){
			//delete queue
			ClientMasterLogger.others.incrementAndGet();
			if(queueDelete == false){
				boolean s = deleteQueue(deleteQueue);
				if(s){
					queueDelete = true;
				}
			}
		}else if(ar<1.0/3.0){
			//pop specific
			pop++;
			ClientMasterLogger.logStats("pop");
			popMessageFromSpecificOtherClient(othersId);
		}else{
			//pop
			pop++;
			ClientMasterLogger.logStats("pop");
			popFromQueue(myQueue);
		}
	}

	private void read(int myQueue){
		ClientMasterLogger.logStats("read");
		readTopMostFromQueue(myQueue);
	}
	
	protected void write(int othersId, int queueId){
		ClientMasterLogger.logStats("write");
		double ar = Math.random();
		payload = createPayload(minPayload, maxPayload);
		
		if(ar > 1.0/3.0){
			//broadcast
			write++;
			ClientMasterLogger.logStats("write");
			sendBroadcast(queueId, payload);
		}else if(ar > 0.001){
			//send to specific client
			write++;
			ClientMasterLogger.logStats("write");
			sendMessageToSpecificOtherClient(othersId, queueId, payload);
		}else if(ar < 0.001){
			//create queue
			ClientMasterLogger.others.incrementAndGet();
			if(queueDelete == true){
				createNewQueue();
				queueDelete = false;
			}
		}
	}
}
