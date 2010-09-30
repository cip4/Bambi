package org.cip4.bambi.core.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.ThreadUtil.MyMutex;

/**
 * trivial response handler that simply grabs the response and passes it back through getResponse() / isHandled()
 * 
 * @author Rainer Prosi
 * 
 */
public class MessageResponseHandler extends BambiLogFactory implements IResponseHandler
{
	protected JDFResponse resp;
	protected JDFMessage finalMessage;
	private HttpURLConnection connect;
	protected ByteArrayIOStream bufferedInput;
	private MyMutex mutex = new MyMutex();
	private int abort = 0; // 0 no abort handling, 1= abort on timeout, 2= has been aborted
	protected String refID;
	private IConverterCallback callBack = null;
	private final long startTime;

	/**
	 * @return the callBack
	 */
	public IConverterCallback getCallBack()
	{
		return callBack;
	}

	/**
	 * @param _callBack the callBack to set
	 */
	public void setCallBack(final IConverterCallback _callBack)
	{
		this.callBack = _callBack;
	}

	/**
	 * @param _refID the ID of the sent message
	 * 
	 */
	public MessageResponseHandler(final String _refID)
	{
		super();
		refID = _refID;
		resp = null;
		finalMessage = null;
		connect = null;
		bufferedInput = null;
		startTime = System.currentTimeMillis();
	}

	/**
	 * @param jmf
	 */
	public MessageResponseHandler(JDFJMF jmf)
	{
		this(jmf.getMessageElement(null, null, 0).getID());
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#handleMessage()
	 * @return true if handled, even if not finalized
	 */
	public boolean handleMessage()
	{
		if (finalMessage == null)
		{
			if (bufferedInput != null)
			{
				JDFDoc d = MimeUtil.getJDFDoc(bufferedInput.getInputStream(), 0);
				if (callBack != null && d != null)
				{
					log.info("preparing jmf response");
					d = callBack.prepareJMFForBambi(d);
				}
				if (d != null)
				{
					final JDFJMF jmf = d.getJMFRoot();
					if (jmf != null)
					{
						resp = jmf.getResponse(refID);
						if (resp == null)
						{
							VElement messageVector = jmf.getMessageVector(EnumFamily.Response, null);
							if (messageVector != null && messageVector.size() == 1)
							{
								resp = (JDFResponse) messageVector.get(0);
								if (StringUtil.getNonEmpty(resp.getrefID()) == null)
								{
									log.warn("Response with missing refID - guess that only one is it: " + refID);
									resp.setrefID(refID);
								}
							}
						}
						if (resp != null)
						{
							if (checkAcknowledge())
							{
								return true;
							}
						}
						else
						{
							finalMessage = jmf.getAcknowledge(refID);
						}
					}
				}
			}
			else if (resp != null && checkAcknowledge())
			{
				return true;
			}
		}
		finalizeHandling();
		return true;
	}

	/**
	 *  
	 * @return 
	 */
	private boolean checkAcknowledge()
	{
		final JDFResponse r = resp;
		final boolean isAcknowledgeResponse = r.getAcknowledged();
		if (isAcknowledgeResponse) // must wait for an acknowledge
		{
			String refIDMes = StringUtil.getNonEmpty(r.getrefID());
			if (refIDMes == null)
			{
				refIDMes = refID;
			}
			final AcknowledgeMap aMap = AcknowledgeMap.getMap();
			aMap.addHandler(refIDMes, this);
			return true;
		}
		else
		// the response is "the" final response
		{
			finalMessage = resp;
		}
		return false;
	}

	/**
	 * 
	 */
	protected void finalizeHandling()
	{
		if (mutex == null)
		{
			return;
		}
		abort = 0;
		ThreadUtil.notifyAll(mutex);
		mutex = null;
		if (resp != null)
		{
			String rID = resp.getAttribute(AttributeName.REFID, null, null);
			if (refID == null)
			{
				rID = refID;
			}
			if (rID != null)
			{
				final AcknowledgeMap aMap = AcknowledgeMap.getMap();
				aMap.removeHandler(rID);
			}
		}
	}

	/**
	 * @return the Acknowledge or Response that was handled
	 */
	public JDFMessage getFinalMessage()
	{
		return finalMessage;
	}

	/**
	 * @param message the Acknowledge or Response that was handled
	 */
	public void setMessage(final JDFMessage message)
	{
		finalMessage = message;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#getConnection()
	 */
	public HttpURLConnection getConnection()
	{
		return connect;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#setConnection(java.net.HttpURLConnection)
	 */
	public void setConnection(final HttpURLConnection uc)
	{
		connect = uc;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#setBufferedStream(org.cip4.jdflib.util.ByteArrayIOStream)
	 */
	public void setBufferedStream(final ByteArrayIOStream bis)
	{
		bufferedInput = bis;
	}

	/**
	 * @return the buffered input stream, may also be null in case of snafu
	 */
	public InputStream getBufferedStream()
	{
		if (bufferedInput != null)
		{
			return bufferedInput.getInputStream();
		}
		if (connect == null)
		{
			return null;
		}
		try
		{
			final InputStream inputStream = connect.getInputStream();
			bufferedInput = new ByteArrayIOStream(inputStream);
			inputStream.close();
		}
		catch (final IOException x)
		{
			// nop
		}
		return bufferedInput.getInputStream();
	}

	/**
	 * @param wait1 milliseconds to wait for a connection
	 * @param wait2 milliseconds to wait for the response after the connection has been established
	 * 
	 * @param bAbort if true, abort handling after timeout
	 */
	public void waitHandled(final int wait1, final int wait2, final boolean bAbort)
	{
		if (mutex == null)
		{
			return;
		}
		abort = bAbort ? 1 : 0;
		ThreadUtil.wait(mutex, wait1);
		if (mutex != null && connect != null && wait2 >= 0) // we have established a connection but have not yet read anything
		{
			ThreadUtil.wait(mutex, wait2);
		}
		if (abort == 1)
		{
			abort++;
		}
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#isAborted()
	 */
	public boolean isAborted()
	{
		final long t = System.currentTimeMillis();
		if (t - startTime > 1000 * 24 * 60 * 60)
		{
			return true;
		}
		return mutex == null ? false : abort == 2;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#getResponse()
	 */
	public JDFResponse getResponse()
	{
		return resp;
	}

	/**
	 * return the jmf message's response code -1 if no response was received
	 * @return 
	 */
	public int getJMFReturnCode()
	{
		return finalMessage == null ? -1 : finalMessage.getReturnCode();
	}
}