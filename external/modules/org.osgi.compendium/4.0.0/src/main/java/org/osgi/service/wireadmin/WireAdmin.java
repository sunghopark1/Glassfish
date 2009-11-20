/*
 * $Header: /cvshome/build/org.osgi.service.wireadmin/src/org/osgi/service/wireadmin/WireAdmin.java,v 1.7 2005/08/12 00:06:24 hargrave Exp $
 *
 * Copyright (c) OSGi Alliance (2002, 2005). All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.osgi.service.wireadmin;

import java.util.Dictionary;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Wire Administration service.
 * 
 * <p>
 * This service can be used to create <code>Wire</code> objects connecting a
 * Producer service and a Consumer service. <code>Wire</code> objects also have
 * wire properties that may be specified when a <code>Wire</code> object is
 * created. The Producer and Consumer services may use the <code>Wire</code>
 * object's properties to manage or control their interaction. The use of
 * <code>Wire</code> object's properties by a Producer or Consumer services is
 * optional.
 * 
 * <p>
 * Security Considerations. A bundle must have
 * <code>ServicePermission[WireAdmin,GET]</code> to get the Wire Admin service to
 * create, modify, find, and delete <code>Wire</code> objects.
 * 
 * @version $Revision: 1.7 $
 */
public interface WireAdmin {
	/**
	 * Create a new <code>Wire</code> object that connects a Producer service to a
	 * Consumer service.
	 * 
	 * The Producer service and Consumer service do not have to be registered
	 * when the <code>Wire</code> object is created.
	 * 
	 * <p>
	 * The <code>Wire</code> configuration data must be persistently stored. All
	 * <code>Wire</code> connections are reestablished when the <code>WireAdmin</code>
	 * service is registered. A <code>Wire</code> can be permanently removed by
	 * using the {@link #deleteWire}method.
	 * 
	 * <p>
	 * The <code>Wire</code> object's properties must have case insensitive
	 * <code>String</code> objects as keys (like the Framework). However, the case
	 * of the key must be preserved.
	 * 
	 * <p>
	 * The <code>WireAdmin</code> service must automatically add the following
	 * <code>Wire</code> properties:
	 * <ul>
	 * <li>{@link WireConstants#WIREADMIN_PID}set to the value of the
	 * <code>Wire</code> object's persistent identity (PID). This value is
	 * generated by the Wire Admin service when a <code>Wire</code> object is
	 * created.</li>
	 * <li>{@link WireConstants#WIREADMIN_PRODUCER_PID}set to the value of
	 * Producer service's PID.</li>
	 * <li>{@link WireConstants#WIREADMIN_CONSUMER_PID}set to the value of
	 * Consumer service's PID.</li>
	 * </ul>
	 * If the <code>properties</code> argument already contains any of these keys,
	 * then the supplied values are replaced with the values assigned by the
	 * Wire Admin service.
	 * 
	 * <p>
	 * The Wire Admin service must broadcast a <code>WireAdminEvent</code> of type
	 * {@link WireAdminEvent#WIRE_CREATED}after the new <code>Wire</code> object
	 * becomes available from {@link #getWires}.
	 * 
	 * @param producerPID The <code>service.pid</code> of the Producer service to
	 *        be connected to the <code>Wire</code> object.
	 * @param consumerPID The <code>service.pid</code> of the Consumer service to
	 *        be connected to the <code>Wire</code> object.
	 * @param properties The <code>Wire</code> object's properties. This argument
	 *        may be <code>null</code> if the caller does not wish to define any
	 *        <code>Wire</code> object's properties.
	 * @return The <code>Wire</code> object for this connection.
	 * 
	 * @throws java.lang.IllegalArgumentException If <code>properties</code>
	 *         contains invalid wire types or case variants of the same key
	 *         name.
	 */
	public Wire createWire(String producerPID, String consumerPID,
			Dictionary properties);

	/**
	 * Delete a <code>Wire</code> object.
	 * 
	 * <p>
	 * The <code>Wire</code> object representing a connection between a Producer
	 * service and a Consumer service must be removed. The persistently stored
	 * configuration data for the <code>Wire</code> object must destroyed. The
	 * <code>Wire</code> object's method {@link Wire#isValid}will return
	 * <code>false</code> after it is deleted.
	 * 
	 * <p>
	 * The Wire Admin service must broadcast a <code>WireAdminEvent</code> of type
	 * {@link WireAdminEvent#WIRE_DELETED}after the <code>Wire</code> object
	 * becomes invalid.
	 * 
	 * @param wire The <code>Wire</code> object which is to be deleted.
	 */
	public void deleteWire(Wire wire);

	/**
	 * Update the properties of a <code>Wire</code> object.
	 * 
	 * The persistently stored configuration data for the <code>Wire</code> object
	 * is updated with the new properties and then the Consumer and Producer
	 * services will be called at the respective
	 * {@link Consumer#producersConnected}and
	 * {@link Producer#consumersConnected}methods.
	 * 
	 * <p>
	 * The Wire Admin service must broadcast a <code>WireAdminEvent</code> of type
	 * {@link WireAdminEvent#WIRE_UPDATED}after the updated properties are
	 * available from the <code>Wire</code> object.
	 * 
	 * @param wire The <code>Wire</code> object which is to be updated.
	 * @param properties The new <code>Wire</code> object's properties or
	 *        <code>null</code> if no properties are required.
	 * 
	 * @throws java.lang.IllegalArgumentException If <code>properties</code>
	 *         contains invalid wire types or case variants of the same key
	 *         name.
	 */
	public void updateWire(Wire wire, Dictionary properties);

	/**
	 * Return the <code>Wire</code> objects that match the given <code>filter</code>.
	 * 
	 * <p>
	 * The list of available <code>Wire</code> objects is matched against the
	 * specified <code>filter</code>.<code>Wire</code> objects which match the
	 * <code>filter</code> must be returned. These <code>Wire</code> objects are not
	 * necessarily connected. The Wire Admin service should not return invalid
	 * <code>Wire</code> objects, but it is possible that a <code>Wire</code> object
	 * is deleted after it was placed in the list.
	 * 
	 * <p>
	 * The filter matches against the <code>Wire</code> object's properties
	 * including {@link WireConstants#WIREADMIN_PRODUCER_PID},
	 * {@link WireConstants#WIREADMIN_CONSUMER_PID}and
	 * {@link WireConstants#WIREADMIN_PID}.
	 * 
	 * @param filter Filter string to select <code>Wire</code> objects or
	 *        <code>null</code> to select all <code>Wire</code> objects.
	 * @return An array of <code>Wire</code> objects which match the
	 *         <code>filter</code> or <code>null</code> if no <code>Wire</code>
	 *         objects match the <code>filter</code>.
	 * @throws org.osgi.framework.InvalidSyntaxException If the specified
	 *         <code>filter</code> has an invalid syntax.
	 * @see org.osgi.framework.Filter
	 */
	public Wire[] getWires(String filter) throws InvalidSyntaxException;
}
