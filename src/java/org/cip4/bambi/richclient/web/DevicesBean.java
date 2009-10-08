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
package org.cip4.bambi.richclient.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ajax4jsf.event.PushEventListener;
import org.cip4.bambi.richclient.data.DevicesContext;
import org.cip4.bambi.richclient.data.DevicesContextFactory;
import org.cip4.bambi.richclient.model.Device;
import org.cip4.bambi.richclient.model.DeviceList;
import org.cip4.bambi.richclient.model.QueueEntry;

/**
 * Backing bean for devices functionality. This bean is Application scoped and just exists once. Thats important for performance!
 * @author smeissner
 * @date 23.09.2009
 */
/**
 * TODO Please insert comment!
 * @author smeissner
 * @date 08.10.2009
 */
/**
 * TODO Please insert comment!
 * @author smeissner
 * @date 08.10.2009
 */
public class DevicesBean implements Runnable {

	/**
	 * expiration time in milliseconds.
	 */
	private final static long EXPIRATION_TIME = 2000;

	/**
	 * contains session uuid for current instance
	 */
	private final String sessionUuid;

	private long nextExpiration;

	private final DevicesContext devicesContext;

	private DeviceList deviceList;

	private Device selectedDevice;

	PushEventListener listener;

	private String selectedDeviceId;

	private final Thread thread;

	private List<QueueEntry> selectedDeviceQueue;

	private Hashtable<String, Integer> hashSelectedDeviceQueue;

	private Set<Integer> selectedDeviceQueueChanges;

	/**
	 * Default constructor. Initialize bean.
	 */
	public DevicesBean() {
		devicesContext = DevicesContextFactory.getInstance();

		// generate session id
		sessionUuid = UUID.randomUUID().toString();

		// initialize pushing
		thread = new Thread(this);
		thread.setName("DeviceThread");
		thread.setDaemon(false);
		thread.start();
	}

	/**
	 * Returns number of devices in list.
	 * @return devices count
	 */
	public int getDevicesCount() {
		int result = 0;

		if (deviceList != null) {
			result = deviceList.getDevices().size();
		}

		return result;
	}

	/**
	 * Returns list of devices.
	 * @return devices list.
	 */
	public List<Device> getDeviceList() {
		// cache device list till expiration
		if (deviceList == null || isExpired()) {
			deviceList = devicesContext.getDeviceList();
		}

		// return device list
		return deviceList.getDevices();
	}

	/**
	 * Returns total number of requests.
	 * @return Number of requests
	 */
	public int getNumRequests() {
		return deviceList.getNumRequests();
	}

	/**
	 * Setter for selectedDevice attribute.
	 * @param selectedDevice the selectedDevice to set
	 */
	public void setSelectedDeviceId(String selectedDeviceId) {
		// set id
		this.selectedDeviceId = selectedDeviceId;

		// reset queue storage
		hashSelectedDeviceQueue = new Hashtable<String, Integer>();
		selectedDeviceQueue = new ArrayList<QueueEntry>();

		// load queue
		Device device = devicesContext.getDevice(selectedDeviceId, sessionUuid);

		for (QueueEntry entry : device.getQueue().getQueueEntries()) {
			put2SelectedDeviceQueue(entry);
		}
	}

	/**
	 * Getter for selectedDevice attribute.
	 * @return the selectedDevice
	 */
	public String getSelectedDeviceId() {
		return selectedDeviceId;
	}

	/**
	 * Returns selected device object.
	 * @return selected device object.
	 */
	public Device getSelectedDevice() {
		// cache selected device list till expiration
		if (selectedDeviceId == null) {
			selectedDevice = new Device.Builder("0").build();
		} else {
			selectedDevice = devicesContext.getDevice(selectedDeviceId, sessionUuid);
		}

		// return selected device
		return selectedDevice;
	}

	/**
	 * Returns sort priority.
	 * @return collection of sortpriority
	 */
	public Collection<String> getQueueEntrySortPriority() {
		List<String> lst = new ArrayList<String>(1);
		lst.add("columnSubmission");
		return lst;
	}

