package hla.rti13.java1;

public class TimeAdvanceWasNotInProgress extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public TimeAdvanceWasNotInProgress( String reason )
	{
		super( reason );
	}

	public TimeAdvanceWasNotInProgress( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public TimeAdvanceWasNotInProgress()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public TimeAdvanceWasNotInProgress( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public TimeAdvanceWasNotInProgress( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
