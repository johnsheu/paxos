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
	PaxosValue highestProposalAcceptedValue = null;
	Long highestProposalAccepted = null;

	Long highestPrepareResp = null;


	//learner variables
	
	public PaxosSM()
	{
		clear();
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
		if( type == PaxosMessage.Type.PREP_REQ && msg.getProposalNumber() > highestPrepareResp )
		{
		    msg.setValue( highestProposalAcceptedValue );
		    highestPrepareResp = msg.getProposalNumber();
		    msg.setType( PaxosMessage.Type.PREP_RESP );
		    //send back msg
		}
		else if( type == PaxosMessage.Type.PREP_RESP )
		{
		    //process Paxos Message
		    
		    //if majority has responded, send out accept message
		}
		else if( type == PaxosMessage.Type.ACC_REQ && msg.getProposalNumber() > highestPrepareResp )
		{
		    highestProposalAccepted = msg.getProposalNumber();
		    highestProposalAcceptedValue = msg.getValue();
		    msg.setType( PaxosMessage.Type.ACC_INF );
		    //send message to distinguished learner
		}
		else if( type == PaxosMessage.Type.ACC_INF )
		{
		}
	}


}
