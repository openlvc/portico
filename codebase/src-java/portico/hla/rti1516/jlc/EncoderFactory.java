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
package hla.rti1516.jlc;

import hla.rti1516.LogicalTime; 

/**
 * Factory for the various HLA data types.
 */
public interface EncoderFactory
{
	HLAASCIIchar createHLAASCIIchar();

	HLAASCIIchar createHLAASCIIchar( byte b );

	HLAASCIIstring createHLAASCIIstring();

	HLAASCIIstring createHLAASCIIstring( String s );

	HLAboolean createHLAboolean();

	HLAboolean createHLAboolean( boolean b );

	HLAbyte createHLAbyte();

	HLAbyte createHLAbyte( byte b );

	HLAfixedRecord createHLAfixedRecord();

	HLAfloat32BE createHLAfloat32BE();

	HLAfloat32BE createHLAfloat32BE( float f );

	HLAfloat32LE createHLAfloat32LE();

	HLAfloat32LE createHLAfloat32LE( float f );

	HLAfloat64BE createHLAfloat64BE();

	HLAfloat64BE createHLAfloat64BE( double d );

	HLAfloat64LE createHLAfloat64LE();

	HLAfloat64LE createHLAfloat64LE( double d );

	HLAhandle createHLAhandle();

	HLAhandle createHLAhandle( byte[] b );

	HLAinteger16BE createHLAinteger16BE();

	HLAinteger16BE createHLAinteger16BE( short s );

	HLAinteger16LE createHLAinteger16LE();

	HLAinteger16LE createHLAinteger16LE( short s );

	HLAinteger32BE createHLAinteger32BE();

	HLAinteger32BE createHLAinteger32BE( int i );

	HLAinteger32LE createHLAinteger32LE();

	HLAinteger32LE createHLAinteger32LE( int i );

	HLAinteger64BE createHLAinteger64BE();

	HLAinteger64BE createHLAinteger64BE( long l );

	HLAinteger64LE createHLAinteger64LE();

	HLAinteger64LE createHLAinteger64LE( long l );

	HLAlogicalTime createHLAlogicalTime();

	HLAlogicalTime createHLAlogicalTime( LogicalTime t );

	HLAoctet createHLAoctet();

	HLAoctet createHLAoctet( byte b );

	HLAoctetPairBE createHLAoctetPairBE();

	HLAoctetPairBE createHLAoctetPairBE( short s );

	HLAoctetPairLE createHLAoctetPairLE();

	HLAoctetPairLE createHLAoctetPairLE( short s );

	HLAopaqueData createHLAopaqueData();

	HLAopaqueData createHLAopaqueData( byte[] b );

	HLAunicodeString createHLAunicodeString();

	HLAunicodeString createHLAunicodeString( String s );

	HLAvariableArray createHLAvariableArray();

	HLAvariableArray createHLAvariableArray( DataElementFactory factory );
}
