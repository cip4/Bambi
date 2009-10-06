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

import org.cip4.bambi.richclient.value.DeviceVO;

/**
 * Hotfolder Pojo including static builder class.
 * @author smeissner
 * @date 25.09.2009
 */
public class Hotfolder {

	private final String errorFolder;

	private final String inputFolder;

	private final String outputFolder;

	/**
	 * Builder class to create object.
	 * @author smeissner
	 * @date 25.09.2009
	 */
	public static class Builder {
		private String errorFolder = "";
		private String inputFolder = "";
		private String outputFolder = "";

		/**
		 * Default builder constructor.
		 */
		public Builder() {
		}
		
		/**
		 * Custom builder constructor. 
		 * Accepting a device view object for initialize.
		 * @param deviceId device id
		 */
		public Builder(DeviceVO vo) {
			errorFolder = vo.getErrorFolder();
			inputFolder = vo.getInputFolder();
			outputFolder = vo.getOutputFolder();
		}

		// Builder methods
		public Builder errorFolder(String val) {
			errorFolder = val;
			return this;
		}

		public Builder inputFolder(String val) {
			inputFolder = val;
			return this;
		}

		public Builder outputFolder(String val) {
			outputFolder = val;
			return this;
		}

		/**
		 * Creates and returns a new hotfolder object.
		 * @return hotfolder instance
		 */
		public Hotfolder build() {
			return new Hotfolder(this);
		}
	}

	/**
	 * Private custom constructor for initializing hotfolder object by builder.
	 * @param builder Builder instance
	 */
	private Hotfolder(Builder builder) {
		errorFolder = builder.errorFolder;
		inputFolder = builder.inputFolder;
		outputFolder = builder.outputFolder;
	}

	/**
	 * Getter for errorFolder attribute.
	 * @return the errorFolder
	 */
	public String getErrorFolder() {
		return errorFolder;
	}

	/**
	 * Getter for inputFolder attribute.
	 * @return the inputFolder
	 */
	public String getInputFolder() {
		return inputFolder;
	}

	/**
	 * Getter for outputFolder attribute.
	 * @return the outputFolder
	 */
	public String getOutputFolder() {
		return outputFolder;
	}

}
