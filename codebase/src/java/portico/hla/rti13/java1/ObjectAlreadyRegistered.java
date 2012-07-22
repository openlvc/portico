package hla.rti13.java1;

public class ObjectAlreadyRegistered extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public ObjectAlreadyRegistered( String reason )
	{
		super( reason );
	}

	public ObjectAlreadyRegistered( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public ObjectAlreadyRegistered()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public ObjectAlreadyRegistered( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public ObjectAlreadyRegistered( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
