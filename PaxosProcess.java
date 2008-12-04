import java.util.concurrent.atomic.AtomicBoolean;
import java.util.BitSet;

import java.util.ArrayList;
import java.net.InetSocketAddress;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class PaxosProcess
{
	private class PaxosProcessComm extends Thread
	{
		public PaxosProcessComm()
		{

		}

		public void run()
		{
			while ( !isInterrupted() )
			{
				try
				{
					Thread.yield();
					Message message = communicator.readMessageBlocking();
					//  Do whatever the message requests us to do
					if ( message instanceof ClientMessage )
					{
						if ( !( isLeader && canLead ) )
							continue;

						ClientMessage msg = (ClientMessage)message;
						stateMachine.processClientMessage( msg );
						continue;
					}
					else if ( message instanceof PaxosMessage )
					{
						PaxosMessage msg = (PaxosMessage)message;
						if ( isLeader && !canLead )
						{
							for ( int i = 0; i < addresses.length; i += 1 )
							{
								if ( addresses[i].equals( message.getAddress() ) )
								{
									responders.set( i );
									if ( responders.cardinality() > addresses.length / 2 )
									{
										canLead = true;
									}
									break;
								}
							}
						}
						if ( leaderIndex >= 0 && leaderIndex < addresses.length &&
							message.getAddress().equals( addresses[leaderIndex] ) )
						{
							leaderAlive.set( true );
						}
						stateMachine.processMessage( msg );
						continue;
					}
					else if ( message instanceof ErrorMessage )
					{
						ErrorMessage msg = (ErrorMessage)message;
						//System.err.println( msg.toString() );
						continue;
					}
				}
				catch ( InterruptedException ex )
				{
					return;
				}
			}
		}
	}
	
	private class PaxosProcessActor extends Thread
	{
		public PaxosProcessActor()
		{

		}
		
		public void run()
		{
			while ( !isInterrupted() )
			{
				try
				{
					Thread.yield();
					if ( isLeader )
					{
						//  Send heartbeat messages
						PaxosMessage message = new PaxosMessage();
						message.setType( PaxosMessage.Type.KEEPALIVE );
						broadcastMessage( message );
						sleep( sleepTime );
					}
					else
					{
						//  Check for leader heartbeat
						sleep( sleepTime * 4L );
						boolean result = leaderAlive.getAndSet( false );
						if ( !result )
						{
							leaderIndex += 1;
							if ( leaderIndex >= 0 && leaderIndex < addresses.length &&
								addresses[leaderIndex].equals( address ) )
							{
								setLeader( true );
								stateMachine.processLeaderElection();
							}
						}
					}
				}
				catch ( InterruptedException ex )
				{
					return;
				}
			}
		}
	}

	private Communicator communicator = null;

	private PaxosProcessComm processComm = null;
	
	private PaxosProcessActor processAct = null;

	private PaxosSM stateMachine = null;

	private InetSocketAddress[] addresses = null;

	private InetSocketAddress address = null;

	private BitSet responders = null;

	private long sleepTime = 250L;

	private boolean isLeader = false;

	private boolean canLead = false;

	private int leaderIndex = 0;

	private AtomicBoolean leaderAlive = null;

	private boolean hackdrop1 = false;

	private boolean hackdrop2 = false;
	
	public PaxosProcess( int index, InetSocketAddress[] addresses )
	{
		this.addresses = addresses.clone();
		this.address = this.addresses[index];
		stateMachine.setNumProcesses( addresses.length );
		responders = new BitSet( addresses.length );
		leaderAlive = new AtomicBoolean( true );

		communicator = new Communicator( address.getPort() );
		stateMachine = new PaxosSM( this );
	}

	public void start()
	{
		synchronized ( communicator )
		{
			if ( processComm != null )
				return;

			communicator.start();
			communicator.clear();

			processComm = new PaxosProcessComm();
			processComm.start();
			
			processAct = new PaxosProcessActor();
			processAct.start();
		}
	}

	public void kill()
	{
		synchronized ( communicator )
		{
			if ( processComm == null )
				return;

			processComm.interrupt();
			processAct.interrupt();

			try
			{
				processComm.join();
			}
			catch ( InterruptedException ex )
			{

			}
			
			try
			{
				processAct.join();
			}
			catch ( InterruptedException ex )
			{
				
			}
			
			processComm = null;
			processAct = null;

			setLeader( false );
		}
	}

	public void stop()
	{
		synchronized ( communicator )
		{
			communicator.stop();
			kill();
		}
	}

	public boolean isStarted()
	{
		synchronized ( communicator )
		{
			return processComm != null;
		}
	}

	/*
	 * START PaxosSM interface
	 */

	public void sendMessage( Message message )
	{
		communicator.sendMessage( message );
	}

	public void broadcastMessage( Message message )
	{
		if ( message instanceof PaxosMessage )
		{
			PaxosMessage msg = (PaxosMessage)message;
			if ( msg.getType() == PaxosMessage.Type.ACC_REQ )
			{
				if ( hackdrop1 )
				{
					hackdrop1 = false;
					return;
				}
			}
			else if ( msg.getType() == PaxosMessage.Type.ACC_CAST )
			{
				if ( hackdrop2 )
				{
					hackdrop2 = false;
					return;
				}
			}
		}

		for ( int i = 0; i < addresses.length; i += 1 )
		{
			Message msg = null;
			try
			{
				msg = message.clone();
			}
			catch ( CloneNotSupportedException ex )
			{
				ex.printStackTrace();
			}
			msg.setAddress( addresses[i] );
			communicator.sendMessage( msg );
		}
	}

	/*
	 * END PaxosSM interface
	 */

	public void setSleepTime( long sleepTime )
	{
		this.sleepTime = sleepTime;
	}

	public long getSleepTime()
	{
		return sleepTime;
	}

	public void setLeader( boolean isLeader )
	{
		this.isLeader = isLeader;
		stateMachine.setLeader( isLeader );
	}

	public void forceLeader( boolean isLeader )
	{
		setLeader( isLeader );
		canLead = isLeader;
	}

	public boolean isLeader()
	{
		return isLeader;
	}

	public String getStatus()
	{
		return
			"PaxosProcess (:" + address + "):\n" +
			"* alive: " + isStarted() + '\n' +
			"* sleepTime: " + sleepTime + '\n' +
			"* isLeader: " + isLeader + '\n' +
			"* state:\n" + stateMachine.dump() + '\n';
	}

	public static void main( String[] args )
	{
		if ( args.length != 2 )
		{
			System.out.print( "java PaxosProcess <file> <index>\n" );
			System.exit( 1 );
		}

		int index = -1;
		try
		{
			index = Integer.parseInt( args[1] );
		}
		catch ( NumberFormatException ex )
		{
			ex.printStackTrace();
		}

		String name = args[0];
		ArrayList<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
		int linecount = 1;
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader( new FileReader( name ) );
			String line;
			while ( ( line = reader.readLine() ) != null )
			{
				String[] words = line.split( "\\s+" );
				if ( words.length != 2 )
				{
					System.err.print( "error: file \"" + name + "\", line " + linecount + ":\n" );
					System.err.print( "       line does not contain address and port\n" );
					System.exit( 1 );
				}
				int port = Integer.parseInt( words[1] );
				InetSocketAddress address = new InetSocketAddress( words[0], port );
				addresses.add( address );
				linecount += 1;
			}
		}
		catch ( FileNotFoundException ex )
		{
			ex.printStackTrace();
			System.exit( 1 );
		}
		catch ( IOException ex )
		{
			ex.printStackTrace();
			System.exit( 1 );
		}
		catch ( NumberFormatException ex )
		{
			System.err.print( "error: file \"" + name + "\", line " + linecount + ":\n" );
			ex.printStackTrace();
			System.exit( 1 );
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch ( IOException ex )
			{
				ex.printStackTrace();
			}
		}

		if ( index < 0 || index > addresses.size() )
		{
			System.err.print( "invalid index\n" );
			System.exit( 1 );
		}

		InetSocketAddress[] laddresses = (InetSocketAddress[])( addresses.toArray() );
		PaxosProcess process = new PaxosProcess( index, laddresses );
		if ( index == 0 )
			process.forceLeader( true );
		process.start();

		try
		{
			reader = new BufferedReader( new InputStreamReader( System.in ) );
			String line = null;
			while ( ( line = reader.readLine() ) != null )
			{
				if ( line.equalsIgnoreCase( "s" ) )
				{
					System.out.println( process.getStatus() );
				}
				else if ( line.equalsIgnoreCase( "d1" ) )
				{
					process.hackdrop1 = true;
				}
				else if ( line.equalsIgnoreCase( "d2" ) )
				{
					process.hackdrop2 = true;
				}
			}
		}
		catch ( IOException ex )
		{
			ex.printStackTrace();
			System.exit( 1 );
		}
	}
}

