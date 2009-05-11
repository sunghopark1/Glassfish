/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.glassfish.flashlight.datatree.TreeNode;

/**
 * @author rajeshwar patil
 */
@Provider
@Produces(MediaType.TEXT_HTML)
public class TreeNodeHtmlProvider extends DomProviderUtil implements MessageBodyWriter<List<TreeNode>> {

     @Context
     protected UriInfo uriInfo;

     public long getSize(final List<TreeNode> proxy, final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType) {
          return -1;
     }


     public boolean isWriteable(final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType) {
         if ("java.util.List<org.glassfish.flashlight.datatree.TreeNode>".equals(genericType.toString())) {
             return mediaType.isCompatible(MediaType.TEXT_HTML_TYPE);
         }
         return false;
     }


     public void writeTo(final List<TreeNode> proxy, final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType,
               final MultivaluedMap<String, Object> httpHeaders,
               final OutputStream entityStream) throws IOException, WebApplicationException {
         entityStream.write(getHtml(proxy).getBytes());
     }


     private String getHtml(List<TreeNode> proxy) {
        String result;
        result = "<html>" + "<body>" + "\n" ;
        result = result + "<h1>" + getTypeKey() + "</h1>";
            result = result + getAttributes(proxy) + "<br>";
        result = result + "<h2>" + getResourcesKey() + "</h2>";
            result = result + getResourcesLinks(proxy);
        result = result + "</html>" + "</body>";
        return result;
    }


    private String getTypeKey() {
       return upperCaseFirstLetter(eleminateHypen(getName(uriInfo.getPath(), '/')));
    }


    private String getAttributes(List<TreeNode> nodeList) {
        String result ="";
        for (TreeNode node : nodeList) {
            //process only the leaf nodes, if any
            if (!node.hasChildNodes()) {
                result = result + node.getName() + ":&nbsp;&nbsp;&nbsp;&nbsp;" + htmlForNodeValue(node.getValue());
                result = result +  "<br>";
            }
        }

        return result;
    }


    private String getResourcesKey() {
        return quote("resources");
    }


    private String getResourcesLinks(List<TreeNode> nodeList) {
        String result = "";
        String elementName;
        for (TreeNode node: nodeList) { //for each element
            //process only the non-leaf nodes, if any
            if (node.hasChildNodes()) {
                try {
                    elementName = node.getName();
                    result = result + "<a href=" + getElementLink(uriInfo, elementName) + ">";
                    result = result + elementName;
                    result = result + "</a>";
                    result = result +  "<br>";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }


    private String htmlForNodeValue(Object value) {
        String result ="";

        //FIXME
        //check  the value for type Statistics and handle appropriately

        result =  value.toString();
        return result;
    }

}
