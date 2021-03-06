/*******************************************************************************
 * Copyright (c) 1998, 2010 Oracle. All rights reserved.
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
package org.eclipse.persistence.jaxb.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import org.eclipse.persistence.jaxb.javamodel.Helper;
import org.eclipse.persistence.jaxb.javamodel.JavaClass;
import org.eclipse.persistence.jaxb.javamodel.JavaHasAnnotations;
import org.eclipse.persistence.jaxb.javamodel.JavaMethod;
import org.eclipse.persistence.jaxb.xmlmodel.XmlElementRef;
import org.eclipse.persistence.jaxb.xmlmodel.XmlElementWrapper;
import org.eclipse.persistence.jaxb.xmlmodel.XmlElements;
import org.eclipse.persistence.jaxb.xmlmodel.XmlJavaTypeAdapter;

/**
 *  INTERNAL:
 *  <p><b>Purpose:</b>To store information about a property on a class during JAXB 2.0 Generation
 *  <p><b>Responsibilities:</b><ul>
 *  <li>Store information about the java property such as property name, if it's a method or field,
 *  and it's type.</li>
 *  <li>Store some schema-specific information such as the schema name, the schema type, and mimeType</li>
 *  <li>Store some JAXB 2.0 Runtime specific information such as JAXB 2.0 Adapter classes</li>
 *  </ul>
 *  <p>This class is used to store information about a property on a JAXB 2.0 annotated class. It is 
 *  created by the AnnotationsProcessor during pre-processing and stored on a TypeInfo object
 *  
 *  @see org.eclipse.persistence.jaxb.compiler.TypeInfo
 *  @see org.eclipse.persistence.jaxb.compiler.AnnotationsProcessor
 *  
 *  @author mmacivor
 */
public class Property {
    private String propertyName;
    private QName schemaName;
    private boolean isMethodProperty;
    private QName schemaType;
    private boolean isSwaAttachmentRef;
    private boolean isMtomAttachment;
    private boolean isInlineBinaryData;
    private String mimeType;
    private JavaClass type;
    private JavaClass adapterClass;
    private JavaHasAnnotations element;
    private JavaClass genericType;
    private boolean isAttribute = false;
    private boolean isAnyAttribute = false;
    private boolean isAnyElement = false;
    private Helper helper;
    private String getMethodName;
    private String setMethodName;
    private boolean isRequired = false;
    private boolean isNillable = false;
    private boolean isXmlList = false;
    private boolean isTransient;
    private String defaultValue;
    private boolean isMixedContent = false;
    private boolean xmlElementType = false;
    private JavaClass originalType;
    
    private XmlJavaTypeAdapter xmlJavaTypeAdapter;
    private XmlElementWrapper xmlElementWrapper;
    private boolean isXmlValue = false;
    private boolean isXmlId = false;
    private boolean isXmlIdRef = false;
    
    private String inverseReferencePropertyName;
    private String inverseReferencePropertyGetMethodName;
    private String inverseReferencePropertySetMethodName;
    private JavaClass inverseReferencePropertyContainerClass;
    private boolean isInverseReference;
    
    // XmlAnyElement specific attributes
    private boolean lax;
    private String domHandlerClassName;
      
    // XmlMap specific attributes
    private JavaClass keyType;
	private JavaClass valueType;	
	public static final String DEFAULT_KEY_NAME =  "key";
	public static final String DEFAULT_VALUE_NAME =  "value";
	private boolean isMap = false;
	
	// XmlElements specific attributes
    private Collection<Property> choiceProperties;
    private XmlElements xmlElements;
    private boolean isChoice = false;
	
    // XmlElementRef specific attributes
    private ArrayList<ElementDeclaration> referencedElements;
    private List<XmlElementRef> xmlElementRefs;
    private boolean isReference = false;

    public Property() {}

    public Property(Helper helper) {
        this.helper = helper;
    }

    public void setHelper(Helper helper) {
        this.helper = helper;
    }
    
