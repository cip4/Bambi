/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2024 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.core.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

import javax.mail.Multipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.MessageSender.SendReturn;
import org.cip4.jdflib.auto.JDFAutoSignal.EnumChannelMode;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.extensions.XJDF20;
import org.cip4.jdflib.extensions.XJDFHelper;
import org.cip4.jdflib.extensions.XJDFZipWriter;
import org.cip4.jdflib.extensions.XJMFHelper;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResourceInfo;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFMilestone;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.EnumUtil;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.net.HTTPDetails;

/**
 * MessageDetails describes one jmf or mime package that is queued for a given url
 *
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 *
 *         before May 26, 2009
 */
public class MessageDetails
{
	private static final String CB_DETAILS = "CBDetails";
	private static final String CALLBACK_CLASS = "CallbackClass";
	private static final String MESSAGE = "Message";
	static final String HTTP = "HTTP";
	static final String MIME = "Mime";
	static final private Log log = LogFactory.getLog(MessageDetails.class);
	final protected JDFJMF jmf;
	protected JDFNode jdf;
	protected IResponseHandler respHandler;
	protected MIMEDetails mimeDet;
	final protected String senderID;
	final protected String url;
	protected IConverterCallback callback;
	final protected long createTime;
	protected boolean fireForget;

	private MessageSender.SendReturn sendReturn;
	private static boolean allSignalsReliable = false;
	final private String name;

	/**
	 * constructor for a single jmf message
	 *
	 * @param _jmf the jmf to send
	 * @param _respHandler the response handler to handle the response after the message is queued
	 * @param _callback the callback to apply to the message prior to sending it
	 * @param hdet the http details
	 * @param detailedURL the complete, fully expanded url to send to
	 */
	protected MessageDetails(final JDFJMF _jmf, final IResponseHandler _respHandler, final IConverterCallback _callback, final HTTPDetails hdet, final String detailedURL)
	{
		jmf = _jmf;
		jdf = null;
		final JDFMessage mess = jmf == null ? null : jmf.getMessageElement(null, null, 0);
		name = getMessageName(mess);
		senderID = jmf == null ? null : jmf.getSenderID();
		url = detailedURL;
		createTime = System.currentTimeMillis();
		sendReturn = null;
		setRespHandler(_respHandler, _callback);
		if (hdet == null)
		{
			mimeDet = null;
		}
		else
		{
			mimeDet = new MIMEDetails();
			mimeDet.httpDetails = hdet;
		}
		fireForget = true;
		if (jmf != null)
		{
			if (allSignalsReliable)
			{
				fireForget = jmf.getSignal(0) == null;
			}
			else
			{
				fireForget = jmf.getChildWithAttribute("Signal", "ChannelMode", null, EnumChannelMode.Reliable.getName(), 0, true) == null;
			}
		}
	}

	/**
	 *
	 * @param jmf
	 * @param jdf
	 * @param _respHandler the response handler to handle the response after the message is queued
	 * @param _callback the callback to apply to the message prior to sending it
	 * @param mdet the http and mime details
	 * @param _url the complete, fully expanded url to send to
	 */
	protected MessageDetails(final JDFJMF jmf, final JDFNode jdf, final IResponseHandler _respHandler, final IConverterCallback _callback, final MIMEDetails mdet,
			final String _url)
	{
		this(jmf, _respHandler, _callback, null, _url);
		mimeDet = mdet;
		this.jdf = jdf;
	}

	/**
	 * @param _respHandler
	 * @param _callback
	 */
	private void setRespHandler(final IResponseHandler _respHandler, final IConverterCallback _callback)
	{
		respHandler = _respHandler;
		callback = _callback;
		if (respHandler != null && _callback != null)
		{
			respHandler.setCallBack(_callback);
		}
	}

