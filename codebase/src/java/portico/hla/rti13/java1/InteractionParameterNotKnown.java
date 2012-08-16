package hla.rti13.java1;

public class InteractionParameterNotKnown extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InteractionParameterNotKnown( String reason )
	{
		super( reason );
	}

	public InteractionParameterNotKnown( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InteractionParameterNotKnown()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InteractionParameterNotKnown( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InteractionParameterNotKnown( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
