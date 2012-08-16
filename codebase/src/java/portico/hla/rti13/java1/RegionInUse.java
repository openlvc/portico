package hla.rti13.java1;

public class RegionInUse extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public RegionInUse( String reason )
	{
		super( reason );
	}

	public RegionInUse( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public RegionInUse()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public RegionInUse( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public RegionInUse( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
