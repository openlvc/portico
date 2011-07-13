package hla.rti13.java1;

public class InvalidHandleValuePairSetContext extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InvalidHandleValuePairSetContext( String reason )
	{
		super( reason );
	}

	public InvalidHandleValuePairSetContext( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InvalidHandleValuePairSetContext()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InvalidHandleValuePairSetContext( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InvalidHandleValuePairSetContext( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
