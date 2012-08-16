/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e.encoding;

/**
 * Factory for the various HLA data types.
 */
public interface EncoderFactory {
   HLAASCIIchar createHLAASCIIchar();

   HLAASCIIchar createHLAASCIIchar(byte b);

   HLAASCIIstring createHLAASCIIstring();

   HLAASCIIstring createHLAASCIIstring(String s);

   HLAboolean createHLAboolean();

   HLAboolean createHLAboolean(boolean b);

   HLAbyte createHLAbyte();

   HLAbyte createHLAbyte(byte b);

   <T extends DataElement> HLAvariantRecord<T> createHLAvariantRecord(T discriminant);

   HLAfixedRecord createHLAfixedRecord();

   <T extends DataElement> HLAfixedArray<T> createHLAfixedArray(DataElementFactory<T> factory, int size);

   <T extends DataElement> HLAfixedArray<T> createHLAfixedArray(T... elements);

   HLAfloat32BE createHLAfloat32BE();

   HLAfloat32BE createHLAfloat32BE(float f);

   HLAfloat32LE createHLAfloat32LE();

   HLAfloat32LE createHLAfloat32LE(float f);

   HLAfloat64BE createHLAfloat64BE();

   HLAfloat64BE createHLAfloat64BE(double d);

   HLAfloat64LE createHLAfloat64LE();

   HLAfloat64LE createHLAfloat64LE(double d);

   HLAinteger16BE createHLAinteger16BE();

   HLAinteger16BE createHLAinteger16BE(short s);

   HLAinteger16LE createHLAinteger16LE();

   HLAinteger16LE createHLAinteger16LE(short s);

   HLAinteger32BE createHLAinteger32BE();

   HLAinteger32BE createHLAinteger32BE(int i);

   HLAinteger32LE createHLAinteger32LE();

   HLAinteger32LE createHLAinteger32LE(int i);

   HLAinteger64BE createHLAinteger64BE();

   HLAinteger64BE createHLAinteger64BE(long l);

   HLAinteger64LE createHLAinteger64LE();

   HLAinteger64LE createHLAinteger64LE(long l);

   HLAoctet createHLAoctet();

   HLAoctet createHLAoctet(byte b);

   HLAoctetPairBE createHLAoctetPairBE();

   HLAoctetPairBE createHLAoctetPairBE(short s);

   HLAoctetPairLE createHLAoctetPairLE();

   HLAoctetPairLE createHLAoctetPairLE(short s);

   HLAopaqueData createHLAopaqueData();

   HLAopaqueData createHLAopaqueData(byte[] b);

   HLAunicodeChar createHLAunicodeChar();

   HLAunicodeChar createHLAunicodeChar(short c);

   HLAunicodeString createHLAunicodeString();

   HLAunicodeString createHLAunicodeString(String s);

   <T extends DataElement> HLAvariableArray<T> createHLAvariableArray(DataElementFactory<T> factory, T... elements);
}
