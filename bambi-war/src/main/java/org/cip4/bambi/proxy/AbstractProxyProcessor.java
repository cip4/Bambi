/**
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
import java.util.HashMap;
import java.util.Map;

import javax.mail.Multipart;

import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.MessageResponseHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.QueueEntry;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.auto.JDFAutoQueueFilter.EnumQueueEntryDetails;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueFilter;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFResubmissionParams;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.EnumActivation;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * also used for resubmitqueueentry
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * before 13.02.2009
 */
public abstract class AbstractProxyProcessor extends AbstractDeviceProcessor
{
	protected class SubmitQueueEntryResponseHandler extends MessageResponseHandler
	{

		/**
		 * @param refID the command id that is responded to
		 * 
		 */
		public SubmitQueueEntryResponseHandler(final String refID)
		{
			super(refID);
		}
	}

	protected int rc;
	protected JDFNotification notification;
	protected IConverterCallback slaveCallBack;
	protected EnumActivation activation;
	protected int submitWait;

	/**
	 * get the currently set activation
	 * @return the currently set activation
	 */
	public EnumActivation getActivation()
	{
		return activation;
	}

	/**
	 * get the currently set activation
	 * @return true if this is a "live" submission rather than informative
	 */
	public boolean isLive()
	{
		return !EnumActivation.Informative.equals(activation);
	}

