package hla.rti13.java1;

public class FederateLoggingServiceCalls extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public FederateLoggingServiceCalls( String reason )
	{
		super( reason );
	}

	public FederateLoggingServiceCalls( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public FederateLoggingServiceCalls()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public FederateLoggingServiceCalls( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public FederateLoggingServiceCalls( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
