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

public interface LogicalTime extends Comparable, java.io.Serializable
{
	public boolean isInitial();

	public boolean isFinal();

	/**
     * Returns a LogicalTime whose value is (this + val).
     */
	public LogicalTime add( LogicalTimeInterval val ) throws IllegalTimeArithmetic;

	/**
     * Returns a LogicalTime whose value is (this - val).
     */
	public LogicalTime subtract( LogicalTimeInterval val ) throws IllegalTimeArithmetic;

	/**
     * Returns a LogicalTimeInterval whose value is the time interval between this and val.
     */
	public LogicalTimeInterval distance( LogicalTime val );

	public int compareTo( Object other );

	/**
     * Returns true iff this and other represent the same logical time Supports standard Java
     * mechanisms.
     */
	public boolean equals( Object other );

	/**
     * Two LogicalTimes for which equals() is true should yield same hash code
     */
	public int hashCode();

	public String toString();

	public int encodedLength();

	public void encode( byte[] buffer, int offset );
}//end LogicalTime 
