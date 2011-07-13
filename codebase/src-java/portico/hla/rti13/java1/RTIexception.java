package hla.rti13.java1;

public class RTIexception extends java.lang.Exception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public int _serial;

	public RTIexception( String reason, int serial )
	{
		super( reason );
		_serial = serial;
	}

	/**
     * Just create an empty exception
     */
    public RTIexception()
    {
	    super();
    }

    /**
     * @param message The message to create the exception with
     */
    public RTIexception( String message )
    {
	    super( message );
    }

    /**
     * @param cause The cause of the exception
     */
    public RTIexception( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public RTIexception( String message, Throwable cause )
    {
	    super( message, cause );
    }
   
	public String toString()
	{
		return super.toString() + " (serial " + _serial + ")";
	}
}
