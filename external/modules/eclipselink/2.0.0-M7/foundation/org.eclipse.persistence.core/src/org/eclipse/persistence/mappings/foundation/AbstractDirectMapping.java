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
package org.eclipse.persistence.mappings.foundation;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.*;
import org.eclipse.persistence.expressions.*;
import org.eclipse.persistence.internal.databaseaccess.DatabaseAccessor;
import org.eclipse.persistence.internal.databaseaccess.DatabasePlatform;
import org.eclipse.persistence.internal.descriptors.*;
import org.eclipse.persistence.internal.expressions.SQLSelectStatement;
import org.eclipse.persistence.internal.helper.*;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.internal.queries.JoinedAttributeManager;
import org.eclipse.persistence.internal.sessions.*;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.*;
import org.eclipse.persistence.mappings.querykeys.DirectQueryKey;
import org.eclipse.persistence.mappings.querykeys.QueryKey;
import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.sessions.remote.*;
import org.eclipse.persistence.sessions.ObjectCopyingPolicy;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.internal.security.PrivilegedClassForName;
import org.eclipse.persistence.internal.security.PrivilegedNewInstanceFromClass;

/**
 * <b>Purpose</b>: Maps an attribute to the corresponding database field type.
 * The list of field types that are supported by EclipseLink's direct to field mapping
 * is dependent on the relational database being used.
 * A converter can be used to convert between the object and data type if they do not match.
 *
 * @see Converter
 * @see ObjectTypeConverter
 * @see TypeConversionConverter
 * @see SerializedObjectConverter
 * @see ClassInstanceConverter
 *
 * @author Sati
 * @since TopLink/Java 1.0
 */
public abstract class AbstractDirectMapping extends DatabaseMapping  implements MapKeyMapping {

    /** DatabaseField which this mapping represents. */
    protected DatabaseField field;

    /** To specify the conversion type */
    protected transient Class attributeClassification;
    protected transient String attributeClassificationName;
    
    /** PERF: Also store object class of attribute in case of primitive. */
    protected transient Class attributeObjectClassification;

    /** Allows user defined conversion between the object attribute value and the database value. */
    protected Converter converter;
    protected String converterClassName;

    /** Support specification of the value to use for null. */
    protected transient Object nullValue;
    
    protected DatabaseTable keyTableForMapKey = null;

    /**
     * PERF: Indicates if this mapping's attribute is a simple atomic value and cannot be modified, only replaced.
     * This is a tri-state to allow user to set to true or false, as default is false but
     * some data-types such as Calendar or byte[] or converter types may be desired to be used as mutable.
     */
    protected Boolean isMutable;

    /**
     * Default constructor.
     */
    public AbstractDirectMapping() {
        super();
        this.setWeight(WEIGHT_1);
    }

    /**
     * INTERNAL:
     * Used when initializing queries for mappings that use a Map
     * Called when the selection query is being initialized to add the fields for the map key to the query
     */
    public void addAdditionalFieldsToQuery(ReadQuery selectionQuery, Expression baseExpression){
        if (selectionQuery.isObjectLevelReadQuery()){
            if (baseExpression == null){
                ((ObjectLevelReadQuery)selectionQuery).addAdditionalField((DatabaseField)getField().clone());
            } else {
                ((ObjectLevelReadQuery)selectionQuery).addAdditionalField(baseExpression.getField(getField()));
            }
        
        } else if (selectionQuery.isDataReadQuery()){
            if (baseExpression == null){
                ((SQLSelectStatement)((DataReadQuery)selectionQuery).getSQLStatement()).addField((DatabaseField)getField().clone());
                ((SQLSelectStatement)((DataReadQuery)selectionQuery).getSQLStatement()).addTable((DatabaseTable)getField().getTable().clone());
            } else {
                ((SQLSelectStatement)((DataReadQuery)selectionQuery).getSQLStatement()).addField(baseExpression.getTable(getField().getTable()).getField(getField()));
            }
        }
    }

    /**
     * INTERNAL:
     * Used when initializing queries for mappings that use a Map
     * Called when the insert query is being initialized to ensure the fields for the map key are in the insert query
     */
    public void addFieldsForMapKey(AbstractRecord joinRow){
        joinRow.put(getField(), null);
    }

    /**
     * INTERNAL:
     * For mappings used as MapKeys in MappedKeyContainerPolicy.  Add the target of this mapping to the deleted 
     * objects list if necessary
     *
     * This method is used for removal of private owned relationships
     * DirectMappings are dealt with in their parent delete, so this is a no-op.
     * 
     * @param object
     * @param manager
     */
    public void addKeyToDeletedObjectsList(Object object, Map deletedObjects){
    }
    
    /**
     * PUBLIC:
     * Return the converter on the mapping.
     * A converter can be used to convert between the object's value and database value of the attribute.
     */
    public Converter getConverter() {
        return converter;
    }

