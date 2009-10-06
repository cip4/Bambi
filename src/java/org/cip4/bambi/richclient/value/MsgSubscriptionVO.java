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

/**
 * MsgSubscription value object PoJo.
 * @author smeissner
 * @date 06.10.2009
 */
public class MsgSubscriptionVO {
	private String channelId;
	private String family;
	private String lastTime;
	private String messageType;
	private String queueEntryId;
	private int repeatStep;
	private int repeatTime;
	private String senderId;
	private int sent;
	private String type;
	private String url;

	/**
	 * Getter for channelId attribute.
	 * @return the channelId
	 */
	public String getChannelId() {
		return channelId;
	}

	/**
	 * Setter for channelId attribute.
	 * @param channelId the channelId to set
	 */
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	/**
	 * Getter for family attribute.
	 * @return the family
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * Setter for family attribute.
	 * @param family the family to set
	 */
	public void setFamily(String family) {
		this.family = family;
	}

	/**
	 * Getter for lastTime attribute.
	 * @return the lastTime
	 */
	public String getLastTime() {
		return lastTime;
	}

	/**
	 * Setter for lastTime attribute.
	 * @param lastTime the lastTime to set
	 */
	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}

	/**
	 * Getter for messageType attribute.
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * Setter for messageType attribute.
	 * @param messageType the messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
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
	 * Getter for repeatStep attribute.
	 * @return the repeatStep
	 */
	public int getRepeatStep() {
		return repeatStep;
	}

	/**
	 * Setter for repeatStep attribute.
	 * @param repeatStep the repeatStep to set
	 */
	public void setRepeatStep(int repeatStep) {
		this.repeatStep = repeatStep;
	}

	/**
	 * Getter for repeatTime attribute.
	 * @return the repeatTime
	 */
	public int getRepeatTime() {
		return repeatTime;
	}

	/**
	 * Setter for repeatTime attribute.
	 * @param repeatTime the repeatTime to set
	 */
	public void setRepeatTime(int repeatTime) {
		this.repeatTime = repeatTime;
	}

	/**
	 * Getter for senderId attribute.
	 * @return the senderId
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * Setter for senderId attribute.
	 * @param senderId the senderId to set
	 */
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	/**
	 * Getter for sent attribute.
	 * @return the sent
	 */
	public int getSent() {
		return sent;
	}

	/**
	 * Setter for sent attribute.
	 * @param sent the sent to set
	 */
	public void setSent(int sent) {
		this.sent = sent;
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

}
