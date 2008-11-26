import java.util.concurrent.atomic.AtomicBoolean;

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
					Message message = communicator.readMessageBlocking();
					//  Do whatever the message requests us to do
					if ( message instanceof ClientMessage )
					{
						if ( !isLeader )
							continue;

						ClientMessage msg = (ClientMessage)message;
						switch( msg.getType() )
						{
							case NONE:
							{
								break;
							}
							case ADD:
							{
								break;
							}
							case EDIT:
							{
								break;
							}
							case DELETE:
							{
								break;
							}
							case READ:
							{
								break;
							}
						}
						continue;
					}
					else if ( message instanceof PaxosMessage )
					{
						PaxosMessage msg = (PaxosMessage)message;
						if ( leaderIndex >= 0 && leaderIndex < ports.length &&
							message.getAddress().getPort() == ports[leaderIndex] )
							leaderAlive.set( true );
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
					if ( isLeader )
					{
						//  Send heartbeat messages
						for ( int i = 0; i < ports.length; i += 1 )
						{
							PaxosMessage message = new PaxosMessage();
							message.setAddress( "localhost", ports[i] );
							message.setType( PaxosMessage.Type.KEEPALIVE );
							communicator.sendMessage( message );
						}
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

	private int port = -1;

	private int[] ports = null;

	private long sleepTime = 250L;

	private boolean isLeader = false;

	private int leaderIndex = -1;

	private AtomicBoolean leaderAlive = null;
	
	public PaxosProcess( int port, int[] ports )
	{
		communicator = new Communicator( port );
		this.port = port;
		this.ports = ports.clone();
		if ( ports.length >= 1 )
			leaderIndex = 0;
		leaderAlive = new AtomicBoolean( true );
	}

	public void start()
	{
		synchronized ( communicator )
		{
			if ( processComm != null )
				return;

			communicator.start();

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
	 * START Paxos API
	 */

	/*
	 * END Paxos API
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
	}

	public boolean getLeader()
	{
		return isLeader;
	}

	public String getStatus()
	{
		return
			"PaxosProcess (:" + port + "):\n" +
			"* alive: " + isStarted() + '\n' +
			"* sleepTime: " + sleepTime + '\n' +
			"* isLeader: " + isLeader + '\n';
	}
}

