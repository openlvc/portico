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
 * 
 * Represents a Region in federate's space. A federate creates a Region by calling
 * RTIambassador.createRegion. The federate mdifies the Region by invoking Region methods on it.
 * The federate modifies a Region by first modifying its local instance, then supplying the
 * modified instance to RTIambassador.notifyOfRegionModification.
 * 
 * The Region is conceptually an array, with the extents addressed by index running from 0 to
 * getNumberOfExtents()-1.
 */
public interface Region
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * @return long Number of extents in this Region
	 */
	public long getNumberOfExtents();

	/**
	 * @return long Lower bound of extent along indicated dimension
	 * @param extentIndex int
	 * @param dimensionHandle int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public long getRangeLowerBound( int extentIndex, int dimensionHandle )
		throws ArrayIndexOutOfBounds;

	/**
	 * @return long Upper bound of extent along indicated dimension
	 * @param extentIndex int
	 * @param dimensionHandle int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public long getRangeUpperBound( int extentIndex, int dimensionHandle )
		throws ArrayIndexOutOfBounds;

	/**
	 * @return int Handle of routing space of which this Region is a subset
	 */
	public int getSpaceHandle();

	/**
	 * Modify lower bound of extent along indicated dimension.
	 * 
	 * @param extentIndex int
	 * @param dimensionHandle int
	 * @param newLowerBound long
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public void setRangeLowerBound( int extentIndex, int dimensionHandle, long newLowerBound )
		throws ArrayIndexOutOfBounds;

	/**
	 * Modify upper bound of extent along indicated dimension.
	 * 
	 * @param extentIndex int
	 * @param dimensionHandle int
	 * @param newUpperBound long
	 * @exception hla.rti.ArrayIndexOutOfBounds The exception description.
	 */
	public void setRangeUpperBound( int extentIndex, int dimensionHandle, long newUpperBound )
		throws ArrayIndexOutOfBounds;
} 
