package middleware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Handler implements Runnable {
	/*
	 * 
	 * actions: see in client class for meanings
	 * 
	 * message format: see in client class
	 */

	Socket clientSocket;
	MyConnection myCon = null;
	long start = 0;
	long stop = 0;
	long beforeDBReq = 0;
	long afterDBReq = 0;
	BufferedReader in;
	

	public Handler(Socket socket, long time) {
		this.clientSocket = socket;
		start = time;
	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			String action;
			String clientId;
			String othersId;
			String queueId;
			String payload;
			if ((action = in.readLine()) != null) {
				if ((clientId = in.readLine()) != null) {
					if ((othersId = in.readLine()) != null) {
						if ((queueId = in.readLine()) != null) {
							if ((payload = in.readLine()) != null) {
								query(action, Integer.parseInt(clientId),
										Integer.parseInt(othersId),
										Integer.parseInt(queueId), payload);
							}
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				clientSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	protected void query(String action, int clientId, int othersId,
			int queueId, String payload) {
		try{
			myCon = Controller.conPool.getConnection();
			//client stuff
			beforeDBReq = System.nanoTime();
			
			
			// register new client
			if (action.equals("rnc")) {
				ResultSet res = myCon.regNewCl.executeQuery();
				myCon.con.commit();
				afterDBReq = System.nanoTime();
				if (res.next()) {
					genericWriteToClient(res.getString(1));
				}
				res.close();
			}
			
			// register old client (rejoin)
			if (action.equals("roc")) {
				myCon.regOldCl.clearParameters();
				myCon.regOldCl.setInt(1, clientId);
				myCon.regOldCl.executeUpdate();
				myCon.con.commit();
				afterDBReq = System.nanoTime();
			}

			// deregister client
			if (action.equals("dc")) {
				myCon.deRegCl.clearParameters();
				myCon.deRegCl.setInt(1, clientId); 
				myCon.deRegCl.executeUpdate();
				myCon.con.commit();
				afterDBReq = System.nanoTime();
			}
			
			
			
			
			

			// call dependent on action

			// send message to specific other client
			if (action.equals("so")) {
				// queueId, senderId, receiverId, sequenceNumber, payload
				myCon.broadcast.clearParameters();
				myCon.broadcast.setInt(1, queueId);
				myCon.broadcast.setInt(2, clientId);
				myCon.broadcast.setInt(3, othersId);
				myCon.broadcast.setString(4, payload);
				myCon.broadcast.executeUpdate();
				myCon.con.commit();
				afterDBReq = System.nanoTime();
			}

			// send broadcast to queue
			if (action.equals("sb")) {
				// queueId, senderId, receiverId, sequenceNumber, payload 
				myCon.broadcast.clearParameters();
				myCon.broadcast.setInt(1, queueId);
				myCon.broadcast.setInt(2, clientId);
				myCon.broadcast.setInt(3, 0);
				myCon.broadcast.setString(4, payload);
				myCon.broadcast.executeUpdate();
				myCon.con.commit();
				afterDBReq = System.nanoTime();
			}



			// delete queue
			if (action.equals("dq")) {
				//give the queue twice
				try{
					myCon.deleteQueue.clearParameters();
					myCon.deleteQueue.setInt(1, queueId);
					int i = myCon.deleteQueue.executeUpdate();
					myCon.con.commit();
					afterDBReq = System.nanoTime();
					if(i>0){
						genericWriteToClient("true");
					}else{
						genericWriteToClient("false");
					}
				}catch(SQLException e){
					myCon.con.rollback();
					afterDBReq = System.nanoTime();
					genericWriteToClient("false");
					//do nothing because happens very often
				}
			}

			// pop message from queue
			if (action.equals("pq")) {
				//queue, clientId
				try{
					myCon.pop.clearParameters(); 
					myCon.pop.setInt(1, queueId);
					myCon.pop.setInt(2, clientId);
					ResultSet res = myCon.pop.executeQuery();
					myCon.con.commit();
					afterDBReq = System.nanoTime();
					if (res.next()) {
							genericWriteToClient(res.getString(1));
					}
					res.close();
				}catch(SQLException e){
					genericWriteToClient("-1");
					e.printStackTrace();
				}
			}

			// read top most message from queue
			if (action.equals("rq")) {
				//queueId, clientId
				myCon.readTopMost.clearParameters();
				myCon.readTopMost.setInt(1, queueId);
				myCon.readTopMost.setInt(2, clientId);
				ResultSet res = myCon.readTopMost.executeQuery();
				afterDBReq = System.nanoTime();
				if (res.next()) {
					genericWriteToClient(res.getString(1));
				}else{
					genericWriteToClient("");
				}
				res.close();
			}

			// pop message from specific other client
			if (action.equals("ro")) {
				//clientId, othersId
				try{
					myCon.popFromOther.clearParameters();
					myCon.popFromOther.setInt(1, clientId);
					myCon.popFromOther.setInt(2, othersId);
					ResultSet res = myCon.popFromOther.executeQuery();
					myCon.con.commit();
					afterDBReq = System.nanoTime();
					if (res.next()) {
						genericWriteToClient(res.getString(1));
					}
					res.close();
				}catch(SQLException e){
					genericWriteToClient("-1");
					e.printStackTrace();
				}
					
			}

			// get queues with messages for the client asking
			if (action.equals("gq")) {
				//clientId
				myCon.getQueuesForClient.clearParameters();
				myCon.getQueuesForClient.setInt(1, clientId);
				ResultSet res = myCon.getQueuesForClient.executeQuery();
				afterDBReq = System.nanoTime();
				String message = "0";
				while(res.next()){
					message = message+";"+res.getString(1);
				}
				genericWriteToClient(message);
				res.close();
			}

			// get all queueIds
			if (action.equals("gaq")) {
				myCon.getQueueIds.clearParameters();
				ResultSet res = myCon.getQueueIds.executeQuery();
				afterDBReq = System.nanoTime();
				String message = "0";
				while(res.next()){
					message = message+";"+res.getString(1);
				}
				genericWriteToClient(message);
				res.close();
				
			}
			
			// get all active clients
			if(action.equals("gac")){
				myCon.getQueueIds.clearParameters();
				ResultSet res = myCon.getClientIds.executeQuery();
				afterDBReq = System.nanoTime();
				String message = "0";
				while(res.next()){
					message = message +";"+res.getString(1);
				}
				genericWriteToClient(message);
				res.close();
			}

			// create new queue
			if (action.equals("cq")) {
				ResultSet res = myCon.createNewQueue.executeQuery();
				myCon.con.commit();
				afterDBReq = System.nanoTime();
				if (res.next()) {
					genericWriteToClient(res.getString(1));
				}
				res.close();
			}


		} catch (Exception e) {
			e.printStackTrace();
			try {
				myCon.con.rollback();
				afterDBReq = System.nanoTime();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			Controller.conPool.returnConnection(myCon);
			try {
				in.close();
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//logging action:
			stop = System.nanoTime();
			String logMessage = action+";"+Long.toString(start)+";"
							+Long.toString(beforeDBReq-start)+";"
							+Long.toString(afterDBReq-beforeDBReq)+";"+Long.toString(stop-afterDBReq);
			Controller.logger.log(logMessage);
			


		}

	}

	protected void genericWriteToClient(String message) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			out.println(message);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try{
				out.close();
				in.close();
				clientSocket.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}





}
