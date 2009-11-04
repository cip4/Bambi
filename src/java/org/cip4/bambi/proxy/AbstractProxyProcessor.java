/**
 * The CIP4 Software License, Version 1.0
 *
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
 * copyright (c) 1999-2006, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi.proxy;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.mail.Multipart;

import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.JMFBuilder;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.MessageSender.MessageResponseHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.QueueEntry;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFResubmissionParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;

/**
 * also used for resubmitqueueentry
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * before 13.02.2009
 */
public abstract class AbstractProxyProcessor extends AbstractDeviceProcessor
{
	protected class QueueResubmitter
	{
		final private String slaveQEID;
		final private String myQEID;
		final private JDFNode jdf;

		/**
		 * @param _doc
		 * @param slaveID
		 * @param localqeID
		 */
		public QueueResubmitter(final JDFNode _jdf, final String slaveID, final String localqeID)
		{
			this.jdf = _jdf;
			slaveQEID = slaveID;
			myQEID = localqeID;
		}

		/**
		 * @return
		 */
		public int resubmit()
		{
			final AbstractProxyDevice device = (AbstractProxyDevice) _parent;

			final IProxyProperties proxyProperties = getParent().getProxyProperties();
			final String slaveURL = proxyProperties.getSlaveURL();
			if (slaveURL == null)
			{
				return 106; // already running if only hot folder; ciao
			}
			final MIMEDetails ud = new MIMEDetails();
			ud.httpDetails.chunkSize = proxyProperties.getSlaveHTTPChunk();
			ud.transferEncoding = proxyProperties.getSlaveMIMEEncoding();
			final boolean expandMime = proxyProperties.getSlaveMIMEExpansion();
			final boolean isMime = proxyProperties.isSlaveMimePackaging();

			final JMFBuilder b = getBuilderForSlave();
			final JDFJMF jmf = b.buildResubmitQueueEntry(slaveQEID, null);
			final JDFResubmissionParams rsp = jmf.getCommand(0).getResubmissionParams(0);
			// required in case we convert e.g. to JDF2.0
			KElement modNode = jdf;
			if (slaveCallBack != null)
			{
				if (isMime)
				{
					final JDFDoc d = slaveCallBack.updateJDFForExtern(new JDFDoc(jdf.getOwnerDocument()));
					modNode = d.getRoot();
				}
				slaveCallBack.updateJMFForExtern(jmf.getOwnerDocument_JDFElement());
			}

			if (isMime)
			{
				rsp.setURL("dummy"); // replaced by mimeutil
			}
			else
			// setup http get for JDF
			{
				String jdfURL = device.getDeviceURL();
				jdfURL = StringUtil.replaceString(jdfURL, "/jmf/", "/showJDF/" + AbstractProxyDevice.SLAVEJMF + "/");
				modNode.getOwnerDocument_KElement().write2File((String) null, 0, true);
				jdfURL += "?Callback=true&qeID=" + myQEID;
				rsp.setURL(jdfURL);
			}
			if (modNode != null)
			{
				try
				{

					final JDFMessage r = writeToQueue(jmf.getOwnerDocument_JDFElement(), modNode.getOwnerDocument_KElement(), slaveURL, ud, expandMime, isMime);
					if (r != null)
					{
						if (!EnumType.ResubmitQueueEntry.equals(r.getEnumType())) // total snafu???
						{
							return 1;
						}
						else
						{
							rc = r.getReturnCode();
							return rc;
						}
					}
					else
					{
						log.error("resubmit- no response at: " + slaveURL);
						return 1;
					}
				}
				catch (final IOException x)
				{
					log.error("resubmit - IOEXception at: ", x);
					modNode = null;
				}
			}
			return 1;
		}
	}

	protected class SubmitQueueEntryResponseHandler extends MessageResponseHandler
	{

		/**
		 * @param refID
		 * 
		 */
		public SubmitQueueEntryResponseHandler(final String refID)
		{
			super(refID);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage()
		{
			final boolean b = super.handleMessage();
			return b;
		}

	}

	protected int rc;
	protected JDFNotification notification;
	protected IConverterCallback slaveCallBack;

	/**
	 * @param parent the parent device
	 */
	public AbstractProxyProcessor(final AbstractProxyDevice parent)
	{
		super();
		_parent = parent;
		slaveCallBack = parent.getSlaveCallback();
		rc = 0;
		notification = null;

	}

	/**
	 * get the JMF Builder for messages to the slave device
	 * @return
	 */
	protected JMFBuilder getBuilderForSlave()
	{
		final JMFBuilder builder = ((AbstractProxyDevice) _parent).getBuilderForSlave();
		return builder;
	}

