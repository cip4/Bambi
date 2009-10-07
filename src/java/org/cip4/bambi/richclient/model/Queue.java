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

import org.cip4.bambi.richclient.value.QueueEntryVO;
import org.cip4.bambi.richclient.value.QueueVO;

/**
 * Queue Pojo including static builder class.
 * @author smeissner
 * @date 04.10.2009
 */
public class Queue {
	private final String context;
	private final String descriptiveName;
	private final String deviceId;
	private final boolean pull;
	private final boolean refresh;
	private final String status;
	private final List<QueueEntry> queueEntries;

	/**
	 * Builder class to create object.
	 * @author smeissner
	 * @date 25.09.2009
	 */
	public static class Builder {
		private String context;
		private String descriptiveName;
		private String deviceId;
		private boolean pull;
		private boolean refresh;
		private String status;
		private List<QueueEntry> queueEntries;

		/**
		 * Default constructor.
		 */
		public Builder() {
		}

		/**
		 * Custom builder constructor. Accepting a queue view object for initialize.
		 * @param deviceId device id
		 */
		public Builder(QueueVO vo) {
			context = vo.getContext();
			descriptiveName = vo.getDescriptiveName();
			deviceId = vo.getDeviceId();
			pull = vo.isPull();
			refresh = vo.isRefresh();
			status = vo.getStatus();

			// resolve devices list
			if (vo.getQueueEntries() != null) {
				queueEntries = new ArrayList<QueueEntry>(vo.getQueueEntries().size());

				for (QueueEntryVO queueEntryVO : vo.getQueueEntries())
					queueEntries.add(new QueueEntry.Builder(queueEntryVO).build());
			} else {
				queueEntries = new ArrayList<QueueEntry>();
			}
		}

		/**
		 * Custom builder constructor. Accepting a queue view object for initialize.
		 * @param deviceId device id
		 */
		public Builder(Queue queue) {
			context = queue.context;
			descriptiveName = queue.descriptiveName;
			deviceId = queue.deviceId;
			pull = queue.pull;
			refresh = queue.refresh;
			status = queue.status;
			queueEntries = queue.queueEntries;
		}

		// Builder methods
		public Builder context(String val) {
			context = val;
			return this;
		}

		public Builder descriptiveName(String val) {
			descriptiveName = val;
			return this;
		}

		public Builder deviceId(String val) {
			deviceId = val;
			return this;
		}

		public Builder pull(boolean val) {
			pull = val;
			return this;
		}

		public Builder refresh(boolean val) {
			refresh = val;
			return this;
		}

		public Builder status(String val) {
			status = val;
			return this;
		}

		public Builder queueEntries(List<QueueEntry> val) {
			queueEntries = val;
			return this;
		}

		/**
		 * Creates and returns a new device object.
		 * @return device instance
		 */
		public Queue build() {
			return new Queue(this);
		}
	}

	/**
	 * Private custom constructor for initializing device object by builder.
	 * @param builder Builder instance
	 */
	private Queue(Builder builder) {
		context = builder.context;
		descriptiveName = builder.descriptiveName;
		deviceId = builder.deviceId;
		pull = builder.pull;
		refresh = builder.refresh;
		status = builder.status;

		if (builder.queueEntries != null)
			queueEntries = Collections.unmodifiableList(builder.queueEntries);
		else
			queueEntries = null;
	}

	/**
	 * Getter for context attribute.
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Getter for descriptiveName attribute.
	 * @return the descriptiveName
	 */
	public String getDescriptiveName() {
		return descriptiveName;
	}

	/**
	 * Getter for deviceId attribute.
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Getter for pull attribute.
	 * @return the pull
	 */
	public boolean isPull() {
		return pull;
	}

	/**
	 * Getter for refresh attribute.
	 * @return the refresh
	 */
	public boolean isRefresh() {
		return refresh;
	}

	/**
	 * Getter for status attribute.
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Getter for queueEntries attribute.
	 * @return the queueEntries
	 */
	public List<QueueEntry> getQueueEntries() {
		return queueEntries;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Queue of " + deviceId + "; size:" + Integer.toString(queueEntries.size());
	}

}
