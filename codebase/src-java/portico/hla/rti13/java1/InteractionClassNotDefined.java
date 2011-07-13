package hla.rti13.java1;

public class InteractionClassNotDefined extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InteractionClassNotDefined( String reason )
	{
		super( reason );
	}

	public InteractionClassNotDefined( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InteractionClassNotDefined()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InteractionClassNotDefined( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InteractionClassNotDefined( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
