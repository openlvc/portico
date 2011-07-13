package hla.rti13.java1;

public class OwnershipAcquisitionPending extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public OwnershipAcquisitionPending( String reason )
	{
		super( reason );
	}

	public OwnershipAcquisitionPending( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public OwnershipAcquisitionPending()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public OwnershipAcquisitionPending( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public OwnershipAcquisitionPending( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
