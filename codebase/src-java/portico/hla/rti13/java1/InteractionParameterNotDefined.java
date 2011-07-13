package hla.rti13.java1;

public class InteractionParameterNotDefined extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InteractionParameterNotDefined( String reason )
	{
		super( reason );
	}

	public InteractionParameterNotDefined( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InteractionParameterNotDefined()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InteractionParameterNotDefined( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InteractionParameterNotDefined( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