    public void setAdapterClass(JavaClass adapterCls) {
        adapterClass = adapterCls;
        JavaClass newType  = helper.getJavaClass(Object.class);
                
        // look for marshal method
        for (Iterator<JavaMethod> methodIt = adapterClass.getDeclaredMethods().iterator(); methodIt.hasNext(); ) {
            JavaMethod method = methodIt.next();
            if (method.getName().equals("marshal")) {
            	JavaClass returnType = method.getReturnType();            	
            	if(!returnType.getQualifiedName().equals(newType.getQualifiedName())){
            		newType = (JavaClass) method.getReturnType();
            		break;
            	}
            }
        }
        setType(newType);
    }
    
    public JavaHasAnnotations getElement() {
        return element;
    }
    
    public void setElement(JavaHasAnnotations element) {
        this.element = element;
    }

    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public QName getSchemaName() {
        return this.schemaName;
    }
    
    public void setSchemaName(QName schemaName) {
        this.schemaName = schemaName;
    }
    
    public boolean isMethodProperty() {
        return isMethodProperty;
    }
    
    public void setMethodProperty(boolean isMethod) {
        isMethodProperty = isMethod;
    }
    
    public void setType(JavaClass cls) {
        if(cls == null){
            return;
        }
        if(type != null && type == cls){
        	return;
        }
    	String clsName= cls.getRawName();
        if(type != null && isCollectionType(type)){  
            genericType = cls;
        }else if(isCollectionType(cls)){
        	if(cls.hasActualTypeArguments()){
        		ArrayList typeArgs =  (ArrayList) cls.getActualTypeArguments();
        		genericType = (JavaClass) typeArgs.get(0);
        	}else{
        		genericType = helper.getJavaClass(Object.class);
        	}
            type = cls;  
                	
        }else if(cls.isArray()  && !clsName.equals("byte[]")  && !clsName.equals("java.lang.Byte[]")){
        	type = cls;
        	genericType = cls.getComponentType();
        }else{
            type = cls;
        }
        
        if(isMap()){
        	Object[] types = type.getActualTypeArguments().toArray();
        	
     	    if(types.length >=2){        	        	
     	        keyType = (JavaClass)types[0];     	        
     	        valueType = (JavaClass)types[1];
     	    }else{
     	    	keyType = helper.getJavaClass(Object.class);     	        
     	        valueType = helper.getJavaClass(Object.class);	    
             }
        }
       
    }
    
    public JavaClass getType() {
        return type;
    }
    
    public JavaClass getGenericType() {
        return genericType;
    }
    
    public void setGenericType(JavaClass genericType) {
        this.genericType = genericType;
    }
    
    public QName getSchemaType() {
        return schemaType;
    }
    
    public void setSchemaType(QName type) {
        schemaType = type;
    }
    
    public boolean isSwaAttachmentRef() {
        return isSwaAttachmentRef;
    }
    
    public void setIsSwaAttachmentRef(boolean b) {
        isSwaAttachmentRef = b;
    }
    
    public boolean isMtomAttachment() {
        return isMtomAttachment;
    }
    
    public void setIsMtomAttachment(boolean b) {
        isMtomAttachment = b;
    }

    /**
     * Indicates if XOP encoding should be disabled for datatypes that 
     * are bound to base64-encoded binary data in XML.
     * 
     * @return true if XOP encoding should be disabled for datatypes
     * that are bound to base64-encoded binary data in XML; false if 
     * not
     */
    public boolean isInlineBinaryData() {
        return isInlineBinaryData;
    }

    /**
     * Sets the flag that indicates if XOP encoding should  be disabled
     * for datatypes that are bound to base64-encoded binary data in 
     * XML.

     * @param b if true, XOP encoding will be disabled for datatypes 
     * that are bound to base64-encoded binary data in XML.
     */
    public void setisInlineBinaryData(boolean b) {
        isInlineBinaryData = b;
    }

    public boolean isRequired() {
        return isRequired;
    }
    
