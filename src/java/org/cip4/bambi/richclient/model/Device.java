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

import org.cip4.bambi.richclient.value.DeviceVO;
import org.cip4.bambi.richclient.value.EmployeeVO;
import org.cip4.bambi.richclient.value.MsgSubscriptionVO;

/**
 * Device Pojo including static builder class.
 * @author smeissner
 * @date 23.09.2009
 */
public class Device {

	private final String context;
	private final String id;
	private final String status;
	private final String type;
	private final String url;
	private final int numRequests;
	private final boolean root;
	private final boolean modify;
	private final Hotfolder hotfolder;
	private final JobStatistic jobStatistic;
	private final Queue queue;
	private final String typeExpression;
	private final String watchUrl;
	private final List<MsgSubscription> msgSubscriptions;
	private final List<Employee> employees;
	private final List<Employee> knownEmployees;

	/**
	 * Builder class to create object.
	 * @author smeissner
	 * @date 25.09.2009
	 */
	public static class Builder {
		// required parameters
		private final String id;

		// optional parameters - initialized to default values
		private String context = "";
		private String status = "";
		private String type = "";
		private String url = "";
		private int numRequests = 0;
		private boolean root = false;
		private boolean modify = false;
		private Hotfolder hotfolder = null;
		private JobStatistic jobStatistic = null;
		private Queue queue = null;
		private String typeExpression;
		private String watchUrl;
		private List<MsgSubscription> msgSubscriptions = null;
		private List<Employee> employees = null;
		private List<Employee> knownEmployees = null;

		/**
		 * Custom builder constructor. Accepting a deviceId for initialize.
		 * @param deviceId device id
		 */
		public Builder(String deviceId) {
			id = deviceId;
		}

		/**
		 * Custom builder constructor. Accepting a device view object for initialize.
		 * @param deviceVO device view object
		 */
		public Builder(DeviceVO vo) {
			id = vo.getId();
			context = vo.getContext();
			status = vo.getStatus();
			type = vo.getType();
			url = vo.getUrl();
			numRequests = vo.getNumRequests();
			root = vo.isRoot();
			modify = vo.isModify();
			hotfolder = new Hotfolder.Builder(vo).build();
			jobStatistic = new JobStatistic.Builder(vo).build();
			queue = null;
			typeExpression = vo.getTypeExpression();
			watchUrl = vo.getWatchUrl();

			// resolve subscriptions
			if (vo.getMsgSubscriptions() != null) {
				msgSubscriptions = new ArrayList<MsgSubscription>(vo.getMsgSubscriptions().size());

				for (MsgSubscriptionVO msgSubscriptionVO : vo.getMsgSubscriptions())
					msgSubscriptions.add(new MsgSubscription.Builder(msgSubscriptionVO).build());
			} else {
				msgSubscriptions = new ArrayList<MsgSubscription>();
			}

			// employees
			if (vo.getEmployees() != null) {
				employees = new ArrayList<Employee>(vo.getEmployees().size());

				for (EmployeeVO employeeVO : vo.getEmployees())
					employees.add(new Employee.Builder(employeeVO).build());
			} else {
				employees = new ArrayList<Employee>();
			}

			// knownEmployees
			if (vo.getKnownEmployees() != null) {
				knownEmployees = new ArrayList<Employee>(vo.getKnownEmployees().size());

				for (EmployeeVO employeeVO : vo.getKnownEmployees())
					knownEmployees.add(new Employee.Builder(employeeVO).build());
			} else {
				knownEmployees = new ArrayList<Employee>();
			}
		}

		/**
		 * Custom builder constructor. Accepting a device object for initialize.
		 * @param device
		 */
		public Builder(Device device) {
			id = device.getId();
			context = device.getContext();
			status = device.getStatus();
			type = device.getType();
			url = device.getUrl();
			numRequests = device.getNumRequests();
			root = device.isRoot();
			modify = device.isModify();
			hotfolder = device.getHotfolder();
			jobStatistic = device.getJobStatistic();
			queue = device.queue;
			typeExpression = device.typeExpression;
			watchUrl = device.watchUrl;
			employees = device.getEmployees();
			knownEmployees = device.getKnownEmployees();
			msgSubscriptions = device.getMsgSubscriptions();
		}

