package hla.rti13.java1;

import org.portico.lrc.PorticoConstants;

/**
 * As with {@link MemoryExhausted}, this class is included in here only to prevent federates that
 * reference these variabled from breaking. These value have no use in Portico.
 */
public final class RTIconstants
{
	public static final int MAX_FEDERATION           = 128; // arbitrary for now
	public static final int MAX_FEDERATE             = PorticoConstants.MAX_FEDERATES;
	public static final int MAX_NAME_LENGTH          = Integer.MAX_VALUE; // shouldn't be a problem
	public static final int MAX_USER_TAG_LENGTH      = Integer.MAX_VALUE; // shouldn't be a problem
	public static final int MAX_OBJECT_CLASSES       = Integer.MAX_VALUE; // shouldn't be a problem
	public static final int MAX_ATTRIBUTES_PER_CLASS = Integer.MAX_VALUE; // shouldn't be a problem
	public static final int MAX_INTERACTION_CLASSES  = Integer.MAX_VALUE; // shouldn't be a problem
	public static final int MAX_PARAMETERS_PER_CLASS = Integer.MAX_VALUE; // shouldn't be a problem
	public static final int MAX_SPACES               = Integer.MAX_VALUE; // shouldn't be a problem
	public static final int MAX_DIMENSIONS_PER_SPACE = Integer.MAX_VALUE; // shouldn't be a problem
	public static final long MIN_EXTENT              = PorticoConstants.MIN_EXTENT;
	public static final long MAX_EXTENT              = PorticoConstants.MAX_EXTENT;
	public static final String DEFAULT_SPACE_NAME    = "defaultSpace";
	public static final String DEFAULT_SPACE_DIMENSION_NAME = "dimension";
}