	/**
	 * constructor when deserializing from a file <br/>
	 * note that the handlers are NOT reconstructed at startup - some synchronization may be lost
	 *
	 * @param element the serialized representation
	 */
	public MessageDetails(final KElement element)
	{
		url = element.getAttribute(AttributeName.URL, null, null);
		senderID = element.getAttribute(AttributeName.SENDERID, null, null);
		fireForget = element.getBoolAttribute("FireForget", null, true);
		long t0;
		try
		{
			t0 = new JDFDate(element.getAttribute(AttributeName.TIMESTAMP)).getTimeInMillis();
		}
		catch (final DataFormatException e)
		{
			t0 = System.currentTimeMillis();
		}
		if (t0 < 10000)
		{
			t0 = System.currentTimeMillis();
		}

		createTime = t0;
		final String cbClass = element.getAttribute(CALLBACK_CLASS, null, null);
		if (cbClass != null)
		{
			try
			{
				final Class<?> c = Class.forName(cbClass);
				callback = (IConverterCallback) c.newInstance();
				final KElement cbd = element.getElement(CB_DETAILS);
				final JDFAttributeMap m = cbd == null ? null : cbd.getAttributeMap();
				callback.setCallbackDetails(m);
			}
			catch (final Throwable x)
			{
				log.warn("Illegal callback class - limp along with null: " + cbClass);// nop
			}
		}
		final KElement jmf1 = element.getElement(ElementName.JMF);
		// must clone the root
		jmf = (JDFJMF) (jmf1 == null ? null : jmf1.cloneNewDoc());
		final KElement jdf1 = element.getElement(ElementName.JDF);
		jdf = (JDFNode) (jdf1 == null ? null : jdf1.cloneNewDoc());
		final JDFMessage mess = jmf == null ? null : jmf.getMessageElement(null, null, 0);
		name = getMessageName(mess);

		final KElement mime = element.getElement(MIME);
		if (mime != null)
		{
			mimeDet = new MIMEDetails();
			mimeDet.transferEncoding = mime.getNonEmpty(AttributeName.ENCODING);
			final KElement httpDet = mime.getElement(HTTP);
			if (httpDet != null)
			{
				mimeDet.httpDetails = new HTTPDetails();
				final JDFAttributeMap map = httpDet.getAttributeMap();
				for (final String key : map.keySet())
				{
					mimeDet.httpDetails.setHeader(key, map.get(key));
				}
			}
		}
		else
		{
			mimeDet = null;
		}
	}

	/**
	 *
	 * @param mess
	 * @return
	 */
	String getMessageName(final JDFMessage mess)
	{
		String nam = mess == null ? "Null_JMF" : mess.getType();
		if (mess instanceof JDFSignal)
		{
			if (ElementName.NOTIFICATION.equals(nam))
			{
				final JDFNotification not = ((JDFSignal) mess).getNotification();
				final JDFMilestone ms = not == null ? null : not.getMilestone();
				final String msName = ms == null ? null : ms.getMilestoneType();
				if (!StringUtil.isEmpty(msName))
				{
					nam = "Milestone_" + msName;
				}
			}
			else if (ElementName.RESOURCE.equals(nam))
			{
				final JDFResourceInfo ri = mess.getResourceInfo(0);
				if (ri != null)
				{
					nam += "_" + ri.getResourceName();
				}
			}
			final String sender = mess.getSenderID();
			if (!StringUtil.isEmpty(sender))
			{
				nam += "." + sender;
			}
		}
		return nam;
	}

