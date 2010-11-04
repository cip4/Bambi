package org.cip4.bambi.core.messaging;

import java.io.File;
import java.util.zip.DataFormatException;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;

import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.MessageSender.SendReturn;
import org.cip4.jdflib.auto.JDFAutoSignal.EnumChannelMode;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.extensions.XJDF20;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.UrlUtil.HTTPDetails;
import org.cip4.jdflib.util.mime.MimeReader;

/**
 * MessageDetails describes one jmf or mime package that is queued for a given url
 * 
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * before May 26, 2009
 */
class MessageDetails extends BambiLogFactory
{
	final protected JDFJMF jmf;
	final protected Multipart mime;
	protected IResponseHandler respHandler;
	final protected MIMEDetails mimeDet;
	final protected String senderID;
	final protected String url;
	protected IConverterCallback callback;
	final protected long createTime;
	protected boolean fireForget;
	private MessageSender.SendReturn sendReturn;
	private static boolean allSignalsReliable = false;

	/**
	 * constructor for a single jmf message
	 * @param _jmf the jmf to send
	 * @param _respHandler the response handler to handle the response after the message is queued
	 * @param _callback the callback to apply to the message prior to sending it
	 * @param hdet the http details
	 * @param detailedURL the complete, fully expanded url to send to
	 */
	protected MessageDetails(final JDFJMF _jmf, final IResponseHandler _respHandler, final IConverterCallback _callback, final HTTPDetails hdet, final String detailedURL)
	{
		jmf = _jmf;
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
		mime = null;
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
	 * @param _mime
	 * @param _respHandler the response handler to handle the response after the message is queued
	 * @param _callback the callback to apply to the message prior to sending it
	 * @param mdet the http and mime details
	 * @param _senderID the senderID of the sender
	 * @param _url the complete, fully expanded url to send to
	 */
	protected MessageDetails(final Multipart _mime, final IResponseHandler _respHandler, final IConverterCallback _callback, final MIMEDetails mdet, final String _senderID, final String _url)
	{
		mime = _mime;
		mimeDet = mdet;
		senderID = _senderID;
		url = _url;
		setRespHandler(_respHandler, _callback);
		jmf = null;
		createTime = System.currentTimeMillis();
		fireForget = true;
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
		catch (DataFormatException e)
		{
			t0 = System.currentTimeMillis();
		}
		createTime = t0;
		final String cbClass = element.getAttribute("CallbackClass", null, null);
		if (cbClass != null)
		{
			try
			{
				final Class<?> c = Class.forName(cbClass);
				callback = (IConverterCallback) c.newInstance();
			}
			catch (final Exception x)
			{
				log.warn("Illegal callback class - limp along with null: " + cbClass);// nop
			}
		}
		final KElement jmf1 = element.getElement(ElementName.JMF);
		// must clone the root
		jmf = (JDFJMF) (jmf1 == null ? null : new JDFDoc(ElementName.JMF).getRoot().copyInto(jmf1, false));
		final String mimeURL = element.getAttribute("MimeUrl", null, null);
		if (mimeURL != null)
		{
			mime = new MimeReader(mimeURL).getMultiPart();
			if (mime != null)
			{
				final File mimFile = UrlUtil.urlToFile(mimeURL);
				final boolean bZapp = mimFile.delete();
				if (!bZapp)
				{
					mimFile.deleteOnExit();
				}
			}
		}
		else
		{
			mime = null;
		}
		final String encoding = element.getAttribute("TransferEncoding", null, null);
		if (encoding != null)
		{
			mimeDet = new MIMEDetails();
			mimeDet.transferEncoding = encoding;
		}
		else
		{
			mimeDet = null;
		}
	}

	/**
	 * method to display this as XML - used in the web UI
	 * 
	 * @param messageList the parent list 
	 * @param i
	 */
	void appendToXML(final KElement messageList, final int i)
	{
		final KElement message = messageList.appendElement("Message");
		message.setAttribute(AttributeName.URL, url);
		message.setAttribute(AttributeName.SENDERID, senderID);
		message.setAttribute("FireForget", fireForget, null);
		message.setAttribute("Return", sendReturn == null ? "unsent" : sendReturn.name(), null);

		final JDFDate d = new JDFDate(createTime);
		message.setAttribute(AttributeName.TIMESTAMP, d.getDateTimeISO());
		if (i >= 0)
		{
			if (callback != null)
			{
				message.setAttribute("CallbackClass", callback.getClass().getCanonicalName());
			}
			if (jmf != null)
			{
				final KElement makeNewJMF = displayJMF(jmf);
				message.copyElement(makeNewJMF, null);
			}
			else
			// mime
			{
				if (mimeDet != null)
				{
					message.setAttribute("TransferEncoding", mimeDet.transferEncoding);
				}
				BodyPart bp = null;
				try
				{
					bp = mime == null ? null : mime.getBodyPart(0);
				}
				catch (final MessagingException e)
				{
					// nop
				}
				final JDFDoc jmfBP = MimeUtil.getJDFDoc(bp);
				final JDFJMF _jmf = jmfBP == null ? null : jmfBP.getJMFRoot();
				final KElement makeNewJMF = displayJMF(_jmf);
				message.copyElement(makeNewJMF, null);
			}
		}
		else
		{
			message.setAttribute(AttributeName.TIMESTAMP, d.getDateTimeISO());
		}
	}

	/**
	 * @param _jmf
	 * @return 
	 */
	private KElement displayJMF(final JDFJMF _jmf)
	{
		final XJDF20 xjdf20 = new XJDF20();
		xjdf20.bUpdateVersion = false;
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
		JDFMessage m = jmf == null ? null : jmf.getMessageElement(null, null, 0);
		if (m != null)
			ret += " Message Type=" + m.getType();
		if (mime != null)
			ret += "Mime";

		return ret;
	}

	/**
	 * Setter for allSignalsReliable attribute., if true, all signals are assumed fire and forget
	 * @param allSignalsReliable the allSignalsFireForget to set
	 */
	public static void setAllSignalsReliable(boolean allSignalsReliable)
	{
		MessageDetails.allSignalsReliable = allSignalsReliable;
	}

	public boolean isFireForget()
	{
		return fireForget;
	}

	public void setFireForget(boolean fireForget)
	{
		this.fireForget = fireForget;
	}

	/**
	 *  
	 * @param sendReturn
	 */
	public void setReturn(SendReturn sendReturn)
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
}