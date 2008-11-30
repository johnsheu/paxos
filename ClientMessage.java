import java.io.Serializable;

public class ClientMessage extends Message implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;

	private PaxosValue value = null;

	public ClientMessage()
	{

	}

	public ClientMessage( PaxosValue value )
	{
		this.value = value;
	}

	public void setValue( PaxosValue value )
	{
		this.value = value;
	}

	public PaxosValue getValue()
	{
		return value;
	}
}

