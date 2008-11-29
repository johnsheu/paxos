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
	HashMap<Long, PaxosValue> highestProposalAcceptedValueMap = null;
	HashMap<Long, Long> highestProposalAcceptedMap = null;

	HashMap<Long, Long> highestPrepareRequestMap = null;

	HashMap<Long, HashMap<Long, Long>> numPrepareRespMap = null;
	HashMap<Long, HashMap<Long, TreeMap<Long, PaxosValue>>> highestPrepareRespMap = null;

	long numProcesses;


	//learner variables
	
	public PaxosSM()
	{
		clear();
		highestProposalAcceptedValueMap = new HashMap<Long, PaxosValue>();
		highestProposalAcceptedMap = new HashMap<Long, Long>();

		highestPrepareRespMap = new HashMap<Long, Long>();
		
		prepareResponses = new HashMap<Long, HashMap<Long, TreeSet<PaxosValue>>();
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
		Long round = msg.getRound();
		Long propNum = msg.getProposalNumber();

		if( type == PaxosMessage.Type.PREP_REQ && msg.getProposalNumber() > highestPrepareRespMap.get( round ) )
		{
		    msg.setValue( highestProposalAcceptedValueMap.get( round ));
		    msg.setHighestAcceptedNumber( highestProposalAcceptedMap.get( round ));
		    ///WHAT??? does this work????
		    highestPrepareRespMap.put( round, propNum );
		    msg.setType( PaxosMessage.Type.PREP_RESP );
		    //send back msg
		}
		else if( type == PaxosMessage.Type.PREP_RESP )
		{
		    //process Paxos Message

		    HashMap<Long, Long> prepResponses = numPrepareRespMap.get( msg.getRound() );
		    if( prepResponses == null )
		    {
			prepResponses = new HashMap<Long, Long>();
			prepResponses.put( propNum, new Long( 1L ) );
			prepareResponses.put( round, prepResponses );
		    }
		    else
			prepResponses.put( propNum, Long.valueOf( prepResponses.get( propNum ).longValue() + 1L ) );

		    //set new max if necessary
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
		    
		    
		    //if majority has responded, send out accept message
		    msg.setType( PaxosMessage.Type.ACC_REQ );
		    //if there is a max value, set it so that, otherwise pick a response 
		    //msg.setValue( 
		    msg.highestAcceptedNumber = nul;
		}
		else if( type == PaxosMessage.Type.ACC_REQ && msg.getProposalNumber() > highestPrepareRespMap.get(round) )
		{
		    highestProposalAcceptedMap.put( round, msg.getProposalNumber() );
		    highestProposalAcceptedValueMap.put( round, msg.getValue() );
		    msg.setType( PaxosMessage.Type.ACC_INF );
		    //send message to distinguished learner
		}
		else if( type == PaxosMessage.Type.ACC_INF )
		{
		}
	}


}
