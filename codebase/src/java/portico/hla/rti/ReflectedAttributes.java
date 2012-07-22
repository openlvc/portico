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
 * This packages the attributes supplied to the federate for reflectAttributeValues. This is
 * conceptually an array with an initial capacity and the ability to grow. You enumerate by
 * stepping index from 0 to size()-1.
 * 
 */
public interface ReflectedAttributes
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Return attribute handle at index position.
	 * 
	 * @return int attribute handle
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getAttributeHandle( int index ) throws ArrayIndexOutOfBounds;

	/**
	 * Return order handle at index position.
	 * 
	 * @return int order type
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getOrderType( int index ) throws ArrayIndexOutOfBounds;

	/**
	 * Return Region handle at index position.
	 * 
	 * @return int region handle
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public Region getRegion( int index ) throws ArrayIndexOutOfBounds;

	/**
	 * Return transport handle at index position.
	 * 
	 * @return int transport type
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getTransportType( int index ) throws ArrayIndexOutOfBounds;

	/**
	 * Return copy of value at index position.
	 * 
	 * @return byte[] copy (clone) of value
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public byte[] getValue( int index ) throws ArrayIndexOutOfBounds;

	/**
	 * Return length of value at index position.
	 * 
	 * @return int value length
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getValueLength( int index ) throws ArrayIndexOutOfBounds;

	/**
	 * Get the reference of the value at position index (not a clone)
	 * 
	 * @return byte[] the reference
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public byte[] getValueReference( int index ) throws ArrayIndexOutOfBounds;

	/**
	 * @return int Number of attribute handle-value pairs
	 */
	public int size();
} 
