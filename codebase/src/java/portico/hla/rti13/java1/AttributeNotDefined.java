package hla.rti13.java1;

public class AttributeNotDefined extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AttributeNotDefined( String reason )
	{
		super( reason );
	}

	public AttributeNotDefined( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AttributeNotDefined()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AttributeNotDefined( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AttributeNotDefined( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
