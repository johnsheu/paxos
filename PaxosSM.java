import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;
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

	HashMap<Long, Long> highestPrepareRespMap = null;


	//learner variables
	
	public PaxosSM()
	{
		clear();
		highestProposalAcceptedValueMap = new HashMap<Long, PaxosValue>();
		highestProposalAcceptedMap = new HashMap<Long, Long>();

		highestPrepareRespMap = new HashMap<Long, Long>();
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

		if( type == PaxosMessage.Type.PREP_REQ && msg.getProposalNumber() > highestPrepareRespMap.get(round) )
		{
		    msg.setValue( highestProposalAcceptedValueMap.get(round) );
		    highestPrepareRespMap.put( round, msg.getProposalNumber() );
		    msg.setType( PaxosMessage.Type.PREP_RESP );
		    //send back msg
		}
		else if( type == PaxosMessage.Type.PREP_RESP )
		{
		    //process Paxos Message
		    
		    //if majority has responded, send out accept message
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