    public void setIsRequired(boolean b) {
        isRequired = b;
    }

    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mType) {
        mimeType = mType;
    }
    
    public boolean isAttribute() {
        return isAttribute || isAnyAttribute;
    }
    
    public boolean isAnyAttribute() {
        return isAnyAttribute;
    }
    
    public void setIsAttribute(boolean attribute) {
        isAttribute = attribute;
    }
    
    public void setIsAnyAttribute(boolean anyAtribute) {
        isAnyAttribute = anyAtribute;
    }
    
    public String getGetMethodName() {
        return getMethodName;
    }
    
    public void setGetMethodName(String methodName) {
        getMethodName = methodName;
    }
    
    public String getSetMethodName() {
        return setMethodName;
    }
    
    public void setSetMethodName(String methodName) {
        setMethodName = methodName;
    }
    
    /**
     * Indicates if this property represents a choice property.
     * 
     * @return
     */
    public boolean isChoice() {
        return isChoice;
    }    

    /**
     * Set flag to indicate whether this property represents a choice 
     * property.
     * 
     * @param choice
     */
    public void setChoice(boolean choice) {
        isChoice = choice;
    }    

    /**
     * Returns indicator for XmlAnyElement
     * 
     * @return
     */
    public boolean isAny() {
    	return isAnyElement;
    }
    
    /**
     * Set indicator for XmlAnyElement.
     * 
     * @param isAnyElement
     * @return
     */
    public void setIsAny(boolean isAnyElement) {
        this.isAnyElement = isAnyElement;
    }
    
    /**
     * Indicates if this Property is a reference property.
     * 
     * @return
     */
    public boolean isReference() {
    	return isReference;
    }
    
    /**
     * Set flag to indicate whether this property represents a reference 
     * property.
     * 
     * @param isReference
     */
    public void setIsReference(boolean isReference) {
        this.isReference = isReference;
    }

    public boolean isNillable() {
        return isNillable;
    }

    public void setNillable(boolean isNillable) {
        this.isNillable = isNillable;
    }
    
    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return the isSetDefaultValue
     */
    public boolean isSetDefaultValue() {
        return defaultValue != null;
    }
    
    public boolean isMixedContent() {
        return isMixedContent;
    }
    
    public void setMixedContent(boolean b) {
        this.isMixedContent = b;
    }

    public void setHasXmlElementType(boolean hasXmlElementType) {
        this.xmlElementType = hasXmlElementType;
    }

    public boolean isXmlElementType() {
        return xmlElementType;
    }
    
    public boolean isCollectionType(JavaClass type) {
        if (helper.getJavaClass(java.util.Collection.class).isAssignableFrom(type) 
                || helper.getJavaClass(java.util.List.class).isAssignableFrom(type) 
                || helper.getJavaClass(java.util.Set.class).isAssignableFrom(type)) {
            return true;
        }
        return false;
    }
    

    /**
     * Return the generic type if it was set (collection or array item type) otherwise return the
     * type of this property
     * 
     * @return
     */
    public JavaClass getActualType() {
        if (genericType != null) {
            return genericType;
        }
        return type;
    }

    /**
     * Get the original type of the property.  This is typically used when
     * the type has been changed via @XmlElement annotation and the 
     * original type is desired.
     *  
     * @return
     */
    public JavaClass getOriginalType() {
        return originalType;
    }
    
    /**
     * Set the original type of the property.  This is typically used when
     * the type will been changed via @XmlElement annotation and the 
     * original type may be needed.
     *  
     * @return
     */
    public void  setOriginalType(JavaClass type) {
        originalType = type;
    }
    
    /**
     * Indicates if an XmlJavaTypeAdapter has been set, i.e. the
     * xmlJavaTypeAdapter property is non-null.
     * 
     * @return true if xmlJavaTypeAdapter is non-null, false otherwise
     * @see XmlJavaTypeAdapter
     */
    public boolean isSetXmlJavaTypeAdapter() {
        return getXmlJavaTypeAdapter() != null;
    }

    /**
     * Return the xmlJavaTypeAdapter set on this Property.
     * 
     * @return xmlJavaTypeAdapter, or null if not set
     * @see XmlJavaTypeAdapter
     */
    public XmlJavaTypeAdapter getXmlJavaTypeAdapter() {
        return xmlJavaTypeAdapter;
    }

    /**
     * Set an XmlJavaTypeAdapter on this Property.  This call sets the adapterClass
     * property to the given adapter's value.
     * 
     * @param xmlJavaTypeAdapter
     * @see XmlJavaTypeAdapter
     */
    public void setXmlJavaTypeAdapter(XmlJavaTypeAdapter xmlJavaTypeAdapter) {
        this.xmlJavaTypeAdapter = xmlJavaTypeAdapter;
        if (xmlJavaTypeAdapter == null) {
            adapterClass = null;
            setType(originalType);
        } else {
            // set the adapter class
            setAdapterClass(helper.getJavaClass(xmlJavaTypeAdapter.getValue()));
        }
    }
    
    /**
     * Indicates if an XmlElementWrapper has been set, i.e. the
     * xmlElementWrapper property is non-null.

     * @return true if xmlElementWrapper is non-null, false otherwise
     */
    public boolean isSetXmlElementWrapper() {
        return getXmlElementWrapper() != null;
    }
    
    /**
     * Return the XmlElementWrapper set on this property.
     * 
     * @return XmlElementWrapper instance if non-null, null otherwise
     */
    public XmlElementWrapper getXmlElementWrapper() {
        return xmlElementWrapper;
    }

    /**
     * Set the XmlElementWrapper for this property.
     * 
     * @param xmlElementWrapper
     */
    public void setXmlElementWrapper(XmlElementWrapper xmlElementWrapper) {
        this.xmlElementWrapper = xmlElementWrapper;
    }
    
    /**
     * Set the isXmlValue property.
     * 
     * @param isXmlValue
     */
    public void setIsXmlValue(boolean isXmlValue) {
        this.isXmlValue = isXmlValue;
    }
    
    /**
     * Indicates if this property is an XmlValue.
     * 
     * @return
     */
    public boolean isXmlValue() {
        return this.isXmlValue;
    }
    
    /**
     * Set the isXmlList property.
     * 
     * @param isXmlList
     */
    public void setIsXmlList(boolean isXmlList) {
        this.isXmlList = isXmlList;
    }
    
    /**
     * Indicates if this property is an XmlList.
     * 
     * @return
     */
    public boolean isXmlList() {
        return this.isXmlList;
    }
    
    public String getInverseReferencePropertyName() {
        return this.inverseReferencePropertyName;
    }
    
    public void setInverseReferencePropertyName(String name) {
        this.inverseReferencePropertyName = name;
    }
    
    public String getInverseReferencePropertyGetMethodName() {
        return this.inverseReferencePropertyGetMethodName;
    }
    
    public String getInverseReferencePropertySetMethodName() {
        return this.inverseReferencePropertySetMethodName;
    }
    
    public void setInverseReferencePropertyGetMethodName(String methodName) {
        this.inverseReferencePropertyGetMethodName = methodName;
    }
    
    public void setInverseReferencePropertySetMethodName(String methodName) {
        this.inverseReferencePropertySetMethodName = methodName;
    }
    
    public JavaClass getInverseReferencePropertyContainerClass() {
        return this.inverseReferencePropertyContainerClass;
    }
    
    public void setInverseReferencePropertyContainerClass(JavaClass cls) {
        this.inverseReferencePropertyContainerClass = cls;
    }
    
    /**
     * Indicates if this property is an ID field.
     * 
     * @return
     */
    public boolean isXmlId() {
        return isXmlId;
    }

    /**
     * Sets the indicator that identifies this property as an ID field.
     * 
     * @param isXmlIdRef
     */
    public void setIsXmlId(boolean isXmlId) {
        this.isXmlId = isXmlId;
    }

    /**
     * Indicates if this property is a reference to an ID field.
     * 
     * @return
     */
    public boolean isXmlIdRef() {
        return isXmlIdRef;
    }

    /**
     * Sets the indicator that identifies this property as a reference
     * to an ID field.
     * 
     * @param isXmlIdRef
     */
    public void setIsXmlIdRef(boolean isXmlIdRef) {
        this.isXmlIdRef = isXmlIdRef;
    }

    // XmlAnyElement specific methods
    
    /**
     * Used with XmlAnyElement.
     * 
     * @return
     */
    public boolean isLax() {
        return lax;
    }

    /**
     * Used with XmlAnyElement.
     * 
     * @param b
     */
    public void setLax(boolean b) {
        lax = b;
    }
    
    /**
     * Return the DomHandler class name.
     * Used with XmlAnyElement.
     * 
     * @return
     */
    public String getDomHandlerClassName() {
        return domHandlerClassName;
    }

    /**
     * Set the DomHandler class name.
     * Used with XmlAnyElement.
     * 
     * @param domHandlerClassName
     */
    public void setDomHandlerClassName(String domHandlerClassName) {
        this.domHandlerClassName = domHandlerClassName;
    }
    
    public JavaClass getKeyType() {
		return keyType;
	}

	public void setKeyType(JavaClass keyType) {
		this.keyType = keyType;
	}

	public JavaClass getValueType() {
		return valueType;
	}

	public void setValueType(JavaClass valueType) {
		this.valueType = valueType;
	}

    public boolean isMap() {
        return isMap;
    }
	
	public void setIsMap(boolean isMap) {
		this.isMap = isMap;
	}

    public boolean isInverseReference() {
        return isInverseReference;
    }

    public void setInverseReference(boolean isInverseReference) {
        this.isInverseReference = isInverseReference;
    }
    
    /**
     * Return the XmlElements object set for this Property.  Typically 
     * this will only be set if we are dealing with a 'choice'.
     * 
     * @return
     */
    public XmlElements getXmlElements() {
        return xmlElements;
    }

    /**
     * Set the XmlElements object for this Property.  Typically 
     * this will only be set if we are dealing with a 'choice'.
     * 
     * @param xmlElements
     */
    public void setXmlElements(XmlElements xmlElements) {
        this.xmlElements = xmlElements;
    }

    /**
     * Return the choice properties set on this property.  Typically this
     * will only contain properties if we are dealing with a 'choice'.
     * 
     * @return
     */
    public Collection<Property> getChoiceProperties() {
        return this.choiceProperties;
    }
    
    /** 
     * Set the choice properties for this property.  Typically this
     * will only contain properties if we are dealing with a 'choice'.
     * 
     * @param properties
     */
    public void setChoiceProperties(Collection<Property> properties) {
        this.choiceProperties = properties;
    }
    
    /**
     * Return the List of XmlElementRef(s) for this Property.
     * 
     * @return
     */
    public List<XmlElementRef> getXmlElementRefs() {
        return xmlElementRefs;
    }

    /**
     * Set the List of XmlElementRef(s) for this Property.
     * 
     * @param xmlElementRefs
     */
    public void setXmlElementRefs(List<XmlElementRef> xmlElementRefs) {
        this.xmlElementRefs = xmlElementRefs;
    }
    
    /**
     * Add an ElementDeclaration to the list of referenced elements. Typically this 
     * will only contain ElementDeclarations if we are dealing with a 'reference'.
     * 
     * @param element
     */
    public void addReferencedElement(ElementDeclaration element) {
        if (referencedElements == null) {
            referencedElements = new ArrayList<ElementDeclaration>();
        }
        if (!referencedElements.contains(element)) {
            referencedElements.add(element);
        }
    }
    
    /**
     * Return the list of referenced elements.  Typically this will only
     * contain ElementDeclarations if we are dealing with a 'reference'.
     * 
     * @return
     */
    public List<ElementDeclaration> getReferencedElements() {
        return referencedElements;
    }
}
