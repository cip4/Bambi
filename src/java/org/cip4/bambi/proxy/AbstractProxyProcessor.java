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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.QueueEntry;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * before 13.02.2009
 */
public abstract class AbstractProxyProcessor extends AbstractDeviceProcessor
{

	private static Log log = LogFactory.getLog(AbstractProxyProcessor.class);
	protected IConverterCallback slaveCallBack;

	/**
	 * @param parent the parent device
	 */
	public AbstractProxyProcessor(final AbstractProxyDevice parent)
	{
		super();
		_parent = parent;
		slaveCallBack = parent.getSlaveCallback();

	}

	protected JDFDoc writeToQueue(final JDFDoc docJMF, final XMLDoc docJDF, final String strUrl, final MIMEDetails urlDet, final boolean expandMime, final boolean isMime) throws IOException
	{
		if (strUrl == null)
		{
			log.error("writeToQueue: attempting to write to null url");
			return null;
		}
		final SubmitQueueEntryResponseHandler sqh = new SubmitQueueEntryResponseHandler();
		if (isMime)
		{
			final Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF, expandMime);
			JMFFactory.getJMFFactory().send2URL(mp, strUrl, sqh, slaveCallBack, urlDet, _parent.getDeviceID());
		}
		else
		{
			JMFFactory.getJMFFactory().send2URL(docJMF.getJMFRoot(), strUrl, sqh, slaveCallBack, _parent.getDeviceID());
		}
		sqh.waitHandled(10000, true);
		if (sqh.doc == null)
		{
			log.warn("submission timeout sending to " + strUrl);
			final JDFCommand c = docJMF.getJMFRoot().getCommand(0);
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
			return respJMF.getOwnerDocument_JDFElement();
		}

		return sqh.doc;
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
	AbstractProxyDevice getParent()
	{
		return (AbstractProxyDevice) _parent;
	}

	/**
	 * @param qurl
	 * @param deviceOutputHF
	 * @param ud
	 * @param expandMime
	 * @param isMime
	 * @return the updated queuentry, null if the submit failed
	 */
	protected IQueueEntry submitToQueue(final URL qurl, final File deviceOutputHF, final MIMEDetails ud, final boolean expandMime, final boolean isMime)
	{
		final JDFJMF jmf = JDFJMF.createJMF(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.SubmitQueueEntry);

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

		final JDFNode node = getCloneJDFForSlave(); // the retained internal node
		KElement modNode = node; // the external node
		final JDFQueueEntry qe = currentQE.getQueueEntry();

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

				JDFDoc d = writeToQueue(jmf.getOwnerDocument_JDFElement(), modNode.getOwnerDocument_KElement(), urlString, ud, expandMime, isMime);
				if (d != null)
				{
					final JDFJMF jmfResp = d.getJMFRoot();
					if (jmfResp == null)
					{
						d = null;
					}
					else
					{
						final JDFResponse r = jmfResp.getResponse(0);
						if (r == null)
						{
							d = null;
						}
						else
						{
							if (!EnumType.SubmitQueueEntry.equals(r.getEnumType())) // total snafu???
							{
								log.error("Device returned rc=" + r.getReturnCode());
								_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
							}
							else if (r.getReturnCode() != 0)
							{
								final int rc = r.getReturnCode();
								log.error("Error returncode in response: rc=" + rc);
							}
							else
							{
								final JDFQueueEntry qeR = r.getQueueEntry(0);
								if (qeR != null)
								{
									submitted(qeR.getQueueEntryID(), qeR.getQueueEntryStatus(), urlString, r.getSenderID());
								}
								else
								{
									log.error("No QueueEntry in the submitqueuentry response");
								}
							}
						}
					}
				}
				if (d == null)
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
		final JDFDoc docClone = (JDFDoc) nod.getOwnerDocument_JDFElement().clone();
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

}
