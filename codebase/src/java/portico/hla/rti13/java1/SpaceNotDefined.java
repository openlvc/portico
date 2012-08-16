package hla.rti13.java1;

public class SpaceNotDefined extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public SpaceNotDefined( String reason )
	{
		super( reason );
	}

	public SpaceNotDefined( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public SpaceNotDefined()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public SpaceNotDefined( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public SpaceNotDefined( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
