/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e;

import javax.imageio.spi.ServiceRegistry;

import org.portico.impl.hla1516e.types.time.DoubleTimeFactory;
import org.portico.impl.hla1516e.types.time.LongTimeFactory;

import java.util.HashSet;
//import java.util.Iterator;
import java.util.Set;

/**
 * Helper class for locating LogicalTimeFactory. Uses Service concept described
 * by ServiceRegistry.
 *
 * @see ServiceRegistry
 */
public class LogicalTimeFactoryFactory {
   /**
    * Locates and constructs a LogicalTimeFactory matching the specified name.
    * Each federation chooses its implementation by passing the appropriate name
    * to createFederationExecution.
    * If the supplied name is the empty string, the HLAfloat64TimeFactory is
    * returned.  If the supplied implementation name does not match any name
    * supported by the library, then a NULL pointer is returned.
    *
    * @param name
    * @return
    */
   public static LogicalTimeFactory getLogicalTimeFactory(String name)
   {
      if (name.equals("")) {
         name = "HLAfloat64Time";
      }
      /** Java 9 syas NO ServiceRegistry use for generic types
      Iterator<LogicalTimeFactory> i = ServiceRegistry.lookupProviders(LogicalTimeFactory.class);
      while (i.hasNext()) {
         LogicalTimeFactory logicalTimeFactory = i.next();
         if (logicalTimeFactory.getName().equals(name)) {
            return logicalTimeFactory;
         }
      }
      return null;*/

      if( name.equalsIgnoreCase("HLAfloat64Time") )
    	  return new DoubleTimeFactory();
      else if( name.equalsIgnoreCase("HLAinteger64Time") )
    	  return new LongTimeFactory();
      else
    	  return null;
   }

   public static <T extends LogicalTimeFactory> T getLogicalTimeFactory(Class<T> logicalTimeFactoryClass)
   {
      /** Java 9 syas NO ServiceRegistry use for generic types
      Iterator<LogicalTimeFactory> i = ServiceRegistry.lookupProviders(LogicalTimeFactory.class);
      while (i.hasNext()) {
         LogicalTimeFactory logicalTimeFactory = i.next();
         if (logicalTimeFactoryClass.isInstance(logicalTimeFactory)) {
            return logicalTimeFactoryClass.cast(logicalTimeFactory);
         }
      }
      return null;*/

      if( logicalTimeFactoryClass.isInstance(new DoubleTimeFactory()) )
          return logicalTimeFactoryClass.cast( new DoubleTimeFactory() );
      else if( logicalTimeFactoryClass.isInstance(new LongTimeFactory()) )
          return logicalTimeFactoryClass.cast( new LongTimeFactory() );
      else
    	  return null;
      
   }

   public static Set<LogicalTimeFactory> getAvailableLogicalTimeFactories()
   {
      Set<LogicalTimeFactory> factories = new HashSet<LogicalTimeFactory>();
      factories.add( new DoubleTimeFactory() );
      factories.add( new LongTimeFactory() );
      return factories;

      /** Java 9 syas NO ServiceRegistry use for generic types
      Iterator<LogicalTimeFactory> i = ServiceRegistry.lookupProviders(LogicalTimeFactory.class);
      Set<LogicalTimeFactory> factories = new HashSet<LogicalTimeFactory>();
      while (i.hasNext()) {
         LogicalTimeFactory logicalTimeFactory = i.next();
         factories.add(logicalTimeFactory);
      }
      return factories;*/
   }
}