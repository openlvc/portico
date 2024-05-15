/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-201X".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Helper class for locating LogicalTimeFactory. Uses Service concept described
 * by ServiceLoader.
 *
 * @see ServiceLoader
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
      ServiceLoader<LogicalTimeFactory> loader = ServiceLoader.load(LogicalTimeFactory.class);
      for (LogicalTimeFactory logicalTimeFactory : loader) {
         if (logicalTimeFactory.getName().equals(name)) {
            return logicalTimeFactory;
         }
      }
      return null;
   }

   public static <T extends LogicalTimeFactory> T getLogicalTimeFactory(Class<T> logicalTimeFactoryClass)
   {
      ServiceLoader<LogicalTimeFactory> loader = ServiceLoader.load(LogicalTimeFactory.class);
      for (LogicalTimeFactory logicalTimeFactory : loader) {
         if (logicalTimeFactoryClass.isInstance(logicalTimeFactory)) {
            return logicalTimeFactoryClass.cast(logicalTimeFactory);
         }
      }
      return null;
   }

   public static Set<LogicalTimeFactory> getAvailableLogicalTimeFactories()
   {
      Set<LogicalTimeFactory> factories = new HashSet<LogicalTimeFactory>();
      ServiceLoader<LogicalTimeFactory> loader = ServiceLoader.load(LogicalTimeFactory.class);
      for (LogicalTimeFactory logicalTimeFactory : loader) {
         factories.add(logicalTimeFactory);
      }
      return factories;
   }
}