package hla.rti13.java1;

public class FederateWasNotAskedToReleaseAttribute extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public FederateWasNotAskedToReleaseAttribute( String reason )
	{
		super( reason );
	}

	public FederateWasNotAskedToReleaseAttribute( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public FederateWasNotAskedToReleaseAttribute()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public FederateWasNotAskedToReleaseAttribute( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public FederateWasNotAskedToReleaseAttribute( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
