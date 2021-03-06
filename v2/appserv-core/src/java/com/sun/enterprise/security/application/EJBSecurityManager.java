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
package com.sun.enterprise.security.application;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.lang.reflect.Method;
import java.util.Collections;
import java.security.AccessController;
import java.security.Permission;
import java.security.Principal;
import java.security.AccessControlContext;
import java.security.CodeSource;
import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;
import java.security.ProtectionDomain;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.net.URL;
import java.security.Policy;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import com.sun.ejb.Invocation;
import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.InvocationException;
import com.sun.enterprise.Switch;
import com.sun.enterprise.SecurityManager;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactory;
import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactoryMgr;
import com.sun.enterprise.deployment.web.SecurityRoleReference;
import com.sun.enterprise.security.AppservAccessController;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.factory.SecurityManagerFactory;
import com.sun.enterprise.security.factory.FactoryForSecurityManagerFactory;
import com.sun.enterprise.security.factory.FactoryForSecurityManagerFactoryImpl;
import com.sun.enterprise.security.auth.LoginContextDriver;
import com.sun.enterprise.security.authorize.PolicyContextHandlerImpl;
import com.sun.enterprise.security.LoginException;
import com.sun.enterprise.security.authorize.*;
import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.audit.AuditManagerFactory;
import com.sun.enterprise.security.CachedPermission;
import com.sun.enterprise.security.CachedPermissionImpl;
import com.sun.enterprise.security.PermissionCache;
import com.sun.enterprise.security.PermissionCacheFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.TypeUtil;

// logging
import java.util.logging.*;
import com.sun.logging.LogDomains;

import java.security.ProtectionDomain;

/**
 * This class is used by the EJB server to manage security. All 
 * the container object only call into this object for managing 
 * security. This class cannot be subclassed. 
 * 
 * An instance of this class should be created per deployment unit.
 * @author Harpreet Singh
 */
public final class EJBSecurityManager implements SecurityManager {

    private static Logger _logger=null;
    static {
        _logger=LogDomains.getLogger(LogDomains.SECURITY_LOGGER);
    }
    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(EJBSecurityManager.class);
    
    private static AuditManager auditManager =
            AuditManagerFactory.getAuditManagerInstance();

    private static final PolicyContextHandlerImpl pcHandlerImpl =
            (PolicyContextHandlerImpl)PolicyContextHandlerImpl.getInstance();

    private static SecurityRoleMapperFactory roleMapperFactory = 
	SecurityRoleMapperFactoryMgr.getFactory();

    private EjbDescriptor deploymentDescriptor = null;
    private Switch theSwitch;
    // Objects required for Run-AS
    private RunAsIdentityDescriptor runAs = null;
    
    // jacc related
    private static PolicyConfigurationFactory pcf = null;
    private String ejbName = null;
    // contextId id is the same as an appname. This will be used to get
    // a PolicyConfiguration object per application.
    private String contextId = null;
    private String codebase = null;
    private CodeSource codesource = null;
    private String realmName = null;
    // this stores the role ref permissions. So will not need to spend runtime 
    // resources generating permissions.
    private Hashtable cacheRoleToPerm = new Hashtable();

    // we use two protection domain caches until we decide how to 
    // set the codesource in the protection domain of system apps.
    // PD's in protectionDomainCache have the (privileged) codesource
    // of the EJBSecurityManager class. The PD used in pre-dispatch
    // authorization decisions MUST not be constructed using a privileged
    // codesource (or else all pre-distpatch access decisions will be granted).
    private Map cacheProtectionDomain = 
	Collections.synchronizedMap(new WeakHashMap());
    private Map protectionDomainCache = 
	Collections.synchronizedMap(new WeakHashMap());

    private Map accessControlContextCache = 
	Collections.synchronizedMap(new WeakHashMap());

    private PermissionCache uncheckedMethodPermissionCache = null;

    private Policy policy = null;
     
    private static CodeSource managerCodeSource = 
	EJBSecurityManager.class.getProtectionDomain().getCodeSource();

    private boolean isMdb;

