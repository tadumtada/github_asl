package middleware;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class ConnectionPool {
	BlockingQueue<MyConnection> queue;
	public ConnectionPool(){
		this.queue = new ArrayBlockingQueue<MyConnection>(Controller.numDbConnections);
		for(int i=0;i<Controller.numDbConnections;i++){
			queue.offer(new MyConnection());
		}
	}
	
	public MyConnection getConnection(){
		MyConnection con = null;
		while(con == null){
			try {
				con = queue.poll(5000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return con;
	}
	
	public void returnConnection(MyConnection con){
		queue.offer(con);
	}

}
