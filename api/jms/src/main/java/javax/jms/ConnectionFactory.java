/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.jms;

/** A <CODE>ConnectionFactory</CODE> object encapsulates a set of connection 
  * configuration 
  * parameters that has been defined by an administrator. A client uses 
  * it to create a connection with a JMS provider.
  *
  * <P>A <CODE>ConnectionFactory</CODE> object is a JMS administered object and
  *  supports concurrent use.
  *
  * <P>JMS administered objects are objects containing configuration 
  * information that are created by an administrator and later used by 
  * JMS clients. They make it practical to administer the JMS API in the 
  * enterprise.
  *
  * <P>Although the interfaces for administered objects do not explicitly 
  * depend on the Java Naming and Directory Interface (JNDI) API, the JMS API 
  * establishes the convention that JMS clients find administered objects by 
  * looking them up in a JNDI namespace.
  *
  * <P>An administrator can place an administered object anywhere in a 
  * namespace. The JMS API does not define a naming policy.
  *
  * <P>It is expected that JMS providers will provide the tools an
  * administrator needs to create and configure administered objects in a 
  * JNDI namespace. JMS provider implementations of administered objects 
  * should be both <CODE>javax.jndi.Referenceable</CODE> and 
  * <CODE>java.io.Serializable</CODE> so that they can be stored in all 
  * JNDI naming contexts. In addition, it is recommended that these 
  * implementations follow the JavaBeans<SUP><FONT SIZE="-2">TM</FONT></SUP> 
  * design patterns.
  *
  * <P>This strategy provides several benefits:
  *
  * <UL>
  *   <LI>It hides provider-specific details from JMS clients.
  *   <LI>It abstracts administrative information into objects in the Java 
  *       programming language ("Java objects")
  *       that are easily organized and administered from a common 
  *       management console.
  *   <LI>Since there will be JNDI providers for all popular naming 
  *       services, this means that JMS providers can deliver one implementation 
  *       of administered objects that will run everywhere.
  * </UL>
  *
  * <P>An administered object should not hold on to any remote resources. 
  * Its lookup should not use remote resources other than those used by the
  * JNDI API itself.
  *
  * <P>Clients should think of administered objects as local Java objects. 
  * Looking them up should not have any hidden side effects or use surprising 
  * amounts of local resources.
  *
  * @version     1.1 - February 1, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.Connection
  * @see         javax.jms.QueueConnectionFactory
  * @see         javax.jms.TopicConnectionFactory
  */

public interface ConnectionFactory {
        /** Creates a connection with the default user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *
      * @return a newly created connection
      *
      * @exception JMSException if the JMS provider fails to create the
      *                         connection due to some internal error.
      * @exception JMSSecurityException  if client authentication fails due to 
      *                         an invalid user name or password.
       * @since 1.1 
     */ 

    Connection
    createConnection() throws JMSException;


    /** Creates a connection with the specified user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *  
      * @param userName the caller's user name
      * @param password the caller's password
      *  
      * @return a newly created  connection
      *
      * @exception JMSException if the JMS provider fails to create the 
      *                         connection due to some internal error.
      * @exception JMSSecurityException  if client authentication fails due to 
      *                         an invalid user name or password.
      * @since 1.1  
      */ 

    Connection
    createConnection(String userName, String password) 
					     throws JMSException;
}
