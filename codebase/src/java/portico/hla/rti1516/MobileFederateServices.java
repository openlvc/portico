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

/**
 * Conveys the interfaces for all services that a federate must supply and which may not execute
 * in the federate's space.
 * 
 */
public final class MobileFederateServices implements java.io.Serializable
{
	private static final long serialVersionUID = 98121116105109L;
	
	public hla.rti1516.LogicalTimeFactory _timeFactory;

	public hla.rti1516.LogicalTimeIntervalFactory _intervalFactory;

	/**
     * @param timeFactory hla.rti1516.LogicalTimeFactory
     * @param intervalFactory hla.rti1516.LogicalTimeIntervalFactory
     */
	public MobileFederateServices( LogicalTimeFactory timeFactory,
	                               LogicalTimeIntervalFactory intervalFactory )
	{
		_timeFactory = timeFactory;
		_intervalFactory = intervalFactory;
	}
}
//end MobileFederateServices 

