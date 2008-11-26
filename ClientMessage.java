import java.io.Serializable;

public class ClientMessage extends Message implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum Type
	{
		NONE,
		ADD,
		EDIT,
		DELETE,
		READ,
	}

	private Type type = Type.NONE;

	private String key = "";

	private String value = "";

	public ClientMessage()
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

	public String getKey()
	{
		return key;
	}

	public void setKey( String key )
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue( String value )
	{
		this.value = value;
	}
}

