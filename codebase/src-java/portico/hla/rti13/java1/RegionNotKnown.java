package hla.rti13.java1;

public class RegionNotKnown extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public RegionNotKnown( String reason )
	{
		super( reason );
	}

	public RegionNotKnown( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public RegionNotKnown()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public RegionNotKnown( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public RegionNotKnown( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
