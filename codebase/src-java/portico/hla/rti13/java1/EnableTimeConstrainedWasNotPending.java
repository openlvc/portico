package hla.rti13.java1;

public class EnableTimeConstrainedWasNotPending extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public EnableTimeConstrainedWasNotPending( String reason )
	{
		super( reason );
	}

	public EnableTimeConstrainedWasNotPending( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public EnableTimeConstrainedWasNotPending()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public EnableTimeConstrainedWasNotPending( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public EnableTimeConstrainedWasNotPending( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
