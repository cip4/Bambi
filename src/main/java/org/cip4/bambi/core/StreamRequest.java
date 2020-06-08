/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2016 The International Cooperation for the Integration of 
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.cip4.jdflib.core.JDFConstants;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.ByteArrayIOFileStream;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * class to package an input stream together with the context information of the request
 * 
 * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class StreamRequest extends ContainerRequest
{

	/**
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static StreamRequest createStreamRequest(final HttpServletRequest request) throws IOException
	{
		StreamRequest sr = new StreamRequest(request.getInputStream());
		final String contentType = request.getContentType();
		sr.setContentType(contentType);
		sr.setRequestURI(request.getRequestURL().toString());
		sr.setHeaderMap(sr.getHeaderMap(request));
		sr.setParameterMap(new JDFAttributeMap(sr.getParameterMap(request)));
		sr.setRemoteHost(request.getRemoteHost());
		return sr;
	}

	/**
	 * @param file
	 * @return
	 *  
	 */
	public static StreamRequest createStreamRequest(final File file)
	{
		BufferedInputStream fileStream = FileUtil.getBufferedInputStream(file);
		if (fileStream == null)
		{
			return null;
		}
		StreamRequest sr = new StreamRequest(fileStream);
		final String contentType = UrlUtil.getMimeTypeFromURL(file.getName());
		sr.setContentType(contentType);
		sr.setRequestURI(file.getAbsolutePath());
		return sr;
	}

	/**
	 *  
	 */
	private Map<String, String> getParameterMap(HttpServletRequest request)
	{
		Map<String, String[]> pm = request.getParameterMap();
		Map<String, String> retMap = new JDFAttributeMap();
		Set<String> keyset = pm.keySet();
		for (String key : keyset)
		{
			String[] strings = pm.get(key);
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
	 * @return map of headers, null if no headers exist
	 */
	private JDFAttributeMap getHeaderMap(HttpServletRequest request)
	{
		Enumeration<String> headers = request.getHeaderNames();
		if (!headers.hasMoreElements())
		{
			return null;
		}
		final JDFAttributeMap map = new JDFAttributeMap();
		while (headers.hasMoreElements())
		{
			String header = headers.nextElement();
			Enumeration<String> e = request.getHeaders(header);
			VString v = new VString(e);
			if (v.size() > 0)
			{
				map.put(header, StringUtil.setvString(v, JDFConstants.COMMA, null, null));
			}
		}
		return map.size() == 0 ? null : map;
	}

	/**
	 * @param theStream
	 */
	public StreamRequest(InputStream theStream)
	{
		super();
		this.theStream = new ByteArrayIOFileStream(theStream, 10000000);
	}

	/**
	 * @param theStream
	 */
	public StreamRequest(ByteArrayIOStream theStream)
	{
		super();
		this.theStream = theStream;
	}

	final ByteArrayIOStream theStream;

	/**
	 * @return
	 */
	public InputStream getInputStream()
	{
		return theStream.getInputStream();
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return
	*/
	@Override
	public String toString()
	{
		return super.toString() + " stream size: " + theStream.size();
	}

	@Override
	protected void finalize() throws Throwable
	{
		if (theStream != null)
			theStream.close();
		super.finalize();
	}

}