	protected JDFMessage writeToQueue(final JDFDoc docJMF, final XMLDoc docJDF, final String strUrl, final MIMEDetails urlDet, final boolean expandMime, final boolean isMime) throws IOException
	{
		if (strUrl == null)
		{
			log.error("writeToQueue: attempting to write to null url");
			return null;
		}
		final JDFCommand c = docJMF.getJMFRoot().getCommand(0);
		final SubmitQueueEntryResponseHandler sqh = new SubmitQueueEntryResponseHandler(c.getID());
		if (isMime)
		{
			final Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF, expandMime);
			_parent.getJMFFactory().send2URL(mp, strUrl, sqh, slaveCallBack, urlDet, _parent.getDeviceID());
		}
		else
		{
			JMFFactory.getJMFFactory().send2URL(docJMF.getJMFRoot(), strUrl, sqh, slaveCallBack, _parent.getDeviceID());
		}
		sqh.waitHandled(10000, 30000, false);
		final JDFMessage handlerResponse = handleQueueAcknowledge(sqh);
		if (handlerResponse == null)
		{
			log.warn("submission timeout sending to " + strUrl);
			final JDFJMF respJMF = c.createResponse();
			final JDFResponse r = respJMF.getResponse(0);
			final HttpURLConnection connection = sqh.getConnection();
			if (connection == null)
			{
				r.setErrorText("Invalid http connection", null);
			}
			else
			{
				final int responseCode = connection.getResponseCode();
				final String errorText = "Invalid http response - RC=" + responseCode;
				log.warn(errorText);
				r.setErrorText(errorText, null);
			}
			r.setReturnCode(3); // TODO correct rcs
			return r;
		}

		return handlerResponse;
	}

	/**
	 * @param sqh
	 * @return
	 */
	private JDFMessage handleQueueAcknowledge(final MessageResponseHandler sqh)
	{
		JDFMessage handlerResponse = sqh.getResponse();
		if (handlerResponse != null)
		{
			JDFMessage finalResp = sqh.getFinalMessage();
			if (finalResp == null)
			{
				sqh.waitHandled(120000, -1, true);
				finalResp = sqh.getFinalMessage();
			}
			if (finalResp != null)
			{
				handlerResponse = finalResp;
			}
		}
		return handlerResponse;
	}

	/**
	 * @param slaveQEID
	 * @param newStatus
	 * @param slaveURL
	 * @param slaveDeviceID the deviceID as returned from the slave
	 */
	protected void submitted(final String slaveQEID, final EnumQueueEntryStatus newStatus, final String slaveURL, final String slaveDeviceID)
	{
		final JDFQueueEntry qe = currentQE.getQueueEntry();
		BambiNSExtension.setSlaveQueueEntryID(qe, slaveQEID);
		BambiNSExtension.setSlaveSubmissionTime(qe, new JDFDate());
		BambiNSExtension.setDeviceURL(qe, slaveURL);
		if (StringUtil.getNonEmpty(slaveDeviceID) != null)
		{
			qe.setDeviceID(slaveDeviceID);
		}
		_queueProcessor.updateEntry(qe, newStatus, null, null);
	}

	/**
	 * @return the AbstractProxyDevice cast of _parent
	 */
	@Override
	public AbstractProxyDevice getParent()
	{
		return (AbstractProxyDevice) _parent;
	}

