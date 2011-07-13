/*
 *   This file is provided as a copy from the SISO (http://www.sisostds.org) DLC standard for
 *   HLA 1516 (SISO-STD-004.1-2004).
 * 
 *   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *   OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED.  IN NO EVENT SHALL THE DEVELOPERS OF THIS PROJECT OR
 *   ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *   SUCH DAMAGE.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 */
package hla.rti1516;

public class TransportationType implements java.io.Serializable
{
	private static final long serialVersionUID = 98121116105109L;
	
	protected int _value; //each instance's value 

	private static final int _lowestValue = 1;

	protected static int _nextToAssign = _lowestValue; //begins at lowest 

	/** 
	 This is the only public constructor. Each user-defined instance of a 
	 TransportationType 
	 must be initialized with one of the defined static values. 
	 * @param otherTransportationTypeValue must be a defined static value or 
	 another instance. 
	 */
	public TransportationType( TransportationType otherTransportationTypeValue )
	{
		_value = otherTransportationTypeValue._value;
	}

	/** 
	 Private to class and subclasses 
	 */
	protected TransportationType()
	{
		_value = _nextToAssign++;
	}

	TransportationType( int value ) throws RTIinternalError
	{
		_value = value;
		if( value < _lowestValue || value >= _nextToAssign )
			throw new RTIinternalError( "TransportationType: illegal value " + value );
	}

	/** 
	 * @return String with value "TransportationType(n)" where n is value 
	 */
	public String toString()
	{
		return "TransportationType(" + _value + ")";
	}

	/** 
	 Allows comparison with other instance of same type. 
	 * @return true if supplied object is of type TransportationType and has 
	 same value; 
	 false otherwise 
	 */
	public boolean equals( Object otherTransportationTypeValue )
	{
		if( otherTransportationTypeValue instanceof TransportationType )
			return _value == ((TransportationType)otherTransportationTypeValue)._value;
		else
			return false;
	}

	public int hashCode()
	{
		return _value;
	}

	public int encodedLength()
	{
		return 1;
	}

	public void encode( byte[] buffer, int offset )
	{
		buffer[offset] = (byte)_value;
	}

	public static TransportationType decode( byte[] buffer, int offset ) throws CouldNotDecode
	{
		int val = buffer[offset];
		TransportationType neo;
		try
		{
			neo = new TransportationType( val );
		}
		catch( RTIinternalError e )
		{
			throw new CouldNotDecode( e.getMessage() );
		}
		return neo;
	}

	static public final TransportationType HLA_RELIABLE = new TransportationType();

	static public final TransportationType HLA_BEST_EFFORT = new TransportationType();
}