	/**
	 * Updates queue of selected device.
	 * @return null (no action)
	 */
	public String updateQueue() {

		if (selectedDeviceId == null) {
			return null;
		}

		// reset selectedDeviceQueueChanges
		selectedDeviceQueueChanges = new HashSet<Integer>();

		// get queue changes
		Device device = devicesContext.getDeviceDiff(selectedDeviceId, sessionUuid);

		// iterate over all QueueEntry objects
		for (QueueEntry entry : device.getQueue().getQueueEntries()) {
			// update selectedDeviceQueue list
			Integer pos = put2SelectedDeviceQueue(entry);

			// memory position
			selectedDeviceQueueChanges.add(pos);
		}

		// no action
		return null;
	}

	/**
	 * Inserts or updates a queueEntry in selectedDeviceQueue
	 * @param entry QueueEntry to insert / update
	 * @return position of QueueEntry in List
	 */
	private Integer put2SelectedDeviceQueue(QueueEntry entry) {
		// position
		Integer pos;

		if (hashSelectedDeviceQueue.containsKey(entry.getQueueEntryId())) {
			// update
			pos = hashSelectedDeviceQueue.get(entry.getQueueEntryId());
			selectedDeviceQueue.set(pos, entry);
		} else {
			// add
			pos = selectedDeviceQueue.size();
			hashSelectedDeviceQueue.put(entry.getQueueEntryId(), pos);
			selectedDeviceQueue.add(entry);
		}

		// return position
		return pos;
	}

	/**
	 * Returns selected device queue.
	 * @return selected device queue.
	 */
	public List<QueueEntry> getSelectedDeviceQueue() {
		return selectedDeviceQueue;
	}

	/**
	 * Returns selected keys of device queue changes.
	 * @return selected keys of device queue changes.
	 */
	public Set<Integer> getSelectedDeviceQueueChanges() {
		return selectedDeviceQueueChanges;
	}

	/**
	 * Method to resume queue.
	 * @return null (no action)
	 */
	public String queueResume() {
		// hold queue
		devicesContext.queueResume(selectedDeviceId);

		// no navigation action
		return null;
	}

	/**
	 * Method to hold on queue.
	 * @return null (no action)
	 */
	public String queueHold() {
		// hold queue
		devicesContext.queueHold(selectedDeviceId);

		// no navigation action
		return null;
	}

	/**
	 * Method to open queue
	 * @return null (no action)
	 */
	public String queueOpen() {
		// close queue
		devicesContext.queueOpen(selectedDeviceId);

		// no navigation action
		return null;
	}

	/**
	 * Method to close queue
	 * @return null (no action)
	 */
	public String queueClose() {
		// close queue
		devicesContext.queueClose(selectedDeviceId);

		// no navigation action
		return null;
	}

	/**
	 * Method to flush queue
	 * @return null (no action)
	 */
	public String queueFlush() {
		// flush queue
		devicesContext.queueFlush(selectedDeviceId);

		// no navigation action
		return null;
	}

	/**
	 * Add Listener for pushing.
	 * @param listener
	 */
	public void addListener(EventListener listener) {
		synchronized (listener) {
			if (this.listener != listener) {
				this.listener = (PushEventListener) listener;
			}
		}
	}

	/**
	 * Runnable implementation for supporting threading.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (thread != null) {
			try {
				// new time string object
				if (listener != null)
					listener.onEvent(new EventObject(this));
				Thread.sleep(2000);

			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * Helper method manages expiration time.
	 * @return
	 */
	private boolean isExpired() {
		boolean result = true;

		// now as long
		long now = Calendar.getInstance().getTimeInMillis();

		if (now < nextExpiration) {
			// no expiration
			result = false;
		} else {
			// set new expiration time
			nextExpiration = now + EXPIRATION_TIME;
		}

		return result;
	}
}