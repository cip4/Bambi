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
package org.cip4.bambi.richclient.value;

import java.util.List;

/**
 * Device value object PoJo, includes all device attributes.
 * @author smeissner
 * @date 30.09.2009
 */
public class DeviceListVO {
	private String context;
	private String deviceType;
	private long memFree;
	private long memTotal;
	private int numRequests;
	private List<DeviceVO> devices;

	/**
	 * Getter for context attribute.
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Setter for context attribute.
	 * @param context the context to set
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * Getter for deviceType attribute.
	 * @return the deviceType
	 */
	public String getDeviceType() {
		return deviceType;
	}

	/**
	 * Setter for deviceType attribute.
	 * @param deviceType the deviceType to set
	 */
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * Getter for memFree attribute.
	 * @return the memFree
	 */
	public long getMemFree() {
		return memFree;
	}

	/**
	 * Setter for memFree attribute.
	 * @param memFree the memFree to set
	 */
	public void setMemFree(long memFree) {
		this.memFree = memFree;
	}

	/**
	 * Getter for memTotal attribute.
	 * @return the memTotal
	 */
	public long getMemTotal() {
		return memTotal;
	}

	/**
	 * Setter for memTotal attribute.
	 * @param memTotal the memTotal to set
	 */
	public void setMemTotal(long memTotal) {
		this.memTotal = memTotal;
	}

	/**
	 * Getter for numRequests attribute.
	 * @return the numRequests
	 */
	public int getNumRequests() {
		return numRequests;
	}

	/**
	 * Setter for numRequests attribute.
	 * @param numRequests the numRequests to set
	 */
	public void setNumRequests(int numRequests) {
		this.numRequests = numRequests;
	}

	/**
	 * Getter for devices attribute.
	 * @return the devices
	 */
	public List<DeviceVO> getDevices() {
		return devices;
	}

	/**
	 * Setter for devices attribute.
	 * @param devices the devices to set
	 */
	public void setDevices(List<DeviceVO> devices) {
		this.devices = devices;
	}

}
