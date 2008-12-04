import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class PaxosClient
{
	private Communicator communicator = null;

	private int port = -1;

	private InetSocketAddress[] addresses = null;

	public PaxosClient( int port )
	{
		communicator = new Communicator( port );
		communicator.start();
		this.port = port;
	}

	private Message getMessageReply( Message message )
	{
		InetSocketAddress address = message.getAddress();
		communicator.sendMessage( message );
		long timeout = System.currentTimeMillis() + 200L;
		while ( System.currentTimeMillis() < timeout )
		{
			Message reply;
			while ( ( reply = communicator.readMessage() ) != null )
			{
				if ( !address.equals( reply.getAddress() ) )
					continue;
				return reply;
			}
			try
			{
				Thread.currentThread().sleep( 100 );
			}
			catch ( InterruptedException ex )
			{

			}
		}
		ErrorMessage emessage = new ErrorMessage();
		emessage.setAddress( address );
		emessage.setError( "getMessageReply timed out" );
		return emessage;
	}

	/*
	 * Execute a command line.
	 */
	public void commandLine( String command )
	{
		String[] args = command.split( "\\s+" );

		if ( args.length == 0 )
			return;

		if ( args[0].length() != 0 && args[0].charAt( 0 ) == '#' )
			//  Comment
			return;

		if ( args[0].equalsIgnoreCase( "clear" ) )
		{
			char esc = 27;
			String seq = esc + "[2J";
			System.out.print( seq );
			return;
		}
		else if ( args[0].equalsIgnoreCase( "add" ) )
		{
			if ( args.length != 3 )
			{
				System.out.print( "add <key> <value>\n" );
				return;
			}
			if ( addresses == null )
			{
				System.out.print( "error: no PaxosProcess instances started\n" );
				return;
			}
			ClientMessage message = new ClientMessage();
			PaxosValue value = new PaxosValue( args[1], args[2], PaxosValue.Type.ADD );
			message.setValue( value );
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
			return;
		}
		if ( args[0].equalsIgnoreCase( "edit" ) )
		{
			if ( args.length != 3 )
			{
				System.out.print( "edit <key> <value>\n" );
				return;
			}
			if ( addresses == null )
			{
				System.out.print( "error: no PaxosProcess instances started\n" );
				return;
			}
			ClientMessage message = new ClientMessage();
			PaxosValue value = new PaxosValue( args[1], args[2], PaxosValue.Type.EDIT );
			message.setValue( value );
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
			return;
		}
		if ( args[0].equalsIgnoreCase( "delete" ) )
		{
			if ( args.length != 3 )
			{
				System.out.print( "delete <key>>\n" );
				return;
			}
			if ( addresses == null )
			{
				System.out.print( "error: no PaxosProcess instances started\n" );
				return;
			}
			ClientMessage message = new ClientMessage();
			PaxosValue value = new PaxosValue( args[1], "", PaxosValue.Type.DELETE );
			message.setValue( value );
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
			return;
		}
		if ( args[0].equalsIgnoreCase( "read" ) )
		{
			if ( args.length != 3 )
			{
				System.out.print( "read <key>\n" );
				return;
			}
			if ( addresses == null )
			{
				System.out.print( "error: no PaxosProcess instances started\n" );
				return;
			}
			ClientMessage message = new ClientMessage();
			PaxosValue value = new PaxosValue( args[1], "", PaxosValue.Type.READ );
			message.setValue( value );
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
			return;
		}
		else if ( args[0].equalsIgnoreCase( "start" ) )
		{
			if ( args.length != 2 )
			{
				System.out.print( "start <file>\n" );
				return;
			}
			if ( addresses != null )
			{
				System.err.print( "error: processes already started\n" );
				return;
			}

			addresses = readAddresses( args[1] );
			if ( addresses == null )
			{
				return;
			}
			else if ( addresses.length <= 0 )
			{
				System.err.print( "error: no addresses in file\n" );
				return;
			}

//			PaxosProcess[] processes = new PaxosProcess[addresses.length];
//			for ( int i = 0; i < addresses.length; i += 1 )
//				processes[i] = new PaxosProcess( i, addresses );
//			processes[0].forceLeader( true );
//			for ( int i = 0; i < addresses.length; i += 1 )
//				processes[i].start();
			return;
		}
		/*
		else if ( args[0].equalsIgnoreCase( "kill" ) )
		{
			if ( args.length < 2 )
			{
				System.out.print( "kill <index1> [index2] [index3] ...\n" );
				return;
			}

			for ( int i = 1; i < args.length; i += 1 )
			{
				int index = 0;
				try
				{
					index = Integer.parseInt( args[i] );
				}
				catch ( NumberFormatException ex )
				{
					ex.printStackTrace();
					return;
				}

				if ( index < 0 || index >= addresses.length )
				{
					System.out.print( "error: index out of bounds\n" );
					return;
				}

				processList.get( index ).kill();
			}

			return;
		}
		*/
		else if ( args[0].equalsIgnoreCase( "source" ) )
		{
			if ( args.length != 2 )
			{
				System.out.print( "source <file>\n" );
				return;
			}
			readFile( args[1] );
			return;
		}
		else if ( args[0].equalsIgnoreCase( "sleep" ) )
		{
			if ( args.length != 2 )
			{
				System.out.print( "sleep <time>\n" );
				return;
			}
			float time = 0.0f;
			try
			{
				time = Float.parseFloat( args[1] );
			}
			catch ( NumberFormatException ex )
			{
				ex.printStackTrace();
				return;
			}

			long stime = (long)( time * 1000.0f );
			try
			{
				Thread.currentThread().sleep( stime );
			}
			catch ( InterruptedException ex )
			{

			}
			return;
		}
		/*
		else if ( args[0].equalsIgnoreCase( "status" ) )
		{
			if ( args.length < 2 )
			{
				System.out.print( "status <index1> [index2] [index3] ...\n" );
				return;
			}

			for ( int i = 1; i < args.length; i += 1 )
			{
				int index = 0;
				try
				{
					index = Integer.parseInt( args[i] );
				}
				catch ( NumberFormatException ex )
				{
					ex.printStackTrace();
					return;
				}

				if ( index < 0 || index >= processList.size() )
				{
					System.out.print( "error: index out of bounds\n" );
					return;
				}

				System.out.print( processList.get( index ).getStatus() );
			}

			return;
		}
		else if ( args[0].equalsIgnoreCase( "set_sleeptime" ) )
		{
			if ( args.length != 3 )
			{
				System.out.print( "set_sleeptime <index> <time>\n" );
				return;
			}

			int index = 0;
			try
			{
				index = Integer.parseInt( args[1] );
			}
			catch ( NumberFormatException ex )
			{
				ex.printStackTrace();
				return;
			}

			if ( index < 0 || index >= processList.size() )
			{
				System.out.print( "error: index out of bounds\n" );
				return;
			}

			long value = 0L;
			try
			{
				value = Long.parseLong( args[2] );
			}
			catch ( NumberFormatException ex )
			{
				ex.printStackTrace();
				return;
			}

			processList.get( index ).setSleepTime( value );
			return;
		}*/
		else if ( args[0].equalsIgnoreCase( "quit" ) ||
			args[0].equalsIgnoreCase( "exit" ) )
		{
			System.exit( 0 );
		}
		else
		{
			System.out.print( "Commands:\n" );
			System.out.print( " add\n" );
			System.out.print( " clear\n" );
			System.out.print( " delete\n" );
			System.out.print( " edit\n" );
			System.out.print( " exit\n" );
//			System.out.print( " kill\n" );
			System.out.print( " quit\n" );
			System.out.print( " read\n" );
//			System.out.print( " set_sleeptime\n" );
			System.out.print( " sleep\n" );
			System.out.print( " source\n" );
			System.out.print( " start\n" );
//			System.out.print( " status\n" );
			System.out.print( "Call a command with no arguments for usage info\n" );
			return;
		}
	}

	private void readFile( String name )
	{
		try
		{
			BufferedReader reader = new BufferedReader( new FileReader( name ) );
			String line;
			while ( ( line = reader.readLine() ) != null )
			{
				System.out.print( "> " + line + '\n' );
				commandLine( line );
			}
		}
		catch ( FileNotFoundException ex )
		{
			ex.printStackTrace();
		}
		catch ( IOException ex )
		{
			ex.printStackTrace();
		}
	}

	private InetSocketAddress[] readAddresses( String name )
	{
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
					return null;
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
			return null;
		}
		catch ( IOException ex )
		{
			ex.printStackTrace();
			return null;
		}
		catch ( NumberFormatException ex )
		{
			System.err.print( "error: file \"" + name + "\", line " + linecount + ":\n" );
			ex.printStackTrace();
			return null;
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

		InetSocketAddress[] ret = new InetSocketAddress[addresses.size()];
		return addresses.toArray( ret );
	}

	public static void main( String[] args )
	{
		if ( args.length < 1 || args.length > 2 )
		{
			System.out.print( "usage: java PaxosClient <port> [cmdfile]\n" );
			System.out.print( "       <port> - port number to listen on\n" );
			System.out.print( "       [cmdfile] - (optional) file of commands to run before stdin\n\n" );
			System.exit( 0 );
		}

		int port = 0;
		try
		{
			port = Integer.parseInt( args[0] );
		}
		catch ( NumberFormatException ex )
		{
			ex.printStackTrace();
			System.exit( 1 );
		}

		PaxosClient manager = new PaxosClient( port );

		if ( args.length == 2 )
			manager.readFile( args[1] );

		BufferedReader reader = new BufferedReader( 
			new InputStreamReader( System.in ) );
		String line;
		try
		{
			System.out.print( "> " );
			while ( ( line = reader.readLine() ) != null )
			{
				manager.commandLine( line );
				System.out.print( "> " );
			}
		}
		catch ( IOException ex )
		{
			ex.printStackTrace();
		}
	}
}

