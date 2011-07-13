package hla.rti13.java1;

public class ObjectClassNotSubscribed extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public ObjectClassNotSubscribed( String reason )
	{
		super( reason );
	}

	public ObjectClassNotSubscribed( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public ObjectClassNotSubscribed()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public ObjectClassNotSubscribed( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public ObjectClassNotSubscribed( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
