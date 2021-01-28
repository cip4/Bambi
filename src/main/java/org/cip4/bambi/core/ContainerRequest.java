/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2021 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
package org.cip4.bambi.core;

import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.cip4.jdflib.core.JDFConstants;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.UrlUtil.HttpMethod;
import org.cip4.jdflib.util.net.HTTPDetails;

/**
 * class to package an XML document together with the context information of the request
 *
 * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class ContainerRequest extends BambiLogFactory
{
	/**
	 */
	public ContainerRequest()
	{
		super();
		requestURI = null;
		contentType = null;
		headerMap = null;
		parameterMap = null;
		remoteHost = null;
		name = null;
		setPost(true);
	}

	protected void apply(final HttpServletRequest request)
	{
		final String contentType = request.getContentType();
		setContentType(contentType);
		setRequestURI(request.getRequestURL().toString());
		setHeaderMap(getHeaderMap(request));
		setParameterMap(new JDFAttributeMap(getParameterMap(request)));
		setRemoteHost(request.getRemoteHost());
		setMethod(request.getMethod());
	}

	/**
	 *
	 */
	private Map<String, String> getParameterMap(final HttpServletRequest request)
	{
		final Map<String, String[]> pm = request.getParameterMap();
		final Map<String, String> retMap = new JDFAttributeMap();
		final Set<String> keyset = pm.keySet();
		for (final String key : keyset)
		{
			final String[] strings = pm.get(key);
			if (strings != null && strings.length > 0)
			{
				String s = strings[0];
				for (int i = 1; i < strings.length; i++)
				{
					s += JDFConstants.COMMA + strings[i];
				}
				s = StringUtil.getNonEmpty(s);
				if (s != null)
				{
					retMap.put(key, s);
				}
			}
		}
		return retMap.size() == 0 ? null : retMap;
	}

	/**
	 * returns the headers as an attributemap
	 *
	 * @return map of headers, null if no headers exist
	 */
	protected JDFAttributeMap getHeaderMap(final HttpServletRequest request)
	{
		final Enumeration<String> headers = request.getHeaderNames();
		if (!headers.hasMoreElements())
		{
			return null;
		}
		final JDFAttributeMap map = new JDFAttributeMap();
		while (headers.hasMoreElements())
		{
			final String header = headers.nextElement();
			final Enumeration<String> e = request.getHeaders(header);
			final VString v = new VString(e);
			if (v.size() > 0)
			{
				map.put(header, StringUtil.setvString(v, JDFConstants.COMMA, null, null));
			}
		}
		return map.size() == 0 ? null : map;
	}

	/**
	 * get a descriptive name for this
	 *
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 *
	 * set a descriptive name for this
	 *
	 * @param name
	 */
	public void setName(final String name)
	{
		this.name = name;
	}

	private String requestURI;
	private String contentType;
	private String remoteHost;
	protected String name;

	/**
	 * @param remoteHost the remoteHost to set
	 */
	public void setRemoteHost(final String remoteHost)
	{
		this.remoteHost = remoteHost;
	}

	private UrlUtil.HttpMethod method;
	private JDFAttributeMap headerMap;
	private JDFAttributeMap parameterMap;

	/**
	 * get the map of request headers
	 *
	 * @return the headerMap
	 */
	public JDFAttributeMap getHeaderMap()
	{
		return headerMap;
	}

	/**
	 * get the map of request uri parameters
	 *
	 * @return the headerMap
	 */
	public JDFAttributeMap getParameterMap()
	{
		return parameterMap == null ? null : parameterMap.clone();
	}

	/**
	 * @param sr
	 * @return
	 */
	public String getDumpHeader()
	{
		String header = "Context Path: " + getRequestURI();
		header += "\nMethod: " + getMethod() + " Content Type: " + getContentType(false) + " time:" + new JDFDate().getFormattedDateTime(JDFDate.DATETIMEISO_MILLI);
		header += "\nRemote host: " + getRemoteHost();
		return header;
	}

	/**
	 * @param headerMap the headerMap to set
	 */
	public void setHeaderMap(final JDFAttributeMap headerMap)
	{
		this.headerMap = headerMap;
	}

	/**
	 * @param parameterMap the parameterMap to set
	 */
	public void setParameterMap(final JDFAttributeMap parameterMap)
	{
		this.parameterMap = parameterMap == null ? null : parameterMap.clone();
	}

	/**
	 * @param parameterMap the parameterMap to set
	 */
	public void setParameter(final String key, final String value)
	{
		if (parameterMap == null)
		{
			parameterMap = new JDFAttributeMap();
		}
		if (StringUtil.isEmpty(value))
			parameterMap.remove(key);
		else
			parameterMap.put(key, value);
	}

	/**
	 * @return the requestURI
	 */
	public String getRequestURI()
	{
		return requestURI;
	}

	/**
	 * @param requestURI the requestURI to set
	 */
	public void setRequestURI(final String requestURI)
	{
		this.requestURI = requestURI;
	}

	/**
	 * @return
	 */
	public String getDeviceID()
	{
		final String localURL = getLocalURL();
		return ServletContainer.getDeviceIDFromURL(localURL);
	}

	/**
	 * @return the tokenized request
	 *
	 */
	public VString getContextList()
	{
		final String s = getRequestURI();
		final VString v = StringUtil.tokenize(s, "/", false);
		return v;
	}

	/**
	 * @return
	 */
	public String getLocalURL()
	{
		final VString v = getContextList();
		final VString v2 = new VString();
		for (int i = 2; i < v.size(); i++)
			v2.add(v.get(i));
		return StringUtil.setvString(v2, JDFConstants.SLASH, null, null);
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return
	 */
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ((name != null) ? " Name=" + name : JDFConstants.EMPTYSTRING) + " URL=" + requestURI
				+ ((contentType != null) ? " Content Type=" + contentType : JDFConstants.EMPTYSTRING) + " Method=" + getMethod()
				+ ((parameterMap == null || parameterMap.size() == 0) ? "" : " Parameters: {" + parameterMap.showKeys(JDFConstants.BLANK) + "}");
	}

	/**
	 * @param bStrip if true, only get first token until ;
	 * @return the contentType
	 */
	public String getContentType(final boolean bStrip)
	{
		if (bStrip)
		{
			final String ct = StringUtil.token(contentType, 0, ";");
			return ct == null ? ct : ct.trim();
		}
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(final String contentType)
	{
		this.contentType = StringUtil.normalize(contentType, true);
	}

	/**
	 * sets all values except the main contents
	 *
	 * @param request the container to clone
	 */
	public void setContainer(final ContainerRequest request)
	{
		setRequestURI(request.getRequestURI());
		setContentType(request.getContentType(false));
		setMethod(request.getMethod());
		setRemoteHost(request.getRemoteHost());

		JDFAttributeMap map = request.getHeaderMap();
		if (map != null)
			setHeaderMap(map.clone());

		map = request.getParameterMap();
		setParameterMap(map);
		if (request.getName() != null)
			setName(request.getName());
	}

	/**
	 * @return the method
	 */
	public String getMethod()
	{
		return method.name();
	}

	/**
	 * @return the method
	 */
	public HttpMethod getMethodEnum()
	{
		return method;
	}

	/**
	 * @param bPost if true, POST, else GET
	 */
	public void setPost(final boolean bPost)
	{
		this.method = bPost ? HttpMethod.POST : HttpMethod.GET;
	}

	/**
	 * @return true if this is a POST message
	 */
	public boolean isPost()
	{
		return HttpMethod.POST.equals(method);
	}

	/**
	 * check whether this request is for deviceID
	 *
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
		return reqDeviceID == null || deviceID.equalsIgnoreCase(reqDeviceID);
	}

	/**
	 *
	 * @param checkContext
	 * @return
	 */
	public boolean isMyContext(final String checkContext)
	{
		if (checkContext == null)
		{
			return true;
		}

		final String myContext = getContext();
		return checkContext.equalsIgnoreCase(myContext);

	}

	/**
	 * @param method the method string
	 */
	public void setMethod(final String method)
	{
		try
		{
			this.method = HttpMethod.valueOf(method.toUpperCase().trim());
		}
		catch (final Exception x)
		{
			log.error("invalid method type: " + method);
		}
	}

	/**
	 * @param method the method string
	 */
	public void setMethod(final HttpMethod method)
	{
		this.method = method;
	}

	/**
	 * @return the context
	 */
	public String getContext()
	{
		return StringUtil.token(requestURI, 2 + 1, JDFConstants.SLASH);
	}

	/**
	 *
	 *
	 * @return the war file name portion of the request context, i.e the location where index.html etc are located
	 *
	 */
	public String getContextRoot()
	{
		return JDFConstants.SLASH + StringUtil.token(requestURI, 2, JDFConstants.SLASH);
	}

	/**
	 * get a request header value
	 *
	 * @param header the header key
	 * @return String - the header value, null if header is not set
	 */
	public String getHeader(final String header)
	{
		return headerMap == null ? null : headerMap.getIgnoreCase(header);
	}

	/**
	 * get a request header value
	 *
	 * @param header the header key
	 * @return String - the header value, null if header is not set
	 */
	public String getParameter(final String header)
	{
		return parameterMap == null ? null : parameterMap.getIgnoreCase(header);
	}

	/**
	 * extract a boolean attribute from a given request
	 *
	 * @param param
	 * @return true if the parameter is"true", else false
	 */
	public boolean getBooleanParam(final String param)
	{
		final String val = StringUtil.getNonEmpty(getParameter(param));
		return val != null && StringUtil.parseBoolean(val, true);
	}

	/**
	 * extract an integer attribute from a given request
	 *
	 * @param param
	 * @return the integer parameter
	 */
	public int getIntegerParam(final String param)
	{
		final String val = getParameter(param);
		return StringUtil.parseInt(val, 0);
	}

	/**
	 * extract a long attribute from a given request
	 *
	 * @param param
	 * @return the long parameter
	 */
	public long getLongParam(final String param)
	{
		final String val = getParameter(param);
		return StringUtil.parseLong(val, 0);
	}

	/**
	 * extract a double attribute from a given request
	 *
	 * @param param
	 * @return the double parameter
	 */
	public double getDoubleParam(final String param)
	{
		final String val = getParameter(param);
		return StringUtil.parseDouble(val, 0.0);
	}

	/**
	 * @return
	 */
	public String getRemoteHost()
	{
		return remoteHost;
	}

	/**
	 *
	 * @return
	 */
	public String getBearerToken()
	{

		final VString tokens = VString.getVString(getHeader(UrlUtil.AUTHORIZATION), null);
		if (tokens != null && tokens.size() == 2 && HTTPDetails.BEARER.equals(tokens.get(0)))
		{
			return tokens.get(1);
		}
		return getParameter("access_token");
	}

}
