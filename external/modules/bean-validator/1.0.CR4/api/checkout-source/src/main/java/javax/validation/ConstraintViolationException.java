// $Id: ConstraintViolationException.java 16808 2009-06-16 20:01:43Z hardy.ferentschik $
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
package javax.validation;

import java.util.Set;

/**
 * Report the result of constraint violations
 *                                                    `
 * @author Emmanuel Bernard
 */
public class ConstraintViolationException extends ValidationException {
	private final Set<ConstraintViolation<?>> constraintViolations;

	/**
	 * Creates a constraint violation report
	 *
	 * @param message error message
	 * @param constraintViolations Set of ConstraintViolation
	 */
	public <T> ConstraintViolationException(String message,
										Set<ConstraintViolation<?>> constraintViolations) {
		super( message );
		this.constraintViolations = constraintViolations;
	}

	/**
	 * Creates a constraint violation report
	 *
	 * @param constraintViolations Set of ConstraintViolation
	 */
	public <T> ConstraintViolationException(Set<ConstraintViolation<?>> constraintViolations) {
		super();
		this.constraintViolations = constraintViolations;
	}

	/**
	 * Set of constraint violations reported during a validation
	 *
	 * @return Set of CosntraintViolation
	 */
	public Set<ConstraintViolation<?>> getConstraintViolations() {
		return constraintViolations;
	}
}
