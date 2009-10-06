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

import org.cip4.bambi.richclient.value.MsgSubscriptionVO;

/**
 * MsgSubscription Pojo including static builder class.
 * @author smeissner
 * @date 06.10.2009
 */
public class MsgSubscription {
	private final String channelId;
	private final String family;
	private final String lastTime;
	private final String messageType;
	private final String queueEntryId;
	private final int repeatStep;
	private final int repeatTime;
	private final String senderId;
	private final int sent;
	private final String type;
	private final String url;

	/**
	 * Builder class to create object.
	 * @author smeissner
	 * @date 25.09.2009
	 */
	public static class Builder {
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
		 * Default constructor.
		 */
		public Builder() {
		}

		/**
		 * Custom builder constructor. Accepting a queue view object for initialize.
		 * @param deviceId device id
		 */
		public Builder(MsgSubscriptionVO vo) {
			channelId = vo.getChannelId();
			family = vo.getFamily();
			lastTime = vo.getLastTime();
			messageType = vo.getMessageType();
			queueEntryId = vo.getQueueEntryId();
			repeatStep = vo.getRepeatStep();
			repeatTime = vo.getRepeatTime();
			senderId = vo.getSenderId();
			sent = vo.getSent();
			type = vo.getType();
			url = vo.getUrl();
		}

		// Builder methods
		public Builder channelId(String val) {
			channelId = val;
			return this;
		}

		public Builder family(String val) {
			family = val;
			return this;
		}

		public Builder lastTime(String val) {
			lastTime = val;
			return this;
		}

		public Builder messageType(String val) {
			messageType = val;
			return this;
		}

		public Builder queueEntryId(String val) {
			queueEntryId = val;
			return this;
		}

		public Builder repeatStep(int val) {
			repeatStep = val;
			return this;
		}

		public Builder repeatTime(int val) {
			repeatTime = val;
			return this;
		}

		public Builder senderId(String val) {
			senderId = val;
			return this;
		}

		public Builder sent(int val) {
			sent = val;
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

		/**
		 * Creates and returns a new device object.
		 * @return device instance
		 */
		public MsgSubscription build() {
			return new MsgSubscription(this);
		}
	}

	/**
	 * Private custom constructor for initializing MsgSubscription object by builder.
	 * @param builder Builder instance
	 */
	private MsgSubscription(Builder builder) {
		channelId = builder.channelId;
		family = builder.family;
		lastTime = builder.lastTime;
		messageType = builder.messageType;
		queueEntryId = builder.queueEntryId;
		repeatStep = builder.repeatStep;
		repeatTime = builder.repeatTime;
		senderId = builder.senderId;
		sent = builder.sent;
		type = builder.type;
		url = builder.url;
	}

	/**
	 * Getter for channelId attribute.
	 * @return the channelId
	 */
	public String getChannelId() {
		return channelId;
	}

	/**
	 * Getter for family attribute.
	 * @return the family
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * Getter for lastTime attribute.
	 * @return the lastTime
	 */
	public String getLastTime() {
		return lastTime;
	}

	/**
	 * Getter for messageType attribute.
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * Getter for queueEntryId attribute.
	 * @return the queueEntryId
	 */
	public String getQueueEntryId() {
		return queueEntryId;
	}

	/**
	 * Getter for repeatStep attribute.
	 * @return the repeatStep
	 */
	public int getRepeatStep() {
		return repeatStep;
	}

	/**
	 * Getter for repeatTime attribute.
	 * @return the repeatTime
	 */
	public int getRepeatTime() {
		return repeatTime;
	}

	/**
	 * Getter for senderId attribute.
	 * @return the senderId
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * Getter for sent attribute.
	 * @return the sent
	 */
	public int getSent() {
		return sent;
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

}
