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

import java.util.Date;

/**
 * QueueEntry value Object PoJo, includes all device attributes.
 * @author smeissner
 * @date 03.10.2009
 */
public class QueueEntryVO {
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
	 * Getter for descriptiveName attribute.
	 * @return the descriptiveName
	 */
	public String getDescriptiveName() {
		return descriptiveName;
	}

	/**
	 * Setter for descriptiveName attribute.
	 * @param descriptiveName the descriptiveName to set
	 */
	public void setDescriptiveName(String descriptiveName) {
		this.descriptiveName = descriptiveName;
	}

	/**
	 * Getter for jobId attribute.
	 * @return the jobId
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * Setter for jobId attribute.
	 * @param jobId the jobId to set
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * Getter for jobPartId attribute.
	 * @return the jobPartId
	 */
	public String getJobPartId() {
		return jobPartId;
	}

	/**
	 * Setter for jobPartId attribute.
	 * @param jobPartId the jobPartId to set
	 */
	public void setJobPartId(String jobPartId) {
		this.jobPartId = jobPartId;
	}

	/**
	 * Getter for priority attribute.
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Setter for priority attribute.
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Getter for queueEntryId attribute.
	 * @return the queueEntryId
	 */
	public String getQueueEntryId() {
		return queueEntryId;
	}

	/**
	 * Setter for queueEntryId attribute.
	 * @param queueEntryId the queueEntryId to set
	 */
	public void setQueueEntryId(String queueEntryId) {
		this.queueEntryId = queueEntryId;
	}

	/**
	 * Getter for startTime attribute.
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Setter for startTime attribute.
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * Setter for endTime attribute.
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
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
	 * Setter for status attribute.
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Getter for submissionTime attribute.
	 * @return the submissionTime
	 */
	public Date getSubmissionTime() {
		return submissionTime;
	}

	/**
	 * Setter for submissionTime attribute.
	 * @param submissionTime the submissionTime to set
	 */
	public void setSubmissionTime(Date submissionTime) {
		this.submissionTime = submissionTime;
	}

}
