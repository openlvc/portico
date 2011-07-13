package hla.rti13.java1;

public class RestoreInProgress extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public RestoreInProgress( String reason )
	{
		super( reason );
	}

	public RestoreInProgress( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public RestoreInProgress()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public RestoreInProgress( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public RestoreInProgress( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
