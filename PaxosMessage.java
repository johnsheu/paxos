import java.io.Serializable;

public class PaxosMessage extends Message implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum Type
	{
		NONE,
		KEEPALIVE,
		PREP_REQ,
		PREP_RESP,
		ACC_REQ,
		ACC_INF,
	}

	private Type type = Type.NONE;

	public PaxosMessage()
	{
		
	}

	public Type getType()
	{
		return type;
	}

	public void setType( Type type )
	{
		this.type = type;
	}
}

