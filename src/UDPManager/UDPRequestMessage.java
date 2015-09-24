package UDPManager;

import java.io.*;
import java.net.*;

public class UDPRequestMessage {
	
	public InetAddress RequestSenderIPAddress;
	public int RequestSenderPort;
	public String RequestMessageString;
		
	/**
	 * Basic constructor
	 */
	public UDPRequestMessage()
	{
		
	}
	
	/**
	 * Constructs an (incoming) UDP request message with data from the received socket
	 * @param _Method
	 * @param _Username
	 */
	
	public UDPRequestMessage(DatagramPacket _RequestPacket)
	{
		RequestMessageString = new String(_RequestPacket.getData());
		RequestSenderIPAddress = _RequestPacket.getAddress();
	}
	
	/**
	 * Initializes an (outgoing) UDP request message
	 * @param _MessageData
	 */
	public UDPRequestMessage(String _MessageData)
	{
		RequestMessageString = new String(_MessageData);
	}
	
	/**
	 * Creates a byte array of the message data, correctly formatted and ready to transmit
	 * @return
	 */
	
	public byte[] GetFormattedMessageData()
	{
		return RequestMessageString.getBytes();
	}
}