		// Builder methods
		public Builder context(String val) {
			context = val;
			return this;
		}

		public Builder status(String val) {
			status = val;
			return this;
		}

		public Builder type(String val) {
			type = val;
			return this;
		}

		public Builder url(String val) {
			url = val;
			return this;
		}

		public Builder numRequests(int val) {
			numRequests = val;
			return this;
		}

		public Builder root(boolean val) {
			root = val;
			return this;
		}

		public Builder hotfolder(Hotfolder val) {
			hotfolder = val;
			return this;
		}

		public Builder jobStatistic(JobStatistic val) {
			jobStatistic = val;
			return this;
		}

		public Builder queue(Queue val) {
			queue = val;
			return this;
		}

		public Builder typeExpression(String val) {
			typeExpression = val;
			return this;
		}

		public Builder watchUrl(String val) {
			watchUrl = val;
			return this;
		}

		/**
		 * Creates and returns a new device object.
		 * @return device instance
		 */
		public Device build() {
			return new Device(this);
		}
	}

	/**
	 * Private custom constructor for initializing device object by builder.
	 * @param builder Builder instance
	 */
	private Device(Builder builder) {
		id = builder.id;
		context = builder.context;
		status = builder.status;
		type = builder.type;
		url = builder.url;
		numRequests = builder.numRequests;
		root = builder.root;
		modify = builder.modify;
		hotfolder = builder.hotfolder;
		jobStatistic = builder.jobStatistic;
		queue = builder.queue;
		typeExpression = builder.typeExpression;
		watchUrl = builder.watchUrl;
		msgSubscriptions = Collections.unmodifiableList(builder.msgSubscriptions);
		employees = Collections.unmodifiableList(builder.employees);
		knownEmployees = Collections.unmodifiableList(builder.knownEmployees);
	}

	/**
	 * Getter for context attribute.
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Getter for id attribute.
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Getter for status attribute.
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Getter for type attribute.
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Getter for url attribute.
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Getter for numRequests attribute.
	 * @return the numRequests
	 */
	public int getNumRequests() {
		return numRequests;
	}

	/**
	 * Getter for root attribute.
	 * @return the root
	 */
	public boolean isRoot() {
		return root;
	}

	/**
	 * Getter for modify attribute.
	 * @return the modify
	 */
	public boolean isModify() {
		return modify;
	}

	/**
	 * Getter for hotfolder attribute.
	 * @return the hotfolder
	 */
	public Hotfolder getHotfolder() {
		return hotfolder;
	}

	/**
	 * Getter for queue attribute.
	 * @return the queue
	 */
	public JobStatistic getJobStatistic() {
		return jobStatistic;
	}

	/**
	 * Getter for queue attribute.
	 * @return the queue
	 */
	public Queue getQueue() {
		return queue;
	}

	/**
	 * Getter for typeExpression attribute.
	 * @return the typeExpression
	 */
	public String getTypeExpression() {
		return typeExpression;
	}

	/**
	 * Getter for watchUrl attribute.
	 * @return the watchUrl
	 */
	public String getWatchUrl() {
		return watchUrl;
	}

	/**
	 * Getter for msgSubscriptions attribute.
	 * @return the msgSubscriptions
	 */
	public List<MsgSubscription> getMsgSubscriptions() {
		return msgSubscriptions;
	}

	/**
	 * Getter for employees attribute.
	 * @return the employees
	 */
	public List<Employee> getEmployees() {
		return employees;
	}

	/**
	 * Getter for knownEmployees attribute.
	 * @return the knownEmployees
	 */
	public List<Employee> getKnownEmployees() {
		return knownEmployees;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (queue.getQueueEntries() != null) {
			return "Device: " + id + "; queue size:" + Integer.toString(queue.getQueueEntries().size());
		} else {
			return "Device: " + id + "; queue size: 0";
		}
	}
}
