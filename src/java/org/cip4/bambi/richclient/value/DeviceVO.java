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
 * Device value Object PoJo, includes all device attributes.
 * @author smeissner
 * @date 30.09.2009
 */
public class DeviceVO {
	private String context;
	private String id;
	private String status;
	private String type;
	private String url;
	private int numRequests;
	private boolean root;
	private boolean modify;
	private String errorFolder;
	private String inputFolder;
	private String outputFolder;
	private int queueWaiting;
	private int queueCompleted;
	private int queueRunning;
	private String queueStatus;
	private List<MsgSubscriptionVO> msgSubscriptions;

	/**
	 * Default constructor.
	 */
	public DeviceVO() {

	}

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
	 * Getter for id attribute.
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Setter for id attribute.
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * Getter for type attribute.
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter for type attribute.
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter for url attribute.
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Setter for url attribute.
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
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
	 * Getter for root attribute.
	 * @return the root
	 */
	public boolean isRoot() {
		return root;
	}

	/**
	 * Setter for root attribute.
	 * @param root the root to set
	 */
	public void setRoot(boolean root) {
		this.root = root;
	}

	/**
	 * Getter for modify attribute.
	 * @return the modify
	 */
	public boolean isModify() {
		return modify;
	}

	/**
	 * Setter for modify attribute.
	 * @param modify the modify to set
	 */
	public void setModify(boolean modify) {
		this.modify = modify;
	}

	/**
	 * Getter for errorFolder attribute.
	 * @return the errorFolder
	 */
	public String getErrorFolder() {
		return errorFolder;
	}

	/**
	 * Setter for errorFolder attribute.
	 * @param errorFolder the errorFolder to set
	 */
	public void setErrorFolder(String errorFolder) {
		this.errorFolder = errorFolder;
	}

	/**
	 * Getter for inputFolder attribute.
	 * @return the inputFolder
	 */
	public String getInputFolder() {
		return inputFolder;
	}

	/**
	 * Setter for inputFolder attribute.
	 * @param inputFolder the inputFolder to set
	 */
	public void setInputFolder(String inputFolder) {
		this.inputFolder = inputFolder;
	}

	/**
	 * Getter for outputFolder attribute.
	 * @return the outputFolder
	 */
	public String getOutputFolder() {
		return outputFolder;
	}

	/**
	 * Setter for outputFolder attribute.
	 * @param outputFolder the outputFolder to set
	 */
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	/**
	 * Getter for queueWaiting attribute.
	 * @return the queueWaiting
	 */
	public int getQueueWaiting() {
		return queueWaiting;
	}

	/**
	 * Setter for queueWaiting attribute.
	 * @param queueWaiting the queueWaiting to set
	 */
	public void setQueueWaiting(int queueWaiting) {
		this.queueWaiting = queueWaiting;
	}

	/**
	 * Getter for queueCompleted attribute.
	 * @return the queueCompleted
	 */
	public int getQueueCompleted() {
		return queueCompleted;
	}

	/**
	 * Setter for queueCompleted attribute.
	 * @param queueCompleted the queueCompleted to set
	 */
	public void setQueueCompleted(int queueCompleted) {
		this.queueCompleted = queueCompleted;
	}

	/**
	 * Getter for queueRunning attribute.
	 * @return the queueRunning
	 */
	public int getQueueRunning() {
		return queueRunning;
	}

	/**
	 * Setter for queueRunning attribute.
	 * @param queueRunning the queueRunning to set
	 */
	public void setQueueRunning(int queueRunning) {
		this.queueRunning = queueRunning;
	}

	/**
	 * Getter for queueStatus attribute.
	 * @return the queueStatus
	 */
	public String getQueueStatus() {
		return queueStatus;
	}

	/**
	 * Setter for queueStatus attribute.
	 * @param queueStatus the queueStatus to set
	 */
	public void setQueueStatus(String queueStatus) {
		this.queueStatus = queueStatus;
	}

	/**
	 * Setter for msgSubscriptions attribute.
	 * @param msgSubscriptions the msgSubscriptions to set
	 */
	public void setMsgSubscriptions(List<MsgSubscriptionVO> msgSubscriptions) {
		this.msgSubscriptions = msgSubscriptions;
	}

	/**
	 * Getter for msgSubscriptions attribute.
	 * @return the msgSubscriptions
	 */
	public List<MsgSubscriptionVO> getMsgSubscriptions() {
		return msgSubscriptions;
	}
}
