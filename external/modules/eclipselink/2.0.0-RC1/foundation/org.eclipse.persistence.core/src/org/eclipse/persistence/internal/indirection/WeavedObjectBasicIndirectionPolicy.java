/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.indirection;

import java.beans.PropertyChangeListener;

import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.indirection.WeavedAttributeValueHolderInterface;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.internal.security.PrivilegedMethodInvoker;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;

/**
 * A WeavedObjectBasicIndirectionPolicy is used by OneToOne mappings that are LAZY through weaving
 * and which use Property(method) access.
 * 
 * It extends BasicIndirection by providing the capability of calling the set method that was initially
 * mapped in addition to the set method for the weaved valueholder in order to coordinate the value of the
 * underlying property with the value stored in the valueholder
 * 
 * @author Tom Ware
 */
public class WeavedObjectBasicIndirectionPolicy extends BasicIndirectionPolicy {  
    /** Name of the initial set method. */
    protected String setMethodName = null;
    /** Lazily initialized set method based on the set method name. */
    protected Method setMethod = null;
    
    public WeavedObjectBasicIndirectionPolicy(String setMethodName) {
        super();
        this.setMethodName = setMethodName;
    }    
    
    /**
     * Return the "real" attribute value, as opposed to any wrapper.
     * This will trigger the wrapper to instantiate the value. In a weaved policy, this will
     * also call the initial setter method to coordinate the values of the valueholder with
     * the underlying data.
     */
    public Object getRealAttributeValueFromObject(Object object, Object attribute) {
        Object value = super.getRealAttributeValueFromObject(object, attribute);
        // Provide the indirection policy with a callback that allows it to do any updates it needs as the result of getting the value.
        updateValueInObject(object, value, attribute);
        return value;
    }
    
    /**
     * This method will lazily initialize the set method
     * Lazy initialization occurs to that we are not required to have a handle on
     * the actual class that we are using until runtime.  This helps to satisfy the 
     * weaving requirement that demands that we avoid loading domain classes into
     * the main class loader until after weaving occurs.
     */
    protected Method getSetMethod() {
        if (setMethod == null) {
            ForeignReferenceMapping sourceMapping = (ForeignReferenceMapping)mapping;
            // The parameter type for the set method must always be the return type of the get method.
            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = sourceMapping.getReferenceClass();
            try {
                setMethod = Helper.getDeclaredMethod(sourceMapping.getDescriptor().getJavaClass(), setMethodName, parameterTypes);
            } catch (NoSuchMethodException e){
                throw DescriptorException.errorAccessingSetMethodOfEntity(sourceMapping.getDescriptor().getJavaClass(), setMethodName ,sourceMapping.getDescriptor(), e);
            }
        }
        return setMethod;
    }

    
    /**
     * Coordinate the valueholder for this mapping with the underlying property by calling the
     * initial setter method.
     */
    public void updateValueInObject(Object object, Object value, Object attributeValue){
        setRealAttributeValueInObject(object, value);
        ((WeavedAttributeValueHolderInterface)attributeValue).setIsCoordinatedWithProperty(true);
    }
    
    /**
     * Set the value of the appropriate attribute of target to attributeValue.
     * In this case, place the value inside the target's ValueHolder.
     */
    public void setRealAttributeValueInObject(Object target, Object attributeValue) {
        // If the target object is using change tracking, it must be disable first to avoid thinking the value changed.
        PropertyChangeListener listener = null;
        ChangeTracker trackedObject = null;
        if (target instanceof ChangeTracker) {
            trackedObject = (ChangeTracker)target;
            listener = trackedObject._persistence_getPropertyChangeListener();
            trackedObject._persistence_setPropertyChangeListener(null);
        }
        Object[] parameters = new Object[1];
        parameters[0] = attributeValue;
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()) {
                try {
                    AccessController.doPrivileged(new PrivilegedMethodInvoker(getSetMethod(), target, parameters));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw DescriptorException.illegalAccessWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, throwableException);
                    } else {
                        throw DescriptorException.targetInvocationWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, throwableException);
                     }
                }
            } else {
                PrivilegedAccessHelper.invokeMethod(getSetMethod(), target, parameters);
            }
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccessWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, exception);
        } catch (IllegalArgumentException exception) {
              throw DescriptorException.illegalArgumentWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, exception);
        } catch (InvocationTargetException exception) {
            throw DescriptorException.targetInvocationWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, exception);
        } finally {
            if (trackedObject != null) {
                trackedObject._persistence_setPropertyChangeListener(listener);
            }
        }
    }  
}
