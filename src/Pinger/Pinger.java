package Pinger;

import java.io.*; 
import java.net.*;
import java.util.Arrays;


import UDPManager.*;

public class Pinger {

	/**
	 * Global variables and default values
	 */
	static boolean IsClient = false;
	static int LocalPort;
	static int PacketCount;
	static final int PingerMessageLength = 12;
	static int RemoteHostPort;
	static InetAddress RemoteHostAddress;
	static UDPManager ClientUDPManager;
	static UDPManager ServerUDPManager;	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		// First, check if too many arguments were included! If so, write error and exit
		if(args.length > 8)
		{
			System.out.println("Error: missing or additional arguments");
			return;
		}
		
		// Read command line arguments and set class variables
		for(int arg = 0; arg < args.length; arg++)
		{
			if(args[arg].equals("-c"))
			{
				try
				{
					IsClient = true;
					PacketCount = Integer.parseInt(args[arg+1]);
				}
				catch(Exception e)
				{
					System.out.println("Error: missing or additional arguments");
					return;
				}
			}
			if(args[arg].equals("-l"))
			{
				try
				{
					LocalPort = Integer.parseInt(args[arg+1]);
				}
				catch(Exception e)
				{
					System.out.println("Error: missing or additional arguments");
					return;
				}
			}
			if(args[arg].equals("-r"))
			{
				try
				{
					RemoteHostPort = Integer.parseInt(args[arg+1]);
				}
				catch(Exception e)
				{
					System.out.println("Error: missing or additional arguments");
					return;
				}
			}
			if(args[arg].equals("-h"))
			{
				try
				{
					RemoteHostAddress = InetAddress.getByName(args[arg+1]);
				}
				catch(Exception e)
				{
					System.out.println("Error: invalid remote host address");
					return;
				}
			}			
		}
		
