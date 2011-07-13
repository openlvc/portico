package hla.rti13.java1;

public class InteractionClassNotPublished extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InteractionClassNotPublished( String reason )
	{
		super( reason );
	}

	public InteractionClassNotPublished( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InteractionClassNotPublished()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InteractionClassNotPublished( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InteractionClassNotPublished( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
