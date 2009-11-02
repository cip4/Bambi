/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2009 The International Cooperation for the Integration of 
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        The International Cooperation for the Integration of 
 *        Processes in  Prepress, Press and Postpress (www.cip4.org)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of 
 *    Processes in  Prepress, Press and Postpress" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4",
 *    nor may "CIP4" appear in their name, without prior written
 *    permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR
 * THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration 
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software 
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi.richclient.model;

import org.cip4.bambi.richclient.value.EmployeeVO;

/**
 * Employee PoJo including static builder class.
 * @author smeissner
 * @date 02.11.2009
 */
public class Employee {
	private final String productId;
	private final String roles;
	private final String descriptiveName;

	/**
	 * Builder class to create object.
	 * @author smeissner
	 * @date 25.09.2009
	 */
	public static class Builder {
		private String productId;
		private String roles;
		private String descriptiveName;

		/**
		 * Default constructor.
		 */
		public Builder() {
		}

		/**
		 * Custom builder constructor. Accepting a device view object for initialize.
		 * @param deviceId device id
		 */
		public Builder(EmployeeVO vo) {
			productId = vo.getProductId();
			roles = vo.getRoles();
			descriptiveName = vo.getDescriptiveName();
		}

		// Builder methods
		public Builder productId(String val) {
			productId = val;
			return this;
		}

		public Builder roles(String val) {
			roles = val;
			return this;
		}

		public Builder descriptiveName(String val) {
			descriptiveName = val;
			return this;
		}

		/**
		 * Creates and returns a new device object.
		 * @return device instance
		 */
		public Employee build() {
			return new Employee(this);
		}
	}

	/**
	 * Private custom constructor for initializing employee object by builder.
	 * @param builder Builder instance
	 */
	private Employee(Builder builder) {
		productId = builder.productId;
		roles = builder.roles;
		descriptiveName = builder.descriptiveName;
	}

	/**
	 * Getter for productId attribute.
	 * @return the productId
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * Getter for roles attribute.
	 * @return the roles
	 */
	public String getRoles() {
		return roles;
	}

	/**
	 * Getter for descriptiveName attribute.
	 * @return the descriptiveName
	 */
	public String getDescriptiveName() {
		return descriptiveName;
	}
}
