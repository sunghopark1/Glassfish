/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.deployment.impl.reflection.annotation;

import com.sun.enterprise.deployment.annotation.*;
import com.sun.enterprise.deployment.annotation.impl.HandlerProcessingResultImpl;
import com.sun.persistence.api.deployment.DeploymentUnit;
import com.sun.persistence.api.deployment.MergeManager;
import com.sun.persistence.spi.deployment.DeploymentUnitContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * This process TableGenerator annotation at package level
 *
 * @author Servesh Singh
 * @version 1.0
 */
public class TableGeneratorHandler extends HandlerBase {

    public TableGeneratorHandler(MergeManager mergeManager) {
        super(mergeManager);
    }

    public Class<? extends Annotation> getAnnotationType() {
        return javax.persistence.TableGenerator.class;
    }

    public HandlerProcessingResult processAnnotation(AnnotationInfo element)
            throws AnnotationProcessorException {
        DeploymentUnit du = element.getProcessingContext().getHandler(
                DeploymentUnitContext.class)
                .getDeploymentUnit();
        AnnotatedElement annotatedElement = element.getAnnotatedElement();
        if (annotatedElement instanceof Package) {
            Package pkg = Package.class.cast(annotatedElement);
            processAnnotation(pkg, du);
        }
        return HandlerProcessingResultImpl.getDefaultResult(
                getAnnotationType(), ResultType.PROCESSED);
    }

    private void processAnnotation(Package pkg, DeploymentUnit du) {
        if (pkg.isAnnotationPresent(javax.persistence.TableGenerator.class)) {
            du.getPersistenceJar().setTableGenerator(AnnotationToDescriptorConverter.convert(pkg.getAnnotation(
                    javax.persistence.TableGenerator.class)));
        }
    }
}