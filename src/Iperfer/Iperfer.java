package Iperfer;

import java.io.*; 
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;


public class Iperfer {

	/**
	 * Global variables and default values
	 */
	static boolean IsClient = false;
	static boolean IsServer = false;
	static int Port;
	static int SecondCount;
	static InetAddress RemoteHostAddress;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		// First, check if too many arguments were included! If so, write error and exit
		if(args.length > 7)
		{
			System.out.println("Error: missing or additional arguments");
			return;
		}
		
		// Read command line arguments and set class variables
		for(int arg = 0; arg < args.length; arg++)
		{
			if(args[arg].equals("-c"))
			{
				IsClient = true;
			}
			if(args[arg].equals("-s"))
			{
				IsServer = true;
			}
			if(args[arg].equals("-t"))
			{
				try
				{
					SecondCount = Integer.parseInt(args[arg+1]);
				}
				catch(Exception e)
				{
					System.out.println("Error: missing or additional arguments");
					return;
				}
			}
			if(args[arg].equals("-p"))
			{
				try
				{
					Port = Integer.parseInt(args[arg+1]);
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
		
		// Can't be both client and server, can't be neither 
		if ((IsClient && IsServer) || (!IsClient && !IsServer))
		{
			System.out.println("Error: missing or additional arguments");
			return;
		}
		
		
		// Check if we are client or server. Then check if we have command line arguments we need. If so, start client or server.
		if(IsClient)
		{
			if(Port == 0 || SecondCount == 0 || RemoteHostAddress == null)
			{
				System.out.println("Error: missing or additional arguments");
				return;
			}
			StartIperfClient();
		}
		else
		{
			if(Port == 0)
			{
				System.out.println("Error: missing or additional arguments");
				return;
			}
			StartIperfServer();
		}
	}
	
	/**
	 * Start running the Pinger client, sending requests to the server specified in class variables
	 * @throws Exception
	 */
	static private void StartIperfClient() throws Exception
	{
		long sentBytes = 0;
		float secondLimit = (float)SecondCount;
		long startTime = 0;
		long currTime = 0;
		float currSeconds = 0.0f;
		long stopTime = 0;
		double calculatedRate = 0.0;
		double numSeconds = 0.0;
		long sentKB = 0;

		
		//generate 1024 bytes of 0's, store in variable
		byte[] zeros = new byte[1024];
		
		Socket iperfClientSocket;
		//Create an actual socket
		try {
			iperfClientSocket = new Socket(RemoteHostAddress, Port);
		}
		catch (IOException e) {
	        System.out.println(e);
	        return;
	    }
		
		//Set up the TCP Connection
		DataOutputStream streamToServer= new DataOutputStream(iperfClientSocket.getOutputStream());
		
		
		//Get the system time when starting the connection
		startTime = System.currentTimeMillis();
		while (currSeconds < secondLimit)
		{
			//System.out.println("currSeconds=" + currSeconds);
			currTime = System.currentTimeMillis();
			streamToServer.write(zeros, 0, 1024);
			sentBytes += 1024;
			currSeconds = (float)(currTime - startTime) / (float)1000;
			
		}
		
		iperfClientSocket.close();
				
		sentKB = sentBytes / 1024;
		calculatedRate = (sentKB * 8.0) / (1024 * currSeconds);
		System.out.println("sent=" + sentKB + " KB rate=" + calculatedRate + " Mbps"); 
		// while current time - start time < numSeconds
			//send the data
			//increment bytes sent,
			//get current time for calculation
		
		//calculate kilobytes sent
		//calculate rate
		//ouput
		
		
	}
	
	/**
	 * Start the Pinger server, which receives requests and then sends response messages
	 * @throws Exception
	 */
	static private void StartIperfServer() throws Exception
	{
		long receivedBytes = 0;

		int recvdChars = 0;
		byte[] buf = new byte[1024];
		long receivedKB = 0;
		long startTime = 0;
		long stopTime = 0;
		double calculatedRate = 0.0;
		double numSeconds = 0.0;
		
		ServerSocket receiveSocket;
		Socket iperfSocket;
		BufferedInputStream fromClient;
		
		//Constantly sit, wait for 
		while(true)
		{
			receiveSocket = new ServerSocket(Port);
			//accept a connection from an iperf client which will send bytes
			iperfSocket = receiveSocket.accept();
			startTime = System.currentTimeMillis();
			receivedBytes = 0;
			fromClient = new BufferedInputStream(iperfSocket.getInputStream());
			
			while (recvdChars != -1)
			{
				recvdChars = fromClient.read(buf);
				receivedBytes += recvdChars;
				
			}
			
			stopTime = System.currentTimeMillis();
			numSeconds = ((float)(stopTime - startTime)) / 1000;
			
			receivedKB = receivedBytes /1024;
			calculatedRate = (receivedKB * 8.0) / (1024 * numSeconds);
			System.out.println("received=" + receivedKB + " KB rate=" + calculatedRate + " Mbps"); 
			fromClient.close();
			iperfSocket.close();
			receiveSocket.close();
		}
	}
	
}
