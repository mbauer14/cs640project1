import java.io.*; 
import java.net.*;
import java.util.Arrays;

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
		//Create a new UDP Manager that will connect on the specified port
		ClientUDPManager = new UDPManager(LocalPort, 1000);
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
		UDPMessage PingerRequestMessage;
		UDPMessage PingerResponseMessage;
		
		// Start sending ping messages once every second
		for(int i = 1; i <= PacketCount; i ++)
		{
			// Send the ping request
			PingerRequestMessage = new UDPMessage(GeneratePingerMessage(i));
			ClientUDPManager.SendMessage(PingerRequestMessage, RemoteHostAddress, RemoteHostPort);
			
			// Wait maximum one second for the response
			try
			{
				PingerResponseMessage = ClientUDPManager.ReceiveMessage(PingerMessageLength);
				if(PingerResponseMessage == null)
				{
					System.out.println("Timeout occurred.");
				
				}
				else
				{
					PingerStartTime = 0; // reset to 0
					PingerEndTime = System.currentTimeMillis();
					
					// Get the response sequence number
					PingerResponseSequence = DecodeSeqBytes(PingerResponseMessage.MessageData);
					// Decode the binary string representation of start timestamp
					PingerStartTime = DecodeTimestampBytes(PingerRequestMessage.MessageData);
					
					//System.out.println("starttime=" + PingerStartTime + "\n stoptime=" + PingerEndTime);
					PingerResponseTime = PingerEndTime - PingerStartTime;
					// Gather statistics
					ReceivedPackets++;
					SumResponseTimes += PingerResponseTime;
					
					if(PingerResponseTime > MaximumResponseTime)
						MaximumResponseTime = PingerResponseTime;
					
					if(PingerResponseTime < MinimumResponseTime)
						MinimumResponseTime = PingerResponseTime;
					
					// Display results for this request
					//System.out.println("Received " + PingerResponseMessage.ResponseMessageData.length() + " bytes");
					System.out.println("size=" + PingerResponseMessage.MessageData.length + " from=" + RemoteHostAddress.getHostAddress() + " seq=" + PingerResponseSequence + " rtt=" + PingerResponseTime + " ms");
					
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
		long PingerRequestTimestamp;
		UDPMessage PingerRequestMessage;
		
		ServerUDPManager = new UDPManager(LocalPort);
		
		while(true)
		{
			PingerRequestTime = 0; // reset to 0
			
			// Receive request message
			PingerRequestMessage = ServerUDPManager.ReceiveMessage(PingerMessageLength);
			//First 4 bytes are sequence number, extract and decode
			PingerRequestSequence = DecodeSeqBytes(PingerRequestMessage.MessageData);
			//Last 8 bytes are timestamp, extract and decode
			PingerRequestTime = DecodeTimestampBytes(PingerRequestMessage.MessageData);

			System.out.format("time=%d from=%s seq=%d\n", PingerRequestTime, PingerRequestMessage.SenderIPAddress.getHostAddress(), PingerRequestSequence);
			
			// Now close socket, reopen it, and send response message
			UDPMessage PingerResponseMessage = new UDPMessage(PingerRequestMessage.MessageData);
			ServerUDPManager.SendMessage(PingerResponseMessage, PingerRequestMessage.SenderIPAddress, PingerRequestMessage.SenderPort);
		}
	}
	
	/**
	 * Builds a 12-byte Pinger message, with first four bytes denoting sequence number and last eight bytes as timestamp
	 * @param _SequenceNumber
	 * @return
	 */
	static private byte[] GeneratePingerMessage(int _SequenceNumber)
	{

		byte[] totalMessage = new byte[12];	
		GenerateSeqBytes(_SequenceNumber, totalMessage);
		GenerateTimestampBytes(System.currentTimeMillis(), totalMessage);

		// All done!
		return totalMessage;
	}
	
	
	/**
	 * Builds a 12-byte Pinger message, with first four bytes denoting sequence number and last eight bytes as timestamp
	 * @param _SequenceNumber
	 * @return
	 */
	static private void GenerateSeqBytes(int _SequenceNumber, byte[] seqBytes)
	{
		int SequenceNumber = _SequenceNumber;
		int currCharNum = 0;
		//seqBytes holds the entire 12 byte message, only modify first 4 bytes	
		//Converts the int sequency number to bytes 
		//generate in reverse order so decode process is simpler
		for (int i=3; i>=0; i--)
		{
			
			currCharNum = SequenceNumber % 256;
			SequenceNumber /= 256;
			seqBytes[i] = (byte)currCharNum;
		}
		
		return;	
		
	}
	
	static private int DecodeSeqBytes(byte[] seqBytes)
	{
		int SequenceNumber = 0;
		
		//gets each byte in sequence number, adds on to total with multiple of 256
		for (int i=0; i<4; i++)
		{
			SequenceNumber *= 256;
			//Convert to positive number (bytes are signed), but we want unsigned vals
			SequenceNumber += (int)seqBytes[i] & 0xFF;
		}
		
		return SequenceNumber;	
		
	}
	
	
	static private void GenerateTimestampBytes(long _timeStampNum, byte[] tsBytes)
	{
		//seqBytes holds the entire 12 byte message, only modify last 8 bytes	
		//Converts the int timestamp number to bytes 
		//generate in reverse order so decode process is simpler
		long TimeStampNum = _timeStampNum;
		long CurrentTime;
	
		int currCharNum = 0;
		//	
		for (int i=11; i>=4; i--)
		{
			currCharNum = (int)(TimeStampNum % 256);
			TimeStampNum /= 256;
			tsBytes[i] = (byte) currCharNum;
		}
		
		return;	
		
	}
	
	static private long DecodeTimestampBytes(byte[] tsBytes)
	{
		long TimeStampNum = 0;
		
		//gets each byte in timestamp, adds on to total with correct multiple of 256
		for (int i=4; i<12; i++)
		{
			TimeStampNum *= 256;
			//Convert to positive number (bytes are signed), but we want unsigned vals
			TimeStampNum += (long)tsBytes[i] & 0xFF;
		}
		
		return TimeStampNum;	
	}

}
