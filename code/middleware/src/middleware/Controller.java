package middleware;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



public class Controller {
	static ConnectionPool conPool;
	public static ExecutorService threadPoolExecutor;
	public static LinkedBlockingQueue<Runnable> queue;
	// has to be the same in the client
	static int serverPort = 9000;

	// db connection stuff
	public static String dataSourceName = "some name";
	public static String dbServerAddress = "localhost";
	public static String dbName = "asl";
	public static String userName = "user_asl";
	public static String dbPassword = "1234";
	public static int dbPort = 5432;

	public static MiddlewareLogger logger;

	// configurations (metadata)
	static int threadPoolSize;
	public static int numDbConnections;
	public static int numClients = 1;
	public static int loggerBufferSize = 500;
	public static String filePath = "../../logs/";
	public static String middlewareIP;
	public static String version;

	public static void main(String[] args) {
		//dbIp
		dbServerAddress = args[0];
		threadPoolSize = Integer.parseInt(args[1]);
		//has to be equal to all clientMasters together, needed for metadata
		middlewareIP = args[2];
		version = args[3];
		numDbConnections = threadPoolSize;
		
		init();

		// start accepting client connections
		try {
			new RequestAcceptor(new ServerSocket(serverPort)).start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected static void init() {
		System.out.println("started controller");
		logger = new MiddlewareLogger(threadPoolSize, middlewareIP, loggerBufferSize, filePath, version);
		conPool = new ConnectionPool();

		// initializes thread pool (& its queue)
		queue = new LinkedBlockingQueue<Runnable>();
		threadPoolExecutor = new ThreadPoolExecutor(threadPoolSize,
				threadPoolSize, 0, TimeUnit.MILLISECONDS, queue);
		

	}
}
