import java.util.Collections;
import java.util.HashMap;
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
	HashSet< PaxosRoundState > rounds = null;

	long numProcesses;


	//learner variables
	
	public PaxosSM()
	{
		clear();
		rounds = new HashSet< PaxosRoundState >();
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
		Long propNum = msg.getProposalNumber();

		PaxosRoundState r = rounds.get( msg.getRound() );
		if( r == null )
		{
		    r = new PaxosRoundState();;
		    rounds.add( r );
		}

		if( type == PaxosMessage.Type.PREP_REQ && msg.getProposalNumber() > r.highestPrepareRequest )
		{
		    msg.setValue( r.highestProposalAcceptedValue );
		    msg.setHighestAcceptedNumber( r.highestProposalAccepted );
		    r.setHighestPrepareRequest = propNum;
		    msg.setType( PaxosMessage.Type.PREP_RESP );
		    //send back msg
		}
		else if( type == PaxosMessage.Type.PREP_RESP )
		{
		    //process Paxos Message
		    r.incrementNumPrepareResp( propNum );


	     /*
		    HashMap<Long, Long> prepResponses = numPrepareRespMap.get( msg.getRound() );
		    if( prepResponses == null )
		    {
			prepResponses = new HashMap<Long, Long>();
			prepResponses.put( propNum, new Long( 1L ) );
			prepareResponses.put( round, prepResponses );
		    }
		    else
			prepResponses.put( propNum, Long.valueOf( prepResponses.get( propNum ).longValue() + 1L ) );
	      */

		    //set new max if necessary
		    if( msg.getHighestPrepareRequest.longValue() > r.highestPrepareResp( propNum ))
			r.setHighestPrepareResp( propNum, msg.getHighestPrepareRequest(), msg.getValue() );

			    /*
		    HashMap< Long, TreeMap<Long, PaxosValue>> highestPrepRespMap = highestPrepareRespMap.get( round );
		    TreeMap<Long, PaxosValue> highestPrepResp;
		    if( highestPrepRespMap == null )
		    {
			highestPrepRespMap = new HashMap<Long, TreeMap<Long, PaxosValue>>();
			highestPrepareRespMap.put( round, highestPrepRespMap );
		    }
		    highestPrepResp = highestPrepRespMap.get( propNum );
		    if( highestPrepResp == null )
		    {
			highestPrepResp = new TreeMap<Long, PaxosValue> ();
			highestPrepRespMap.put( propNum, highestPrepResp );
		    }
		    
		    highestPrepResp.put( msg.getHighestAcceptedNumber, msg.getValue() );
			    */
		    
		    //if majority has responded, send out accept message
		    if( r.getNumPrepareResp( propNum ).longValue() > numProcesses / 2 + 1 )
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
