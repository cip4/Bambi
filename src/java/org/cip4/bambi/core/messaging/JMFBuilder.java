/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2008 The International Cooperation for the Integration of 
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

import java.util.Vector;

import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.jdflib.auto.JDFAutoQueueFilter.EnumUpdateGranularity;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFDeviceFilter;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFNewJDFQuParams;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFQueueFilter;
import org.cip4.jdflib.jmf.JDFRequestQueueEntryParams;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResubmissionParams;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFStopPersChParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.resource.JDFResource.EnumResourceClass;

/**
 * factory for creating JMF messages
 * 
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * June 20, 2009 split off from JMFFactory
 */
public class JMFBuilder extends BambiLogFactory
{
	private String acknowledgeURL;

	/**
	 * @return the acknowledgeURL
	 */
	public String getAcknowledgeURL()
	{
		return acknowledgeURL;
	}

	/**
	 * @param acknowledgeURL the acknowledgeURL to set
	 */
	public void setAcknowledgeURL(final String acknowledgeURL)
	{
		this.acknowledgeURL = acknowledgeURL;
	}

	/**
	 * 
	 */
	public JMFBuilder() // all static
	{
		super();
		acknowledgeURL = null;
		log.debug("constuctor");
	}

	/**
	 * build a JMF SuspendQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to suspend
	 * @return the message
	 */
	public JDFJMF buildSuspendQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.SuspendQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF HoldQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to hold
	 * @return the message
	 */
	public JDFJMF buildHoldQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.HoldQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF ResumeQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to resume
	 * @return the message
	 */
	public JDFJMF buildResumeQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.ResumeQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF AbortQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to abort
	 * @return the message
	 */
	public JDFJMF buildAbortQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.AbortQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF AbortQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to abort
	 * @return the message
	 */
	public JDFJMF buildRenoveQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.RemoveQueueEntry);
		return jmf;
	}

	/**
	 * @param queueEntryId
	 * @param typ
	 * @return the jmf
	 */
	private JDFJMF buildQueueEntryCommand(final String queueEntryId, final EnumType typ)
	{
		if (queueEntryId == null)
		{
			return null;
		}
		final JDFJMF jmf = createJMF(EnumFamily.Command, typ);
		final JDFCommand command = jmf.getCommand(0);
		command.appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}

	/**
	 * create a JMF that has all builder specific details filled in
	 * @param family
	 * @param typ
	 * @return
	 */
	public JDFJMF createJMF(final EnumFamily family, final EnumType typ)
	{
		final JDFJMF jmf = JDFJMF.createJMF(family, typ);
		{
			final JDFMessage m = jmf.getMessageElement(null, null, 0);
			if (EnumFamily.Command.equals(family))
			{
				final JDFCommand c = (JDFCommand) m;
				if (acknowledgeURL != null)
				{
					c.setAcknowledgeURL(acknowledgeURL);
				}
			}
			else if (EnumFamily.Query.equals(family))
			{
				final JDFQuery q = (JDFQuery) m;
				if (acknowledgeURL != null)
				{
					q.setAcknowledgeURL(acknowledgeURL);
				}
			}
		}
		return jmf;
	}

	/**
	 * build a JMF RemoveQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to remove
	 * @return the message
	 */
	public JDFJMF buildRemoveQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.RemoveQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF Status query
	 * @return the message
	 */
	public JDFJMF buildStatus()
	{
		final JDFJMF jmf = createJMF(EnumFamily.Query, EnumType.Status);
		return jmf;
	}

	/**
	 * build a JMF Status subscription
	 * @param subscriberURL
	 * @param repeatTime
	 * @param repeatStep
	 * @param queueEntryID
	 * @return the message
	 */
	public JDFJMF buildStatusSubscription(final String subscriberURL, final double repeatTime, final int repeatStep, final String queueEntryID)
	{
		final JDFJMF jmf = buildSubscription(EnumType.Status, subscriberURL, repeatTime, repeatStep);
		final JDFQuery query = jmf.getQuery(0);
		final JDFStatusQuParams statusQuParams = query.getCreateStatusQuParams(0);
		statusQuParams.setJobDetails(EnumJobDetails.Brief);
		statusQuParams.setDeviceDetails(EnumDeviceDetails.Brief);

		if (queueEntryID != null)
		{
			statusQuParams.setQueueEntryID(queueEntryID);
		}
		return jmf;
	}

	/**
	 * build a JMF Resource subscription
	 * @param subscriberURL
	 * @param repeatTime
	 * @param repeatStep
	 * @param queueEntryID
	 * @return the message
	 */
	public JDFJMF buildResourceSubscription(final String subscriberURL, final double repeatTime, final int repeatStep, final String queueEntryID)
	{
		final JDFJMF jmf = buildSubscription(EnumType.Resource, subscriberURL, repeatTime, repeatStep);
		final JDFQuery query = jmf.getQuery(0);
		final JDFResourceQuParams resourceQuParams = query.getCreateResourceQuParams(0);
		final Vector<EnumResourceClass> c = new Vector<EnumResourceClass>();
		c.add(EnumResourceClass.Consumable);
		c.add(EnumResourceClass.Handling);
		c.add(EnumResourceClass.Implementation);
		resourceQuParams.setClasses(c);
		if (queueEntryID != null)
		{
			resourceQuParams.setQueueEntryID(queueEntryID);
		}
		return jmf;
	}

	/**
	 * build a JMF Knownmessages query
	 * @return the message
	 */
	public JDFJMF buildKnownMessagesQuery()
	{
		return createQuery(JDFMessage.EnumType.KnownMessages).getJMFRoot();
	}

	/**
	 * build a JMF Knownmessages query
	 * @return the message
	 */
	public JDFJMF buildSubmissionMethodsQuery()
	{
		return createQuery(JDFMessage.EnumType.SubmissionMethods).getJMFRoot();
	}

	/**
	 * build a JMF Knowndevices query
	 * @param details
	 * @return the message
	 */
	public JDFJMF buildKnownDevicesQuery(final org.cip4.jdflib.auto.JDFAutoDeviceFilter.EnumDeviceDetails details)
	{
		final JDFQuery q = createQuery(JDFMessage.EnumType.KnownDevices);
		final JDFDeviceFilter deviceFilter = q.appendDeviceFilter();
		deviceFilter.setDeviceDetails(details);
		return q.getJMFRoot();
	}

	/**
	 * build a JMFNewJDF query
	 * @param jobID
	 * @param jobPartID
	 * @return the message
	 */
	public JDFJMF buildNewJDFuery(final String jobID, final String jobPartID)
	{
		final JDFQuery q = createQuery(JDFMessage.EnumType.NewJDF);
		final JDFNewJDFQuParams nqp = q.appendNewJDFQuParams();
		nqp.setIdentifier(new NodeIdentifier(jobID, jobPartID, null));
		return q.getJMFRoot();
	}

	/**
	 * build a JMF Notification subscription
	 * @param subscriberURL
	 * @return the message
	 */
	public JDFJMF buildNotificationSubscription(final String subscriberURL)
	{
		final JDFJMF jmf = buildSubscription(EnumType.Notification, subscriberURL, 0, 0);
		return jmf;
	}

	/**
	 * build a generic query for a given type
	 * @param typ
	 * @return the query
	 */
	private JDFQuery createQuery(final EnumType typ)
	{
		final JDFJMF jmf = createJMF(EnumFamily.Query, typ);
		return jmf.getQuery(0);
	}

	/**
	 * build a generic query for a given type
	 * @param typ
	 * @return the query
	 */
	private JDFCommand createCommand(final EnumType typ)
	{
		final JDFJMF jmf = createJMF(EnumFamily.Command, typ);
		return jmf.getCommand(0);
	}

	/**
	 * build a generic subscription for a given type
	 * @param typ
	 * @param subscriberURL
	 * @param repeatTime
	 * @param repeatStep
	 * @return the message
	 */
	private JDFJMF buildSubscription(final EnumType typ, final String subscriberURL, final double repeatTime, final int repeatStep)
	{
		final JDFJMF jmf = createJMF(EnumFamily.Query, typ);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setURL(subscriberURL);
		if (repeatTime > 0)
		{
			s.setRepeatTime(repeatTime);
		}
		if (repeatStep > 0)
		{
			s.setRepeatStep(repeatStep);
		}
		s.appendObservationTarget().setObservationPath("*");

		return jmf;
	}

	/**
	 * build a JMF QueueStatus query
	 * @return the message
	 */
	public JDFJMF buildQueueStatus()
	{
		final JDFJMF jmf = createJMF(EnumFamily.Query, EnumType.QueueStatus);
		return jmf;
	}

	/**
	 * build a JMF QueueStatus query
	 * @param subscriberURL
	 * @return the message
	 */
	public JDFJMF buildQueueStatusSubscription(final String subscriberURL)
	{
		final JDFJMF jmf = buildSubscription(EnumType.QueueStatus, subscriberURL, 0, 0);
		final JDFQueueFilter filter = jmf.getQuery(0).appendQueueFilter();
		filter.setUpdateGranularity(EnumUpdateGranularity.ChangesOnly);
		return jmf;
	}

	/**
	 * build a JMF RequestQueueEntry command <br/>
	 * default: JMFFactory.buildRequestQueueEntry(theQueueURL,null)
	 * @param queueURL the queue URL of the device sending the command ("where do you want your SubmitQE's delivered to?")
	 * @param nid the nodeidentifier of the requested qe, default=null
	 * @return the message
	 */
	public JDFJMF buildRequestQueueEntry(final String queueURL, final NodeIdentifier nid)
	{
		// maybe replace DeviceID with DeviceType, just to be able to decrease the
		// Proxy's knowledge about querying devices?
		final JDFJMF jmf = createJMF(EnumFamily.Command, EnumType.RequestQueueEntry);
		final JDFRequestQueueEntryParams qep = jmf.getCommand(0).appendRequestQueueEntryParams();
		qep.setQueueURL(queueURL);
		qep.setIdentifier(nid);
		return jmf;
	}

	/**
	 * create a set of default subscriptions
	 * @param url
	 * @param queueEntryID
	 * @param repeatTime
	 * @param repeatStep
	 *@return the array of subscriptions to be sent
	 */
	public JDFJMF[] createSubscriptions(final String url, final String queueEntryID, final double repeatTime, final int repeatStep)
	{
		final JDFJMF jmfs[] = new JDFJMF[4];
		jmfs[0] = buildStatusSubscription(url, repeatTime, repeatStep, queueEntryID);
		jmfs[1] = buildResourceSubscription(url, 0, 0, queueEntryID);
		jmfs[2] = buildNotificationSubscription(url);
		jmfs[3] = buildQueueStatusSubscription(url);
		return jmfs;
	}

	/**
	 * build a ResubmitQueueEntry message
	 * @param qeID
	 * @param url the url of the jdf to resubmit
	 * @return the jmf
	 */
	public JDFJMF buildResubmitQueueEntry(final String qeID, final String url)
	{
		final JDFCommand c = createCommand(EnumType.ResubmitQueueEntry);
		final JDFResubmissionParams rp = c.appendResubmissionParams();
		rp.setQueueEntryID(qeID);
		rp.setURL(url);
		return c.getJMFRoot();
	}

	/**
	 * build a stopPersistentChannelParams message
	 * @param channelID
	 * @param qeID
	 * @param url the url of the subscription
	 * @return the jmf
	 */
	public JDFJMF buildStopPersistentChannel(final String channelID, final String qeID, final String url)
	{
		final JDFCommand c = createCommand(EnumType.StopPersistentChannel);
		final JDFStopPersChParams scp = c.appendStopPersChParams();
		scp.setChannelID(channelID);
		scp.setURL(url);
		scp.setQueueEntryID(qeID);
		return c.getJMFRoot();
	}
}