    /**
     * PUBLIC:
     * Set the converter on the mapping.
     * A converter can be used to convert between the object's value and database value of the attribute.
     */
    public void setConverter(Converter converter) {
        this.converter = converter;
    }
    
    /**
     * PUBLIC:
     * Set the converter class name on the mapping. It will be instantiated 
     * during the convertClassNamesToClasses.
     * A converter can be used to convert between the object's value and 
     * database value of the attribute.
     */
    public void setConverterClassName(String converterClassName) {
        this.converterClassName = converterClassName;
    }

    /**
     * PUBLIC:
     * Return true if the attribute for this mapping is a simple atomic value that cannot be modified,
     * only replaced.
     * This is false by default unless a mutable converter is used such as the SerializedObjectConverter.
     * This can be set to false in this case, or if a Calendar or byte[] is desired to be used as a mutable value it can be set to true.
     */
    public boolean isMutable() {
        if (isMutable == null) {
            return false;
        }
        return isMutable.booleanValue();
    }

    /**
     * PUBLIC:
     * Return true if the attribute for this mapping is a simple atomic value that cannot be modified,
     * only replaced.
     * This is false by default unless a mutable converter is used such as the SerializedObjectConverter.
     * This can be set to false in this case, or if a Calendar or byte[] is desired to be used as a mutable value it can be set to true.
     */
    public void setIsMutable(boolean isMutable) {
        if (isMutable == true) {
            this.isMutable = Boolean.TRUE;
        } else {
            this.isMutable = Boolean.FALSE;
        }
    }
    
    /**
     * INTERNAL:
     * Clone the attribute from the clone and assign it to the backup.
     */
    public void buildBackupClone(Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        buildClone(clone, backup, unitOfWork);
    }

    /**
     * INTERNAL:
     * Clone the attribute from the original and assign it to the clone.
     */
    public void buildClone(Object original, Object clone, UnitOfWorkImpl unitOfWork) {
        buildCloneValue(original, clone, unitOfWork);
    }

    /**
     * INTERNAL:
     * Clone the attribute from the original and assign it to the clone.
     * If mutability is configured to be true, clone the attribute if it is an instance of
     * byte[], java.util.Calendar or java.util.Date (or their subclasses).
     */
    public void buildCloneValue(Object original, Object clone, AbstractSession session) {
        Object attributeValue = getAttributeValueFromObject(original);
        attributeValue = buildCloneValue(attributeValue, session);
        setAttributeValueInObject(clone, attributeValue);
    }
    
    /**
     * INTERNAL:
     * Clone the actual value represented by this mapping.  Do set the cloned value into the object.
     * @param attributeValue
     * @param session
     * @return
     */
    protected Object buildCloneValue(Object attributeValue, AbstractSession session) {
        Object newAttributeValue = attributeValue;
        if (isMutable() && attributeValue != null) {
            // EL Bug 252047 - Mutable attributes are not cloned when isMutable is enabled on a Direct Mapping
            if (attributeValue instanceof byte[]) {
                int length = ((byte[]) attributeValue).length;
                byte[] arrayCopy = new byte[length];
                System.arraycopy(attributeValue, 0, arrayCopy, 0, length);
                newAttributeValue = arrayCopy;
            } else if (attributeValue instanceof Date) {
                newAttributeValue = ((Date)attributeValue).clone();
            } else if (attributeValue instanceof Calendar) {
                newAttributeValue = ((Calendar)attributeValue).clone();
            } else {
                newAttributeValue = getAttributeValue(getFieldValue(attributeValue, session), session);                    
            }
        }
        return newAttributeValue;
    }
    
    /**
     * INTERNAL:
     * Copy of the attribute of the object.
     * This is NOT used for unit of work but for templatizing an object.
     */
    public void buildCopy(Object copy, Object original, ObjectCopyingPolicy policy) {
        buildCloneValue(original, copy, policy.getSession());
    }

    /**
     * Build a clone of the given element in a unitOfWork
     * @param element
     * @param unitOfWork
     * @param isExisting
     * @return
     */
    public Object buildElementClone(Object attributeValue, Object parent, UnitOfWorkImpl unitOfWork, boolean isExisting){
        return buildCloneValue(attributeValue, unitOfWork);
    }
    
    /**
     * INTERNAL:
     * In case Query By Example is used, this method builds and returns an expression that
     * corresponds to a single attribute and it's value for a directToField mapping.
     */
    public Expression buildExpression(Object queryObject, QueryByExamplePolicy policy, Expression expressionBuilder, Map processedObjects, AbstractSession session) {
        String attributeName = this.getAttributeName();
        Object attributeValue = this.getAttributeValueFromObject(queryObject);

        if (!policy.shouldIncludeInQuery(queryObject.getClass(), attributeName, attributeValue)) {
            //the attribute name and value pair is not to be included in the query.
            return null;
        }

        Expression expression = expressionBuilder.get(attributeName);
        if (attributeValue == null) {
            expression = policy.completeExpressionForNull(expression);
        } else {
            expression = policy.completeExpression(expression, attributeValue, attributeValue.getClass());
        }

        return expression;
    }

