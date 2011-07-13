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
 * This packages the parameters supplied to the RTI for sendInteraction. This is conceptually an
 * array with an initial capacity and the ability to grow. You enumerate by stepping index from 0
 * to size()-1.
 * 
 */
public interface SuppliedParameters
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Add pair beyond last index.
	 * 
	 * @param handle int
	 * @param value byte[]
	 */
	public void add( int handle, byte[] value );

	/**
	 * Removes all handles & values.
	 */
	public void empty();

	/**
	 * Return handle at index position.
	 * 
	 * @return int parameter handle
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getHandle( int index ) throws ArrayIndexOutOfBounds;

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
	 * Remove handle & value corresponding to handle. All other elements shifted down. Not safe
	 * during iteration.
	 * 
	 * @param handle int
	 * @exception hla.rti.ArrayIndexOutOfBounds if handle not in set
	 */
	public void remove( int handle ) throws ArrayIndexOutOfBounds;

	/**
	 * Remove handle & value at index position. All other elements shifted down. Not safe during
	 * iteration.
	 * 
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public void removeAt( int index ) throws ArrayIndexOutOfBounds;

	/** 
	 * @return int Number of elements 
	 */
	public int size();
}
