package org.jboss.webbeans.test.unit.implementation.enterprise.lifecycle;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.enterprise.inject.Current;
import javax.enterprise.inject.Initializer;

@Stateless
public class Mainz implements AlteStadt
{
   @Current
   private RoemerPassage placeOfInterest;
   
   private GutenbergMuseum anotherPlaceOfInterest;
   
   private String name;

   public String getPlaceOfInterest()
   {
      return name;
   }

   @PostConstruct
   public void performPostConstructChecks()
   {
      if ( placeOfInterest != null )
         name = placeOfInterest.getName();
   }
   
   @Initializer
   public void initializeBean(@Current GutenbergMuseum pointOfInterest)
   {
      this.anotherPlaceOfInterest = pointOfInterest;
   }

   public GutenbergMuseum getAnotherPlaceOfInterest()
   {
      return anotherPlaceOfInterest;
   }
}
