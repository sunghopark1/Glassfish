/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.amx.impl.config;

import org.glassfish.admin.amx.core.AMXConstants;

import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;

import org.glassfish.admin.amx.impl.mbean.MBeanImplBase;
import org.glassfish.admin.amx.util.FeatureAvailability;
import org.glassfish.admin.amx.util.TypeCast;
import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.jvnet.hk2.config.*;

import javax.management.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.glassfish.admin.amx.annotation.Stability;
import org.glassfish.admin.amx.annotation.Taxonomy;
import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.config.AMXConfigConstants;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.impl.util.SingletonEnforcer;
import org.glassfish.admin.mbeanserver.PendingConfigBeansNew;
import org.glassfish.admin.mbeanserver.PendingConfigBeanJob;
import org.glassfish.api.amx.AMXLoader;

/**
    Responsible for loading AMXConfigProxy MBeans
 * @author llc
 */
@Taxonomy( stability=Stability.NOT_AN_INTERFACE )
public final class AMXConfigLoader extends MBeanImplBase
    implements AMXConfigLoaderMBean, TransactionListener
{
    private static void debug( final String s ) { System.out.println(s); }
    
    private final MBeanServer mMBeanServer;
    private volatile AMXConfigLoaderThread mLoaderThread;
    
    private final Transactions  mTransactions;
    private final Logger mLogger = ImplUtil.getLogger();

    private final PendingConfigBeansNew    mPendingConfigBeans;
    
    private final ConfigBeanRegistry mRegistry = ConfigBeanRegistry.getInstance();
    
        public
    AMXConfigLoader(
        final MBeanServer mbeanServer,
        final PendingConfigBeansNew pending,
        final Transactions transactions)
    {
        if ( transactions == null ) throw new IllegalStateException();
        
        mTransactions = transactions;
        mPendingConfigBeans = pending;
        mMBeanServer = mbeanServer;
    }
    
        private void
    configBeanRemoved( final ConfigBean cb )
    {
        final ObjectName objectName = mRegistry.getObjectName(cb);
        if ( objectName != null)
        {
            ImplUtil.unregisterAMXMBeans( mMBeanServer, objectName );
            mRegistry.remove(objectName);
        }
        else
        {
            // might or might not be there, but make sure it's gone!
            mPendingConfigBeans.remove( cb );
        }
    }
    
        private void
    issueAttributeChange(
        final ConfigBean cb,
        final String     xmlAttrName,
        final Object     oldValue,
        final Object     newValue,
        final long       whenChanged )
    {
        final ObjectName objectName = mRegistry.getObjectName(cb);
        if ( objectName == null )
        {
            throw new IllegalArgumentException( "Can't issue attribute change for null ObjectName for ConfigBean " + cb.getProxyType().getName() );
        }
        
        boolean changed = false;
        if ( oldValue != null )
        {
            changed = ! oldValue.equals(newValue);
        }
        else if ( newValue != null )
        {
            changed = ! newValue.equals(oldValue);
        }
        
        if ( changed )
        {
            //debug( "issueAttributeChange: " + xmlAttrName + ": {" + oldValue + " => " + newValue + "}");
            // FIXME
            //final AMXConfigImpl amx = AMXConfigImpl.class.cast( AMXImplBase.__getObjectRef__(mMBeanServer, objectName) );
            //amx.issueAttributeChangeForXmlAttrName( xmlAttrName, oldValue, newValue, whenChanged );
        }
    }
    
    private void sortAndDispatch(
        final List<PropertyChangeEvent> events,
        final long    whenChanged )
    {
        //debug( "AMXConfigLoader.sortAndDispatch: " + events.size() + " events" );
        final List<ConfigBean> newConfigBeans   = new ArrayList<ConfigBean>();
        final List<PropertyChangeEvent> remainingEvents = new ArrayList<PropertyChangeEvent>();

        //
        // Process all ADD and REMOVE events first, placing leftovers into 'remainingEvents'
        // We do this even if AMX is *not* running, because they new ConfigBeans need to go
        // into the queue for when and if AMX starts running.
        // 
        for ( final PropertyChangeEvent event : events) 
        {
            final Object oldValue = event.getOldValue();
            final Object newValue = event.getNewValue();
            final Object source   = event.getSource();
            final String propertyName = event.getPropertyName();
            
            if ( oldValue == null && newValue instanceof ConfigBeanProxy )
            {
                // ADD: a new ConfigBean was added
                final ConfigBeanProxy cbp = (ConfigBeanProxy)newValue;
                final ConfigBean cb = asConfigBean( ConfigBean.unwrap(cbp) );
                final Class<? extends ConfigBeanProxy> proxyClass = cb.getProxyType();
                //debug( "AMXConfigLoader.sortAndDispatch: process new ConfigBean: " + proxyClass.getNameProp() );
                final boolean doWait = amxIsRunning();
                handleConfigBean( cb, doWait );   // wait until registered
                newConfigBeans.add( cb );
            }
            else if ( newValue == null && oldValue instanceof ConfigBeanProxy && amxIsRunning() )
            {
                // REMOVE
                final ConfigBeanProxy cbp = (ConfigBeanProxy)oldValue;
                final ConfigBean cb = asConfigBean( ConfigBean.unwrap( cbp ) );
                //debug( "AMXConfigLoader.sortAndDispatch: remove (recursive) ConfigBean: " + mRegistry.getObjectName(cb) );
                configBeanRemoved( cb );
            }
            else
            {
                remainingEvents.add( event );
            }
        }
        
        // we can't issue events if AMX is not running!
        if ( amxIsRunning() )
        {
            for ( final PropertyChangeEvent event : remainingEvents) 
            {
                final Object oldValue = event.getOldValue();
                final Object newValue = event.getNewValue();
                final Object source   = event.getSource();
                final String propertyName = event.getPropertyName();
                final String sourceString = (source instanceof ConfigBeanProxy) ? ConfigSupport.proxyType((ConfigBeanProxy)source).getName() : "" + source;
                
                //debug( "AMXConfigLoader.sortAndDispatch (ATTR change): name = " + propertyName +
                //        ", oldValue = " + oldValue + ", newValue = " + newValue + ", source = " + sourceString );
                if ( source instanceof ConfigBeanProxy )
                {
                    // CHANGE
                    final ConfigBeanProxy cbp = (ConfigBeanProxy)source;
                    final ConfigBean cb = asConfigBean( ConfigBean.unwrap( cbp ) );
                    final Class<? extends ConfigBeanProxy> proxyClass = ConfigSupport.proxyType(cbp);
                    
                    // change events without prior add
                    // we shouldn't have to check for this, but it's a bug in the caller: no even for
                    // new ConfigBean, but changes come along anyway
                    
                    if ( mRegistry.getObjectName(cb) == null )
                    {
                        if ( ! newConfigBeans.contains(cb) )
                        {
                            //debug( "AMXConfigLoader.sortAndDispatch: process new ConfigBean (WORKAROUND): " + proxyClass.getNameProp() );
                            handleConfigBean( cb, false );
                            newConfigBeans.add( cb );
                        }
                    }
                    else
                    {
                        issueAttributeChange( cb, propertyName, oldValue, newValue, whenChanged);
                    }
                }
                else
                {
                    debug( "AMXConfigLoader.sortAndDispatch: WARNING: source is not a ConfigBean" );
                }
            }
        }
    }

        public void
    transactionCommited( final List<PropertyChangeEvent> changes)
    {
        //final PropertyChangeEvent[] changesArray = new PropertyChangeEvent[changes.size()];
        //changes.toArray( changesArray );
        sortAndDispatch( changes, System.currentTimeMillis() );
    }

        public void 
    unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
        // not interested...
    }

    @Override
		protected void
	postRegisterHook( Boolean registrationDone )
	{	
        super.postRegisterHook( registrationDone );
        
		if ( registrationDone.booleanValue() )
		{
            mPendingConfigBeans.swapTransactionListener(this);
		}
	}
    
        public void
    handleNotification( final Notification notif, final Object handback)
    {
    }
    
    @Override
        protected void
	postDeregisterHook()
	{
        super.postDeregisterHook();
        mTransactions.removeTransactionsListener( this );
	}



    private static final class Job
    {
        final ConfigBean mConfigBean;
        final CountDownLatch mLatch;
        
        public Job( final ConfigBean configBean, final CountDownLatch latch )
        {
            mConfigBean = configBean;
            mLatch      = latch;
        }
        
        public void releaseLatch()
        {
            if ( mLatch != null )
            {
                mLatch.countDown();
            }
        }
    }
    
    /**
        No items will be processd until {@link #start} is called.
     */
        protected void
    handleConfigBean( final ConfigBean cb, final boolean waitDone )
    {
        if ( mRegistry.getObjectName(cb) == null)
        {
            final PendingConfigBeanJob job = mPendingConfigBeans.add( cb, waitDone);
            if ( waitDone )
            {
                try
                {
                    job.await();
                }
                catch( InterruptedException e )
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

     /**
     */
        private ConfigBean
    getActualParent( final ConfigBean configBean )
    {
        ConfigBean parent = asConfigBean( configBean.parent() );
        if ( parent != null )
        {
            //debug( "config bean  " + configBean.getProxyType().getNameProp() + " has parent " + configBean.parent().getProxyType().getNameProp() );
            final ObjectName parentObjectName    = mRegistry.getObjectName(parent);
        }
        else
        {
            if ( ! configBean.getProxyType().getName().endsWith( "Domain" ) )
            {
                debug( "WARNING: parent is null for " + configBean.getProxyType().getName() + " (bug in ConfigBeans)");
            }
        }
        
        return parent;
    }
    
        private ObjectName
    getActualParentObjectName( final ConfigBean configBean )
    {
        ObjectName  parentObjectName = null;
        
        final ConfigBean parent = getActualParent( configBean );
        if ( parent != null )
        {
            parentObjectName    = mRegistry.getObjectName(parent);
        }
        
        return parentObjectName;
    }    
     /**
        @return a ConfigBean, or null if it's not a ConfigBean
     */
    @SuppressWarnings("unchecked")
    static ConfigBean asConfigBean( final Object o )
    {
        return (o instanceof ConfigBean) ? (ConfigBean)o : null;
    }


    /**
        Enable registration of MBeans, queued until now.
     */
        public synchronized ObjectName
    start()
    {
        if ( mLoaderThread == null )
        {
            mLoaderThread   = new AMXConfigLoaderThread( mPendingConfigBeans );
            mLoaderThread.setDaemon(true);
            mLoaderThread.start();
        
            // Make the listener start listening
            final ObjectName objectName = JMXUtil.newObjectName( AMXLoader.AMX3_SUPPORT_DOMAIN, "name=config" );
            try
            {
                mMBeanServer.registerMBean( this, objectName );
                //debug( "AMXConfigLoader.start(): registered self as " + objectName );
            }
            catch( Exception e )
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            SingletonEnforcer.register( AMXConfigLoader.class, this );
        }
        return null;
    }
    
        private synchronized boolean
    amxIsRunning()
    {
        return mLoaderThread != null;
    }
    
    private final class AMXConfigLoaderThread extends Thread
    {
        private final PendingConfigBeansNew mPending;
        volatile boolean    mQuit = false;
        
        AMXConfigLoaderThread( final PendingConfigBeansNew pending )
        {
            super( "AMXConfigLoader.AMXConfigLoaderThread" );
            mPending = pending;
        }
        
        void quit() { mQuit = true; }
        
            private ObjectName
        registerOne( final PendingConfigBeanJob job )
        {
            final ConfigBean cb = job.getConfigBean();
            
            ObjectName objectName = mRegistry.getObjectName(cb);
            try 
            {
                // If the ObjectName is null, then it hasn't been registered
                // Due to recursive registration of parents, we could encounter beans
                // that are parents, and thus already registered.
                if ( objectName == null )
                {
                    objectName = registerConfigBeanAsMBean( cb );
                    //debug( "AMXConfigLoaderThread.registerOne(): " + objectName);
                }
            }
            catch( Throwable t )
            {
                t.printStackTrace();
            }
            finally
            {
                job.releaseLatch();
            }
            
            return objectName;
        }
        
        public void run()
        {
            try
            {
                doRun();
            }
            catch( Throwable t )
            {
                t.printStackTrace();
            }
        }
        
            protected void
        doRun() throws Exception
        {
            /*
               First pass *only*: 
               Note when we initially empty the queue; this signifies that
               AMX is "ready" for callers that just started it.
             */
            PendingConfigBeanJob job = mPending.take();  // block until first item is ready
            while ( (! mQuit) && job != null )
            {
                final ObjectName objectName = registerOne(job);
                //debug( "REGISTERED: " + objectName );
                job = mPending.peek();  // don't block, loop exits when queue is first emptied
                if ( job != null )
                {
                    job = mPending.take();
                }
            }
            
            final ObjectName domainConfig = getDomainRootProxy().getDomain().extra().objectName();
            FeatureAvailability.getInstance().registerFeature( AMXConfigConstants.AMX_CONFIG_READY_FEATURE, domainConfig );
            
            // ongoing processing once initial queue has been emptied: blocking behavior
            while ( ! mQuit )
            {
                job = mPending.take();
                registerOne(job);
            }
        }
    }
    
      /**
        Register the ConfigBean, first registering its parent, parent's parent, etc if not
        already present.
     */
        private ObjectName
    registerConfigBeanAsMBean( final ConfigBean cb )
    {
        ObjectName objectName = null;
        
        //debug( "registerConfigBeanAsMBean: " + cb.getProxyType().getName()  + ", object = " + cb );
        final ConfigBean parentCB = getActualParent(cb);
        if ( parentCB != null && mRegistry.getObjectName(parentCB) == null )
        {
            //debug( "REGISTER parent first: " + parentCB.getProxyType().getNameProp() );
            registerConfigBeanAsMBean( parentCB );
            //debug( "REGISTERED parent: " + parentCB.getProxyType().getNameProp() + " as " + JMXUtil.toString(parentCB.getObjectName()) );
        }
       objectName =  _registerConfigBeanAsMBean( cb, parentCB );
       assert objectName == null || mRegistry.getObjectName(cb) != null;
       return objectName;
    }
    
    /**
        Parent must have been registered already.
     */
        private ObjectName
    _registerConfigBeanAsMBean(
        final ConfigBean cb,
        final ConfigBean parentCB )
    {
        final Class<? extends ConfigBeanProxy> cbClass = cb.getProxyType();
        
        //debug( "_registerConfigBeanAsMBean: " + cb.getProxyType().getNameProp() );
        
        ObjectName objectName = mRegistry.getObjectName(cb);
        if ( objectName != null )
        {
            throw new IllegalArgumentException( "ConfigBean " + cbClass.getName() + " already registered as " + objectName );
        }
        if ( parentCB != null && mRegistry.getObjectName(parentCB) == null )
        {
            throw new IllegalArgumentException( "ConfigBean parent " + parentCB.getProxyType().getName() +
                " must be registered first before child = " +cbClass.getName() );
        }
        
        // debug( "Preparing ConfigBean for registration with ObjectNameInfo = " + objectNameInfo.toString() + ", AMXMBeanMetaData = " + metadata );

        objectName = buildObjectName( cb );
    
        objectName  = createAndRegister( cb, objectName );
        if ( objectName != null )
        {
            ImplUtil.getLogger().fine( "REGISTERED MBEAN: " + objectName );
        }
        
        return objectName;
    }

    private ObjectName createAndRegister(
        final ConfigBean cb,
        final ObjectName objectNameIn )
    {
        ObjectName  objectName = objectNameIn;
        
        final String type = objectNameIn.getKeyProperty(AMXConstants.TYPE_KEY);
        
        ObjectName parentObjectName = getActualParentObjectName( cb );
        
        if ( parentObjectName == null  )
        {
            parentObjectName = getDomainRoot();
        }

        final AMXConfigImpl impl = new AMXConfigImpl( parentObjectName, cb );
        
        try
        {
            final ObjectInstance instance = mMBeanServer.registerMBean( impl, objectNameIn );
            objectName = instance.getObjectName();
            mRegistry.add( cb, objectName );

            //System.out.println( JMXUtil.toString( mMBeanServer.getMBeanInfo(objectName) ) );
        }
        catch( final JMException e )
        {
            debug( ExceptionUtil.toString(e) );
            
            objectName = null;
        }
        return objectName;
    }
    
        private String
    getType(  final ConfigBean cb)
    {
        final ConfigBeanJMXSupport spt = ConfigBeanJMXSupportRegistry.getInstance( cb );
        return spt.getTypeString();
    }
    
        public static String
    getName(final ConfigBean cb)
    {
        final ConfigBeanJMXSupport spt = ConfigBeanJMXSupportRegistry.getInstance( cb );
        
        if ( spt.isSingleton() )
        {
            return null;
        }
       
        String name = null;
        
        final String nameHint = spt.getNameHint();
        
        if ( nameHint == null )
        {
            name = "MISSING_NAME__KEY_MUST_BE_SPECIFIED_IN_INTERFACE";
        }
        else if ( spt.nameHintIsElement() )
        {
            final List<?> leaf = cb.leafElements(nameHint);
            if ( leaf != null ) {
                // verify that it is List<String> -- no other types are supported in this way
                final List<String> items = TypeCast.checkList( leaf, String.class );
                if (items.size() != 1 )
                {
                    throw new IllegalArgumentException("Can't find sub-element of type " + nameHint + " in " + cb.getProxyType().getName() );
                }
                name = items.get(0);
            }
        }
        else
        {
            name = cb.rawAttribute( nameHint );
        }

        return name;
    }
    
    private ObjectName getDomainRoot()
    {
        return ObjectNameBuilder.getDomainRootObjectName();
    }
    
    private DomainRoot getDomainRootProxy()
    {
        return ProxyFactory.getInstance(mMBeanServer).getDomainRootProxy();
    }
    
        private ObjectName
    buildObjectName( final ConfigBean cb )
    {
        ObjectName  parentObjectName;
        final ConfigBean parent = getActualParent(cb);
        
        if ( parent == null )
        {
            parentObjectName = getDomainRoot();
        }
        else
        {
            parentObjectName = mRegistry.getObjectName(parent);
        }
        
        final String type = getType( cb );
        final String name = getName( cb ) ;
        
        //debug( "Type/name for " + cb.getProxyType().getName() + " = " + type + " = " + name );
        
        final ObjectName objectName = ObjectNameBuilder.buildChildObjectName( mServer, parentObjectName, type, name );
        
        //debug( "ObjectName for " + cb.getProxyType().getName() + " = " + objectName + " of parent " + parentObjectName );

        return objectName;
    }
    
}













































