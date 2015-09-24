import java.io.*; 
import java.lang.*;
import java.net.*;
import java.util.*;

public class UDPManager {

	private static DatagramSocket UDPSocket;
	private static int SocketPort = 0;
	private static int SocketTimeout; // default 1 second timeout
	
	/**
	 * Constructor for UDP clients, which do not bind a port
	 * @throws Exception
	 */
	public UDPManager() throws Exception
	{
		UDPSocket = new DatagramSocket();
	}
	
	/**
	 * Constructor for UDP servers, which do bind a port
	 * @param _IPAddress Socket IP address
	 * @param _Port Socket port
	 * @throws Exceptionu
	 */
	public UDPManager(int _Port) throws Exception
	{
		SocketPort = _Port;
		UDPSocket = new DatagramSocket(SocketPort);
	}
	
	/**
	 * Constructor for UDP servers, which do bind a port. This constructor also enforces a timeout.
	 * @param _IPAddress Socket IP address
	 * @param _Port Socket port
	 * @throws Exception
	 */
	public UDPManager(int _Port, int _Timeout) throws Exception
	{
		SocketPort = _Port;
		SocketTimeout = _Timeout;
		UDPSocket = new DatagramSocket(SocketPort);
		UDPSocket.setSoTimeout(SocketTimeout);
	}
	
	/**
	 * Sends a UDP request message over a socket.
	 * @param _Message Message data
	 * @param _DestinationIPAddress Destination host IP address
	 * @param _DestinationPort Destination host port
	 * @throws Exception
	 */
	public void SendMessage(UDPMessage _Message, InetAddress _DestinationIPAddress, int _DestinationPort) throws Exception
	{
		// Function variables
		DatagramPacket SendPacket;
		
		//System.out.println("SendMessage._Message.MessageData.length" + _Message.MessageData.length);
		//System.out.println("SendMessage._DestinationIPAddress" + _DestinationIPAddress);
		//System.out.println("SendMessage._DestinationPort" + _DestinationPort);
		
		SendPacket = new DatagramPacket(_Message.MessageData, _Message.MessageData.length, _DestinationIPAddress, _DestinationPort);
		UDPSocket.send(SendPacket);
		
		// Exit
		SendPacket = null;
		return;
	}
		
	/**
	 * Listens and waits for a UDP request message. Once received, returns the message data.
	 * @return Message data from the UDP request
	 * @throws Exception
	 */
	public UDPMessage ReceiveMessage(int _MessageLength) throws Exception
	{
		// Function variables
		byte[] ReceiveData = new byte[_MessageLength];
		DatagramPacket ReceivePacket;
		UDPMessage ReceiveMessage;
	
		//Create a datagram packet which will receive on UDP socket	
		ReceivePacket = new DatagramPacket(ReceiveData, _MessageLength);
		
		try
		{
			//Get the actual data
			UDPSocket.receive(ReceivePacket);
		}
		catch (SocketTimeoutException e) 
		{
			System.out.println("[UDPManager] Receive response timed out: " + e + ".");
			return null;
		}

		ReceiveMessage = new UDPMessage(ReceivePacket);
		
		// Return the message
		return ReceiveMessage;
	
	}
	
	/**
	 * Resets a UDP socket by closing it and then rebinding it
	 * @throws Exception
	 */
	public void ResetSocket() throws Exception
	{
		UDPSocket.close();
		UDPSocket = new DatagramSocket(SocketPort);
	}
	
	/**
	 * Close the UDP socket
	 */
	public void CloseSocket()
	{
		UDPSocket.close();
	}
	
	
}
