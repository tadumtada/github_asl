package client;

public class PeriodicLogger implements Runnable{
	long start;
	double read;
	double write;
	double pop;
	double fail;
	double success;
	double total;
	double others;

	@Override
	public void run() {
		read = ClientMasterLogger.read.getAndSet(0);
		write = ClientMasterLogger.write.getAndSet(0);
		pop = ClientMasterLogger.pop.getAndSet(0);
		fail = ClientMasterLogger.fail.getAndSet(0);
		others = ClientMasterLogger.others.getAndSet(0);
		success = ClientMasterLogger.successGlobal.getAndSet(0);
		start = System.currentTimeMillis();
		long intervall = start-ClientMasterLogger.lastPeriodicUpdate;
		total =(int)((((double)(read + write + pop)) / intervall)*1000);
		read = (int)((((double)(read)) / intervall)*1000);
		write =(int)((((double)(write)) / intervall)*1000);
		pop = (int)((((double)(pop)) / intervall)*1000);
		others = (int)((((double)(others)) / intervall)*1000); 
		fail = (int)((((double)(fail)) / intervall)*1000);
		success = (int)((((double)(success)) / intervall)*1000);
		//write to file
		ClientMasterLogger.writeStats(total+";"+write+";"+pop+";"+read+";"+others+";"+success+";"+fail);
		ClientMasterLogger.lastPeriodicUpdate = start;
	}

}
