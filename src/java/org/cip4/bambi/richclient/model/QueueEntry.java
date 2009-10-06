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

import java.util.Date;

import org.cip4.bambi.richclient.value.QueueEntryVO;

/**
 * QueueEntry Pojo including static builder class.
 * @author smeissner
 * @date 04.10.2009
 */
public class QueueEntry {
	private final String descriptiveName;
	private final String jobId;
	private final String jobPartId;
	private final int priority;
	private final String queueEntryId;
	private final Date startTime;
	private final Date endTime;
	private final String status;
	private final Date submissionTime;

	/**
	 * Builder class to create object.
	 * @author smeissner
	 * @date 25.09.2009
	 */
	public static class Builder {
		private String descriptiveName;
		private String jobId;
		private String jobPartId;
		private int priority;
		private String queueEntryId;
		private Date startTime;
		private Date endTime;
		private String status;
		private Date submissionTime;

		/**
		 * Default constructor.
		 */
		public Builder() {
		}

		/**
		 * Custom builder constructor. Accepting a queue entry view object for initialize.
		 * @param deviceId device id
		 */
		public Builder(QueueEntryVO vo) {
			descriptiveName = vo.getDescriptiveName();
			jobId = vo.getJobId();
			jobPartId = vo.getJobPartId();
			priority = vo.getPriority();
			queueEntryId = vo.getQueueEntryId();
			startTime = vo.getStartTime();
			endTime = vo.getEndTime();
			status = vo.getStatus();
			submissionTime = vo.getSubmissionTime();
		}

		// Builder methods
		public Builder descriptiveName(String val) {
			descriptiveName = val;
			return this;
		}

		public Builder jobId(String val) {
			jobId = val;
			return this;
		}

		public Builder jobPartId(String val) {
			jobPartId = val;
			return this;
		}

		public Builder priority(int val) {
			priority = val;
			return this;
		}

		public Builder queueEntryId(String val) {
			queueEntryId = val;
			return this;
		}

		public Builder startTime(Date val) {
			startTime = val;
			return this;
		}

		public Builder endTime(Date val) {
			endTime = val;
			return this;
		}

		public Builder status(String val) {
			status = val;
			return this;
		}

		public Builder submissionTime(Date val) {
			submissionTime = val;
			return this;
		}

		/**
		 * Creates and returns a new device object.
		 * @return device instance
		 */
		public QueueEntry build() {
			return new QueueEntry(this);
		}
	}

	/**
	 * Private custom constructor for initializing device object by builder.
	 * @param builder Builder instance
	 */
	private QueueEntry(Builder builder) {
		descriptiveName = builder.descriptiveName;
		jobId = builder.jobId;
		jobPartId = builder.jobPartId;
		priority = builder.priority;
		queueEntryId = builder.queueEntryId;
		startTime = builder.startTime;
		endTime = builder.endTime;
		status = builder.status;
		submissionTime = builder.submissionTime;
	}

	/**
	 * Getter for descriptiveName attribute.
	 * @return the descriptiveName
	 */
	public String getDescriptiveName() {
		return descriptiveName;
	}

	/**
	 * Getter for jobId attribute.
	 * @return the jobId
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * Getter for jobPartId attribute.
	 * @return the jobPartId
	 */
	public String getJobPartId() {
		return jobPartId;
	}

	/**
	 * Getter for priority attribute.
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Getter for queueEntryId attribute.
	 * @return the queueEntryId
	 */
	public String getQueueEntryId() {
		return queueEntryId;
	}

	/**
	 * Getter for startTime attribute.
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Getter for endTime attribute.
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * Getter for status attribute.
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Getter for submissionTime attribute.
	 * @return the submissionTime
	 */
	public Date getSubmissionTime() {
		return submissionTime;
	}
}