    /** Return an instance of EJBSecurityManager. This class is no longer 
     * instantiable directly.
     * @return EJBSecurityManager EJBSM instance with the given descriptor
     * information
     */ 
    public static EJBSecurityManager getInstance(Descriptor des) 
	throws Exception{
	return new EJBSecurityManager(des);
    }
    /**
     * This method creates a new security manager object......Dont instantiate
     * directly.
     */
    private EJBSecurityManager(Descriptor ejbDescriptor) 
	throws Exception
    {
	if(ejbDescriptor == null || !(ejbDescriptor instanceof EjbDescriptor)) {
	    throw new IllegalArgumentException("Illegal Deployment Descriptor Information.");
	}
	this.deploymentDescriptor = (EjbDescriptor) ejbDescriptor;

	this.isMdb = (EjbMessageBeanDescriptor.TYPE.equals(
		this.deploymentDescriptor.getType()));

	// get the default policy
	policy = Policy.getPolicy();

	boolean runas = !(deploymentDescriptor.getUsesCallerIdentity());
	if (runas){
	    runAs = deploymentDescriptor.getRunAsIdentity();

            // Note: runAs may be null even when runas==true if this EJB
            // is an MDB. 
	    if(runAs != null) {
		if (_logger.isLoggable(Level.FINE)){
		    _logger.log(Level.FINE,deploymentDescriptor.getEjbClassName() + 
				 " will run-as: " + runAs.getPrincipal() + 
				 " (" + runAs.getRoleName() + ")");
		}
	    }
	}
	
	theSwitch = Switch.getSwitch();
	initialize();
    }
    private static CodeSource getApplicationCodeSource(String pcid) throws Exception {
	CodeSource result = null;
	String archiveURI = "file:///" + pcid.replace(' ', '_');
	try{
	    java.net.URI uri = null;
	    try{
		uri = new java.net.URI(archiveURI);
		if(uri != null){
		    result = new CodeSource(uri.toURL(), 
                            (java.security.cert.Certificate[]) null);
		}
	    } catch(java.net.URISyntaxException use){
		// manually create the URL
 		_logger.log(Level.SEVERE,"JACC: Error Creating URI ", use);
		throw new RuntimeException(use);
	    }

 	} catch(java.net.MalformedURLException mue){
	    // should never come here.
 	    _logger.log(Level.SEVERE,"JACC: ejbsm.codesourceerror", mue);
	    throw new RuntimeException(mue);
	} 
	return result;
    }

    // obtains PolicyConfigurationFactory once for class
    private static PolicyConfigurationFactory getPolicyFactory() 
	throws PolicyContextException {
	synchronized (EJBSecurityManager.class) {
	    if (pcf == null) {
		try {
		    pcf = PolicyConfigurationFactory.getPolicyConfigurationFactory();
		} catch(ClassNotFoundException cnfe){
		    _logger.severe("jaccfactory.notfound");
		    throw new PolicyContextException(cnfe);
		} catch(PolicyContextException pce){
		    _logger.severe("jaccfactory.notfound");
		    throw pce;
		}
	    }
	}
	return pcf;
    }

    public boolean getUsesCallerIdentity() {
        return (runAs == null);
    }

    public static void loadPolicyConfiguration(EjbDescriptor eDescriptor) throws Exception
    {
	String pcid = getContextID(eDescriptor);
        String appName = eDescriptor.getApplication().getRegistrationName();
        roleMapperFactory.setAppNameForContext(appName, pcid);
	boolean inService = getPolicyFactory().inService(pcid);

	// only load the policy configuration if it isn't already in service.
	// Consequently, all things that deploy modules (as apposed to
	// loading already deployed modules) must make sure pre-exiting
	// pc is either in deleted or open state before this method
	// is called. Note that policy statements are not
	// removed to allow multiple EJB's to be represented by same pc.

	if (!inService) {

	    // translate the deployment descriptor to configure the policy rules.

	    convertEJBMethodPermissions(eDescriptor,pcid);
	    convertEJBRoleReferences(eDescriptor,pcid);

	    if(_logger.isLoggable(Level.FINE)){
		_logger.fine("JACC: policy translated for policy context:" +pcid);
	    }
	} 
    }

    public static String getContextID(EjbDescriptor ejbDesc) {
        return getContextID(ejbDesc.getEjbBundleDescriptor());
    }

    public static String getContextID(EjbBundleDescriptor ejbBundleDesc) {
        String cid = null;
        if (ejbBundleDesc != null ) {
            cid = ejbBundleDesc.getApplication().getRegistrationName() +
                '/' + ejbBundleDesc.getUniqueFriendlyId();
        }
        return cid;
    }


