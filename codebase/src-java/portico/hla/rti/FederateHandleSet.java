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
 * Interface for a set of federate handles.
 */
public interface FederateHandleSet
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Add the handle to the set. Won't squawk if handle already member.
	 * 
	 * @param handle int
	 */
	public void add( int handle );

	/**
	 * Classic clone
	 * 
	 * @return java.lang.Object
	 */
	public Object clone();

	/**
	 * Empties set of its members.
	 * 
	 */
	public void empty();

	/**
	 * Classic equals.
	 * 
	 * @return boolean: true if set of same type and same members.
	 * @param obj java.lang.Object
	 */
	public boolean equals( Object obj );

	public HandleIterator handles();

	/**
	 * Classic hashCode
	 * 
	 * @return int: hash code
	 */
	public int hashCode();

	/**
	 * 
	 * @return boolean: true if set empty.
	 */
	public boolean isEmpty();

	/**
	 * 
	 * @return boolean: true if handle is a meber
	 * @param handle int: an attribute handle
	 */
	public boolean isMember( int handle );

	/**
	 * Remove the handle from the set. Won't squawk if handle not a member.
	 * 
	 * @param handle int
	 */
	public void remove( int handle );

	/**
	 * 
	 * @return int: number of members
	 */
	public int size();

	/** 
	 * 
	 * @return java.lang.String 
	 */
	public String toString();
	
} 
