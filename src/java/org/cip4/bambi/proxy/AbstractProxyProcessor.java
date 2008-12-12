/**
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
import org.cip4.jdflib.core.VElement;
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
 * @author prosirai
 * 
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

	protected JDFDoc writeToQueue(final JDFDoc docJMF, final JDFDoc docJDF, final String strUrl, final MIMEDetails urlDet, final boolean expandMime, final boolean isMime) throws IOException
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
			JMFFactory.send2URL(mp, strUrl, sqh, slaveCallBack, urlDet, _parent.getDeviceID());
		}
		else
		{
			JMFFactory.send2URL(docJMF.getJMFRoot(), strUrl, sqh, slaveCallBack, _parent.getDeviceID());
		}
		sqh.waitHandled(100000, true);
		if (sqh.doc == null)
		{
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
				r.setErrorText(("Invalid http response - RC=" + responseCode), null);
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
	 */
	protected void submitted(final String slaveQEID, final EnumQueueEntryStatus newStatus, final String slaveURL)
	{
		final JDFQueueEntry qe = currentQE.getQueueEntry();
		BambiNSExtension.setSlaveQueueEntryID(qe, slaveQEID);
		BambiNSExtension.setSlaveSubmissionTime(qe, new JDFDate());
		BambiNSExtension.setDeviceURL(qe, slaveURL);
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

		JDFNode nod = getCloneJDFForSlave();
		final JDFQueueEntry qe = currentQE.getQueueEntry();

		if (slaveCallBack != null)
		{
			if (isMime)
			{
				slaveCallBack.updateJDFForExtern(nod.getOwnerDocument_JDFElement());
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
			nod.getOwnerDocument_JDFElement().write2File((String) null, 0, true);
			jdfURL += "?Callback=true&qeID=" + qe.getQueueEntryID();
			qsp.setURL(jdfURL);
		}
		if (nod != null)
		{
			try
			{
				final String urlString = qurl == null ? null : qurl.toExternalForm();

				JDFDoc d = writeToQueue(jmf.getOwnerDocument_JDFElement(), nod.getOwnerDocument_JDFElement(), urlString, ud, expandMime, isMime);
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
								if (rc == 112)
								{
									return null; // no change
								}
							}
							else
							{
								final JDFQueueEntry qeR = r.getQueueEntry(0);
								if (qeR != null)
								{
									submitted(qeR.getQueueEntryID(), qeR.getQueueEntryStatus(), urlString);
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
				nod = null;
			}
		}
		if (nod == null)
		{
			log.error("submitToQueue - no JDFDoc at: " + BambiNSExtension.getDocURL(qe));
			_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
		}
		return new QueueEntry(nod, qe);
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
