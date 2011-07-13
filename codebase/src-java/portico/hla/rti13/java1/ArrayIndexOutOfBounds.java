package hla.rti13.java1;

public class ArrayIndexOutOfBounds extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public ArrayIndexOutOfBounds( String reason )
	{
		super( reason );
	}

	public ArrayIndexOutOfBounds( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public ArrayIndexOutOfBounds()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public ArrayIndexOutOfBounds( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public ArrayIndexOutOfBounds( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