	/**
	 * method to display this as XML - used in the web UI
	 *
	 * @param messageList the parent list
	 * @param i
	 * @param bXJDF
	 */
	void appendToXML(final KElement messageList, final int i, final boolean bXJDF)
	{
		final KElement message = messageList.appendElement(MESSAGE);
		message.setAttribute(AttributeName.NAME, name);
		message.setAttribute(AttributeName.URL, url);
		message.setAttribute(AttributeName.SENDERID, senderID);
		message.setAttribute("FireForget", fireForget, null);
		message.setAttribute("Return", sendReturn == null ? "unsent" : sendReturn.name(), null);
		final JDFDate d = new JDFDate(createTime);
		message.setAttribute(AttributeName.TIMESTAMP, d.getDateTimeISO());
		appendMimeDetails(message);
		if (i >= 0)
		{
			if (callback != null)
			{
				message.setAttribute(CALLBACK_CLASS, callback.getClass().getCanonicalName());
				final JDFAttributeMap callbackDetails = callback.getCallbackDetails();
				if (!JDFAttributeMap.isEmpty(callbackDetails))
				{
					message.appendElement(CB_DETAILS).setAttributes(callbackDetails);
				}
			}
			if (jmf != null)
			{
				final KElement makeNewJMF = bXJDF ? displayJMF(jmf) : jmf;
				message.copyElement(makeNewJMF, null);
			}
			if (jdf != null)
			{
				message.copyElement(jdf, null);
			}
		}
		else
		{
			message.setAttribute(AttributeName.TIMESTAMP, d.getDateTimeISO());
		}
	}

	void appendMimeDetails(final KElement message)
	{
		if (mimeDet != null)
		{
			final KElement mime = message.appendElement(MIME);
			mime.setNonEmpty(AttributeName.ENCODING, mimeDet.transferEncoding);
			appendHttpDetails(mime);

		}

	}

	void appendHttpDetails(final KElement mime)
	{
		final HTTPDetails hd = mimeDet == null ? null : mimeDet.httpDetails;
		if (hd != null)
		{
			final VString headers = hd.getHeaders();
			if (!VString.isEmpty(headers))
			{
				final KElement http = mime.appendElement(HTTP);
				for (final String header : headers)
				{
					http.setNonEmpty(header, hd.getHeader(header));
				}
			}
		}
	}

	/**
	 * @param _jmf
	 * @return
	 */
	private KElement displayJMF(final JDFJMF _jmf)
	{
		final XJDF20 xjdf20 = new XJDF20();
		xjdf20.setUpdateVersion(false);
		final KElement makeNewJMF = xjdf20.makeNewJMF(_jmf);
		return makeNewJMF;
	}

	/**
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String ret = "MessageDetails: from: " + senderID + " to: " + url;
		final JDFMessage m = jmf == null ? null : jmf.getMessageElement(null, null, 0);
		if (m != null)
		{
			ret += " Message Type=" + m.getType();
		}
		if (jdf != null)
		{
			ret += "Package";
		}

		return ret;
	}

	/**
	 * Setter for allSignalsReliable attribute., if true, all signals are assumed fire and forget
	 *
	 * @param allSignalsReliable the allSignalsFireForget to set
	 */
	public static void setAllSignalsReliable(final boolean allSignalsReliable)
	{
		MessageDetails.allSignalsReliable = allSignalsReliable;
	}

	/**
	 *
	 *
	 * @return
	 */
	public boolean isFireForget()
	{
		return fireForget;
	}

	/**
	 *
	 * @param fireForget
	 */
	public void setFireForget(final boolean fireForget)
	{
		this.fireForget = fireForget;
	}

	/**
	 *
	 * @param sendReturn
	 */
	public void setReturn(final SendReturn sendReturn)
	{
		this.sendReturn = sendReturn;

	}

	/**
	 *
	 * @return
	 */
	public MessageSender.SendReturn getReturn()
	{
		return sendReturn;
	}

