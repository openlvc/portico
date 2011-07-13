package hla.rti13.java1;

public class AttributeAcquisitionWasNotRequested extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AttributeAcquisitionWasNotRequested( String reason )
	{
		super( reason );
	}

	public AttributeAcquisitionWasNotRequested( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AttributeAcquisitionWasNotRequested()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AttributeAcquisitionWasNotRequested( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AttributeAcquisitionWasNotRequested( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
