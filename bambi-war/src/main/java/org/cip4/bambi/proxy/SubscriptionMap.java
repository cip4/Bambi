/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2013 The International Cooperation for the Integration of 
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.MessageResponseHandler;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JMFBuilder;

/**
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class SubscriptionMap extends HashMap<EnumType, ProxySubscription>
{
	private final Log log;
	private boolean wantShutDown;

	/**
	 * 
	 */
	protected SubscriptionMap()
	{
		super();
		log = LogFactory.getLog(getClass());
		wantShutDown = true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param refID
	 * @return true if subscription found
	 */
	public boolean incrementHandled(String refID)
	{
		ProxySubscription ps = getSubscription(refID);
		if (ps != null)
		{
			ps.incrementHandled();
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 
	 * send StopPersistantChannel messages to url
	 * @param dev 
	 *  
	 */
	public void shutdown(AbstractProxyDevice dev)
	{
		if (!wantShutDown)
		{
			log.info("skipping shutdown because wantshutdown=false");
			return;
		}
		log.info("retrieving stoppersistantchannel messages; n=" + size());
		long t0 = System.currentTimeMillis();
		Collection<ProxySubscription> v = values();
		for (ProxySubscription ps : v)
		{
			JDFJMF stopper = ps.getStopper();
			dev.sendJMFToSlave(stopper, null);
		}
		// and - just in case - a global cleanup
		final JMFBuilder builder = dev.getBuilderForSlave();
		final JDFJMF stopPersistant = builder.buildStopPersistentChannel(null, null, dev.getDeviceURLForSlave());
		final MessageResponseHandler waitHandler = dev.new StopPersistantHandler(stopPersistant);
		dev.sendJMFToSlave(stopPersistant, waitHandler);
		waitHandler.waitHandled(2222, 30000, true);

		log.info("sent all messages: t=" + ((System.currentTimeMillis() - t0) * 0.001));
	}

	/**
	 * @param refID the refID or type of the message
	 * @return
	 */
	private ProxySubscription getSubscription(String refID)
	{
		if (refID == null)
		{
			return null;
		}
		Collection<ProxySubscription> v = values();
		for (ProxySubscription ps : v)
		{
			if (refID.equals(ps.channelID) || refID.equals(ps.type))
			{
				return ps;
			}
		}
		return null;
	}

	/**
	 * @param deviceRoot
	 */
	protected void copyToXML(KElement deviceRoot)
	{
		Collection<ProxySubscription> v = values();
		Iterator<ProxySubscription> it = v.iterator();
		KElement subs = deviceRoot.appendElement("ProxySubscriptions");
		while (it.hasNext())
			it.next().copyToXML(subs);
	}

	/**
	 * 
	 *  
	 * @param wantShutDown
	 */
	public void setWantShutDown(boolean wantShutDown)
	{
		this.wantShutDown = wantShutDown;
	}
}