/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2010 The International Cooperation for the Integration of 
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
package org.cip4.bambi.core;

import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

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
		setPost(true);
	}

	private String requestURI;
	private String contentType;
	private String remoteHost;

	/**
	 * @param remoteHost the remoteHost to set
	 */
	public void setRemoteHost(String remoteHost)
	{
		this.remoteHost = remoteHost;
	}

	private boolean bPost;
	private JDFAttributeMap headerMap;
	private JDFAttributeMap parameterMap;

	/**
	 * get the map of request headers
	 * @return the headerMap
	 */
	public JDFAttributeMap getHeaderMap()
	{
		return headerMap;
	}

	/**
	 * get the map of request uri parameters
	 * @return the headerMap
	 */
	public JDFAttributeMap getParameterMap()
	{
		return parameterMap;
	}

	/**
	 * @param headerMap the headerMap to set
	 */
	public void setHeaderMap(JDFAttributeMap headerMap)
	{
		this.headerMap = headerMap;
	}

	/**
	 * @param parameterMap the parameterMap to set
	 */
	public void setParameterMap(JDFAttributeMap parameterMap)
	{
		this.parameterMap = parameterMap;
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
	public void setRequestURI(String requestURI)
	{
		this.requestURI = requestURI;
	}

	/**
	 * @return
	 */
	public String getDeviceID()
	{
		String localURL = getLocalURL();
		return BambiServletRequest.getDeviceIDFromURL(localURL);
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
		VString v = getContextList();
		VString v2 = new VString();
		for (int i = 2; i < v.size(); i++)
			v2.add(v.get(i));
		return StringUtil.setvString(v2, "/", null, null);
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return
	*/
	@Override
	public String toString()
	{
		return "ContainerRequest URL=" + requestURI + "\n Content Type=" + contentType + "\n Method=" + getMethod() + "\n Parameters: " + parameterMap;
	}

	/**
	 * @param bStrip if true, only get first token until ;
	 * @return the contentType
	 */
	public String getContentType(boolean bStrip)
	{
		if (bStrip)
		{
			String ct = StringUtil.token(contentType, 0, ";");
			return ct == null ? ct : ct.trim();
		}
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	/**
	 * sets all values except the main contents
	 * @param request the container to clone
	 */
	public void setContainer(ContainerRequest request)
	{
		setRequestURI(request.getRequestURI());
		setContentType(request.getContentType(false));
		setMethod(request.getMethod());
		setRemoteHost(request.getRemoteHost());

		JDFAttributeMap map = request.getHeaderMap();
		if (map != null)
			setHeaderMap(map.clone());

		map = request.getParameterMap();
		if (map != null)
			setParameterMap(map.clone());
	}

	/**
	 * @return the method
	 */
	public String getMethod()
	{
		return bPost ? UrlUtil.POST : UrlUtil.GET;
	}

	/**
	 * @param bPost if true, POST, else GET
	 */
	public void setPost(boolean bPost)
	{
		this.bPost = bPost;
	}

	/**
	 * @return true if this is a POST message
	 */
	public boolean isPost()
	{
		return bPost;
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
		return checkContext.equals(myContext);

	}

	/**
	 * @param method the method string
	 */
	public void setMethod(String method)
	{
		this.bPost = UrlUtil.POST.equalsIgnoreCase(method.trim());
	}

	/**
	 * @return the context
	 */
	public String getContext()
	{
		return StringUtil.token(requestURI, 3, "/");
	}

	/**
	 * @return the war file name portion of the request context
	 */
	public String getContextRoot()
	{
		return StringUtil.token(requestURI, 2, "/");
	}

	/**
	 * get a request header value
	 * 
	 * @param header the header key
	 * @return String - the header value, null if header is not set
	 */
	public String getHeader(String header)
	{
		return headerMap == null ? null : headerMap.get(header);
	}

	/**
	 * get a request header value
	 * 
	 * @param header the header key
	 * @return String - the header value, null if header is not set
	 */
	public String getParameter(String header)
	{
		return parameterMap == null ? null : parameterMap.get(header);
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

	/**
	 * @return
	 */
	public String getRemoteHost()
	{
		return remoteHost;
	}
}
