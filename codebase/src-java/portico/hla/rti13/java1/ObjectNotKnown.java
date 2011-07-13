package hla.rti13.java1;

public class ObjectNotKnown extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public ObjectNotKnown( String reason )
	{
		super( reason );
	}

	public ObjectNotKnown( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public ObjectNotKnown()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public ObjectNotKnown( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public ObjectNotKnown( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
