package org.jboss.webbeans.test.unit.bootstrap.singleSimple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.manager.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class BootstrapTest extends AbstractWebBeansTest
{
   
   @Test(groups="bootstrap")
   public void testSingleSimpleBean()
   {
      List<Bean<?>> beans = getCurrentManager().getBeans();
      Map<Class<?>, Bean<?>> classes = new HashMap<Class<?>, Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean instanceof RIBean)
         {
            classes.put(((RIBean<?>) bean).getType(), bean);
         }
      }
      assert classes.containsKey(Tuna.class);
   }
   
}