	/**
	 * @param qurl
	 * @param deviceOutputHF
	 * @param ud
	 * @param expandMime
	 * @param isMime
	 * @return the updated queueEntry, null if the submit failed
	 */
	protected IQueueEntry submitToQueue(final URL qurl, final File deviceOutputHF, final MIMEDetails ud, final boolean expandMime, final boolean isMime)
	{
		final JDFJMF jmf = getBuilderForSlave().createJMF(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.SubmitQueueEntry);

		final JDFCommand com = (JDFCommand) jmf.getCreateMessageElement(JDFMessage.EnumFamily.Command, null, 0);
		final JDFQueueSubmissionParams qsp = com.appendQueueSubmissionParams();

		final AbstractProxyDevice proxyParent = getParent();
		jmf.setICSVersions(proxyParent.getICSVersions());

		final String deviceURLForSlave = proxyParent.getDeviceURLForSlave();
		qsp.setReturnJMF(deviceURLForSlave);
		if (deviceOutputHF != null)
		{
			qsp.setReturnURL(deviceOutputHF.getPath());
		}
		// fix for returning

		final JDFQueueEntry qe = currentQE.getQueueEntry();
		if (qe != null)
		{
			qsp.copyAttribute(AttributeName.PRIORITY, qe);
			qsp.copyAttribute(AttributeName.GANGNAME, qe);
			qsp.copyAttribute(AttributeName.DESCRIPTIVENAME, qe);
		}

		final JDFNode node = getCloneJDFForSlave(); // the retained internal node
		KElement modNode = node; // the external node
		if (slaveCallBack != null)
		{
			if (isMime)
			{
				final JDFDoc d = slaveCallBack.updateJDFForExtern(new JDFDoc(node.getOwnerDocument()));
				modNode = d.getRoot();
			}
			slaveCallBack.updateJMFForExtern(jmf.getOwnerDocument_JDFElement());
		}

		if (isMime)
		{
			qsp.setURL("dummy"); // replaced by mimeutil
		}
		else
		// setup http get for JDF
		{
			String jdfURL = proxyParent.getDeviceURL();
			jdfURL = StringUtil.replaceString(jdfURL, "/jmf/", "/showJDF/" + AbstractProxyDevice.SLAVEJMF + "/");
			modNode.getOwnerDocument_KElement().write2File((String) null, 0, true);
			jdfURL += "?Callback=true&qeID=" + qe.getQueueEntryID();
			qsp.setURL(jdfURL);
		}
		if (modNode != null)
		{
			try
			{
				final String urlString = qurl == null ? null : qurl.toExternalForm();

				final JDFMessage r = writeToQueue(jmf.getOwnerDocument_JDFElement(), modNode.getOwnerDocument_KElement(), urlString, ud, expandMime, isMime);
				if (r != null)
				{
					if (!EnumType.SubmitQueueEntry.equals(r.getEnumType())) // total snafu???
					{
						log.error("Device returned rc=" + r.getReturnCode());
						_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
					}
					else
					{
						rc = r.getReturnCode();
						if (rc != 0)
						{
							log.warn("Device returned rc=" + rc);
							notification = (JDFNotification) r.getElement(ElementName.NOTIFICATION);
						}
						final JDFQueueEntry qeR = r.getQueueEntry(0);
						if (qeR != null)
						{
							submitted(qeR.getQueueEntryID(), qeR.getQueueEntryStatus(), urlString, r.getSenderID());
						}
						else
						{
							log.error("No QueueEntry in the submitqueuentry response");
							_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
						}
					}
				}
				if (r == null)
				{
					log.error("submitToQueue - no response at: " + BambiNSExtension.getDocURL(qe));
					_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
				}
			}
			catch (final IOException x)
			{
				modNode = null;
			}
		}
		if (modNode == null)
		{
			log.error("submitToQueue - no JDFDoc at: " + BambiNSExtension.getDocURL(qe));
			_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
		}
		return new QueueEntry(node, qe);
	}

	/**
	 * @return {@link JDFNode} the updated clone with updated subscriptions
	 */
	protected JDFNode getCloneJDFForSlave()
	{
		final JDFNode nod = currentQE.getJDF();
		if (nod == null)
		{
			return null;
		}
		final JDFDoc docClone = nod.getOwnerDocument_JDFElement().clone();
		final JDFNode rootClone = docClone.getJDFRoot();
		JDFNode nodClone = rootClone.getJobPart(nod.getJobPartID(false), nod.getJobID(true));
		if (nodClone == null)
		{
			nodClone = rootClone;
		}
		updateNISubscriptions(nodClone);
		return nodClone;
	}

	/**
	 * removes all direct nodeinfo subscriptions and adds new ones to the proxy if required
	 * @param root the node to update subscriptions in
	 */
	void updateNISubscriptions(final JDFNode root)
	{
		if (root == null)
		{
			return;
		}

		final VElement v = root.getvJDFNode(null, null, false);
		for (int i = 0; i < v.size(); i++)
		{
			final JDFNode n = (JDFNode) v.get(i);
			final JDFNodeInfo ni = n.getNodeInfo();
			if (ni == null)
			{
				continue;
			}
			final VElement vJMF = ni.getChildElementVector(ElementName.JMF, null);
			final int sJMF = (vJMF == null) ? 0 : vJMF.size();
			for (int j = 0; j < sJMF; j++)
			{
				vJMF.get(j).deleteNode();
			}
		}
	}

	/**
	 * @param jdf
	 * @param queueEntryID
	 * @return
	 */
	public VString canAccept(final JDFNode jdf, final String queueEntryID)
	{
		if (queueEntryID == null)
		{
			return null;
		}
		final String slaveID = getSlaveQEID(queueEntryID);
		if (slaveID == null)
		{
			return null;
		}
		int iRet = new QueueResubmitter(jdf, slaveID, queueEntryID).resubmit();
		return iRet == 0 ? new VString(getParent().getDeviceID(), null) : null;

	}

	/**
	 * @param queueEntryID
	 * @return
	 */
	private String getSlaveQEID(final String queueEntryID)
	{
		final JDFQueue q = getParent().getQueueProcessor().getQueue();
		return BambiNSExtension.getSlaveQueueEntryID(q.getQueueEntry(queueEntryID));
	}

	/**
	 * @return the QueuentryID as submitted to the slave device
	 */
	public String getSlaveQEID()
	{
		return currentQE == null ? null : BambiNSExtension.getSlaveQueueEntryID(currentQE.getQueueEntry());
	}
}
