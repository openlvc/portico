package hla.rti13.java1;

public class AttributeNotPublished extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AttributeNotPublished( String reason )
	{
		super( reason );
	}

	public AttributeNotPublished( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AttributeNotPublished()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AttributeNotPublished( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AttributeNotPublished( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
