/*
 *   This file is a direct copy from the SISO (http://www.sisostds.org) DLC standard for
 *   HLA 1.3 (SISO-STD-004-2004).
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
package hla.rti;

/**
 * Superclass of all exceptions thrown by the RTI. All RTI exceptions must be
 * caught or specified.
 */
public class RTIexception extends Exception
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected int _serial;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * @param reason String to be carried with exception; equivalent of 'message' parameter in
	 *        .java.lang.Exception
	 */
	public RTIexception( String reason )
	{
		super( reason );
		_serial = 0;
	}

	/**
	 * @param serial serial number also printed with the exception: programmer can assign these to
	 *        tell where the exception was thrown
	 */
	public RTIexception( String reason, int serial )
	{
		super( reason );
		_serial = serial;
	}
	
	public RTIexception( String reason, Throwable cause )
	{
		super( reason, cause );
	}
	
	public RTIexception( Throwable cause )
	{
		super( cause );
	}
	
	public RTIexception()
	{
		super();
	}
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public int getSerial()
	{
		return _serial;
	}

	public String toString()
	{
		String rep = super.toString();
		return rep += " serial:" + _serial;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
} 

