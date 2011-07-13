package hla.rti13.java1;

public class InteractionClassNotKnown extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InteractionClassNotKnown( String reason )
	{
		super( reason );
	}

	public InteractionClassNotKnown( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InteractionClassNotKnown()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InteractionClassNotKnown( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InteractionClassNotKnown( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
