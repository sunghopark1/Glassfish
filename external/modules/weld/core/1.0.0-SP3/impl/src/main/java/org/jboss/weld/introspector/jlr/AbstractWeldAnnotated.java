/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.introspector.jlr;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.introspector.AnnotationStore;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Reflections;
import org.jboss.weld.util.collections.Arrays2;

/**
 * Represents functionality common for all annotated items, mainly different
 * mappings of the annotations and meta-annotations
 * 
 * AbstractAnnotatedItem is an immutable class and therefore threadsafe
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 * 
 * @param <T>
 * @param <S>
 * 
 * @see org.jboss.weld.introspector.WeldAnnotated
 */
public abstract class AbstractWeldAnnotated<T, S> implements WeldAnnotated<T, S>
{

   interface WrappableAnnotatedItem<T, S> extends WeldAnnotated<T, S>
   {

      AnnotationStore getAnnotationStore();

   }

   // Cached string representation
   private String toString;
   private final AnnotationStore annotationStore;
   private final Class<T> rawType;
   private final Type[] actualTypeArguments; 
   private final Type type;
   private final Map<Class<?>, Type> typeClosureAsMap;
   private final Set<Type> typeClosureAsSet;
   private final Set<Type> interfaceOnlyFlattenedTypes;
   private final boolean proxyable;
   private final boolean _parameterizedType;

   /**
    * Constructor
    * 
    * Also builds the meta-annotation map. Throws a NullPointerException if
    * trying to register a null map
    * 
    * @param annotationMap A map of annotation to register
    * 
    */
   public AbstractWeldAnnotated(AnnotationStore annotatedItemHelper, Class<T> rawType, Type type, Set<Type> typeClosure)
   {
      this.annotationStore = annotatedItemHelper;
      this.rawType = rawType;
      this.type = type;
      if (type instanceof ParameterizedType)
      {
         this.actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
      }
      else
      {
         this.actualTypeArguments = new Type[0];
      }
      this._parameterizedType = Reflections.isParameterizedType(rawType);
      this.interfaceOnlyFlattenedTypes = new HashSet<Type>();
      for (Type t : rawType.getGenericInterfaces())
      {
         interfaceOnlyFlattenedTypes.addAll(new Reflections.HierarchyDiscovery(t).getTypeClosure());
      }
      this.typeClosureAsMap = Reflections.buildTypeMap(typeClosure);
      this.typeClosureAsSet = typeClosure;
      this.proxyable = Proxies.isTypesProxyable(typeClosureAsSet);
   }

   public AbstractWeldAnnotated(AnnotationStore annotatedItemHelper)
   {
      this.annotationStore = annotatedItemHelper;
      this.rawType = null;
      this.type = null;
      this.actualTypeArguments = new Type[0];
      this._parameterizedType = false;
      this.typeClosureAsMap = null;
      this.typeClosureAsSet = null;
      this.interfaceOnlyFlattenedTypes = null;
      this.proxyable = false;
   }

   public AnnotationStore getAnnotationStore()
   {
      return annotationStore;
   }

   public <A extends Annotation> A getAnnotation(Class<A> annotationType)
   {
      return getAnnotationStore().getAnnotation(annotationType);
   }

   public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return getAnnotationStore().getMetaAnnotations(metaAnnotationType);
   }

   public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return getAnnotationStore().getDeclaredMetaAnnotations(metaAnnotationType);
   }

   public Annotation[] getMetaAnnotationsAsArray(Class<? extends Annotation> metaAnnotationType)
   {
      return getMetaAnnotations(metaAnnotationType).toArray(new Annotation[0]);
   }

   public Set<Annotation> getAnnotations()
   {
      return getAnnotationStore().getAnnotations();
   }

   /**
    * Checks if an annotation is present on the item
    * 
    * @param annotatedType The annotation type to check for
    * @return True if present, false otherwise.
    * 
    * @see org.jboss.weld.introspector.WeldAnnotated#isAnnotationPresent(Class)
    */
   public boolean isAnnotationPresent(Class<? extends Annotation> annotatedType)
   {
      return getAnnotationStore().isAnnotationPresent(annotatedType);
   }

   /**
    * Compares two AbstractAnnotatedItems
    * 
    * @param other The other item
    * @return True if equals, false otherwise
    */
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof WeldAnnotated<?, ?>)
      {
         WeldAnnotated<?, ?> that = (WeldAnnotated<?, ?>) other;
         return this.getAnnotations().equals(that.getAnnotations()) && this.getJavaClass().equals(that.getJavaClass()) && this.getActualTypeArguments().length == that.getActualTypeArguments().length && Arrays.equals(this.getActualTypeArguments(), that.getActualTypeArguments());
      }
      return false;
   }

   /**
    * Gets the hash code of the actual type
    * 
    * @return The hash code
    */
   @Override
   public int hashCode()
   {
      return getJavaClass().hashCode();
   }

   /**
    * Gets a string representation of the item
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      if (toString != null)
      {
         return toString;
      }
      toString = "Abstract annotated item " + getName();
      return toString;
   }

   @Deprecated
   public Set<Annotation> getQualifiers()
   {
      return getAnnotationStore().getBindings();
   }

   /**
    * Gets (as array) the binding types of the item
    * 
    * Looks at the meta-annotations map for annotations with binding type
    * meta-annotation. Returns default binding (current) if none specified.
    * 
    * @return An array of (binding type) annotations
    * 
    * @see org.jboss.weld.introspector.WeldAnnotated#getBindingsAsArray()
    */
   @Deprecated
   public Annotation[] getBindingsAsArray()
   {
      return getAnnotationStore().getBindingsAsArray();
   }

   /**
    * Indicates if the type is proxyable to a set of pre-defined rules
    * 
    * @return True if proxyable, false otherwise.
    * 
    * @see org.jboss.weld.introspector.WeldAnnotated#isProxyable()
    */
   public boolean isProxyable()
   {
      return proxyable;
   }

   public Class<T> getJavaClass()
   {
      return rawType;
   }

   public Type[] getActualTypeArguments()
   {
      return Arrays2.copyOf(actualTypeArguments, actualTypeArguments.length);
   }

   public Set<Type> getInterfaceOnlyFlattenedTypeHierarchy()
   {
      return Collections.unmodifiableSet(interfaceOnlyFlattenedTypes);
   }

   public abstract S getDelegate();

   public boolean isDeclaredAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return getAnnotationStore().isDeclaredAnnotationPresent(annotationType);
   }

   public boolean isParameterizedType()
   {
      return _parameterizedType;
   }

   public Type getBaseType()
   {
      return type;
   }

   public Set<Type> getTypeClosure()
   {
      return typeClosureAsSet;
   }
   
   public Map<Class<?>, Type> getTypeClosureAsMap()
   {
      return typeClosureAsMap;
   }

}