    /**
     * INTERNAL:
     * Certain key mappings favor different types of selection query.  Return the appropriate
     * type of selectionQuery
     * @return
     */
    public ReadQuery buildSelectionQueryForDirectCollectionKeyMapping(ContainerPolicy containerPolicy){
        DataReadQuery query = new DataReadQuery();
        query.setSQLStatement(new SQLSelectStatement());
        query.setContainerPolicy(containerPolicy);
        return query;
    }
    
    /**
     * INTERNAL:
     * Cascade discover and persist new objects during commit to the map key
     */
    public void cascadeDiscoverAndPersistUnregisteredNewObjects(Object object, Map newObjects, Map unregisteredExistingObjects, Map visitedObjects, UnitOfWorkImpl uow,  boolean getAttributeValueFromObject){
        //objects referenced by this mapping are not registered as they have
        // no identity, this is a no-op.
    }
    
    /**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, Map visitedObjects, boolean getAttributeValueFromObject) {
        //objects referenced by this mapping are not registered as they have
        // no identity, this is a no-op.
    }
    
    /**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, Map visitedObjects) {
        //objects referenced by this mapping are not registered as they have
        // no identity, this is a no-op.
    }
    
    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, Map visitedObjects, boolean getAttributeValueFromObject) {
        //objects referenced by this mapping are not registered as they have
        // no identity, this is a no-op.
    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, Map visitedObjects) {
        //objects referenced by this mapping are not registered as they have
        // no identity, this is a no-op.
    }

    /**
     * INTERNAL:
     * The mapping clones itself to create deep copy.
     */
    public Object clone() {
        AbstractDirectMapping clone = (AbstractDirectMapping)super.clone();

        // Field must be cloned so aggregates do not share fields.
        clone.setField((DatabaseField)getField().clone());

        return clone;
    }

    /**
     * Returns the field this mapping represents.
     */
    protected Vector<DatabaseField> collectFields() {
        Vector databaseField = new Vector(1);

        databaseField.addElement(getField());
        return databaseField;
    }

    /**
     * INTERNAL:
     * Compare the clone and backup clone values and return a change record if the value changed.
     */
    public ChangeRecord compareForChange(Object clone, Object backUp, ObjectChangeSet owner, AbstractSession session) {
        // same code as write from object into row for update
        if ((owner.isNew()) || (!compareObjects(backUp, clone, session))) {
            return buildChangeRecord(clone, owner, session);
        }
        return null;
    }

    
    /**
     * INTERNAL:
     * For mappings used as MapKeys in MappedKeyContainerPolicy, Delete the passed object if necessary.
     * 
     * This method is used for removal of private owned relationships
     * DirectMappings are dealt with in their parent delete, so this is a no-op.
     * 
     * @param objectDeleted
     * @param session
     */
    public void deleteMapKey(Object objectDeleted, AbstractSession session){
    }
    
    /**
     * INTERNAL:
     * Directly build a change record without comparison
     */
    public ChangeRecord buildChangeRecord(Object clone, ObjectChangeSet owner, AbstractSession session) {
        return internalBuildChangeRecord(getAttributeValueFromObject(clone), owner);
    }

    /**
     * INTERNAL:
     * Build a change record
     */
    public ChangeRecord internalBuildChangeRecord(Object newValue, ObjectChangeSet owner) {
        DirectToFieldChangeRecord changeRecord = new DirectToFieldChangeRecord(owner);
        changeRecord.setAttribute(getAttributeName());
        changeRecord.setMapping(this);
        changeRecord.setNewValue(newValue);
        return changeRecord;
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        Object firstValue = getAttributeValueFromObject(firstObject);
        Object secondValue = getAttributeValueFromObject(secondObject);
        return compareObjectValues(firstValue, secondValue, session);        
    }
    
