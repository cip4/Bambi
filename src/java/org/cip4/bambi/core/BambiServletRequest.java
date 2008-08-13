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
public class BambiServletRequest implements HttpServletRequest
{
	private final HttpServletRequest parent;
	private ByteArrayIOStream buffer;

	/**
	 * 
	 * @throws IOException 
	 * 
	 */
	public BambiServletRequest(HttpServletRequest _parent, boolean bufIt)
	{
		parent = _parent;
		if (bufIt)
		{
			buffer = new ByteArrayIOStream();
			try
			{
				IOUtils.copy(parent.getInputStream(), buffer);
			}
			catch (IOException x)
			{
				// nop - keep what we have
			}
		}
		else
			buffer = null;
	}

	/**
	 * extract a boolean attribute from a given request
	 * @param request
	 * @param param
	 * @return true if the parameter is"true", else false
	 */
	public boolean getBooleanParam(String param)
	{
		final String val = getParameter(param);
		return StringUtil.parseBoolean(val, false);
	}

	/**
	 * extract a double attribute from a given request
	 * @param request
	 * @param param
	 * @return
	 */
	public int getIntegerParam(String param)
	{
		final String val = getParameter(param);
		return StringUtil.parseInt(val, 0);
	}

	/**
	 * extract a double attribute from a given request
	 * @param request
	 * @param param
	 * @return
	 */
	public double getDoubleParam(String param)
	{
		final String val = getParameter(param);
		return StringUtil.parseDouble(val, 0.0);
	}

	public Object getAttribute(String arg0)
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
		String s = parent.getContextPath();
		return "/" + StringUtil.token(s, 0, "/");
	}

	public Cookie[] getCookies()
	{
		return parent.getCookies();
	}

	public long getDateHeader(String arg0)
	{
		return parent.getDateHeader(arg0);
	}

	public String getHeader(String arg0)
	{
		return parent.getHeader(arg0);
	}

	public Enumeration getHeaderNames()
	{
		return parent.getHeaderNames();
	}

	public Enumeration getHeaders(String arg0)
	{
		return parent.getHeaders(arg0);
	}

	public ServletInputStream getInputStream() throws IOException
	{
		return parent.getInputStream();
	}

	public InputStream getBufferedInputStream()
	{
		try
		{
			return buffer != null ? buffer.getInputStream() : parent.getInputStream();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public int getIntHeader(String arg0)
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

	public String getParameter(String arg0)
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

	public String[] getParameterValues(String arg0)
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
	public String getRealPath(String arg0)
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

	public RequestDispatcher getRequestDispatcher(String arg0)
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

	public HttpSession getSession(boolean arg0)
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

	@Deprecated
	public boolean isRequestedSessionIdFromUrl()
	{
		return parent.isRequestedSessionIdFromUrl();
	}

	public boolean isRequestedSessionIdFromURL()
	{
		return parent.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdValid()
	{
		return parent.isRequestedSessionIdValid();
	}

	public boolean isSecure()
	{
		return parent.isSecure();
	}

	public boolean isUserInRole(String arg0)
	{
		return parent.isUserInRole(arg0);
	}

	public void removeAttribute(String arg0)
	{
		parent.removeAttribute(arg0);
	}

	public void setAttribute(String arg0, Object arg1)
	{
		parent.setAttribute(arg0, arg1);
	}

	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException
	{
		parent.setCharacterEncoding(arg0);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "BambiServletRequest: \nURI=" + getRequestURI();
	}

}
