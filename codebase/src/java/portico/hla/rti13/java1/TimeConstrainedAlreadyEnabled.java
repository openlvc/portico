package hla.rti13.java1;

public class TimeConstrainedAlreadyEnabled extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public TimeConstrainedAlreadyEnabled( String reason )
	{
		super( reason );
	}

	public TimeConstrainedAlreadyEnabled( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public TimeConstrainedAlreadyEnabled()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public TimeConstrainedAlreadyEnabled( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public TimeConstrainedAlreadyEnabled( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
