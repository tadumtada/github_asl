package middleware;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class MyConnection {
	Connection con;

	PreparedStatement broadcast = null;
	PreparedStatement regOldCl = null;
	PreparedStatement deRegCl = null;
	PreparedStatement regNewCl = null;
	PreparedStatement deleteQueue = null;
	PreparedStatement pop = null;
	PreparedStatement readTopMost = null;
	PreparedStatement popFromOther = null;
	PreparedStatement getQueuesForClient = null;
	PreparedStatement getQueueIds = null;
	PreparedStatement createNewQueue = null;
	PreparedStatement getClientIds = null;
	
	

	public MyConnection() {
		String url = "jdbc:postgresql://" + Controller.dbServerAddress + "/"
				+ Controller.dbName;
		try {
			con = DriverManager.getConnection(url, Controller.userName,
					Controller.dbPassword);
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			con.setAutoCommit(false);
			// make preparestatements

			broadcast = con.prepareStatement(Queries.insertMessage);
			regOldCl = con.prepareStatement(Queries.regOldCl);
			deRegCl = con.prepareStatement(Queries.deRegCl);
			regNewCl = con.prepareStatement(Queries.regNewCl);
			deleteQueue = con.prepareStatement(Queries.deleteQueue);
			pop = con.prepareStatement(Queries.pop);
			readTopMost = con.prepareStatement(Queries.readTopMost);
			popFromOther = con.prepareStatement(Queries.popFromOther);
			getQueuesForClient = con.prepareStatement(Queries.getQueuesForClient);
			getQueueIds = con.prepareStatement(Queries.getAllQueues);
			createNewQueue = con.prepareStatement(Queries.createNewQueue);
			getClientIds = con.prepareStatement(Queries.getClientIds);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