		// Check if we are client or server. Then check if we have command line arguments we need. If so, start client or server.
		if(IsClient)
		{
			if(LocalPort == 0 || RemoteHostPort == 0 || PacketCount == 0 || RemoteHostAddress == null)
			{
				System.out.println("Error: missing or additional arguments");
				return;
			}
			StartPingerClient();
		}
		else
		{
			if(LocalPort == 0)
			{
				System.out.println("Error: missing or additional arguments");
				return;
			}
			SetupPingerServer();
		}
	}
	
	/**
	 * Start running the Pinger client, sending requests to the server specified in class variables
	 * @throws Exception
	 */
	static private void StartPingerClient() throws Exception
	{
		ClientUDPManager = new UDPManager();
		double AverageResponseTime;
		double MaximumResponseTime = 0;
		double MinimumResponseTime = 1000;
		double PercentLostPackets;
		double SumResponseTimes = 0;
		int PingerResponseSequence;
		int ReceivedPackets = 0;
		long PingerEndTime;
		long PingerStartTime;
		long PingerResponseTime;
		String PingerResponseTimestamp;
		UDPRequestMessage PingerRequestMessage;
		UDPResponseMessage PingerResponseMessage;
		
		// Start sending ping messages once every second
		for(int i = 1; i <= PacketCount; i ++)
		{
			// Send the ping request
			PingerRequestMessage = new UDPRequestMessage(GeneratePingerMessage(i));
			ClientUDPManager.SendRequestMessage(PingerRequestMessage, RemoteHostAddress, RemoteHostPort);
			
			// Now close the UDP manager reopen it bound to a port
			ClientUDPManager.CloseSocket();
			ClientUDPManager = new UDPManager(LocalPort, 1000);
			
			// Wait maximum one second for the response
			try
			{
				PingerResponseMessage = ClientUDPManager.ReceiveResponseMessage(PingerMessageLength);
				if(PingerResponseMessage == null)
				{
					System.out.println("Timeout occurred.");
				
				}
				else
				{
					PingerStartTime = 0; // reset to 0
					PingerEndTime = System.currentTimeMillis();
					
					// Get the response sequence number
					PingerResponseSequence = Integer.parseInt(PingerResponseMessage.ResponseMessageData.substring(0,4));
					
					// Decode the binary string representation of start timestamp
					PingerResponseTimestamp = PingerRequestMessage.RequestMessageString.substring(4);
					for(int b = 0; b < 8; b ++)
					{
						PingerStartTime += PingerResponseTimestamp.charAt(b) * Math.pow(256, (PingerResponseTimestamp.length() - 1 - b));
					}
					PingerResponseTime = PingerEndTime - PingerStartTime;
					
					// Debug: display the incoming timestamp in binary
					/*
					System.out.print("Incoming timestamp:\t\t");
					for(int b = 0; b < 8; b ++)
					{
						System.out.print(String.format("%8s", Integer.toBinaryString(PingerResponseTimestamp.charAt(b))).replace(" ", "0") + " ");	
					}
					System.out.print("\n");
					*/
					
					// Gather statistics
					ReceivedPackets++;
					SumResponseTimes += PingerResponseTime;
					if(PingerResponseTime > MaximumResponseTime)
					{
						MaximumResponseTime = PingerResponseTime;
					}
					if(PingerResponseTime < MinimumResponseTime)
					{
						MinimumResponseTime = PingerResponseTime;
					}
					
					
					// Display results for this request
					//System.out.println("Received " + PingerResponseMessage.ResponseMessageData.length() + " bytes");
					System.out.println("size=12 from=" + RemoteHostAddress.getHostAddress() + " seq=" + PingerResponseSequence + " rtt=" + PingerResponseTime + " ms");
					
					// Now wait for a second before next request
					Thread.sleep(1000);
					
				}
			}
			catch(Exception e)
			{
				System.out.println("Caught exception waiting for response: " + e.getMessage());
			}
		}
		
		// Finally, display all statistics
		AverageResponseTime = (double)SumResponseTimes / (double)ReceivedPackets;
		PercentLostPackets = (PacketCount - ReceivedPackets) * 100 / PacketCount;
		System.out.format("sent=%d received=%d lost=%.1f%% rtt min/avg/max=%.3f/%.3f/%.3f ms\n", PacketCount, ReceivedPackets, PercentLostPackets, MinimumResponseTime, AverageResponseTime, MaximumResponseTime);
		
		
	}
	
	/**
	 * Start the Pinger server, which receives requests and then sends response messages
	 * @throws Exception
	 */
	static private void SetupPingerServer() throws Exception
	{
		int PingerRequestSequence;
		long PingerRequestTime;
		String PingerRequestTimestamp;
		UDPRequestMessage PingerRequestMessage;
		
		while(true)
		{
			PingerRequestTime = 0; // reset to 0
			
			// Receive request message
			ServerUDPManager = new UDPManager(LocalPort);
			PingerRequestMessage = ServerUDPManager.ReceiveRequestMessage(PingerMessageLength);
			PingerRequestSequence = Integer.parseInt(PingerRequestMessage.RequestMessageString.substring(0,4));
			PingerRequestTimestamp = PingerRequestMessage.RequestMessageString.substring(4);
			
			// Decode the timestamp
			//System.out.print("Incoming timestamp:\t\t");
			for(int b = 0; b < PingerRequestTimestamp.length(); b ++)
			{
				PingerRequestTime += PingerRequestTimestamp.charAt(b) * Math.pow(256, (PingerRequestTimestamp.length() - 1 - b));
				//System.out.print(String.format("%8s", Integer.toBinaryString(PingerRequestTimestamp.charAt(b))).replace(" ", "0") + " ");	
			}
			
			
			//System.out.println("Received string = " + PingerRequestMessage.RequestMessageString.length() + " bytes, bytes[] = " + PingerRequestMessage.RequestMessageBytes.length);
			System.out.format("time=%d from=%s seq=%d\n", PingerRequestTime, PingerRequestMessage.RequestSenderIPAddress.getHostAddress(), PingerRequestSequence);
			
			// Now close socket, reopen it, and send response message
			ServerUDPManager.CloseSocket();
			ServerUDPManager = new UDPManager();
			UDPResponseMessage PingerResponseMessage = new UDPResponseMessage(PingerRequestMessage.RequestMessageString);
			ServerUDPManager.SendResponseMessage(PingerResponseMessage, PingerRequestMessage.RequestSenderIPAddress, RemoteHostPort);
		}
	}
	
	/**
	 * Builds a 12-byte Pinger message, with first four bytes denoting sequence number and last eight bytes as timestamp
	 * @param _SequenceNumber
	 * @return
	 */
	static private String GeneratePingerMessage(int _SequenceNumber)
	{
		byte TimestampByteArray[] = new byte[8];
		int SequenceNumber = _SequenceNumber;
		long CurrentTime;
		String CurrentTimeBinaryString;
		String PingerMessage;
		
		// First, ensure that SequenceNumber is between 1 and 9999
		SequenceNumber = SequenceNumber % 10000;
		PingerMessage = String.format("%04d", SequenceNumber);
		
		// Now append a timestamp to the message, encoded into an 8-byte char string
		CurrentTime = System.currentTimeMillis();
		CurrentTimeBinaryString = String.format("%64s", Long.toBinaryString(CurrentTime)).replace(" ", "0");
		for(int i = 0; i < 8; i ++)
		{
			int ThisCharValue = Integer.parseInt(CurrentTimeBinaryString.substring(8*i, (8*(i+1))), 2);
			PingerMessage += (char)ThisCharValue;
			//System.out.println("[" + i + "]=" + ThisCharValue);
		}
		
		// Debug: display the timestamp in binary
		/*
		System.out.print("Outgoing timestamp:\t\t");
		for(int i = 0; i < 8; i ++)
		{
			int ThisCharValue = Integer.parseInt(CurrentTimeBinaryString.substring(8*i, (8*(i+1))), 2);
			System.out.print(String.format("%8s", Integer.toBinaryString(ThisCharValue)).replace(" ", "0") + " ");
		}
		System.out.print("\n");
		*/
		
		// All done!
		return PingerMessage;
	}

}