    private void initialize() throws Exception {
	contextId = getContextID(deploymentDescriptor);
        String appName = deploymentDescriptor.getApplication().getRegistrationName();
        roleMapperFactory.setAppNameForContext(appName, contextId);
	codesource = getApplicationCodeSource(contextId);
	ejbName = deploymentDescriptor.getName();

	realmName= deploymentDescriptor.getApplication().getRealm();

        if (realmName == null) {
            Set iorConfigs = deploymentDescriptor.getIORConfigurationDescriptors();
            // iorConfigs is not null from implementation of EjbDescriptor
            Iterator iter = iorConfigs.iterator();
            if (iter != null) {
                // there should be at most one element in the loop from
                // definition of dtd
                while (iter.hasNext()) {
                    EjbIORConfigurationDescriptor iorConfig =
                           (EjbIORConfigurationDescriptor)iter.next();
                    realmName = iorConfig.getRealmName();
                }
            }
        }

	if(_logger.isLoggable(Level.FINE)){
	    _logger.fine("JACC: Context id (id under which all EJB's in application will be created) = " + contextId);
	    _logger.fine("Codebase (module id for ejb "+ejbName+") = "+ codebase);
	}

	// translate the deployment descriptor to populate the role-ref permission cache
	addEJBRoleReferenceToCache(deploymentDescriptor);

	// create and initialize the unchecked permission cache.
	uncheckedMethodPermissionCache =
                PermissionCacheFactory.createPermissionCache(
	                        this.contextId, this.codesource,
				EJBMethodPermission.class,
				this.ejbName);
    }

    /** This method converts ejb role references to jacc permission objects
     * and adds them to the policy configuration object
     * It gets the list of role references from the ejb descriptor. For each
     * such role reference, create a EJBRoleRefPermission and add it to the 
     * PolicyConfiguration object.
     * @param EjbDescriptor, the ejb descriptor
     * @param pcid, the policy context identifier
     */
    private static void 
	convertEJBRoleReferences(EjbDescriptor eDescriptor, String pcid)
	throws PolicyContextException {

	PolicyConfiguration pc = 
	    getPolicyFactory().getPolicyConfiguration(pcid, false);

	assert pc != null;
				     
	if (pc != null) { 

	    String eName = eDescriptor.getName();

	    Iterator iroleref = eDescriptor.getRoleReferences().iterator();
	    while(iroleref.hasNext()){
		SecurityRoleReference roleRef = 
		    (SecurityRoleReference) iroleref.next();
		String rolename = roleRef.getRolename();
		EJBRoleRefPermission ejbrr = 
		    new EJBRoleRefPermission(eName, rolename);
		String rolelink = roleRef.getSecurityRoleLink().getName();

		pc.addToRole(rolelink, ejbrr);

		if(_logger.isLoggable(Level.FINE)){
		    _logger.fine("JACC: Converting role-ref -> "+roleRef.toString()+
				 " to permission with name("+ejbrr.getName()+
				 ") and actions ("+ejbrr.getActions()+
				 ")" + "mapped to role ("+rolelink+")");
		}
	    }
	}
    }

    /** This method converts ejb role references to jacc permission objects
     * and adds them to the corresponding permission cache.
     * @param EjbDescriptor, the ejb descriptor
     */
    private void addEJBRoleReferenceToCache(EjbDescriptor eDescriptor) {

	String eName = eDescriptor.getName();

	Iterator iroleref = eDescriptor.getRoleReferences().iterator();
	while(iroleref.hasNext()){
	    SecurityRoleReference roleRef = 
		(SecurityRoleReference) iroleref.next();
	    String rolename = roleRef.getRolename();
	    EJBRoleRefPermission ejbrr = 
		new EJBRoleRefPermission(eName, rolename);
	    String rolelink = roleRef.getSecurityRoleLink().getName();

	    cacheRoleToPerm.put(eName+"_"+rolename, ejbrr);

	    if(_logger.isLoggable(Level.FINE)){
		_logger.fine("JACC: Converting role-ref -> "+roleRef.toString()+
			     " to permission with name("+ejbrr.getName()+
			     ") and actions ("+ejbrr.getActions()+
			     ")" + "mapped to role ("+rolelink+")");
	    }
	}
    }

    // utility to collect role permisisions in table of collections
    private static HashMap addToRolePermissionsTable(HashMap table, 
						     MethodPermission mp, 
						     EJBMethodPermission ejbmp) {
	if (mp.isRoleBased()){
	    if (table == null) {
		table = new HashMap();
	    }
	    String roleName = mp.getRole().getName();
	    Permissions rolePermissions =
		(Permissions) table.get(roleName);
	    if (rolePermissions == null) {
		rolePermissions = new Permissions();
		table.put(roleName,rolePermissions);
	    }
	    rolePermissions.add(ejbmp);
	    if(_logger.isLoggable(Level.FINE)){
		_logger.fine("JACC DD conversion: EJBMethodPermission ->("+ 
			     ejbmp.getName()+" "+ejbmp.getActions()+
			     ")protected by role -> "+roleName);
	    }
	}
	return table;
    }

