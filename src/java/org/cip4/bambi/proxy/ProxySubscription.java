/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2011 The International Cooperation for the Integration of 
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
package org.cip4.bambi.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.ifaces.IJMFSubscribable;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.util.StringUtil;

/**
 * class to manage subscriptions to the slave device
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
class ProxySubscription
{
	long lastReceived;
	long created;
	int numReceived;
	String channelID;
	String url;
	JDFJMF subscribedJMF;
	String type;
	final Log log;

	/**
	 * 
	 * @param jmf	  
	 * @throws IllegalArgumentException
	 */
	public ProxySubscription(JDFJMF jmf) throws IllegalArgumentException
	{
		log = LogFactory.getLog(getClass());
		subscribedJMF = (JDFJMF) jmf.clone();
		JDFMessage messageElement = subscribedJMF.getMessageElement(null, null, 0);
		type = messageElement.getType();
		channelID = StringUtil.getNonEmpty(jmf.getQuery(0).getID());
		if (channelID == null)
		{
			log.error("Subscription with no channelID");
			throw new IllegalArgumentException("Subscription with no channelID");
		}
		lastReceived = 0;
		numReceived = StringUtil.parseInt(BambiNSExtension.getMyNSAttribute(jmf, "numReceived"), 0);
		created = StringUtil.parseLong(BambiNSExtension.getMyNSAttribute(jmf, AttributeName.CREATIONDATE), System.currentTimeMillis());
		url = ((IJMFSubscribable) messageElement).getSubscription().getURL();
	}

	/**
	 * @param channelID
	 */
	public void setChannelID(String channelID)
	{
		if (this.channelID.equals(channelID))
			return; //nop

		log.info("updating proxy subscription channelID to: " + channelID);
		this.channelID = channelID;
		subscribedJMF.getMessageElement(null, null, 0).setID(channelID);
	}

	/**
	 * send a stoppersistantchannel for myself
	 * @return the jmf element representing the stoppersistantchannel message
	 *
	 */
	public JDFJMF getStopper()
	{
		JMFBuilder builder = JMFBuilderFactory.getJMFBuilder(null);
		JDFJMF jmf = builder.buildStopPersistentChannel(channelID, null, url);
		log.info("generating StopPersistantChannel for ID=" + channelID + " to " + url);
		return jmf;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return
	*/
	@Override
	public String toString()
	{
		return "ProxySubscription: " + subscribedJMF;
	}

	/**
	 * 
	 */
	public void incrementHandled()
	{
		lastReceived = System.currentTimeMillis();
		numReceived++;
	}

	/**
	 * @param subs
	 */
	public void copyToXML(KElement subs)
	{
		subs = subs.appendElement("ProxySubscription");
		subs.copyElement(subscribedJMF, null);
		subs.setAttribute(AttributeName.CHANNELID, channelID);
		subs.setAttribute(AttributeName.URL, url);
		subs.setAttribute(AttributeName.TYPE, subscribedJMF.getMessageElement(null, null, 0).getType());
		subs.setAttribute(AttributeName.CREATIONDATE, XMLResponse.formatLong(created));
		subs.setAttribute("LastReceived", XMLResponse.formatLong(lastReceived));
		subs.setAttribute("NumReceived", StringUtil.formatInteger(numReceived));
	}
}