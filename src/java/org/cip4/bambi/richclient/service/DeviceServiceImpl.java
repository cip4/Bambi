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
import java.util.Collections;
import java.util.List;

import org.cip4.bambi.richclient.model.Device;

/**
 * Singlton class manages data caching and periodical updating form bambi.
 * @author smeissner
 * @date 25.09.2009
 */
public final class DeviceServiceImpl implements DeviceService, Runnable {

	/**
	 * containing the only instance of class.
	 */
	private static final DeviceServiceImpl INSTANCE = new DeviceServiceImpl();

	/**
	 * caches the devices list.
	 */
	private static final List<Device> DEVICE_CACHE = new ArrayList<Device>();

	private static final int UPDATE_INTERVAL = 1000;

	private Thread cacheUpdaterThread;

	/**
	 * Private default constructor. Initializing component.
	 */
	private DeviceServiceImpl() {
		// initialize cacheUpdater thread
		cacheUpdaterThread.setName("updater");
		cacheUpdaterThread = new Thread(this);
	}

	/**
	 * Returns current singleton instance.
	 * @return DeviceServiceImpl instance
	 */
	static DeviceServiceImpl getInstance() {
		return INSTANCE;
	}

	/**
	 * Updates cache periodically.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (true) {

			try {

				// sleep
				Thread.sleep(UPDATE_INTERVAL);
			} catch (InterruptedException e) {
				// throw error
				throw new AssertionError();
			}

		}
	}

	/**
	 * @see org.cip4.bambi.richclient.service.DeviceService#getDeviceList()
	 */
	public List<Device> getDeviceList() {
		return getDeviceList(0L);
	}

	/**
	 * @see org.cip4.bambi.richclient.service.DeviceService#getDeviceList(long)
	 */
	public List<Device> getDeviceList(long lastUpdate) {

		// return unmodifiable list
		return Collections.unmodifiableList(DEVICE_CACHE);
	}
}