    // utility to collect unchecked permissions in collection
    private static Permissions addToUncheckedPermissions(Permissions permissions, 
							 MethodPermission mp, 
							 EJBMethodPermission ejbmp) {
	if(mp.isUnchecked()){
	    if (permissions == null) {
		permissions = new Permissions();
	    }
	    permissions.add(ejbmp);
	    if(_logger.isLoggable(Level.FINE)){
		_logger.fine("JACC DD conversion: EJBMethodPermission ->("
			     +ejbmp.getName()+" "+ejbmp.getActions()+
			     ") is (unchecked)");
	    }
	}
	return permissions;
    }

    // utility to collect excluded permissions in collection
    private static Permissions addToExcludedPermissions(Permissions permissions, 
							MethodPermission mp, 
							EJBMethodPermission ejbmp) {
	if(mp.isExcluded()){
	    if (permissions == null) {
		permissions = new Permissions();
	    }
	    permissions.add(ejbmp);
	    if(_logger.isLoggable(Level.FINE)){
		_logger.fine("JACC DD conversion: EJBMethodPermission ->("
			     +ejbmp.getName()+" "+ejbmp.getActions()+
			     ") is (excluded)");
	    }
	}
	return permissions;
    }

    /**
     * This method converts the dd in two phases. 
     * Phase 1: 
     * gets a map representing the methodPermission elements exactly as they
     * occured for the ejb in the dd. The map is keyed by method-permission 
     * element and each method-permission is mapped to a list of method 
     * elements representing the method elements of the method permision 
     * element. Each method element is converted to a corresponding 
     * EJBMethodPermission and added, based on its associated method-permission, 
     * to the policy configuration object.
     * phase 2: 
     * configures additional EJBMethodPermission policy statements
     * for the purpose of optimizing Permissions.implies matching by the 
     * policy provider. This phase also configures unchecked policy 
     * statements for any uncovered methods. This method gets the list 
     * of method descriptors for the ejb from the EjbDescriptor object. 
     * For each method descriptor, it will get a list of MethodPermission
     * objects that signify the method permissions for the Method and 
     * convert each to a corresponding EJBMethodPermission to be added
     * to the policy configuration object.
     * @param EjbDescriptor, the ejb descriptor for this EJB.
     * @param pcid, the policy context identifier.
     */
    private static void 
	convertEJBMethodPermissions (EjbDescriptor eDescriptor, String pcid) 
	throws PolicyContextException {

	PolicyConfiguration pc = 
	    getPolicyFactory().getPolicyConfiguration(pcid, false);

	assert pc != null;

	if (pc != null) {

	    String eName = eDescriptor.getName();

	    Permissions uncheckedPermissions = null;
	    Permissions excludedPermissions = null;
	    HashMap rolePermissionsTable = null;

	    EJBMethodPermission ejbmp = null;

	    // phase 1
	    Map mpMap = eDescriptor.getMethodPermissionsFromDD();
	    if (mpMap != null) {

		Iterator mpIt = mpMap.keySet().iterator();

		while(mpIt.hasNext()) {

		    MethodPermission mp = (MethodPermission)mpIt.next();

		    Iterator mdIt = ((ArrayList) mpMap.get(mp)).iterator();

		    while(mdIt.hasNext()) {

			MethodDescriptor md = (MethodDescriptor) mdIt.next();

			String mthdName = md.getName();
			String mthdIntf = md.getEjbClassSymbol();
			String mthdParams[] = md.getStyle() == 3 ? 
			    md.getParameterClassNames() : null;

			ejbmp = new EJBMethodPermission(eName,mthdName.equals("*") ? 
							null : mthdName,
							mthdIntf,mthdParams);
			rolePermissionsTable = 
			    addToRolePermissionsTable(rolePermissionsTable,mp,ejbmp);

			uncheckedPermissions = 
			    addToUncheckedPermissions(uncheckedPermissions,mp,ejbmp);

			excludedPermissions = 
			    addToExcludedPermissions(excludedPermissions,mp,ejbmp);
		    }
		}
	    }

	    // phase 2 - configures additional perms:
	    //      . to optimize performance of Permissions.implies
	    //      . to cause any uncovered methods to be unchecked

	    Iterator mdIt = eDescriptor.getMethodDescriptors().iterator();
	    while(mdIt.hasNext()) {

		MethodDescriptor md = (MethodDescriptor)mdIt.next();
		Method mthd = md.getMethod(eDescriptor);
		String mthdIntf = md.getEjbClassSymbol();

		if(mthd == null){
		    continue;
		}

		if(mthdIntf == null || mthdIntf.equals("")) {
		    _logger.severe("MethodDescriptor interface not defined - "+
				   " ejbName: "+eName+
				   " methodName: " +md.getName()+
				   " methodParams: " +md.getParameterClassNames());
		    continue;
		}

		ejbmp = new EJBMethodPermission(eName,mthdIntf,mthd);
	
		Iterator mpIt = eDescriptor.getMethodPermissionsFor(md).iterator();

		while(mpIt.hasNext()) {

		    MethodPermission mp = (MethodPermission) mpIt.next();
		    
		    rolePermissionsTable = 
			addToRolePermissionsTable(rolePermissionsTable,mp,ejbmp);

		    uncheckedPermissions = 
			addToUncheckedPermissions(uncheckedPermissions,mp,ejbmp);

		    excludedPermissions = 
			addToExcludedPermissions(excludedPermissions,mp,ejbmp);
		} 
	    }

	    if (uncheckedPermissions != null) {
		pc.addToUncheckedPolicy(uncheckedPermissions);
	    }
	    if (excludedPermissions != null) {
		pc.addToExcludedPolicy(excludedPermissions);
	    }
	    if (rolePermissionsTable != null) {
		
		Iterator roleIt = rolePermissionsTable.keySet().iterator();

		while (roleIt.hasNext()) {
		    String roleName = (String) roleIt.next();
		    pc.addToRole(roleName,
				 (Permissions)rolePermissionsTable.get(roleName));
		}
	    }
	}
    }