	/**
	 *
	 * get the name
	 *
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 *
	 * @return
	 */
	public String getContentType()
	{
		if (jdf != null)
		{
			EnumVersion maxVersion = jdf.getMaxVersion(true);
			final EnumVersion maxVersion2 = EnumVersion.getEnum(BambiNSExtension.getMyNSAttribute(jdf, AttributeName.MAXVERSION));
			maxVersion = (EnumVersion) EnumUtil.max(maxVersion, maxVersion2);
			if (callback != null && UrlUtil.VND_XJDF.equals(callback.getJDFContentType(maxVersion, callback.isJSON() || BambiNSExtension.isJSON(jdf)))
					&& UrlUtil.VND_XJMF.equals(callback.getJMFContentType(maxVersion, false)))
			{
				return UrlUtil.APPLICATION_ZIP;
			}
			else
			{
				return MimeUtil.MULTIPART_RELATED;
			}
		}
		else if (jmf != null)
		{
			return callback == null ? UrlUtil.VND_JMF : callback.getJMFContentType(jmf.getMaxVersion(), callback.isJSON() || BambiNSExtension.isJSON(jmf));
		}
		else
		{
			return null;
		}
	}

	public InputStream getInputStream()
	{
		try
		{
			final String contentType = getContentType();
			if (UrlUtil.APPLICATION_ZIP.equals(contentType))
			{
				return getZipStream();
			}
			else if (MimeUtil.MULTIPART_RELATED.equals(contentType))
			{
				return getMimeInputStream();
			}
			else if (jmf != null)
			{
				return getJMFInputStream();
			}
			else
			{
				log.warn("Unknown content type for null jmf " + contentType + (jdf == null ? " no jdf " : jdf.getNodeName()));
			}
		}
		catch (final IOException e)
		{
			log.error("error writing stream ", e);
		}
		return null;
	}

	InputStream getJMFInputStream()
	{
		if (callback == null)
		{
			final ByteArrayIOStream bos = new ByteArrayIOStream();
			jmf.write2Stream(bos);
			return bos.getInputStream();
		}
		else
		{
			return callback.getJMFExternStream(jmf.getOwnerDocument_JDFElement().clone());
		}
	}

	/**
	 *
	 * @return
	 */
	InputStream getMimeInputStream()
	{
		final ByteArrayIOStream bos = new ByteArrayIOStream();
		final JDFDoc docJMF;
		final JDFDoc docJDF;
		final boolean extendReferenced;
		if (callback != null)
		{
			docJMF = callback.updateJMFForExtern(jmf.getOwnerDocument_JDFElement().clone());
			docJDF = callback.updateJDFForExtern(jdf.getOwnerDocument_JDFElement().clone());
			extendReferenced = callback.isExtendReferenced();
		}
		else
		{
			docJDF = jdf.getOwnerDocument_JDFElement();
			docJMF = jmf.getOwnerDocument_JDFElement();
			extendReferenced = false;
		}
		// we don't want to retain any nasty characters
		docJDF.setOriginalFileName(null);
		final Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF, extendReferenced);
		try
		{
			MimeUtil.writeToStream(mp, bos, mimeDet);
		}
		catch (final Exception e)
		{
			log.error("Problems writing to stream", e);
			return null;
		}
		return bos.getInputStream();
	}

	InputStream getZipStream() throws IOException
	{
		final ByteArrayIOStream bos = new ByteArrayIOStream();
		final KElement xmlJMF;
		final KElement xmlJDF;
		if (callback != null)
		{
			final XMLDoc d2 = callback.updateJMFForExtern(jmf.getOwnerDocument_JDFElement().clone());
			xmlJMF = d2.getRoot();
			final XMLDoc d3 = callback.updateJDFForExtern(jdf.getOwnerDocument_JDFElement().clone());
			xmlJDF = d3.getRoot();
		}
		else
		{
			xmlJDF = jdf;
			xmlJMF = jmf;
		}
		final JDFMessage msg = jmf.getMessage(0);
		final XJDFZipWriter zw = new XJDFZipWriter();
		zw.setCommandType(msg == null ? null : msg.getEnumType());
		zw.setXjmf(new XJMFHelper(xmlJMF));
		zw.addXJDF(new XJDFHelper(xmlJDF));
		zw.writeStream(bos);

		return bos.getInputStream();
	}

}