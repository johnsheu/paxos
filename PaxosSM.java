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

	ArrayList< PaxosRoundState > rounds = null;

	long numProcesses;


	//learner variables
	
	public PaxosSM()
	{
		clear();
		rounds = new ArrayList< PaxosRoundState >();
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
		PaxosValue value = msg.getValue();

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
			r.setHighestPrepareResp( propNum, msg.getHighestAcceptedNumber(), value );
		    
		    //if majority has responded, send out accept message
		    if( r.getNumPrepareResp( propNum ) > numProcesses / 2 + 1 )
		    {
			msg.setType( PaxosMessage.Type.ACC_REQ );

			//KAREN - need to fix so that it sets the value to a chosen one if getHighestPrepareREsp is null 
			msg.setValue( r.getHighestPrepareRespValue( propNum ) );
			msg.setHighestAcceptedNumber( null );
			
			//send ACC_REQ
		    }
		}
		else if( type == PaxosMessage.Type.ACC_REQ && msg.getProposalNumber() > r.highestPrepareRequest )
		{
		    r.highestProposalAccepted =  msg.getProposalNumber();
		    r.highestProposalAcceptedValue =  value;
		    msg.setType( PaxosMessage.Type.ACC_INF );
		    //send ACC_INF message to distinguished learner
		}
		else if( type == PaxosMessage.Type.ACC_INF )
		{
		    r.incrementAcceptInforms( propNum, value );
		    if( r.getNumAcceptInforms( propNum, value ) > numProcesses / 2 + 1 )
		    {
			//choose value for that round
		    }
		}
	}


}