    private ProtectionDomain getCachedProtectionDomain(Set principalSet,
						       boolean applicationCodeSource) {
    
	ProtectionDomain prdm = null;
 	Principal[] principals = null;
 
	/* Need to use the application codeSource for permission evaluations
	 * as the manager codesource is granted all permissions in server.policy.
	 * The manager codesource needs to be used for doPrivileged to allow system
	 * apps to have all permissions, but we either need to revert to
	 * real doAsPrivileged, or find a way to distinguish system apps.
	 */

	CodeSource cs = null;

	if (applicationCodeSource) {
	    prdm = (ProtectionDomain)cacheProtectionDomain.get(principalSet);
	    cs = codesource;
	} else {
	    prdm = (ProtectionDomain)protectionDomainCache.get(principalSet);
	    cs = managerCodeSource;
	}

 	if(prdm == null) {

            principals = (principalSet == null ? null :
                    (Principal [])principalSet.toArray(new Principal[principalSet.size()]));
	
	    prdm = new ProtectionDomain (cs, null, null, principals);

            // form a new key set so that it does not share with others
            Set newKeySet = ((principalSet != null)? new HashSet(principalSet) : new HashSet());

	    if (applicationCodeSource) {
		cacheProtectionDomain.put(newKeySet,prdm);
	    } else {
                // form a new key set so that it does not share with others
		protectionDomainCache.put(newKeySet,prdm);
	    }

 	    if (_logger.isLoggable(Level.FINE)) {
	        _logger.fine("JACC: new ProtectionDomain added to cache");
            }
 
 	}
    
 	if(_logger.isLoggable(Level.FINE)){
	    if (principalSet == null) {
		_logger.fine("JACC: returning cached ProtectionDomain PrincipalSet: null");
	    } else {
		StringBuffer pBuf = null;
		principals = (Principal [])principalSet.toArray(new Principal[0]);
		for (int i=0; i<principals.length; i++) {
		    if (i == 0) pBuf = new StringBuffer(principals[i].toString());
		    else pBuf.append(" " + principals[i].toString());
		}
		_logger.fine("JACC: returning cached ProtectionDomain - CodeSource: ("
			     + cs + ") PrincipalSet: "+pBuf);
	    }
  	}
	
 	return prdm;
    }


