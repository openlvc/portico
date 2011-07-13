package hla.rti13.java1;

public class AttributeAlreadyBeingDivested extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AttributeAlreadyBeingDivested( String reason )
	{
		super( reason );
	}

	public AttributeAlreadyBeingDivested( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AttributeAlreadyBeingDivested()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AttributeAlreadyBeingDivested( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AttributeAlreadyBeingDivested( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
