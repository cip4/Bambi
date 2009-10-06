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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cip4.bambi.richclient.value.DeviceListVO;
import org.cip4.bambi.richclient.value.DeviceVO;

/**
 * DeviceList View Object PoJo, includes all device list attributes and a device-find-by-id method.
 * @author smeissner
 * @date 28.09.2009
 */
public class DeviceList {
	private final String context;
	private final String deviceType;
	private final long memFree;
	private final long memTotal;
	private final int numRequests;
	private final List<Device> devices;

	private final java.util.Hashtable<String, Device> hashDevices;

	/**
	 * Builder class to create object.
	 * @author smeissner
	 * @date 25.09.2009
	 */
	public static class Builder {
		private String context;
		private String deviceType;
		private long memFree;
		private long memTotal;
		private int numRequests;
		private List<Device> devices;

		/**
		 * Default constructor.
		 */
		public Builder() {
		}

		/**
		 * Custom builder constructor. Accepting a device view object for initialize.
		 * @param deviceId device id
		 */
		public Builder(DeviceListVO vo) {
			context = vo.getContext();
			deviceType = vo.getDeviceType();
			memFree = vo.getMemFree();
			memTotal = vo.getMemTotal();
			numRequests = vo.getNumRequests();

			// resolve devices list
			if (vo.getDevices() != null) {
				devices = new ArrayList<Device>(vo.getDevices().size());

				for (DeviceVO deviceVO : vo.getDevices())
					devices.add(new Device.Builder(deviceVO).build());
			} else {
				devices = new ArrayList<Device>();
			}
		}

		// Builder methods
		public Builder context(String val) {
			context = val;
			return this;
		}

		public Builder deviceType(String val) {
			deviceType = val;
			return this;
		}

		public Builder memFree(long val) {
			memFree = val;
			return this;
		}

		public Builder memTotal(long val) {
			memTotal = val;
			return this;
		}

		public Builder numRequests(int val) {
			numRequests = val;
			return this;
		}

		public Builder devices(List<Device> val) {
			devices = val;
			return this;
		}

		/**
		 * Creates and returns a new device object.
		 * @return device instance
		 */
		public DeviceList build() {
			return new DeviceList(this);
		}
	}

	/**
	 * Private custom constructor for initializing device object by builder.
	 * @param builder Builder instance
	 */
	private DeviceList(Builder builder) {
		context = builder.context;
		deviceType = builder.deviceType;
		memFree = builder.memFree;
		memTotal = builder.memTotal;
		numRequests = builder.numRequests;
		devices = Collections.unmodifiableList(builder.devices);

		// initialize hashDevices
		hashDevices = new java.util.Hashtable<String, Device>(devices.size());

		for (Device device : devices) {
			hashDevices.put(device.getId(), device);
		}
	}

	/**
	 * Getter for context attribute.
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Getter for deviceType attribute.
	 * @return the deviceType
	 */
	public String getDeviceType() {
		return deviceType;
	}

	/**
	 * Getter for memFree attribute.
	 * @return the memFree
	 */
	public long getMemFree() {
		return memFree;
	}

	/**
	 * Getter for memTotal attribute.
	 * @return the memTotal
	 */
	public long getMemTotal() {
		return memTotal;
	}

	/**
	 * Getter for numRequests attribute.
	 * @return the numRequests
	 */
	public int getNumRequests() {
		return numRequests;
	}

	/**
	 * Getter for devices attribute.
	 * @return the devices
	 */
	public List<Device> getDevices() {
		return devices;
	}

	/**
	 * Finds device object in list by its primary key (deviceId).
	 * @param deviceId PK of device to find
	 * @return Device
	 */
	public Device findDeviceById(String deviceId) {
		Device result = null;

		if (hashDevices.containsKey(deviceId)) {
			result = hashDevices.get(deviceId);
		}

		return result;
	}
}
