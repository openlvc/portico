package hla.rti13.java1;

public class TimeAdvanceAlreadyInProgress extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public TimeAdvanceAlreadyInProgress( String reason )
	{
		super( reason );
	}

	public TimeAdvanceAlreadyInProgress( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public TimeAdvanceAlreadyInProgress()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public TimeAdvanceAlreadyInProgress( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public TimeAdvanceAlreadyInProgress( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