    /**
     * This method is called by the EJB container to decide whether or not
     * a method specified in the Invocation should be allowed.
     * @param An invocation object that contains all the details of the 
     * invocation.
     * @return A boolean value indicating if the client should be allowed 
     * to invoke the EJB.
     */
    public boolean authorize(Invocation inv) {
        
        if (inv.auth != null) {
            return inv.auth.booleanValue();
        }
        
	boolean ret=false;

	CachedPermission cp = null;
	Permission ejbmp = null;

	if (inv.invocationInfo == null || inv.invocationInfo.cachedPermission == null) {
	    ejbmp = new EJBMethodPermission(ejbName,inv.getMethodInterface(),inv.method);
	    cp = new CachedPermissionImpl(uncheckedMethodPermissionCache,ejbmp);
	    if (inv.invocationInfo != null) {
		inv.invocationInfo.cachedPermission = cp;
		if (_logger.isLoggable(Level.FINE)){
		    _logger.fine("JACC: permission initialized in InvocationInfo: EJBMethodPermission (Name) = "+ ejbmp.getName() + " (Action) = "+ ejbmp.getActions());
		}
	    }
	} else {
	    cp = inv.invocationInfo.cachedPermission;
	    ejbmp = cp.getPermission();
	}

	String caller = null;
        SecurityContext sc = null;

	pcHandlerImpl.getHandlerData().setInvocation(inv);
	ret = cp.checkPermission();

	if (!ret) {

	    sc = SecurityContext.getCurrent();
  
	    Set principalSet = sc.getPrincipalSet();
 
	    ProtectionDomain prdm = getCachedProtectionDomain(principalSet,true);
   
	    try {
		// set the policy context in the TLS.
		String oldContextId = setPolicyContext(this.contextId);

		try {

		    ret = policy.implies(prdm, ejbmp);
 
		} catch (SecurityException se){
		    _logger.log(Level.SEVERE,"JACC: Unexpected security exception on access decision",se);
		    ret = false;
		} catch (Throwable t) {
		    _logger.log(Level.SEVERE,"JACC: Unexpected exception on access decision",t);
		    ret = false;
		} finally {
		    resetPolicyContext(oldContextId,this.contextId);
		}

	    } catch (Throwable t) {
		_logger.log(Level.SEVERE,"JACC: Unexpected exception manipulating policy context",t);
		ret = false;
	    }
	}

        inv.auth = (ret) ? Boolean.TRUE : Boolean.FALSE;

	if (auditManager.isAuditOn()){
            if (sc == null) {
                sc = SecurityContext.getCurrent();
            }
	    caller = sc.getCallerPrincipal().getName();
	    auditManager.ejbInvocation(caller, ejbName, inv.method.toString(), ret);
	}

        if (ret && inv.isWebService && !inv.preInvokeDone) {
            preInvoke(inv);
        }

        if(_logger.isLoggable(Level.FINE)){
	    _logger.fine("JACC: Access Control Decision Result: " +ret + " EJBMethodPermission (Name) = "+ ejbmp.getName() + " (Action) = "+ ejbmp.getActions() + " (Caller) = " + caller);
	}
  
	return ret;
    }

    /**
     * Checks if any method permissions are set. If they are not set
     * it is possible that this is a J2ee 1.2 APP and we would need to 
     * grant permissions for execution anyways
     */
    private boolean areMethodPermissionsSet(){
	boolean empty =	
	    deploymentDescriptor.getPermissionedMethodsByPermission().isEmpty();
	return !empty;
    }

    /**
     * This method is used by MDB Container - Invocation Manager  to setup
     * the run-as identity information. It has to be coupled with
     * the postSetRunAsIdentity method.
     * This method is called for EJB/MDB Containers
     */
    public void preInvoke (ComponentInvocation inv){
        boolean isWebService = false;
        if (inv instanceof Invocation) {
            isWebService = ((Invocation)inv).isWebService;
        }

        // if it is not a webservice or successful authorization
        // and preInvoke is not call before
        if ((!isWebService || (inv.auth != null && inv.auth.booleanValue()))
                && !inv.preInvokeDone) {
	    if (isMdb) {
	        SecurityContext.setUnauthenticatedContext();
	    }
            if (runAs != null){
 	        inv.setOldSecurityContext(SecurityContext.getCurrent());
                loginForRunAs();
            } 
            inv.preInvokeDone = true;
        }
    }

