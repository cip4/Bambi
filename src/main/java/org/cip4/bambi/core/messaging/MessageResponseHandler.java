/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicReference;

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
import org.cip4.jdflib.util.thread.MyMutex;

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
	private final AtomicReference<MyMutex> mutex;
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
	@Override
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
		mutex = new AtomicReference<>(new MyMutex());
	}

	/**
	 * @param jmf
	 */
	public MessageResponseHandler(final JDFJMF jmf)
	{
		this(jmf.getMessageElement(null, null, 0).getID());
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#handleMessage()
	 * @return true if handled, even if not finalized
	 */
	@Override
	public boolean handleMessage()
	{
		if (finalMessage == null)
		{
			if (bufferedInput != null && bufferedInput.size() > 0)
			{
				JDFDoc d = MimeUtil.getJDFDoc(bufferedInput.getInputStream(), 0);
				if (callBack != null && d != null)
				{
					log.debug("preparing jmf response");
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
							final VElement messageVector = jmf.getMessageVector(EnumFamily.Response, null);
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
			log.info("added Acknowledge for: " + refIDMes);
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
		MyMutex myMutex = mutex.get();
		if (myMutex == null)
		{
			return;
		}
		abort = 0;
		ThreadUtil.notifyAll(myMutex);
		mutex.set(null);
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
	@Override
	public JDFMessage getFinalMessage()
	{
		return finalMessage;
	}

	/**
	 * @param message the Acknowledge or Response that was handled
	 */
	@Override
	public void setMessage(final JDFMessage message)
	{
		finalMessage = message;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#getConnection()
	 */
	@Override
	public HttpURLConnection getConnection()
	{
		return connect;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#setConnection(java.net.HttpURLConnection)
	 */
	@Override
	public void setConnection(final HttpURLConnection uc)
	{
		connect = uc;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#setBufferedStream(org.cip4.jdflib.util.ByteArrayIOStream)
	 */
	@Override
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
			return null;
		}
		return bufferedInput.getInputStream();
	}

	/**
	 * @param wait1 milliseconds to wait for a connection
	 * @param wait2 milliseconds to wait for the response after the connection has been established
	 *
	 * @param bAbort if true, abort handling after timeout
	 */
	@Override
	public void waitHandled(final int wait1, final int wait2, final boolean bAbort)
	{
		MyMutex myMutex = mutex.get();
		if (myMutex == null)
		{
			return;
		}
		abort = bAbort ? 1 : 0;
		ThreadUtil.wait(myMutex, wait1);
		if (mutex.get() != null && connect != null && wait2 >= 0) // we have established a connection but have not yet read anything
		{
			ThreadUtil.wait(myMutex, wait2);
		}
		if (abort == 1)
		{
			abort++;
		}
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#isAborted()
	 */
	@Override
	public boolean isAborted()
	{
		final long t = System.currentTimeMillis();
		if (t - startTime > 1000 * 24 * 60 * 60)
		{
			log.error("aborted handler after " + ((t - startTime) * 0.001) + " seconds");
			return true;
		}
		return mutex.get() == null ? false : abort == 2;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IResponseHandler#getResponse()
	 */
	@Override
	public JDFResponse getResponse()
	{
		return resp;
	}

	/**
	 * return the jmf message's response code -1 if no response was received
	 *
	 * @return
	 */
	public int getJMFReturnCode()
	{
		return finalMessage == null ? -1 : finalMessage.getReturnCode();
	}
}