package org.jboss.weld.tests.extensions.annotatedType.invalidParameters;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

public class Clothes
{
   
   private static InjectionPoint injectionPoint;
   
   @Inject 
   public void setInjectionPoint(InjectionPoint injectionPoint)
   {
      Clothes.injectionPoint = injectionPoint;
   }
   
   public static void reset()
   {
      injectionPoint = null;
   }
   
   public static InjectionPoint getInjectionPoint()
   {
      return injectionPoint;
   }

}
