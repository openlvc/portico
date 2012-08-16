package hla.rti13.java1;

public class EventNotKnown extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public EventNotKnown( String reason )
	{
		super( reason );
	}

	public EventNotKnown( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public EventNotKnown()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public EventNotKnown( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public EventNotKnown( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
