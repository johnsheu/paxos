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

	//proposer variables

	//acceptor variables

	ArrayList< PaxosRoundState > rounds = null;

	long numProcesses;


	//learner variables
	
	public PaxosSM( PaxosProcess process )
	{
		clear();
		rounds = new ArrayList< PaxosRoundState >();
		chosenProposals = new BitSet();
		this.process = process;
		/*	highestProposalAcceptedValueMap = new HashMap<Long, PaxosValue>();
		highestProposalAcceptedMap = new HashMap<Long, Long>();

		highestPrepareRequestMap = new HashMap<Long, Long>();

		numPrepareREspMap = new HashMap< Long, HashMap<Long, Long>>();
		highestPrepareRespMap = new HashMap<Long, HashMap<Long, TreeMap<Long, PaxosValue>>>();
		
		prepareResponses = new HashMap<Long, HashMap<Long, TreeMap<Long, PaxosValue>>>();
		*/
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

	    return new String();
	}

	public void sendPrepareRequest( Long n)
	{
	}

	public void processLeaderElection()
	{
		PaxosMessage reply = new PaxosMessage();
		reply.setType( PaxosMessage.Type.NEWLEADER );
		reply.setChosenProposals( chosenProposals );
		process.broadcastMessage( reply );
	}

	public void processClientMessage( ClientMessage msg )
	{
		PaxosMessage reply = new PaxosMessage();
		reply.setType( PaxosMessage.Type.PREP_REQ );
		reply.setRound( nextRoundNum++ );
		reply.setProposalNumber( 0L );
		reply.setValue( msg.getValue() );
		process.broadcastMessage( reply );
	}

	public void processMessage( PaxosMessage msg )
	{
		PaxosMessage.Type type = msg.getType();

		if ( type == PaxosMessage.Type.NEWLEADER )
		{
			BitSet proposals = msg.getChosenProposals();
			int index = proposals.nextClearBit( 0 );
			while ( index < rounds.size() )
			{
				PaxosRoundState r = rounds.get( index );
				PaxosMessage reply = new PaxosMessage();
				reply.setValue( r.highestProposalAcceptedValue );
				reply.setHighestAcceptedNumber( r.highestProposalAccepted );
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
		
		PaxosRoundState r = rounds.get( (int)msg.getRound() );
		if( r == null )
		{
		    r = new PaxosRoundState();;
		    rounds.add( r );
		}

		if( type == PaxosMessage.Type.PREP_REQ && msg.getProposalNumber() > r.highestPrepareRequest )
		{
		    msg.setValue( r.highestProposalAcceptedValue );
		    msg.setHighestAcceptedNumber( r.highestProposalAccepted );
		    r.highestPrepareRequest = propNum;
		    msg.setType( PaxosMessage.Type.PREP_RESP );
		    //send back msg
			process.sendMessage( msg );
		}
		else if( type == PaxosMessage.Type.PREP_RESP )
		{
			System.out.println( "foo" );
		    //process Paxos Message
		    r.incrementNumPrepareResp( propNum );

 
		    //set new max if necessary
		    if( msg.getHighestAcceptedNumber() > r.getHighestPrepareResp( propNum ))
			r.setHighestPrepareResp( propNum, msg.getHighestAcceptedNumber(), value );
		    
		    //if majority has responded, send out accept message
		    if( r.getNumPrepareResp( propNum ) > numProcesses / 2 + 1 )
		    {
			msg.setType( PaxosMessage.Type.ACC_REQ );

			//KAREN - need to fix so that it sets the value to a chosen one if getHighestPrepareREsp is null 
			msg.setValue( r.getHighestPrepareRespValue( propNum ) );
			msg.setHighestAcceptedNumber( -1L );
			
			//send ACC_REQ
			process.sendMessage( msg );
		    }
		}
		else if( type == PaxosMessage.Type.ACC_REQ && msg.getProposalNumber() > r.highestPrepareRequest )
		{
		    r.highestProposalAccepted =  msg.getProposalNumber();
		    r.highestProposalAcceptedValue =  value;
		    msg.setType( PaxosMessage.Type.ACC_INF );
		    //send ACC_INF message to distinguished learner
			process.sendMessage( msg );
		}
		else if( type == PaxosMessage.Type.ACC_INF )
		{
		    r.incrementAcceptInforms( propNum, value );
		    if( (r.getNumAcceptInforms( propNum, value ) > numProcesses / 2 + 1)  && leader )
		    {
			//inform client
		    }
		}
	}
}
