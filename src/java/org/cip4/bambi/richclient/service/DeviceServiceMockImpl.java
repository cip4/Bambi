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
package org.cip4.bambi.richclient.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cip4.bambi.richclient.model.Device;
import org.cip4.bambi.richclient.model.Queue;

/**
 * Just a mock implementation to simplify frontend development.
 * @author smeissner
 * @date 23.09.2009
 */
class DeviceServiceMockImpl implements DeviceService {

	/**
	 * Length of devices dummy list
	 */
	private static final int DEVICES_LIST_LENGTH = 25;

	/**
	 * Every <code>DEVICES_IS_ROOT</code>th device is root
	 */
	private static final int DEVICES_IS_ROOT = 5;

	private static int instanceCounter = 0;

	/**
	 * Creates a dummy device list.
	 * @see org.cip4.bambi.richclient.service.DeviceService#getDeviceList()
	 */
	public List<Device> getDeviceList() {

		ArrayList<Device> lst = new ArrayList<Device>(DEVICES_LIST_LENGTH);

		for (int i = 0; i < DEVICES_LIST_LENGTH; i++) {
			lst.add(newDevice());
		}

		return lst;
	}

	/**
	 * Generates a new test device object.
	 * @return test Device object.
	 */
	private Device newDevice() {

		int id = (instanceCounter % DEVICES_LIST_LENGTH);
		boolean isRoot = instanceCounter % DEVICES_IS_ROOT == 0;

		int rootNo = (id / DEVICES_IS_ROOT);
		int deviceNo = id - rootNo - 1;

		// set id (from 1 to DEVICES_LIST_LENGTH)
		Device.Builder b = new Device.Builder(Integer.toString(id + 1));

		// set static attributes (name / url ....)
		if (isRoot) {
			b.type("Root " + Integer.toString(rootNo + 1));
			b.url("http://127.0.1.1:8080/richworker/jmf/sim");
		} else {
			b.type("Device " + Integer.toString(deviceNo + 1));
		}

		// count by random
		Queue queue = new Queue.Builder().waiting(randomInteger(1000)).running(randomInteger(2)).completed(randomInteger(1000)).build();
		b.queue(queue);

		// status by random (1 - 3)
		b.status(Integer.toString(randomInteger(3) + 1));

		// every DEVICES_IS_ROOT device is root
		b.root(isRoot);

		// increment counter
		instanceCounter++;

		return b.build();
	}

	/**
	 * Creates a random integer between 0 (included) and max integer (excluded).
	 * @param max upper limit of random integer
	 * @return random integer
	 */
	private int randomInteger(int max) {
		return new Random().nextInt(max);
	}

	/**
	 * Not supported. Returns the whole list.
	 * @see org.cip4.bambi.richclient.service.DeviceService#getDeviceList(long)
	 */
	public List<Device> getDeviceList(long lastUpdate) {
		return getDeviceList();
	}
}