    /**
     * This method is used by Message Driven Bean Container to remove
     * the run-as identity information that was set up using the 
     * preSetRunAsIdentity method
     */
    public void postInvoke (ComponentInvocation inv){
	if (runAs != null && inv.preInvokeDone){
            final ComponentInvocation finv = inv;
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    SecurityContext.setCurrent (finv.getOldSecurityContext());
                    return null;
                }
            });
	}
    }

    /** 
     * Logs in a principal for run-as. This method is called if the
     * run-as principal is required. The user has already logged in -
     * now it needs to change to the new principal. In order that all
     * the correct permissions work - this method logs the new principal
     * with no password -generating valid credentials.
     */
    private void loginForRunAs(){
        AppservAccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
	        LoginContextDriver.loginPrincipal (runAs.getPrincipal(), realmName);
                return null;
            }
        });
    }

    
    /**
     * This method returns a boolean value indicating whether or not the
     * caller is in the specified role.
     * @param The role name in the form of java.lang.String
     * @return A boolean true/false depending on whether or not the caller 
     * has the specified role.
     */
    public boolean isCallerInRole(String  role)	{
	/* In case of Run As - Should check isCallerInRole with 
	 * respect to the old security context.
	 */

	boolean ret= false;

	if(_logger.isLoggable(Level.FINE)){
	    _logger.entering("EJBSecurityManager", "isCallerInRole", role);

	}
	EJBRoleRefPermission ejbrr =
            (EJBRoleRefPermission)cacheRoleToPerm.get(ejbName+"_"+role);
        
	if(ejbrr == null){
	    ejbrr = new EJBRoleRefPermission(ejbName, role);
	}

 	SecurityContext sc;
	if (runAs != null) {
	    InvocationManager im = theSwitch.getInvocationManager();
	    ComponentInvocation ci = im.getCurrentInvocation();
	    sc = ci.getOldSecurityContext();
	} else {
	    sc = SecurityContext.getCurrent();
	}
 	Set principalSet = null;
 	if (sc != null) principalSet = sc.getPrincipalSet();
 
 	ProtectionDomain prdm = getCachedProtectionDomain(principalSet,true);
 
	try {

	    ret = policy.implies(prdm, ejbrr);
	} catch(SecurityException se){
 	    _logger.log(Level.SEVERE,"JACC: Unexpected security exception isCallerInRole",se);
 	    ret = false;
 	} catch (Throwable t) {
 	    _logger.log(Level.SEVERE,"JACC: Unexpected exception isCallerInRole",t);
 	    ret = false;
  	}
 
 	if(_logger.isLoggable(Level.FINE)){
 	    _logger.fine("JACC: isCallerInRole Result: " +ret + " EJBRoleRefPermission (Name) = "+ ejbrr.getName() + " (Action) = "+ ejbrr.getActions() + " (Codesource) = " + prdm.getCodeSource());
 	}
  
  	return ret;
    }

    /**
     * This method returns the Client Principal who initiated the current
     * Invocation.
     * @return A Principal object of the client who made this invocation.
     * or null if the SecurityContext has not been established by the client.
     */
    public Principal getCallerPrincipal() {
	SecurityContext sc = null;
	if (runAs != null){ // Run As
	    /* return the principal associated with the old security
	     * context 
	     */
	    InvocationManager im = theSwitch.getInvocationManager();
	    ComponentInvocation ci =  im.getCurrentInvocation();

	    if (ci == null) {
		throw new InvocationException(); // 4646060
	    }
	    sc = ci.getOldSecurityContext();
            
	} else{
	    // lets optimize a little. no need to look up oldsecctx
	    // its the same as the new one
	    sc = SecurityContext.getCurrent();
	}
 
 	Principal prin;
 
 	if (sc != null) {
	    prin = sc.getCallerPrincipal();
	} else {
	    prin = SecurityContext.getDefaultCallerPrincipal();
	}
  	return prin;
      }

    public void destroy() {

       try {

           PolicyConfigurationFactory pcf = getPolicyFactory();
	   boolean wasInService = pcf.inService(this.contextId);

	   PolicyConfiguration 
	       pc = pcf.getPolicyConfiguration(this.contextId, false);
	   // pc.delete() will be invoked during undeployment

	   if (wasInService) {
	       policy.refresh();
               PermissionCacheFactory.removePermissionCache(uncheckedMethodPermissionCache);
	       uncheckedMethodPermissionCache = null;
	   }
           roleMapperFactory.removeAppNameForContext(this.contextId);

       } catch (PolicyContextException pce){
           String msg = localStrings.getLocalString("ejbsm.could_not_delete",
                                                    "Could not delete policy file during undeployment");
           // Just log it.
           _logger.log(Level.WARNING, msg, pce);
       }

        FactoryForSecurityManagerFactory ffsmf
            = FactoryForSecurityManagerFactoryImpl.getInstance();
        SecurityManagerFactory smf = ffsmf.getSecurityManagerFactory("ejb");
        smf.removeSecurityManager(contextId); 
    }
    /**
     * This will return the subject associated with the current call. If the 
     * run as subject is in effect. It will return that subject. This is done 
     * to support the JACC specification which says if the runas principal is 
     * in effect,  that principal should be used for making a component call.
     * @return Subject the current subject. Null if this is not the run-as 
     * case
     */
    public Subject getCurrentSubject(){
 	// just get the security context will return the empt subject
 	// of the default securityContext when appropriate.
 	return SecurityContext.getCurrent().getSubject();
    }

    /* This method is used by SecurityUtil runMethod to run the
     * action as the subject encapsulated in the current
     * SecurityContext.
     */
    public Object doAsPrivileged(PrivilegedExceptionAction pea) 
	throws Throwable {
	
	SecurityContext sc = SecurityContext.getCurrent();
	
	Set principalSet = sc.getPrincipalSet();
 
	AccessControlContext acc = 
	    (AccessControlContext)accessControlContextCache.get(principalSet);
    
	if(acc == null){
 
	    final ProtectionDomain[] pdArray = new ProtectionDomain[1];
	    pdArray[0] = getCachedProtectionDomain(principalSet,false);

	    try{
 
		if (principalSet != null) {
 
		    final Subject s = sc.getSubject();
 
		    acc = (AccessControlContext)
			AccessController.doPrivileged(new PrivilegedExceptionAction(){
				public java.lang.Object run() throws Exception{
				    return new AccessControlContext
					(new AccessControlContext(pdArray),
					 new SubjectDomainCombiner(s));
				}
			    });
		} else {
		    acc = new AccessControlContext(pdArray);
		}

                // form a new key set so that it does not share with
                // cacheProtectionDomain and protectionDomainCache
		accessControlContextCache.put(new HashSet(principalSet),acc);
 
		_logger.fine("JACC: new AccessControlContext added to cache");

	    } catch(Exception e){
		_logger.log(Level.SEVERE,
			    "java_security.security_context_exception",e);
		acc = null;
		throw e;
	    }
	}
 
	Object rvalue = null;

	String oldContextId = setPolicyContext(this.contextId);

	if(_logger.isLoggable(Level.FINE)){
	    _logger.fine("JACC: doAsPrivileged contextId("+this.contextId+")");
	}

	try {

	    rvalue = AccessController.doPrivileged(pea,acc);

	} finally {
	    resetPolicyContext(oldContextId,this.contextId);
	}

	return rvalue;

    }
    
    /**
     * Runs a business method of an EJB withint the bean's policy context. 
     * The original policy context is restored after method execution.  
     * This method should only be used by com.sun.enterprise.security.SecurityUtil.
     * @param beanClassMethod the EJB business method
     * @param obj the EJB bean instance
     * @param oa parameters passed to beanClassMethod
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws Throwable other throwables in other cases
     * @return return value from beanClassMethod
     */
    public Object runMethod(Method beanClassMethod, Object obj, Object[] oa)
            throws Throwable {
        String oldCtxID = setPolicyContext(this.contextId);
        Object ret = null;
        try {
            ret = beanClassMethod.invoke(obj, oa);
        } finally {
            resetPolicyContext(oldCtxID, this.contextId);
        }
        return ret;
    }
 
    private static void resetPolicyContext(final String newV, String oldV) 
	throws Throwable {
 	if (oldV != newV && newV != null && (oldV == null || !oldV.equals(newV))) {

	    if(_logger.isLoggable(Level.FINE)){
		_logger.fine("JACC: Changing Policy Context ID: oldV = " 
			     + oldV + " newV = " + newV);
	    }
	    try {  
		AppservAccessController.doPrivileged(new PrivilegedExceptionAction(){
			public java.lang.Object run() throws Exception{
			    PolicyContext.setContextID(newV);
			    return null;
			}
		    });
	    } catch (java.security.PrivilegedActionException pae) {
		Throwable cause = pae.getCause();
		if( cause instanceof java.security.AccessControlException) {
		    _logger.log(Level.SEVERE,"setPolicy SecurityPermission required to call PolicyContext.setContextID",cause);
		} else {
		    _logger.log(Level.SEVERE,"Unexpected Exception while setting PolicyContext",cause);
		}
		throw cause;
	    }
	}
    }

    private static String setPolicyContext(String newV) throws Throwable {

	String oldV  = PolicyContext.getContextID();

	resetPolicyContext(newV,oldV);

	return oldV;
    }

}















