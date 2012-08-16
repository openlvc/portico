package hla.rti13.java1;

public class ObjectClassNotPublished extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public ObjectClassNotPublished( String reason )
	{
		super( reason );
	}

	public ObjectClassNotPublished( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public ObjectClassNotPublished()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public ObjectClassNotPublished( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public ObjectClassNotPublished( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
