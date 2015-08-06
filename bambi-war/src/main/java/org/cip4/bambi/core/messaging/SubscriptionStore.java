/**
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
 * copyright (c) 1999-2006, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 * 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi.core.messaging;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.SignalDispatcher.XMLSubscriptions;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.RollingBackupFile;

class SubscriptionStore
{
	/**
	 * 
	 */
	private final SignalDispatcher signalDispatcher;
	private final RollingBackupFile backup;
	private boolean loading;
	private final Log log;

	/**
	 * 
	 * @param signalDispatcher
	 * @param dir
	 */
	SubscriptionStore(SignalDispatcher signalDispatcher, final File dir)
	{
		this.signalDispatcher = signalDispatcher;
		log = LogFactory.getLog(getClass());
		loading = false;
		backup = new RollingBackupFile(FileUtil.getFileInDirectory(dir, new File("subscriptions.xml")), 8);
	}

	/**
	 * load subscriptions from file
	 */
	public void load()
	{
		loading = true;
		final XMLDoc d = XMLDoc.parseFile(backup);
		final KElement root = d == null ? null : d.getRoot();
		try
		{
			if (root != null)
			{
				final VElement v = root.getChildElementVector(MsgSubscription.SUBSCRIPTION_ELEMENT, null);
				for (KElement subElem : v)
				{
					final MsgSubscription sub = new MsgSubscription(this.signalDispatcher, subElem);
					synchronized (this.signalDispatcher.subscriptionMap)
					{
						if (sub.channelID != null)
						{
							signalDispatcher.subscriptionMap.put(sub.channelID, sub);
							log.info("reloading subscription for channelID=" + sub.channelID + " to: " + sub.url);
							JMFFactory.getJMFFactory().getCreateMessageSender(sub.url);
						}
						else
						{
							log.warn("cannot reload subscription without channelID to: " + sub.url);
						}
					}
				}
			}
			else
			{
				log.info("no subscriptions loaded from " + backup);
			}
		}
		catch (final Throwable x)
		{
			log.error("unknown exception while loading subscriptions", x);
		}
		loading = false;
	}

	/**
	 * write all subscriptions to disk
	 */
	public void persist()
	{
		if (loading)
		{
			return;
		}
		final XMLSubscriptions xmls = this.signalDispatcher.new XMLSubscriptions();
		xmls.setXMLRoot(null);
		xmls.listChannels(null, "*");
		xmls.write2File(backup.getNewFile(), 2, false);
	}

	@Override
	public String toString()
	{
		return "SubscriptionStore [backup=" + backup + ", loading=" + loading + "]";
	}

}