    /**
     * INTERNAL:
     * Compare the attribute values.
     */
    protected boolean compareObjectValues(Object firstValue, Object secondValue, AbstractSession session) {
        // PERF: Check identity before conversion.
        if (firstValue == secondValue) {
            return true;
        }

        // CR2114 - following two lines modified; getFieldValue() needs class as an argument
        firstValue = getFieldValue(firstValue, session);
        secondValue = getFieldValue(secondValue, session);
        // PERF:  Check identity/nulls before special type comparison.
        if (firstValue == secondValue) {
            return true;
        }

        if ((firstValue == null) || (secondValue == null)) {
            return false;
        }

        // PERF: Check equals first, as normally no change.
        boolean equal = firstValue.equals(secondValue);
        if (equal) {
            return true;
        }
        
        return Helper.comparePotentialArrays(firstValue, secondValue);
    }
    
    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this mapping to actual class-based
     * settings
     * This method is implemented by subclasses as necessary.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);
        
        if (getAttributeClassificationName() != null) {
            Class attributeClass = null;
            try{
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        attributeClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(getAttributeClassificationName(), true, classLoader));
                    } catch (PrivilegedActionException exception) {
                        throw ValidationException.classNotFoundWhileConvertingClassNames(getAttributeClassificationName(), exception.getException());
                    }
                } else {
                    attributeClass = org.eclipse.persistence.internal.security.PrivilegedAccessHelper.getClassForName(getAttributeClassificationName(), true, classLoader);
                }
            } catch (ClassNotFoundException exc){
                throw ValidationException.classNotFoundWhileConvertingClassNames(getAttributeClassificationName(), exc);
            }
            setAttributeClassification(attributeClass);
        }
            
        if (converter != null) {
            if (converter instanceof TypeConversionConverter) {
                ((TypeConversionConverter)converter).convertClassNamesToClasses(classLoader);
            } else if (converter instanceof ObjectTypeConverter) {
                // To avoid 1.5 dependencies with the EnumTypeConverter check
                // against ObjectTypeConverter.
                ((ObjectTypeConverter) converter).convertClassNamesToClasses(classLoader);
            }
        } 
        
        // Instantiate any custom converter class
        if (converterClassName != null) {
            Class converterClass;
            Converter converter;
    
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        converterClass = (Class) AccessController.doPrivileged(new PrivilegedClassForName(converterClassName, true, classLoader));
                    } catch (PrivilegedActionException exception) {
                        throw ValidationException.classNotFoundWhileConvertingClassNames(converterClassName, exception.getException());
                    }
                    
                    try {
                        converter = (Converter) AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(converterClass));
                    } catch (PrivilegedActionException exception) {
                        throw ValidationException.classNotFoundWhileConvertingClassNames(converterClassName, exception.getException());
                    }
                } else {
                    converterClass = org.eclipse.persistence.internal.security.PrivilegedAccessHelper.getClassForName(converterClassName, true, classLoader);
                    converter = (Converter) org.eclipse.persistence.internal.security.PrivilegedAccessHelper.newInstanceFromClass(converterClass);
                }
            } catch (ClassNotFoundException exc) {
                throw ValidationException.classNotFoundWhileConvertingClassNames(converterClassName, exc);
            } catch (Exception e) {
                // Catches IllegalAccessException and InstantiationException
                throw ValidationException.classNotFoundWhileConvertingClassNames(converterClassName, e);
            }
            
            setConverter(converter);
        }
    }
    
    /**
     * INTERNAL
     * Called when a DatabaseMapping is used to map the key in a collection.  Returns the key.
     */
    public Object createMapComponentFromRow(AbstractRecord dbRow, ObjectBuildingQuery query, AbstractSession session){
        Object key = dbRow.get(getField());
        key = getAttributeValue(key, session);
        return key;
    }

    /**
     * INTERNAL
     * Called when a DatabaseMapping is used to map the key in a collection and a join query is executed.  Returns the key.
     */
    public Object createMapComponentFromJoinedRow(AbstractRecord dbRow, JoinedAttributeManager joinManger, ObjectBuildingQuery query, AbstractSession session){
        return createMapComponentFromRow(dbRow, query, session);
    }
    
    /**
     * INTERNAL:
     * Create a query key that links to the map key
     * @return
     */
    public QueryKey createQueryKeyForMapKey(){
        DirectQueryKey queryKey = new DirectQueryKey();
        queryKey.setField(field);
        return queryKey;
    }
    
    /**
     * INTERNAL:
     * Extract the fields for the Map key from the object to use in a query
     * @return
     */
    public Map extractIdentityFieldsForQuery(Object object, AbstractSession session){
        Map fields = new HashMap();
        Object key = object;
        if (getConverter() != null){
             key = getConverter().convertObjectValueToDataValue(key , session);
        }
        fields.put(getField(), key);
        return fields;
    }
    
    /**
     * INTERNAL:
     * An object has been serialized from the server to the client.
     * Replace the transient attributes of the remote value holders
     * with client-side objects.
     */
    public void fixObjectReferences(Object object, Map objectDescriptors, Map processedObjects, ObjectLevelReadQuery query, RemoteSession session) {
    }

    /**
     * INTERNAL:
     * Return any tables that will be required when this mapping is used as part of a join query
     * @return
     */
    public List<DatabaseTable> getAdditionalTablesForJoinQuery(){
        List tables = new ArrayList(1);
        tables.add(getField().getTable());
        return tables;
    }
    
    /**
     * PUBLIC:
     * Some databases do not properly support all of the base data types. For these databases,
     * the base data type must be explicitly specified in the mapping to tell EclipseLink to force
     * the instance variable value to that data type
     */
    public Class getAttributeClassification() {
        return attributeClassification;
    }

    /**
     * INTERNAL:
     * Return the class name of the attribute type.
     * This is only used by the MW.
     */
    public String getAttributeClassificationName() {
        if ((attributeClassificationName == null) && (attributeClassification != null)) {
            attributeClassificationName = attributeClassification.getName();
        }
        return attributeClassificationName;
    }

    /**
     * INTERNAL:
     * Allows for subclasses to convert the attribute value.
     */
    public Object getAttributeValue(Object fieldValue, Session session) {
        // PERF: Direct variable access.
        Object attributeValue = fieldValue;
        if ((fieldValue == null) && (this.nullValue != null)) {// Translate default null value
            return this.nullValue;
        }

        // Allow for user defined conversion to the object value.
        if (this.converter != null) {
            attributeValue = this.converter.convertDataValueToObjectValue(attributeValue, session);
        } else {
            // PERF: Avoid conversion check when not required.
            if ((attributeValue == null) || (attributeValue.getClass() != this.attributeObjectClassification)) {
                try {
                    attributeValue = session.getDatasourcePlatform().convertObject(attributeValue, this.attributeClassification);
                } catch (ConversionException e) {
                    throw ConversionException.couldNotBeConverted(this, getDescriptor(), e);
                }
            }
        }
        if (attributeValue == null) {// Translate default null value, conversion may have produced null.
            attributeValue = this.nullValue;
        }

        return attributeValue;
    }

    /**
     * INTERNAL:
     * Returns the field which this mapping represents.
     */
    public DatabaseField getField() {
        return field;
    }

    /**
     * INTERNAL:
     */
    public boolean isAbstractDirectMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Get the descriptor for this mapping
     * This method is potentially called when this mapping is used as a map key and
     * will return null since direct mappings do not have reference descriptors
     * @return
     */
    public ClassDescriptor getReferenceDescriptor(){
        return null;
    }

    /**
     * INTERNAL:
     * Return the classification for the field contained in the mapping.
     * This is used to convert the row value to a consistent Java value.
     */
    public Class getFieldClassification(DatabaseField fieldToClassify) {
        // PERF: This method is a major performance code point,
        // so has been micro optimized and uses direct variable access.
        if (fieldToClassify.type != null) {
            return fieldToClassify.type;
        } else {
            if (this.converter != null) {
                return null;
            } else {
                // PERF: Ensure the object type is used for primitives.
                return Helper.getObjectClass(this.attributeClassification);
            }
        }
    }

    /**
     * ADVANCED:
     * Return the class type of the field value.
     * This can be used if field value differs from the object value,
     * has specific typing requirements such as usage of java.sql.Blob or NChar.
     */
    public Class getFieldClassification() {
        if (getField() == null) {
            return null;
        }
        return getField().getType();
    }

    /**
     * ADVANCED:
     * Set the class type of the field value.
     * This can be used if field value differs from the object value,
     * has specific typing requirements such as usage of java.sql.Blob or NChar.
     * This must be called after the field name has been set.
     */
    public void setFieldClassification(Class fieldType) {
        getField().setType(fieldType);
    }

    /**
     * ADVANCED:
     * Set the JDBC type of the field value.
     * This can be used if field type does not correspond directly to a Java class type,
     * such as MONEY.
     * This is used for binding.
     */
    public void setFieldType(int jdbcType) {
        getField().setSqlType(jdbcType);
    }

    /**
     * PUBLIC:
     * Name of the field this mapping represents.
     */
    public String getFieldName() {
        return getField().getQualifiedName();
    }

    /**
     * INTERNAL:
     * Convert the attribute value to a field value.
     * Process any converter if defined, and check for null values.
     */
    public Object getFieldValue(Object attributeValue, AbstractSession session) {
        // PERF: This method is a major performance code point,
        // so has been micro optimized and uses direct variable access.
        Object fieldValue = attributeValue;
        if ((this.nullValue != null) && (this.nullValue.equals(fieldValue))) {
            return null;
        }

        // Allow for user defined conversion to the object value.
        if (this.converter != null) {
            fieldValue = this.converter.convertObjectValueToDataValue(fieldValue, session);
        }
        Class fieldClassification = getFieldClassification(this.field);
        // PERF: Avoid conversion if not required.
        // EclipseLink bug 240407 - nulls not translated when writing to database
        if ((fieldValue == null) || (fieldClassification != fieldValue.getClass())) {
            try {
                fieldValue = session.getPlatform(this.descriptor.getJavaClass()).convertObject(fieldValue, fieldClassification);
            } catch (ConversionException exception) {
                throw ConversionException.couldNotBeConverted(this, this.descriptor, exception);
            }
        }
        return fieldValue;
    }

    /**
     * INTERNAL:
     * Return a Map of any foreign keys defined within the the MapKey
     * @return
     */
    public Map<DatabaseField, DatabaseField> getForeignKeyFieldsForMapKey(){
        return null;
    }
    
    /**
     * INTERNAL:
     * Return the fields that make up the identity of the mapped object.  For mappings with
     * a primary key, it will be the set of fields in the primary key.  For mappings without
     * a primary key it will likely be all the fields
     * @return
     */
    public List<DatabaseField> getIdentityFieldsForMapKey(){
        return getAllFieldsForMapKey();
    }
    
    /**
     * INTERNAL:
     * Get all the fields for the map key
     */
    public List<DatabaseField> getAllFieldsForMapKey(){
        Vector<DatabaseField> fields = new Vector<DatabaseField>(1);
        fields.add(getField());
        return fields;
    }
    
    /**
     * INTERNAL:
     * Return the query that is used when this mapping is part of a joined relationship
     * 
     * This method is used when this mapping is used to map the key in a Map
     * @return
     */
    public ObjectLevelReadQuery getNestedJoinQuery(JoinedAttributeManager joinManager, ObjectLevelReadQuery query, AbstractSession session){
        return null;
    }
    
    /**
     * PUBLIC:
     * Allow for the value used for null to be specified.
     * This can be used to convert database null values to application specific values, when null values
     * are not allowed by the application (such as in primitives).
     * Note: the default value for NULL is used on reads, writes, and query SQL generation
     */
    public Object getNullValue() {
        return nullValue;
    }
    
    /**
     * INTERNAL:
     * Return the selection criteria necessary to select the target object when this mapping
     * is a map key.
     * 
     * DirectMappings do not need any additional selection criteria when they are map keys
     * @return
     */
    public Expression getAdditionalSelectionCriteriaForMapKey(){
        return null;
    }
    
    /**
     * INTERNAL:
     * If required, get the targetVersion of the source object from the merge manager.
     * 
     * Used with MapKeyContainerPolicy to abstract getting the target version of a source key
     * @return
     */
    public Object getTargetVersionOfSourceObject(Object object, Object parent, MergeManager mergeManager){
       return object;
    }
    
    /**
     * INTERNAL:
     * Return the class this key mapping maps or the descriptor for it
     * @return
     */
    public Class getMapKeyTargetType(){
        return getAttributeAccessor().getAttributeClass();
    }
    
    /**
     * INTERNAL:
     * Return the weight of the mapping, used to sort mappings to ensure that
     * DirectToField Mappings get merged first
     */
    public Integer getWeight() {
        return this.weight;
    }
    
    /**
     * INTERNAL:
     * Once descriptors are serialized to the remote session. All its mappings and reference descriptors are traversed. Usually
     * mappings are initialized and serialized reference descriptors are replaced with local descriptors if they already exist on the
     * remote session.
     */
    public void remoteInitialization(DistributedSession session) {
        if (!isRemotelyInitialized()) {
            super.remoteInitialization(session);
            if (this.attributeClassification == null) {
                this.attributeClassification = getAttributeAccessor().getAttributeClass();
            }
            this.attributeObjectClassification = Helper.getObjectClass(this.attributeClassification);
        }
    }
    
    /**
     * INTERNAL:
     * Initialize the attribute classification.
     * @Throws DescriptorException when attributeClassification is null
     */
    public void preInitialize(AbstractSession session) throws DescriptorException {
        super.preInitialize(session);
        // Allow the attribute class to be set by the user.
        if (this.attributeClassification == null) {
            this.attributeClassification = getAttributeAccessor().getAttributeClass();
        }
        this.attributeObjectClassification = Helper.getObjectClass(this.attributeClassification);
        
        // Initialize isMutable if not specified, default is false (assumes not mutable).
        if (this.isMutable == null) {
            if (getConverter() != null) {
                setIsMutable(getConverter().isMutable());
            } else {
                setIsMutable(false);
            }
            // If mapping a temporal type, use the project mutable default.
            if (ClassConstants.UTILDATE.isAssignableFrom(getAttributeClassification())
                    || ClassConstants.CALENDAR.isAssignableFrom(getAttributeClassification())) {
                setIsMutable(session.getProject().getDefaultTemporalMutable());
            }
        }
    }
    
    /**
     * Indicates if the mapping has a converter set on it.
     * 
     * @return true if there is a converter set on the mapping,
     * false otherwise.
     */
    public boolean hasConverter() {
        return getConverter() != null;
    }
    
    /**
     * INTERNAL:
     * The mapping is initialized with the given session.
     * This mapping is fully initialized after this.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);

        if (getField() == null) {
            session.getIntegrityChecker().handleError(DescriptorException.fieldNameNotSetInMapping(this));
        }

        if (keyTableForMapKey == null){
            setField(getDescriptor().buildField(getField()));
        } else {
            setField(getDescriptor().buildField(getField(), keyTableForMapKey));
        }
        setFields(collectFields());

        if (getConverter() != null) {
            getConverter().initialize(this, session);
        }
        
        // Must unwrap Struct types on WLS.
        if (getField().getSqlType() == java.sql.Types.STRUCT) {
            getDescriptor().setIsNativeConnectionRequired(true);
        }
    }

    /**
     * INTERNAL:
     */
    public boolean isDirectToFieldMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Iterate on the appropriate attribute.
     */
    public void iterate(DescriptorIterator iterator) {
        // PERF: Only iterate when required.
        if (iterator.shouldIterateOnPrimitives()) {
            iterator.iteratePrimitiveForMapping(getAttributeValueFromObject(iterator.getVisitedParent()), this);
        }
    }

    /**
     * INTERNAL:
     * Called when iterating through descriptors to handle iteration on this mapping when it is used as a MapKey
     * @param iterator
     * @param element
     */
    public void iterateOnMapKey(DescriptorIterator iterator, Object element){
        if (iterator.shouldIterateOnPrimitives()) {
            iterator.iteratePrimitiveForMapping(element, this);
        }
    }
    
    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     */
    public void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager) {
        setAttributeValueInObject(target, ((DirectToFieldChangeRecord)changeRecord).getNewValue());
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object. This merge is only called when a changeSet for the target
     * does not exist or the target is uninitialized
     */
    public void mergeIntoObject(Object target, boolean isTargetUnInitialized, Object source, MergeManager mergeManager) {
        // If merge into the unit of work, must only merge and raise the event is the value changed.
        if ((mergeManager.shouldMergeCloneIntoWorkingCopy() || mergeManager.shouldMergeCloneWithReferencesIntoWorkingCopy())
                && this.descriptor.getObjectChangePolicy().isObjectChangeTrackingPolicy()) {
            // if it didn't change then there will be no event
            Object attributeValue = getAttributeValueFromObject(source);
            Object targetAttribute = getAttributeValueFromObject(target);
            if (!compareObjectValues(attributeValue, targetAttribute, mergeManager.getSession())) {
                setAttributeValueInObject(target, attributeValue);
                //set the value first, if the owner is new ( or aggregate) the change set may be created directly
                //from the target.
                this.descriptor.getObjectChangePolicy().raiseInternalPropertyChangeEvent(target, getAttributeName(), targetAttribute, attributeValue);
            }
        } else {
            //just set the value and continue
            setAttributeValueInObject(target, getAttributeValueFromObject(source));
        }
    }

    
    /**
     * INTERNAL:
     * Making any mapping changes necessary to use a the mapping as a map key prior to initializing the mapping
     */
    public void preinitializeMapKey(DatabaseTable table) throws DescriptorException {
        keyTableForMapKey = table;
    }

    /**
     * INTERNAL:
     * Allow the selectionQuery to be modified when this MapComponentMapping is used as the value in a Map
     */
    public void postInitializeMapValueSelectionQuery(ReadQuery selectionQuery, AbstractSession session){
        ((SQLSelectStatement)((DataReadQuery)selectionQuery).getSQLStatement()).normalize(session, null);
    }
    
    /**
     * PUBLIC:
     * Some databases do not properly support all of the base data types. For these databases,
     * the base data type must be explicitly specified in the mapping to tell TopLink to force
     * the instance variable value to that data type
     */
    public void setAttributeClassification(Class attributeClassification) {
        this.attributeClassification = attributeClassification;
    }

    /**
     * INTERNAL:
     * Set the name of the class for MW usage.
     */
    public void setAttributeClassificationName(String attributeClassificationName) {
        this.attributeClassificationName = attributeClassificationName;
    }

    /**
     * ADVANCED:
     * Set the field in the mapping.
     * This can be used for advanced field types, such as XML nodes, or to set the field type.
     */
    public void setField(DatabaseField theField) {
        field = theField;
    }

    /**
     * PUBLIC:
     * Allow for the value used for null to be specified.
     * This can be used to convert database null values to application specific values, when null values
     * are not allowed by the application (such as in primitives).
     * Note: the default value for NULL is used on reads, writes, and query SQL generation
     */
    public void setNullValue(Object nullValue) {
        this.nullValue = nullValue;
    }

    /**
     * INTERNAL:
     */
    public String toString() {
        return getClass().getName() + "[" + getAttributeName() + "-->" + getField() + "]";
    }

    /**
     * INTERNAL:
     * Either create a new change record or update with the new value.  This is used
     * by attribute change tracking.
     */
    public void updateChangeRecord(Object clone, Object newValue, Object oldValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) {
        DirectToFieldChangeRecord changeRecord = (DirectToFieldChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (changeRecord == null) {
            objectChangeSet.addChange(internalBuildChangeRecord(newValue, objectChangeSet));
        } else {
            changeRecord.setNewValue(newValue);
        }
    }

    /**
     * INTERNAL:
     * Return if this mapping supports change tracking.
     */
    public boolean isChangeTrackingSupported(Project project) {
        return !isMutable();
    }
    
    /**
     * INTERNAL:
     * Return if this mapping requires its attribute value to be cloned.
     */
    public boolean isCloningRequired() {
        return isMutable() || getDescriptor().getCopyPolicy().buildsNewInstance();
    }

    /**
     * INTERNAL:
     * Allow the key mapping to unwrap the object
     * @param key
     * @param session
     * @return
     */
    
    public Object unwrapKey(Object key, AbstractSession session){
        return key;
    }
    
    /**
     * INTERNAL:
     * Allow for subclasses to perform validation.
     */
    public void validateBeforeInitialization(AbstractSession session) throws DescriptorException {
        if ((getFieldName() == null) || (getFieldName().length() == 0)) {
            session.getIntegrityChecker().handleError(DescriptorException.noFieldNameForMapping(this));
        }
    }

    /**
     * INTERNAL:
     * Allow the key mapping to wrap the object
     * @param key
     * @param session
     * @return
     */
    
    public Object wrapKey(Object key, AbstractSession session){
        return key;
    }
    
    /**
     * INTERNAL:
     * Get the value from the object for this mapping.
     */
    public Object valueFromObject(Object object, DatabaseField field, AbstractSession session) throws DescriptorException {
        return getFieldValue(getAttributeValueFromObject(object), session);
    }

    /**
     * INTERNAL:
     * Extract value from the row and set the attribute to this value in the
     * working copy clone.
     * In order to bypass the shared cache when in transaction a UnitOfWork must
     * be able to populate working copies directly from the row.
     */
    public void buildCloneFromRow(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object clone, ObjectBuildingQuery sourceQuery, UnitOfWorkImpl unitOfWork, AbstractSession executionSession) {
        // Even though the correct value may exist on the original, we can't
        // make that assumption.  It is easy to just build it again from the
        // row even if copy policy already copied it.
        // That optimization is lost.
        Object attributeValue = valueFromRow(databaseRow, joinManager, sourceQuery, executionSession);

        setAttributeValueInObject(clone, attributeValue);
    }

    /**
     * INTERNAL:
     * Builds a shallow original object.  Only direct attributes and primary
     * keys are populated.  In this way the minimum original required for
     * instantiating a working copy clone can be built without placing it in
     * the shared cache (no concern over cycles).
     * @parameter original later the input to buildCloneFromRow
     */
    public void buildShallowOriginalFromRow(AbstractRecord databaseRow, Object original, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession executionSession) {
        readFromRowIntoObject(databaseRow, null, original, query, executionSession);
    }

    /**
     * INTERNAL:
     * Return the mapping's attribute value from the row.
     * The execution session is passed for the case of building a UnitOfWork clone
     * directly from a row, the session set in the query will not know which platform to use
     * for converting the value.  Allows the correct session to be passed in.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession executionSession) {
        // PERF: Direct variable access.
        Object fieldValue = row.get(this.field);
        Object attributeValue = getAttributeValue(fieldValue, executionSession);

        return attributeValue;
    }    

    /**
     * INTERNAL:
     * Returns the value for the mapping directly from the result-set.
     * PERF: Used for optimized object building.
     */
    public Object valueFromResultSet(ResultSet resultSet, ObjectLevelReadQuery query, AbstractSession session, DatabaseAccessor accessor, ResultSetMetaData metaData, int columnNumber, DatabasePlatform platform) throws DatabaseException {
        return accessor.getObject(resultSet, this.field, metaData, columnNumber, platform, true, session);
    }

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) {
        if (this.isReadOnly) {
            return;
        }

        Object attributeValue = getAttributeValueFromObject(object);
        Object fieldValue = getFieldValue(attributeValue, session);

        writeValueIntoRow(row, this.field, fieldValue);

    }

    protected abstract void writeValueIntoRow(AbstractRecord row, DatabaseField field, Object value);

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     * Validation preventing primary key updates is implemented here.
     */
    public void writeFromObjectIntoRowWithChangeRecord(ChangeRecord changeRecord, AbstractRecord row, AbstractSession session) {
        if (this.isReadOnly) {
            return;
        }

        if (this.isPrimaryKeyMapping && !changeRecord.getOwner().isNew()) {
           throw ValidationException.primaryKeyUpdateDisallowed(changeRecord.getOwner().getClassName(), changeRecord.getAttribute());
        }
        
        Object attributeValue = ((DirectToFieldChangeRecord)changeRecord).getNewValue();
        Object fieldValue = getFieldValue(attributeValue, session);

        row.add(this.field, fieldValue);
    }

    /**
     * INTERNAL:
     * Write the attribute value from the object to the row for update.
     */
    public void writeFromObjectIntoRowForUpdate(WriteObjectQuery query, AbstractRecord databaseRow) {
        if (query.getSession().isUnitOfWork()) {
            if (compareObjects(query.getBackupClone(), query.getObject(), query.getSession())) {
                return;
            }
        }

        super.writeFromObjectIntoRowForUpdate(query, databaseRow);
    }

    /**
     * INTERNAL:
     * Write fields needed for insert into the template for with null values.
     */
    public void writeInsertFieldsIntoRow(AbstractRecord databaseRow, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }

        databaseRow.add(getField(), null);
    }
}
