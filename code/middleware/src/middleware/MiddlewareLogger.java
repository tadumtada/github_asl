package middleware;

import java.io.FileWriter;
import java.io.PrintWriter;

public class MiddlewareLogger {
	int arraySize;
	PrintWriter writer;
	String[] buffer;
	int counter = 0;

	public MiddlewareLogger(int threadPoolSize, String middlewareIP, int arraySize, String filePath, String version){
		long time = System.nanoTime();
		this.arraySize = arraySize;
		buffer = new String[arraySize];
		String fileName = filePath+"middleware_IP_"+middlewareIP+"_version_"+ version+"_time_"+time+".log";
		String metadata = "threadPoolSize:"+threadPoolSize+";middlewareIP"+middlewareIP+";version:"+version;
		String cols = "operation; startTimestamp; timeInQueue; timeInDB; afterSendToClient";
		try {
			writer = new PrintWriter(new FileWriter(fileName+".txt", true));
			//writer.println(metadata);
			//writer.println(cols);
			//writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void log(String logMessage) {
		buffer[counter] = logMessage;
		counter++;
		if (counter >= arraySize) {
			for (int i = 0; i < arraySize; i++) {
				writer.println(buffer[i]);
				writer.flush();
			}
			counter = 0;
		}

	}

}
