package hla.rti13.java1;

public class SpecifiedSaveLabelDoesNotExist extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public SpecifiedSaveLabelDoesNotExist( String reason )
	{
		super( reason );
	}

	public SpecifiedSaveLabelDoesNotExist( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public SpecifiedSaveLabelDoesNotExist()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public SpecifiedSaveLabelDoesNotExist( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public SpecifiedSaveLabelDoesNotExist( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
