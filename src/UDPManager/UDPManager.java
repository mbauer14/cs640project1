package UDPManager;

import java.io.*; 
import java.lang.*;
import java.net.*;
import java.util.*;
import UDPManager.*;

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
	 * @throws Exception
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
	public void SendRequestMessage(UDPRequestMessage _Message, InetAddress _DestinationIPAddress, int _DestinationPort) throws Exception
	{
		// Function variables
		DatagramPacket SendPacket;
		
		// Send the request message
		//System.out.println("[UDPManager] Sending request message (" + _Message.RequestMessageData + ") to " + _DestinationIPAddress.toString() + ":" + _DestinationPort);
		SendPacket = new DatagramPacket(_Message.GetFormattedMessageData(), _Message.GetFormattedMessageData().length, _DestinationIPAddress, _DestinationPort);
		UDPSocket.send(SendPacket);
		//System.out.println("[UDPManager] Finished sending request.");
		
		// Exit
		SendPacket = null;
		return;
	}
	
	/**
	 * Sends a UDP response message over a socket. This must be sent in response to a request message.
	 * @param _Message Message data
	 * @param _DestinationIPAddress Destination host IP address
	 * @param _DestinationPort Destination port
	 * @throws Exception
	 */
	public void SendResponseMessage(UDPResponseMessage _Message, InetAddress _DestinationIPAddress, int _DestinationPort) throws Exception
	{
		// Function variables
		DatagramPacket SendPacket;
		
		// Send the response message
		//System.out.println("[UDPManager] Sending response message (" + _Message.ResponseMessageData + ") to " + _DestinationIPAddress.toString() + ":" + _DestinationPort);
		SendPacket = new DatagramPacket(_Message.GetFormattedMessageData(), _Message.GetFormattedMessageData().length, _DestinationIPAddress, _DestinationPort);
		UDPSocket.send(SendPacket);
		//System.out.println("[UDPManager] Finished sending response.");
		
		// Exit
		SendPacket = null;
		return;
	}
	
	/**
	 * Listens and waits for a UDP request message. Once received, returns the message data.
	 * @return Message data from the UDP request
	 * @throws Exception
	 */
	public UDPRequestMessage ReceiveRequestMessage(int _MessageLength) throws Exception
	{
		// Function variables
		byte[] ReceiveData = new byte[_MessageLength];
		DatagramPacket ReceivePacket;
		UDPRequestMessage RequestMessage;

		
		// Initialize the packet which will receive the message
		Arrays.fill(ReceiveData, (byte)' ');
		ReceivePacket = new DatagramPacket(ReceiveData, _MessageLength);
		
		// Now wait for the message to arrive
		UDPSocket.receive(ReceivePacket);
		//System.out.println("[UDPManager] Request received from " + ReceivePacket.getAddress() + ":" + ReceivePacket.getPort() + ", " + new String(ReceiveData) + " (" + ReceivePacket.getData().length + " bytes)");
		RequestMessage = new UDPRequestMessage(ReceivePacket);
		
		// Return the message
		return RequestMessage;
	
	}
	
	/**
	 * Listens and waits for a UDP response message. Once received, returns the message data.
	 * @return Message data from the UDP response
	 * @throws Exception
	 */
	public UDPResponseMessage ReceiveResponseMessage(int _MessageLength) throws Exception
	{
		// Function variables
		byte[] ReceiveData = new byte[_MessageLength];
		DatagramPacket ReceivePacket;
		UDPResponseMessage ResponseMessage;
		
		// Initialize the packet which will receive the message
		Arrays.fill(ReceiveData, (byte)' ');
		ReceivePacket = new DatagramPacket(ReceiveData, _MessageLength);
		
		// Now wait for the message to arrive...
		//System.out.println("[UDPManager] Waiting for response on port " + SocketPort + "...");
		try
		{
			UDPSocket.receive(ReceivePacket);
		}
		catch (SocketTimeoutException e) 
		{
			//System.out.println("[UDPManager] Receive response timed out.");
			return null;
		}
		
		//System.out.println("[UDPManager] Received response from " + ReceivePacket.getAddress() + ":" + ReceivePacket.getPort() + ", " + new String(ReceiveData) + " (" + ReceivePacket.getData().length + " bytes)");
		ResponseMessage = new UDPResponseMessage(ReceivePacket);
		
		// Return the message
		return ResponseMessage;
	
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
