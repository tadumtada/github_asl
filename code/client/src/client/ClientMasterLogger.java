package client;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientMasterLogger {
	static int arraySize;
	static PrintWriter opWriter;
	static PrintWriter statsWriter;
	static String[] buffer;
	static int counter = 0;
	public static AtomicInteger fail = new AtomicInteger();
	public static AtomicInteger write = new AtomicInteger();
	public static AtomicInteger pop = new AtomicInteger();
	public static AtomicInteger read = new AtomicInteger();
	public static AtomicInteger successGlobal = new AtomicInteger();
	public static AtomicInteger others = new AtomicInteger();
	public static long lastPeriodicUpdate;

	public ClientMasterLogger(String version, int arraySize, String filePath,
			String clientMachine, String middlewareIP) {
		fail.set(0);
		write.set(0);
		pop.set(0);
		read.set(0);
		successGlobal.set(0);
		others.set(0);
		lastPeriodicUpdate = System.currentTimeMillis();
		long time = System.nanoTime();
		ClientMasterLogger.arraySize = arraySize;
		buffer = new String[arraySize];

		
		String statsFileName = filePath + "_STATS__clientMasterMachine_"
				+ clientMachine +"_middlewareIP_"+middlewareIP+ "_version_" + version + "_time_" + time+".log";
		String statsCols = "total; write; pop; read; others; success; fail";
		
		String opsFileName = filePath + "_clientMasterMachine_"
				+ clientMachine +"_middlewareIP_"+middlewareIP+ "_version_" + version + "_time_" + time+".log";
		String metadata = "clientMaschine:"+clientMachine+ " ;middlewareIP:"+middlewareIP+" ;version:"+version;
		String opsCols = "operation; startTimestamp ; returnTime";
		
		try{
			statsWriter = new PrintWriter(new FileWriter(statsFileName + ".txt", true));
			//statsWriter.println(metadata);
			//statsWriter.println(statsCols);
			//statsWriter.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		try {
			opWriter = new PrintWriter(new FileWriter(
					opsFileName + ".txt", true));
			//opWriter.println(metadata);
			//opWriter.println(opsCols);
			//opWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}





	public synchronized static void logOp(String operation, long start, long stop, boolean success){
		
		buffer[counter] = operation+";"+start+";"+(stop-start)+";";
		if(success){
			successGlobal.incrementAndGet();
		}else{
			fail.incrementAndGet();
		}
		counter++;
		
		//write to file
		if(counter >=arraySize){
			for(int i=0;i<arraySize;i++){
				opWriter.println(buffer[i]);
				opWriter.flush();
			}
			counter = 0;
			start = System.currentTimeMillis();
			
		}
	}
	
	public synchronized static void logStats(String action){
		//stats for periodic logger
		if(action.equals("read")){
			read.incrementAndGet();
		}else if(action.equals("pop")){
			pop.incrementAndGet();
		}else if(action.equals("write")){
			write.incrementAndGet();
		}else{
			others.incrementAndGet();
		}
	}
	
	public synchronized static void writeStats(String message){
		statsWriter.println(message);
		statsWriter.flush();
	}
	


}
