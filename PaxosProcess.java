import java.util.concurrent.atomic.AtomicBoolean;
import java.util.BitSet;

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
							for ( int i = 0; i < ports.length; i += 1 )
							{
								if ( ports[i] == message.getAddress().getPort() )
								{
									responders.set( i );
									if ( responders.cardinality() > ports.length / 2 )
									{
										canLead = true;
									}
									break;
								}
							}
						}
						if ( leaderIndex >= 0 && leaderIndex < ports.length &&
							message.getAddress().getPort() == ports[leaderIndex] )
						{
							leaderAlive.set( true );
						}
						stateMachine.processMessage( msg );
						continue;
					}
					else if ( message instanceof ErrorMessage )
					{
						ErrorMessage msg = (ErrorMessage)message;
						System.err.println( msg.toString() );
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
							if ( leaderIndex >= 0 && leaderIndex < ports.length &&
								ports[leaderIndex] == port )
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

	private int port = -1;

	private int[] ports = null;

	private BitSet responders = null;

	private long sleepTime = 250L;

	private boolean isLeader = false;

	private boolean canLead = false;

	private int leaderIndex = -1;

	private AtomicBoolean leaderAlive = null;
	
	public PaxosProcess( int port, int[] ports )
	{
		communicator = new Communicator( port );
		stateMachine = new PaxosSM( this );
		this.port = port;
		this.ports = ports.clone();
		if ( ports.length >= 1 )
			leaderIndex = 0;
		stateMachine.setNumProcesses( ports.length );
		responders = new BitSet( ports.length );
		leaderAlive = new AtomicBoolean( true );
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
		for ( int i = 0; i < ports.length; i += 1 )
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
			msg.setAddress( "localhost", ports[i] );
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
			"PaxosProcess (:" + port + "):\n" +
			"* alive: " + isStarted() + '\n' +
			"* sleepTime: " + sleepTime + '\n' +
			"* isLeader: " + isLeader + '\n' +
			"* state:\n" + stateMachine.dump() + '\n';
	}
}

