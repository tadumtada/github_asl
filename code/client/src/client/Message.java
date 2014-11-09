package client;


public class Message {

	protected String action;
	protected int clientId;
	protected int othersId;
	protected int queueId;
	protected int sequenceNumber;
	protected String payload;

	public Message(int clientId) {
		// read is default
		action = " ";
		this.clientId = clientId;
		othersId = 0;
		queueId = 0;
		payload = " ";

	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setOthersId(int othersId) {
		this.othersId = othersId;
	}

	public void setQueueId(int queueId) {
		this.queueId = queueId;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String toString() {
		String message = action + "\n" + clientId + "\n"
				+ Integer.toString(othersId) + "\n" + Integer.toString(queueId)
				+ "\n" + payload;
		return message;
	}
}
