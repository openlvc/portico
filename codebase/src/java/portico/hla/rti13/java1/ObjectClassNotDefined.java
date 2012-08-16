package hla.rti13.java1;

public class ObjectClassNotDefined extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public ObjectClassNotDefined( String reason )
	{
		super( reason );
	}

	public ObjectClassNotDefined( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public ObjectClassNotDefined()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public ObjectClassNotDefined( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public ObjectClassNotDefined( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
