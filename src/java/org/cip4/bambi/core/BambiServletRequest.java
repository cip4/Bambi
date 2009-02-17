/**
 * 
 */
package org.cip4.bambi.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/*
 *
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
 */
public class BambiServletRequest implements HttpServletRequest
{
	private final HttpServletRequest parent;
	private ByteArrayIOStream buffer;

	/**
	 * @param _parent the underlying HttpServletRequest
	 * @param bufIt if true, the underlying streams are buffered and can be read multile times
	 */
	public BambiServletRequest(final HttpServletRequest _parent, final boolean bufIt)
	{
		parent = _parent;
		if (bufIt)
		{
			buffer = new ByteArrayIOStream();
			try
			{
				IOUtils.copy(parent.getInputStream(), buffer);
			}
			catch (final IOException x)
			{
				// nop - keep what we have
			}
		}
		else
		{
			buffer = null;
		}
	}

	/**
	 * get the deviceID for this request
	 * @return the deviceID
	 */
	public String getDeviceID()
	{
		String deviceID = getParameter("id");
		if (deviceID == null)
		{
			deviceID = getPathInfo();
			deviceID = getDeviceIDFromURL(deviceID);
		}
		return deviceID;
	}

	/**
	 * @param context
	 * @return true if the context fits
	 */
	public boolean isMyContext(final String context)
	{
		if (context == null)
		{
			return true;
		}

		final String reqContext = getContext();
		return context.equals(StringUtil.token(reqContext, 0, "/"));

	}

	/**
	 * check whether this request is for deviceID
	 * @param deviceID the deviceID to check
	 * @return true if mine
	 */
	public boolean isMyRequest(final String deviceID)
	{
		if (deviceID == null)
		{
			return true;
		}
		final String reqDeviceID = getDeviceID();
		return reqDeviceID == null || deviceID.equals(reqDeviceID);
	}

	/**
	 * get the static context string
	 * @return the context string
	 */
	public String getContext()
	{
		String context = getParameter("cmd");
		if (context == null)
		{
			context = UrlUtil.getLocalURL(getContextPath(), getRequestURI());
		}
		return context;
	}

	/**
	 * @param url
	 * @return the deviceID
	 */
	public static String getDeviceIDFromURL(String url)
	{
		url = StringUtil.token(url, -1, "/");
		return url;
	}

	/**
	 * extract a boolean attribute from a given request
	 * @param param
	 * @return true if the parameter is"true", else false
	 */
	public boolean getBooleanParam(final String param)
	{
		final String val = getParameter(param);
		return StringUtil.parseBoolean(val, false);
	}

	/**
	 * extract an integer attribute from a given request
	 * @param param
	 * @return the integer parameter
	 */
	public int getIntegerParam(final String param)
	{
		final String val = getParameter(param);
		return StringUtil.parseInt(val, 0);
	}

	/**
	 * extract a double attribute from a given request
	 * @param param
	 * @return the double parameter
	 */
	public double getDoubleParam(final String param)
	{
		final String val = getParameter(param);
		return StringUtil.parseDouble(val, 0.0);
	}

	public Object getAttribute(final String arg0)
	{
		return parent.getAttribute(arg0);
	}

	public Enumeration getAttributeNames()
	{
		return parent.getAttributeNames();
	}

	public String getAuthType()
	{
		return parent.getAuthType();
	}

	public String getCharacterEncoding()
	{
		return parent.getCharacterEncoding();
	}

	public int getContentLength()
	{
		return parent.getContentLength();
	}

	public String getContentType()
	{
		return parent.getContentType();
	}

	public String getContextPath()
	{
		return parent.getContextPath();
	}

	public String getContextRoot()
	{
		final String s = parent.getContextPath();
		return "/" + StringUtil.token(s, 0, "/");
	}

	public Cookie[] getCookies()
	{
		return parent.getCookies();
	}

	public long getDateHeader(final String arg0)
	{
		return parent.getDateHeader(arg0);
	}

	public String getHeader(final String arg0)
	{
		return parent.getHeader(arg0);
	}

	public Enumeration getHeaderNames()
	{
		return parent.getHeaderNames();
	}

	public Enumeration getHeaders(final String arg0)
	{
		return parent.getHeaders(arg0);
	}

	public ServletInputStream getInputStream() throws IOException
	{
		return parent.getInputStream();
	}

