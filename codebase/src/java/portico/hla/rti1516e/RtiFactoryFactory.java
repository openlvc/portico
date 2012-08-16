/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e;

import hla.rti1516e.exceptions.RTIinternalError;

import javax.imageio.spi.ServiceRegistry;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Helper class for locating RtiFactory. Uses Service concept described
 * by ServiceRegistry.
 *
 * @see ServiceRegistry
 */
public class RtiFactoryFactory {
   public static RtiFactory getRtiFactory(String name)
      throws
      RTIinternalError
   {
      Iterator<RtiFactory> i = ServiceRegistry.lookupProviders(RtiFactory.class);
      while (i.hasNext()) {
         RtiFactory rtiFactory = i.next();
         if (rtiFactory.rtiName().equals(name)) {
            return rtiFactory;
         }
      }
      throw new RTIinternalError("Cannot find factory matching " + name);
   }

   public static RtiFactory getRtiFactory()
      throws
      RTIinternalError
   {
      Iterator<RtiFactory> i = ServiceRegistry.lookupProviders(RtiFactory.class);
      if (i.hasNext()) {
         return i.next();
      }
      throw new RTIinternalError("Cannot find factory");
   }

   public static Set<RtiFactory> getAvailableRtiFactories()
   {
      Iterator<RtiFactory> i = ServiceRegistry.lookupProviders(RtiFactory.class);
      Set<RtiFactory> factories = new HashSet<RtiFactory>();
      while (i.hasNext()) {
         RtiFactory rtiFactory = i.next();
         factories.add(rtiFactory);
      }
      return factories;
   }
}
