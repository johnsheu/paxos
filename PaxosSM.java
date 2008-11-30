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


public class PaxosSM
{
	private boolean leader;

	//proposer variables

	//acceptor variables

    /*	HashMap<Long, PaxosValue> highestProposalAcceptedValueMap = null;
	HashMap<Long, Long> highestProposalAcceptedMap = null;

	HashMap<Long, Long> highestPrepareRequestMap = null;

	HashMap<Long, HashMap<Long, Long>> numPrepareRespMap = null;
	HashMap<Long, HashMap<Long, TreeMap<Long, PaxosValue>>> highestPrepareRespMap = null;
    */
	ArrayList< PaxosRoundState > rounds = null;

	long numProcesses;


	//learner variables
	
	public PaxosSM()
	{
		clear();
		rounds = new ArrayList< PaxosRoundState >();
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

	public String dump()
	{

	    return new String();
	}

	public void sendPrepareRequest( Long n)
	{
	}


	public void processMessage( PaxosMessage msg )
	{
		PaxosMessage.Type type = msg.getType();		
		long propNum = msg.getProposalNumber();

		PaxosRoundState r = rounds.get( msg.getRound().intValue() );
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
		}
		else if( type == PaxosMessage.Type.PREP_RESP )
		{
		    //process Paxos Message
		    r.incrementNumPrepareResp( propNum );


		    //set new max if necessary
		    if( msg.getHighestAcceptedNumber().longValue() > r.getHighestPrepareResp( propNum ))
			r.setHighestPrepareResp( propNum, msg.getHighestAcceptedNumber(), msg.getValue() );
		    
		    //if majority has responded, send out accept message
		    if( r.getNumPrepareResp( propNum ) > numProcesses / 2 + 1 )
			    msg.setType( PaxosMessage.Type.ACC_REQ );

		    //KAREN - need to fix so that it sets the value to a chosen one if getHighestPrepareREsp is null 
		    msg.setValue( r.getHighestPrepareRespValue( propNum ) );
		    msg.setHighestAcceptedNumber( null );
		}
		else if( type == PaxosMessage.Type.ACC_REQ && msg.getProposalNumber() > r.highestPrepareRequest )
		{
		    r.highestProposalAccepted =  msg.getProposalNumber();
		    r.highestProposalAcceptedValue =  msg.getValue();
		    msg.setType( PaxosMessage.Type.ACC_INF );
		    //send message to distinguished learner
		}
		else if( type == PaxosMessage.Type.ACC_INF )
		{
		}
	}


}
