// $Id: MinValidatorForString.java 16748 2009-06-10 18:41:22Z epbernard $
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
package org.hibernate.validation.constraints.impl;

import java.math.BigDecimal;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Min;

/**
 * Check that the String being validated represents a number, and has a value
 * more than or equal to the minimum value specified.
 *
 * @author Alaa Nassef
 */
public class MinValidatorForString implements ConstraintValidator<Min, String> {

	private long minValue;

	public void initialize(Min minValue) {
		this.minValue = minValue.value();
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( value == null ) {
			return true;
		}
		try {
			return new BigDecimal( ( String ) value ).compareTo( BigDecimal.valueOf( minValue ) ) != -1;
		}
		catch ( NumberFormatException nfe ) {
			return false;
		}
	}
}