	/**
	 * 
	 * set the activation for submission,
	 * @param activation the activation to set - if null assume Active
	 */
	public void setActivation(EnumActivation activation)
	{
		if (activation == null)
			activation = EnumActivation.Active;
		this.activation = activation;
	}

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
		activation = EnumActivation.Active;
		submitWait = 10000;
	}

	/**
	 * get the JMF Builder for messages to the slave device
	 * @return the JMF Builder for messages to the slave device
	 */
	protected JMFBuilder getBuilderForSlave()
	{
		final JMFBuilder builder = getParent().getBuilderForSlave();
		return builder;
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
	 * finalize setup after successful submission to a slave
	 * 
	 * @param slaveQEID the queentryID at the slave
	 * @param newStatus the retured status from the slave
	 * @param slaveURL the URL that was submitted to
	 * @param slaveDeviceID the deviceID as returned from the slave
	 */
	protected void submitted(final String slaveQEID, final EnumQueueEntryStatus newStatus, final String slaveURL, final String slaveDeviceID)
	{
		if (!isLive())
		{
			log.info("Informative submission to slave " + slaveDeviceID + " completed for:" + slaveQEID);
			return;
		}
		log.info("Submitted to slave " + slaveDeviceID + " qe: " + slaveQEID);
		getParent().addSlaveSubscriptions(0, slaveQEID, false);
		final JDFQueueEntry qe = currentQE.getQueueEntry();
		BambiNSExtension.setSlaveSubmissionTime(qe, new JDFDate());
		BambiNSExtension.setDeviceURL(qe, slaveURL);
		if (StringUtil.getNonEmpty(slaveDeviceID) != null)
		{
			qe.setDeviceID(slaveDeviceID);
		}
		_queueProcessor.updateEntry(qe, newStatus, null, null);
		_queueProcessor.updateCache(qe, slaveQEID);
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
	 * 
	 *  
	 * @author rainer prosi
	 * @date Jan 17, 2013
	 */
	protected class QueueSubmitter
	{
		protected class QueueResubmitter
		{
			final private String slaveQEID;
			final private String myQEID;
			final private JDFNode jdf;

			/**
			 * @param _jdf the jdf node to resubmit
			 * @param slaveID the queueentryid in the context of the slave
			 * @param localqeID the queueentryid in the context of bambi
			 */
			public QueueResubmitter(final JDFNode _jdf, final String slaveID, final String localqeID)
			{
				this.jdf = _jdf;
				slaveQEID = slaveID;
				myQEID = localqeID;
			}

			/**
			 * @return int the return / error code, 0 if success
			 */
			public int resubmit()
			{
				final AbstractProxyDevice device = getParent();

				final IProxyProperties proxyProperties = device.getProperties();
				final String slaveURL = proxyProperties.getSlaveURL();
				if (slaveURL == null)
				{
					return 106; // already running if only hot folder; ciao
				}

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

						final JDFMessage r = writeToQueue(jmf.getOwnerDocument_JDFElement(), modNode.getOwnerDocument_KElement());
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
							getLog().error("resubmit- no response at: " + slaveURL);
							return 1;
						}
					}
					catch (final IOException x)
					{
						getLog().error("resubmit - IOEXception at: ", x);
						modNode = null;
					}
				}
				return 1;
			}
		}

		public void setQurl(URL qurl)
		{
			this.qurl = qurl;
		}

		public void setMime(boolean isMime)
		{
			this.isMime = isMime;
		}

		public void setExpandMime(boolean expandMime)
		{
			this.expandMime = expandMime;
		}

		public void setUd(MIMEDetails ud)
		{
			this.ud = ud;
		}

		public void setDeviceOutputHF(File deviceOutputHF)
		{
			this.deviceOutputHF = deviceOutputHF;
		}

		private URL qurl;
		private boolean isMime;
		private boolean expandMime;
		private MIMEDetails ud;
		private File deviceOutputHF;

		/**
		 * @param url
		*/
		public QueueSubmitter(String url)
		{
			this.qurl = UrlUtil.stringToURL(url);
			final IProxyProperties proxyProperties = getParent().getProperties();
			this.deviceOutputHF = proxyProperties.getSlaveOutputHF();
			this.ud = new MIMEDetails();
			ud.httpDetails.chunkSize = proxyProperties.getSlaveHTTPChunk();
			ud.transferEncoding = proxyProperties.getSlaveMIMEEncoding();
			this.expandMime = proxyProperties.getSlaveMIMEExpansion();
			this.isMime = proxyProperties.isSlaveMimePackaging();
			ud.modifyBoundarySemicolon = !proxyProperties.getSlaveMIMESemicolon();
		}

		/**
		 * @return the updated queueEntry, null if the submit failed
		 */
		public IQueueEntry submitToQueue()
		{
			log.info("Submitting " + (isMime ? "mime" : "jdf") + " qe " + currentQE.getQueueEntryID() + " " + (qurl == null ? "null url" : qurl.toExternalForm()));
			final AbstractProxyDevice proxyParent = getParent();
			final String deviceURLForSlave = proxyParent.getDeviceURLForSlave();
			final JDFJMF jmf = getBuilderForSlave().buildSubmitQueueEntry(deviceURLForSlave);

			final JDFCommand com = (JDFCommand) jmf.getCreateMessageElement(JDFMessage.EnumFamily.Command, null, 0);
			final JDFQueueSubmissionParams qsp = com.getCreateQueueSubmissionParams(0);

			prepareQueueFilterforSubmit(com);
			// fix for returning

			prepareQSP(qsp, deviceOutputHF);

			final JDFNode node = getCloneJDFForSlave(); // the retained internal node
			log.debug("get clone: " + (node == null ? "null" : node.getJobID(false)));
			KElement modNode = node; // the external node
			if (slaveCallBack != null)
			{
				if (isMime)
				{
					log.debug("calling slave callback for mime submission");
					final JDFDoc d = slaveCallBack.updateJDFForExtern(new JDFDoc(node.getOwnerDocument()));
					modNode = d.getRoot();
				}
				log.debug("calling slave callback for jmf submission");
				slaveCallBack.updateJMFForExtern(jmf.getOwnerDocument_JDFElement());
			}

			setJDFURL(isMime, qsp, modNode);
			final JDFQueueEntry qe = currentQE.getQueueEntry();

			if (modNode != null)
			{
				final String urlString = qurl == null ? null : qurl.toExternalForm();
				try
				{
					final JDFMessage r = writeToQueue(jmf.getOwnerDocument_JDFElement(), modNode.getOwnerDocument_KElement());
					if (isLive()) // only evaluate response for live submission - who cares what is happening informative
					{
						processSubmitResponse(qe, urlString, r);
					}
				}
				catch (final IOException x)
				{
					log.error("snafu submitting to queue at " + urlString, x);
					modNode = null;
				}
			}
			if (modNode == null)
			{
				log.error("submitToQueue - no JDFDoc at: " + BambiNSExtension.getDocURL(qe));
				_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
			}
			log.info("Submission completed: " + qe == null ? "null" : qe.getQueueEntryID());
			return isLive() ? new QueueEntry(node, qe) : null;
		}

		/**
		 * @param com
		 */
		private void prepareQueueFilterforSubmit(final JDFCommand com)
		{
			JDFQueueFilter qf = com.getCreateQueueFilter(0);
			qf.setMaxEntries(999);
			qf.setQueueEntryDetails(EnumQueueEntryDetails.Brief);
		}

		/**
		 * 
		 * copy details from queueentry to queuesubmissionparams
		 * @param qsp the queuesubmissionparams to fill
		 * @param deviceOutputHF the device output hot folder, if any
		 */
		protected void prepareQSP(final JDFQueueSubmissionParams qsp, File deviceOutputHF)
		{
			if (deviceOutputHF != null)
			{
				qsp.setReturnURL(deviceOutputHF.getPath());
			}
			final JDFQueueEntry qe = currentQE.getQueueEntry();
			if (qe != null)
			{
				qsp.copyAttribute(AttributeName.PRIORITY, qe);
				qsp.copyAttribute(AttributeName.GANGNAME, qe);
				qsp.copyAttribute(AttributeName.DESCRIPTIVENAME, qe);
				qsp.setAttribute(AttributeName.ACTIVATION, activation == null ? null : activation.getName());
			}
		}

		protected JDFMessage writeToQueue(final JDFDoc docJMF, final XMLDoc docJDF) throws IOException
		{
			log.debug("submitting to queue, url=" + qurl);
			if (qurl == null)
			{
				log.error("writeToQueue: attempting to write to null url");
				return null;
			}
			final JDFCommand c = docJMF.getJMFRoot().getCommand(0);
			final SubmitQueueEntryResponseHandler sqh = new SubmitQueueEntryResponseHandler(c.getID());
			if (isMime)
			{
				log.info("submitting MIME JMF, ID=" + c.getID());
				final Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF, expandMime);
				_parent.getJMFFactory().send2URL(mp, qurl.toExternalForm(), sqh, slaveCallBack, ud, _parent.getDeviceID());
			}
			else
			{
				log.info("submitting RAW JMF, ID=" + c.getID());
				getParent().sendJMFToSlave(docJMF.getJMFRoot(), sqh);
			}
			sqh.waitHandled(submitWait, 6 * submitWait, false);
			final JDFMessage handlerResponse = handleQueueAcknowledge(sqh);
			if (handlerResponse == null)
			{
				log.warn("submission timeout sending to " + qurl);
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
					final String errorText = "Null JMF response - HTTP RC=" + responseCode;
					log.warn(errorText);
					r.setErrorText(errorText, null);
				}
				r.setReturnCode(3); // TODO correct rcs
				return r;
			}

			return handlerResponse;
		}

		private void processSubmitResponse(final JDFQueueEntry qe, final String urlString, final JDFMessage r)
		{
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
					}
					notification = (JDFNotification) r.getElement(ElementName.NOTIFICATION);
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
				evaluateResponseQueue(r);
			}
			else
			{
				log.error("submitToQueue - no response at: " + BambiNSExtension.getDocURL(qe));
				_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
			}
		}

		/**
		 * @param r
		 */
		private void evaluateResponseQueue(JDFMessage r)
		{
			JDFQueue q = r == null ? null : r.getQueue(0);
			Map<String, JDFQueueEntry> hm = q == null ? null : q.getQueueEntryIDMap();

			JDFQueueEntry qe = r.getQueueEntry(0);
			String newQEID = qe == null ? null : StringUtil.getNonEmpty(qe.getQueueEntryID());
			if (newQEID != null)
			{
				if (hm == null)
				{
					hm = new HashMap<String, JDFQueueEntry>();
				}
				hm.put(newQEID, qe);
			}
			// this may be a bug...
			if (hm == null || hm.size() == 0)
			{
				log.warn("no queueentry in submitqueueentry response");
				return;
			}
			JDFAttributeMap map = new JDFAttributeMap(AttributeName.DEVICEID, r.getSenderID());
			QueueProcessor queueProcessor = _parent.getQueueProcessor();
			VElement myQueueEntries = queueProcessor.getQueue().getQueueEntryVector(map, null);
			if (myQueueEntries == null || myQueueEntries.size() == 0)
				return;

			// find any running entries and revert them to waiting if they are not provided in the queue
			for (KElement myElm : myQueueEntries)
			{
				JDFQueueEntry myQE = (JDFQueueEntry) myElm;

				String slaveQEID = BambiNSExtension.getSlaveQueueEntryID(myQE);
				JDFQueueEntry deviceEntry = hm.get(slaveQEID);
				if (deviceEntry == null)
				{
					log.warn("reverting missing queue entry: " + myQE.getQueueEntryID());
					queueProcessor.updateEntry(myQE, EnumQueueEntryStatus.Waiting, null, null);
				}
			}

		}
	}

	private void setJDFURL(final boolean isMime, final JDFQueueSubmissionParams qsp, KElement modNode)
	{
		final AbstractProxyDevice proxyParent = getParent();

		if (isMime)
		{
			qsp.setURL("dummy"); // replaced by mimeutil
		}
		else
		// setup http get for JDF
		{
			String jdfURL = proxyParent.getDeviceURL();
			jdfURL = StringUtil.replaceString(jdfURL, "/jmf/", "/showJDF/");
			modNode.getOwnerDocument_KElement().write2File((String) null, 0, true);
			jdfURL += "?Callback=true&raw=true&activation=" + activation.getName() + "&qeID=" + currentQE.getQueueEntryID();
			qsp.setURL(jdfURL);
		}
	}

	/**
	 * @return {@link JDFNode} the updated clone with updated subscriptions and Activation
	 */
	protected JDFNode getCloneJDFForSlave()
	{
		final JDFNode nod = currentQE.getJDF();
		if (nod == null)
		{
			log.error("no jdf to clone: qe=" + currentQE.getQueueEntryID());
			return null;
		}
		final JDFDoc docClone = nod.getOwnerDocument_JDFElement().clone();
		final JDFNode rootClone = docClone.getJDFRoot();
		JDFNode nodClone = rootClone.getJobPart(nod.getJobPartID(false), nod.getJobID(true));
		if (nodClone == null)
		{
			nodClone = rootClone;
			log.warn("no matching part - using root: qe=" + currentQE.getQueueEntryID());
		}
		updateNISubscriptions(nodClone);
		if (activation != null)
		{
			nodClone.setActivation(activation);
		}
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
			final VElement vJMF = ni == null ? null : ni.getChildrenByTagName(ElementName.JMF, null, null, false, false, -1, false);
			if (vJMF != null)
			{
				for (KElement jmf : vJMF)
				{
					jmf.deleteNode();
				}
			}
		}
	}

	/**
	 * @param m
	 * @param resp
	 * @param doc 
	 * @return true if all went well
	 */
	protected boolean returnFromSlave(final JDFMessage m, final JDFResponse resp, JDFDoc doc)
	{
		final JDFReturnQueueEntryParams retQEParams = m.getReturnQueueEntryParams(0);

		// get the returned JDFDoc from the incoming ReturnQE command and pack it in the outgoing
		JDFNode root = doc == null ? null : doc.getJDFRoot();
		if (root == null)
		{
			final String errorMsg = "failed to parse the JDFDoc from the incoming " + "ReturnQueueEntry with QueueEntryID=" + currentQE.getQueueEntryID();
			JMFHandler.errorResponse(resp, errorMsg, 2, EnumClass.Error);
		}
		else if (currentQE != null)
		{
			// brutally overwrite the current node with this
			currentQE.setJDF(root);
			final String docFile = getParent().getJDFStorage(currentQE.getQueueEntryID());
			if (docFile != null)
				root.getOwnerDocument_JDFElement().write2File(docFile, 2, true);
			_statusListener.replaceNode(root);
		}
		else
		{
			final JDFQueueEntry qeBambi = getParent().getQueueProcessor().getQueueEntry(retQEParams.getQueueEntryID(), null);
			if (qeBambi != null)
			{
				final String docFile = getParent().getJDFStorage(qeBambi.getQueueEntryID());
				if (docFile != null)
				{
					root.getOwnerDocument_JDFElement().write2File(docFile, 2, true);
				}
			}
		}

		final VString aborted = retQEParams.getAborted();
		final VString completed = retQEParams.getCompleted();
		EnumQueueEntryStatus finalStatus;
		if (aborted != null && aborted.size() != 0)
		{
			finalStatus = EnumQueueEntryStatus.Aborted;
		}
		else if (completed != null && completed.size() != 0)
		{
			finalStatus = EnumQueueEntryStatus.Completed;
		}
		else
		{
			finalStatus = root == null ? EnumQueueEntryStatus.Aborted : EnumNodeStatus.getQueueEntryStatus(root.getPartStatus(null, -1));
			if (finalStatus == null)
			{
				finalStatus = EnumQueueEntryStatus.Aborted;
			}
		}
		finalizeProcessDoc(finalStatus);
		return true;
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
			log.warn("null queueentryID");
			return null;
		}
		final String slaveID = getSlaveQEID(queueEntryID);
		if (slaveID == null)
		{
			log.warn("no matching queueentryID");
			return null;
		}
		int iRet = new QueueSubmitter(getParent().getProperties().getSlaveURL()).new QueueResubmitter(jdf, slaveID, queueEntryID).resubmit();
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
