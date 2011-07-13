package hla.rti13.java1;

public class RestoreNotRequested extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public RestoreNotRequested( String reason )
	{
		super( reason );
	}

	public RestoreNotRequested( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public RestoreNotRequested()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public RestoreNotRequested( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public RestoreNotRequested( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
