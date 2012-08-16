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

public final class OrderType implements java.io.Serializable
{
	private static final long serialVersionUID = 98121116105109L;
	
	private int _value; // each instance's value

	private static final int _lowestValue = 1;

	private static int _nextToAssign = _lowestValue; // begins at lowest

	/**
     * This is the only public constructor. Each user-defined instance of a OrderType must be
     * initialized with one of the defined static values.
     * 
     * @param otherOrderTypeValue must be a defined static value or another instance.
     */
	public OrderType( OrderType otherOrderTypeValue )
	{
		_value = otherOrderTypeValue._value;
	}

	/**
     * Private to class
     */
	private OrderType()
	{
		_value = _nextToAssign++;
	}

	OrderType( int value ) throws RTIinternalError
	{
		_value = value;
		if( value < _lowestValue || value >= _nextToAssign )
			throw new RTIinternalError( "OrderType: illegal value " + value );
	}

	/**
     * @return String with value "OrderType(n)" where n is value
     */
	public String toString()
	{
		return "OrderType(" + _value + ")";
	}

	/**
     * Allows comparison with other instance of same type.
     * 
     * @return true if supplied object is of type OrderType and has same value; false otherwise
     */
	public boolean equals( Object otherOrderTypeValue )
	{
		if( otherOrderTypeValue instanceof OrderType )
			return _value == ((OrderType)otherOrderTypeValue)._value;
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

	public static OrderType decode( byte[] buffer, int offset ) throws CouldNotDecode
	{
		int val = buffer[offset];
		OrderType neo;
		try
		{
			neo = new OrderType( val );
		}
		catch( RTIinternalError e )
		{
			throw new CouldNotDecode( e.getMessage() );
		}
		return neo;
	}

	static public final OrderType RECEIVE = new OrderType();

	static public final OrderType TIMESTAMP = new OrderType();
} 

