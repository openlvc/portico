package hla.rti13.java1;

public class EnableTimeConstrainedPending extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public EnableTimeConstrainedPending( String reason )
	{
		super( reason );
	}

	public EnableTimeConstrainedPending( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public EnableTimeConstrainedPending()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public EnableTimeConstrainedPending( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public EnableTimeConstrainedPending( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
