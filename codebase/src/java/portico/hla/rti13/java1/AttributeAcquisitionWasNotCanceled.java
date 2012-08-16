package hla.rti13.java1;

public class AttributeAcquisitionWasNotCanceled extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AttributeAcquisitionWasNotCanceled( String reason )
	{
		super( reason );
	}

	public AttributeAcquisitionWasNotCanceled( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AttributeAcquisitionWasNotCanceled()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AttributeAcquisitionWasNotCanceled( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AttributeAcquisitionWasNotCanceled( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
