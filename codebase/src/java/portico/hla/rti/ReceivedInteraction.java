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
 * This packages the information supplied to the federate for receiveInteraction. The parameters
 * are conceptually an array with an initial capacity and the ability to grow. You enumerate by
 * stepping index from 0 to size()-1.
 * 
 */
public interface ReceivedInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Return order type
	 * 
	 * @return int order type
	 */
	public int getOrderType();

	/**
	 * Return parameter handle at index position.
	 * 
	 * @return int parameter handle
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getParameterHandle( int index ) throws ArrayIndexOutOfBounds;

	/**
	 * Return Region out of which interaction was received.
	 */
	public Region getRegion();

	/**
	 * Return transport type
	 * 
	 * @return int transport type
	 */
	public int getTransportType();

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
	 * @return int Number of parameter handle-value pairs
	 */
	public int size();
} 
