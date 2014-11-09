package middleware;

public class Queries {

	
	public static String insertMessage = "WITH id AS(INSERT INTO metadata(message_id,queue_id,sender_id,receiver_id) "
			+ "VALUES (DEFAULT,?,?,?) RETURNING message_id) INSERT INTO messages(message_id, payload) "
			+ "SELECT message_id,? FROM id";

	public static String regOldCl = "UPDATE clients SET flag = TRUE WHERE client_id = ?; ";

	public static String deRegCl = "UPDATE clients SET flag = FALSE WHERE client_id = ?; ";

	public static String regNewCl = "INSERT INTO clients (client_id,flag) "
			+ "VALUES (DEFAULT,TRUE) RETURNING client_id;";

	public static String deleteQueue = "DELETE FROM queues WHERE queue_id = ? ;";


	
	public static String pop = "WITH id AS (DELETE FROM messages WHERE message_id IN "
			+ "(SELECT message_id FROM metadata WHERE queue_id = ? AND (receiver_id = ? OR receiver_id = 0) "
			+ "ORDER BY timestamp ASC LIMIT 1) RETURNING *)"
			+ "DELETE FROM metadata WHERE message_id = (SELECT message_id FROM id) RETURNING (SELECT payload FROM id);";

	public static String readTopMost = "SELECT payload FROM messages "
			+ "WHERE message_id = (SELECT message_id FROM metadata WHERE queue_id = ? "
			+ "AND (receiver_id = ? OR receiver_id = 0) "
			+ "ORDER BY timestamp ASC LIMIT 1) ;";
	

	public static String popFromOther = "WITH id AS (DELETE FROM messages WHERE message_id IN "
			+ "(SELECT message_id FROM metadata WHERE   (receiver_id = ? OR receiver_id = 0) AND sender_id = ?"
			+ "ORDER BY timestamp ASC LIMIT 1) RETURNING *)"
			+ "DELETE FROM metadata WHERE message_id = (SELECT message_id FROM id) RETURNING (SELECT payload FROM id);";

	public static String getQueuesForClient = "SELECT DISTINCT queue_id FROM metadata "
			+ "WHERE receiver_id = ? OR receiver_id = 0 ;";
	
	public static String getAllQueues = "SELECT queue_id FROM queues ;";
	
	public static String createNewQueue = "INSERT INTO queues (queue_id) "
			+ "VALUES (DEFAULT) RETURNING *;";

	public static String getClientIds = "SELECT client_id FROM clients WHERE flag = true;";

}