	/**
	 * flush the buffer an dull it
	 */
	public void flush()
	{
		buffer = null;
	}

	/**
	 * get the buffered input stream
	 * @return the buffered stream, null if this is not buffered and the inputstream throws an exception
	 */
	public InputStream getBufferedInputStream()
	{
		try
		{
			return buffer != null ? buffer.getInputStream() : parent.getInputStream();
		}
		catch (final Exception e)
		{
			return null;
		}
	}

	public int getIntHeader(final String arg0)
	{
		return parent.getIntHeader(arg0);
	}

	public String getLocalAddr()
	{
		return parent.getLocalAddr();
	}

	public Locale getLocale()
	{
		return parent.getLocale();
	}

	public Enumeration getLocales()
	{
		return parent.getLocales();
	}

	public String getLocalName()
	{
		return parent.getLocalName();
	}

	public int getLocalPort()
	{
		return parent.getLocalPort();
	}

	public String getMethod()
	{
		return parent.getMethod();
	}

	public String getParameter(final String arg0)
	{
		return parent.getParameter(arg0);
	}

	public Map getParameterMap()
	{
		return parent.getParameterMap();
	}

	public Enumeration getParameterNames()
	{
		return parent.getParameterNames();
	}

	public String[] getParameterValues(final String arg0)
	{
		return parent.getParameterValues(arg0);
	}

	public String getPathInfo()
	{
		return parent.getPathInfo();
	}

	public String getPathTranslated()
	{
		return parent.getPathTranslated();
	}

	public String getProtocol()
	{
		return parent.getProtocol();
	}

	public String getQueryString()
	{
		return parent.getQueryString();
	}

	public BufferedReader getReader() throws IOException
	{
		return parent.getReader();
	}

	@Deprecated
	public String getRealPath(final String arg0)
	{
		return parent.getRealPath(arg0);
	}

	public String getRemoteAddr()
	{
		return parent.getRemoteAddr();
	}

	public String getRemoteHost()
	{
		return parent.getRemoteHost();
	}

	public int getRemotePort()
	{
		return parent.getRemotePort();
	}

	public String getRemoteUser()
	{
		return parent.getRemoteUser();
	}

	public RequestDispatcher getRequestDispatcher(final String arg0)
	{
		return parent.getRequestDispatcher(arg0);
	}

	public String getRequestedSessionId()
	{
		return parent.getRequestedSessionId();
	}

	public String getRequestURI()
	{
		return parent.getRequestURI();
	}

	public StringBuffer getRequestURL()
	{
		return parent.getRequestURL();
	}

	public String getScheme()
	{
		return parent.getScheme();
	}

	public String getServerName()
	{
		return parent.getServerName();
	}

	public int getServerPort()
	{
		return parent.getServerPort();
	}

	public String getServletPath()
	{
		return parent.getServletPath();
	}

	public HttpSession getSession()
	{
		return parent.getSession();
	}

	public HttpSession getSession(final boolean arg0)
	{
		return parent.getSession(arg0);
	}

	public Principal getUserPrincipal()
	{
		return parent.getUserPrincipal();
	}

	public boolean isRequestedSessionIdFromCookie()
	{
		return parent.isRequestedSessionIdFromCookie();
	}

	/**
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	@Deprecated
	public boolean isRequestedSessionIdFromUrl()
	{
		return parent.isRequestedSessionIdFromUrl();
	}

	/**
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL()
	{
		return parent.isRequestedSessionIdFromURL();
	}

	/**
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid()
	{
		return parent.isRequestedSessionIdValid();
	}

	/**
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure()
	{
		return parent.isSecure();
	}

	/**
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(final String arg0)
	{
		return parent.isUserInRole(arg0);
	}

	/**
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(final String arg0)
	{
		parent.removeAttribute(arg0);
	}

	/**
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(final String arg0, final Object arg1)
	{
		parent.setAttribute(arg0, arg1);
	}

	/**
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(final String arg0) throws UnsupportedEncodingException
	{
		parent.setCharacterEncoding(arg0);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "BambiServletRequest: \nURI=" + getCompleteRequestURL();
	}

	/**
	 * @return the complete request URL including parameters
	 */
	public String getCompleteRequestURL()
	{
		String details = getRequestURL().toString();
		final String q = getQueryString();
		if (StringUtil.getNonEmpty(q) != null)
		{
			details += "?" + q;
		}
		return details;
	}

}
