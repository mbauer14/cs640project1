package UDPManager;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class UDPResponseMessage {

	public String ResponseMessageData;
	public InetAddress ResponseSenderIPAddress;
	public int ResponseSenderPort;
	
	/**
	 * Basic UDPResponseMessage constructor, creates empty object
	 */
	public UDPResponseMessage()
	{
	
	}
	
	
	/**
	 * Constructs an (incoming) UDP response message with data from the received socket
	 * @param _Method
	 * @param _Username
	 */
	public UDPResponseMessage(DatagramPacket _ResponsePacket)
	{
		ResponseMessageData = new String(_ResponsePacket.getData());
		ResponseSenderIPAddress = _ResponsePacket.getAddress();
	}
	
	/**
	 * Initializes an (outgoing) UDP response message
	 * @param _MessageData
	 */
	public UDPResponseMessage(String _MessageData)
	{
		ResponseMessageData = new String(_MessageData);
	}
	
	/**
	 * Creates a byte array of the message data, correctly formatted and ready to transmit
	 * @return
	 */
	
	public byte[] GetFormattedMessageData()
	{
		return ResponseMessageData.getBytes();
	}
	
}
