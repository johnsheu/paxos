import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.SortedSet;
import java.util.Iterator;
import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.BitSet;

public class PaxosSM
{
	private PaxosProcess process = null;

	private boolean leader;

	private long nextRoundNum = -1L;

	private long uniqueID = 0L;

	private BitSet chosenProposals = null;

	HashMap<Long, PaxosRoundState > rounds = null;

	long numProcesses;

	
	public PaxosSM( PaxosProcess process )
	{
		clear();
		rounds = new HashMap<Long, PaxosRoundState >();
		chosenProposals = new BitSet();
		this.process = process;
	}

	public void clear()
	{
	}
	
	public boolean isLeader()
	{
		return leader;
	}
	
	public void setLeader( boolean leader )
	{
		this.leader = leader;
	}

	public long getUniqueID()
	{
		return uniqueID;
	}

	public void setUniqueID( long value )
	{
		uniqueID = value;
	}

	public String dump()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "{ " );
		long index = 0;
		PaxosRoundState state = null;
		while ( ( state = rounds.get( index ) ) != null )
		{
			builder.append( "[" + index++ );
			builder.append( " (" + state.accepted + ") " );
			builder.append( state.highestProposalAcceptedValue );
			builder.append( "] " );
		}
		builder.append( "}" );
		return builder.toString();
	}

	public void sendPrepareRequest( Long n)
	{
	}

    public void setNumProcesses( int num )
    {
	numProcesses = num;
    }

	public void processLeaderElection()
	{
		PaxosMessage reply = new PaxosMessage();
		reply.setType( PaxosMessage.Type.NEWLEADER );
		reply.setChosenProposals( chosenProposals );
		reply.setProposalNumber( uniqueID );
		process.broadcastMessage( reply );
	}

	public void processClientMessage( ClientMessage msg )
	{
		PaxosMessage reply = new PaxosMessage();
		reply.setType( PaxosMessage.Type.PREP_REQ );
		reply.setRound( nextRoundNum );
		reply.setProposalNumber( uniqueID );
		reply.setValue( msg.getValue() );
		PaxosRoundState prs = new PaxosRoundState();
		prs.clientValue = msg.getValue();
		rounds.put( nextRoundNum++, prs );
		process.broadcastMessage( reply );
	}

	public void processMessage( PaxosMessage msg )
	{
		PaxosMessage.Type type = msg.getType();

		if ( type == PaxosMessage.Type.NEWLEADER )
		{
			System.out.println( "NEWLEADER" );
			BitSet proposals = msg.getChosenProposals();
			int index = proposals.nextClearBit( 0 );
			PaxosRoundState r = null;
			while ( ( r = rounds.get( new Long( index ) ) ) != null )
			{
				PaxosMessage reply = new PaxosMessage();
				reply.setValue( r.highestProposalAcceptedValue );
				reply.setHighestAcceptedNumber( r.highestProposalAccepted );
				reply.setProposalNumber( msg.getProposalNumber() );
				reply.setType( PaxosMessage.Type.PREP_RESP );
				reply.setRound( index );
				reply.setAddress( msg.getAddress() );
				process.sendMessage( reply );
				index = proposals.nextClearBit( index + 1 );
			}
			PaxosMessage round = new PaxosMessage();
			round.setType( PaxosMessage.Type.KEEPALIVE );
			round.setRound( nextRoundNum - 1 );
			round.setAddress( msg.getAddress() );
			process.sendMessage( round );
			return;
		}

		long propNum = msg.getProposalNumber();
		long roundNum = msg.getRound();
		if ( roundNum >= nextRoundNum )
			nextRoundNum = roundNum + 1;
		PaxosValue value = msg.getValue();

		if ( roundNum < 0 )
			return;

		PaxosRoundState r = rounds.get( new Long(msg.getRound()) );
		if ( r == null )
		{
		    r = new PaxosRoundState();
			r.clientValue = new PaxosValue( "", "", PaxosValue.Type.NONE );
		    rounds.put( new Long( msg.getRound()), r );
		}

		if ( type == PaxosMessage.Type.PREP_REQ && msg.getProposalNumber() > r.highestPrepareRequest )
		{
		    System.out.println( "PREP_REQ: " + value );
			if ( r.highestProposalAcceptedValue != null )
			    msg.setValue( r.highestProposalAcceptedValue );
		    msg.setHighestAcceptedNumber( r.highestProposalAccepted );
		    r.highestPrepareRequest = propNum;
		    msg.setType( PaxosMessage.Type.PREP_RESP );
		    //send back msg
			process.sendMessage( msg );
		}
		else if ( type == PaxosMessage.Type.PREP_RESP )
		{
			System.out.println( "PREP_RESP: " + value );
		    //process Paxos Message
		    r.incrementNumPrepareResp( propNum );

 
		    //set new max if necessary
		    if( msg.getHighestAcceptedNumber() > r.getHighestPrepareResp( propNum ))
			r.setHighestPrepareResp( propNum, msg.getHighestAcceptedNumber(), value );
		    
		    //if majority has responded, send out accept message
		    if( r.getNumPrepareResp( propNum ) > numProcesses / 2 && !r.hasAcceptMajority(propNum))
		    {
			r.setHasAcceptMajority( propNum, true );
			msg.setType( PaxosMessage.Type.ACC_REQ );

			//KAREN - need to fix so that it sets the value to a chosen one if getHighestPrepareREsp is null 
			PaxosValue pv = r.getHighestPrepareRespValue( propNum );
			if( pv == null )
			{
			    pv = r.clientValue;
			}
			msg.setValue( pv );
			long newprop = r.getHighestPrepareResp( propNum );
			if ( newprop != -1L )
				msg.setProposalNumber( newprop );
			msg.setHighestAcceptedNumber( -1L );
			
			//send ACC_REQ
			process.broadcastMessage( msg );
		    }
		}
		else if ( type == PaxosMessage.Type.ACC_REQ && msg.getProposalNumber() >= r.highestPrepareRequest )
		{
		    System.out.println( "ACC_REQ: " + value );
		    r.highestProposalAccepted =  msg.getProposalNumber();
		    r.highestProposalAcceptedValue =  value;
		    msg.setType( PaxosMessage.Type.ACC_INF );
		    //send ACC_INF message to distinguished learner
			process.sendMessage( msg );
		}
		else if ( type == PaxosMessage.Type.ACC_INF )
		{
		    System.out.println( "ACC_INF: " + value );
		    r.incrementAcceptInforms( propNum, value );
		    if( (r.getNumAcceptInforms( propNum, value ) == numProcesses / 2 + 1)  && leader )
		    {
				//inform client
				msg.setType( PaxosMessage.Type.ACC_CAST );
				process.broadcastMessage( msg );
		    }
		}
		else if ( type == PaxosMessage.Type.ACC_CAST )
		{
			System.out.println( "ACC_CAST: " + value );
			r.accepted = true;
			chosenProposals.set( (int)roundNum );
		}
	}
}

