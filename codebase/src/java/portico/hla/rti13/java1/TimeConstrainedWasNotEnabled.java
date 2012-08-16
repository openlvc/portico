package hla.rti13.java1;

public class TimeConstrainedWasNotEnabled extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public TimeConstrainedWasNotEnabled( String reason )
	{
		super( reason );
	}

	public TimeConstrainedWasNotEnabled( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public TimeConstrainedWasNotEnabled()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public TimeConstrainedWasNotEnabled( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public TimeConstrainedWasNotEnabled( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
