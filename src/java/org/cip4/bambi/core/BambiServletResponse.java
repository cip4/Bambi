/**
 * 
 */
package org.cip4.bambi.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.cip4.jdflib.util.ByteArrayIOStream;

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
/**
 * buffered servlet request class
 * @author prosirai
 * 
 */
class BambiServletResponse extends BambiLogFactory implements HttpServletResponse
{
	private final HttpServletResponse parent;
	private ByteArrayIOStream buffer;
	private final BambiServletRequest theRequest;

	/**
	 * construct a new {@link BambiServletResponse}
	 * 
	 * @param _parent the original response
	 * @param bBuffer if true, buffer this
	 * @param request the matching request - used mainly for debugging context
	* @deprecated replaced by {@link XMLRequest}
	 */
	@Deprecated
	private BambiServletResponse(final HttpServletResponse _parent, final boolean bBuffer, final BambiServletRequest request)
	{
		parent = _parent;
		theRequest = request;
		if (bBuffer)
		{
			buffer = new ByteArrayIOStream();
		}
		else
		{
			buffer = null;
		}
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(final Cookie arg0)
	{
		parent.addCookie(arg0);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(final String arg0, final long arg1)
	{
		parent.addDateHeader(arg0, arg1);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(final String arg0, final String arg1)
	{
		parent.addHeader(arg0, arg1);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(final String arg0, final int arg1)
	{
		parent.addIntHeader(arg0, arg1);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(final String arg0)
	{
		return parent.containsHeader(arg0);
	}

	/**
	 * 
	 */
	@SuppressWarnings("deprecation")
	public String encodeRedirectUrl(final String arg0)
	{
		return parent.encodeRedirectUrl(arg0);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(final String arg0)
	{
		return parent.encodeRedirectURL(arg0);
	}

	/**
	 * 
	 */
	@SuppressWarnings("deprecation")
	public String encodeUrl(final String arg0)
	{
		return parent.encodeUrl(arg0);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(final String arg0)
	{
		return parent.encodeURL(arg0);
	}

	/**
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException
	{
		parent.flushBuffer();
	}

	/**
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize()
	{
		return parent.getBufferSize();
	}

	/**
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding()
	{
		return parent.getCharacterEncoding();
	}

	/**
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType()
	{
		return parent.getContentType();
	}

	/**
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale()
	{
		return parent.getLocale();
	}

	/**
	 * never access this directly - always use flush finally
	 * @return
	 * @throws NotImplementedException
	 */
	public ServletOutputStream getOutputStream() throws NotImplementedException
	{
		throw new NotImplementedException("Use getBufferedOutputStream");
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public OutputStream getBufferedOutputStream() throws IOException
	{
		return buffer == null ? parent.getOutputStream() : buffer;
	}

	/**
	 * @return
	 */
	public InputStream getBufferedInputStream()
	{
		return buffer == null ? null : buffer.getInputStream();
	}

	/**
	 * @return
	 */
	public int getBufferedCount()
	{
		return buffer == null ? -1 : buffer.size();
	}

	/**
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException
	{
		return parent.getWriter();
	}

	/**
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted()
	{
		return parent.isCommitted();
	}

	/**
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset()
	{
		parent.reset();
	}

	/**
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer()
	{
		parent.resetBuffer();
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(final int arg0, final String arg1) throws IOException
	{
		parent.sendError(arg0, arg1);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(final int arg0) throws IOException
	{
		parent.sendError(arg0);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(final String arg0) throws IOException
	{
		parent.sendRedirect(arg0);
	}

	/**
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(final int arg0)
	{
		parent.setBufferSize(arg0);
	}

	/**
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(final String arg0)
	{
		parent.setCharacterEncoding(arg0);
	}

	/**
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(final int arg0)
	{
		parent.setContentLength(arg0);
	}

	/**
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(final String arg0)
	{
		parent.setContentType(arg0);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(final String arg0, final long arg1)
	{
		parent.setDateHeader(arg0, arg1);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 * @param arg0
	 * @param arg1
	 */
	public void setHeader(final String arg0, final String arg1)
	{
		parent.setHeader(arg0, arg1);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 * @param arg0
	 * @param arg1
	 */
	public void setIntHeader(final String arg0, final int arg1)
	{
		parent.setIntHeader(arg0, arg1);
	}

	/**
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 * @param arg0
	 */
	public void setLocale(final Locale arg0)
	{
		parent.setLocale(arg0);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 * @param arg0
	 * @param arg1
	 * @deprecated
	 */
	@Deprecated
	public void setStatus(final int arg0, final String arg1)
	{
		parent.setStatus(arg0, arg1);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 * @param arg0
	 */
	public void setStatus(final int arg0)
	{
		parent.setStatus(arg0);
	}

	/**
	 * 
	 */
	public void flush()
	{

		if (buffer != null)
		{
			try
			{
				final ServletOutputStream outputStream = parent.getOutputStream();
				IOUtils.copy(buffer.getInputStream(), outputStream);
				outputStream.flush();
				outputStream.close();
				buffer = null;
			}
			catch (final IOException e)
			{
				log.error("Error while flushing response stream to: " + theRequest.getRequestURI(), e);
			}
		}

	}

}
