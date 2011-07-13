package hla.rti13.java1;

public class ObjectClassNotKnown extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public ObjectClassNotKnown( String reason )
	{
		super( reason );
	}

	public ObjectClassNotKnown( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public ObjectClassNotKnown()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public ObjectClassNotKnown( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public ObjectClassNotKnown( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
