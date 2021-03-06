/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2014 The International Cooperation for the Integration of 
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
package org.cip4.bambi.core.messaging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFAcknowledge;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * Map of channelID / Acknowledges for a device <br/>
 * this class handles outgoing asynchronous requests, i.e. incoming acknowledge messages (we sent an AcknowledgeURL)
 * 
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * June 20, 2009
 */
public class AcknowledgeMap extends BambiLogFactory implements IMessageHandler
{
	private static AcknowledgeMap ackMap = null;
	private long lastCleanup;
	private final Set<String> doneHandled;

	/**
	 * @return the ackMap
	 */
	public static AcknowledgeMap getMap()
	{
		if (ackMap == null)
		{
			ackMap = new AcknowledgeMap();
		}
		return ackMap;
	}

	/**
	 */
	public AcknowledgeMap()
	{
		super();
		theMap = new HashMap<String, IResponseHandler>();
		lastCleanup = 0;
		doneHandled = new HashSet<String>();
	}

	/**
	 * @param refID
	 * @param handler
	 */
	public void addHandler(final String refID, final IResponseHandler handler)
	{
		if (System.currentTimeMillis() - lastCleanup > 3600000)
		{
			cleanup();
		}
		log.info("adding acknowledge handler for: " + refID);
		theMap.put(refID, handler);
	}

	/**
	 * 
	 */
	private void cleanup()
	{
		synchronized (theMap)
		{
			final Iterator<String> it = theMap.keySet().iterator();
			final VString v = new VString();
			while (it.hasNext())
			{
				final String key = it.next();
				final IResponseHandler h = theMap.get(key);
				if (h.isAborted())
				{
					v.add(key);
				}
			}
			// 2 loops in so we don't invalidate the iterator...
			for (String key : v)
			{
				log.info("removing aborted acknowledge handler: " + key);
				removeHandler(key);
			}
		}
		if (doneHandled.size() > 1000)
		{
			doneHandled.clear();
			log.info("clearing old done handled list");
		}
		lastCleanup = System.currentTimeMillis();
	}

	/**
	 * @return
	 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#isSubScribable()
	 */
	@Override
	public boolean isSubScribable()
	{
		return true;
	}

	private final HashMap<String, IResponseHandler> theMap;

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#getFamilies()
	 */
	@Override
	public EnumFamily[] getFamilies()
	{
		final EnumFamily[] f = { EnumFamily.Acknowledge };
		return f;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#getMessageType()
	 */
	@Override
	public String getMessageType()
	{
		return "*"; // any will do
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
	 */
	@Override
	public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
	{
		final JDFAcknowledge a = (JDFAcknowledge) inputMessage;
		final String channelID = StringUtil.getNonEmpty(a.getrefID());
		if (channelID == null)
		{
			JMFHandler.errorResponse(response, "Handling Acknowledge with no refID, bailing out", 9, EnumClass.Error);
			return true;
		}
		if (doneHandled.contains(channelID))
		{
			KElement jobElem = a.getChildWithAttribute(null, AttributeName.JOBID, null, null, 0, false);
			String jobID = jobElem == null ? "" : " JobID=" + jobElem.getAttribute(AttributeName.JOBID);
			if (jobElem == null)
			{
				jobElem = a.getChildWithAttribute(null, AttributeName.QUEUEENTRYID, null, null, 0, false);
				jobID = jobElem == null ? "" : " QueueEntryID=" + jobElem.getAttribute(AttributeName.QUEUEENTRYID);
			}
			log.warn("Multiply handled Acknowledge - ignored refID= " + channelID + " Message ID= " + a.getID() + jobID);
			return true;
		}
		IResponseHandler handler = waitForHandler(channelID);
		if (handler == null)
		{
			JMFHandler.errorResponse(response, "Cannot handle Acknowledge with unknown refID " + channelID + ", bailing out", 6, EnumClass.Error);
			return true;
		}
		handler.setMessage(a);
		final boolean b = handler.handleMessage();
		if (b || handler.isAborted())
		{
			removeHandler(channelID);
			log.info("handled Acknowledge refID=" + channelID + " Messade ID= " + a.getID());
		}
		return b;
	}

	private IResponseHandler waitForHandler(final String channelID)
	{
		IResponseHandler handler = null;
		for (int i = 1; i <= 3; i++)
		{
			handler = theMap.get(channelID);
			if (handler == null)
			{
				if (i == 1)
					log.warn("Race condition in acknowledge for " + channelID + " ? lets wait: " + (i * i * 100) + " milliseconds");
				if (!ThreadUtil.sleep(100 * i * i))
					break;
			}
			else
			{
				if (i > 1)
					log.info("Race condition in acknowledge " + channelID + " resolved after " + (i * i * 100) + " milliseconds");
				break;
			}
		}
		if (handler == null)
			log.warn("Gave up waiting for race condition in acknowledge " + channelID);
		return handler;
	}

	/**
	 * @param rid
	 */
	public void removeHandler(final String rid)
	{
		synchronized (theMap)
		{
			theMap.remove(rid);
		}
		doneHandled.add(rid);
	}

	/**
	 * @param channelID
	 */
	public void addDone(final String channelID)
	{
		synchronized (theMap)
		{
			doneHandled.add(channelID);
		}
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IMessageHandler#isAcknowledge()
	 */
	@Override
	public boolean isAcknowledge()
	{
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "AcknowledgeMap: " + theMap.size();
	}
}
