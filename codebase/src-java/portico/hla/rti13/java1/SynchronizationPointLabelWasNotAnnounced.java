package hla.rti13.java1;

public class SynchronizationPointLabelWasNotAnnounced extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public SynchronizationPointLabelWasNotAnnounced( String reason )
	{
		super( reason );
	}

	public SynchronizationPointLabelWasNotAnnounced( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public SynchronizationPointLabelWasNotAnnounced()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public SynchronizationPointLabelWasNotAnnounced( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public SynchronizationPointLabelWasNotAnnounced( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
