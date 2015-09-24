import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

public class UDPMessage {
	
	public InetAddress SenderIPAddress;
	public int SenderPort;
	public byte[] MessageData;
		
	/**
	 * Basic constructor
	 */
	public UDPMessage()
	{
		
	}
	
	/**
	 * Constructs an (incoming) UDP request message with data from the received socket
	 * @param _Method
	 * @param _Username
	 * @throws UnsupportedEncodingException 
	 */
	public UDPMessage(DatagramPacket _RequestPacket) throws UnsupportedEncodingException
	{
		MessageData = _RequestPacket.getData();
		SenderIPAddress = _RequestPacket.getAddress();
		SenderPort = _RequestPacket.getPort();
	}
	
	
	/**
	 * Initializes an (outgoing) UDP request message
	 * @param _MessageData
	 */
	public UDPMessage(byte[] _MessageData)
	{
		MessageData = _MessageData;
	}
	
}
