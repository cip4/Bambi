/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2021 The International Cooperation for the Integration of
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
 * (C) 2020-2021 Heidelberger Druckmaschinen AG
 */
package org.cip4.bambi.core;

import java.io.InputStream;

import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.lib.jdf.jsonutil.JSONObjHelper;
import org.cip4.lib.jdf.jsonutil.JSONWriter;
import org.json.simple.JSONObject;

public class NetResponse extends XMLResponse
{
	private static JSONWriter theJW;
	private JSONWriter jw;

	NetResponse(final KElement theXML)
	{
		super(theXML);
		jw = null;
	}

	/**
	 * @param r
	 */
	public NetResponse(final XMLResponse r, final String contentType)
	{
		super(r, contentType);
		jw = null;
	}

	/**
	 * @return
	 */
	public boolean isJSON()
	{
		return jw != null;
	}

	/**
	 * @param isJSON
	 * @param jwIn a pre configured writer
	 */
	public void setJSON(final boolean isJSON)
	{
		jw = isJSON ? getJW() : null;
	}

	/**
	 * @return
	 */
	@Override
	public InputStream getInputStream()
	{
		if (theBuffer == null && isJSON())
		{
			updateJsonBuffer();
		}
		return super.getInputStream();
	}

	void updateJsonBuffer()
	{
		final KElement e = getXML();
		if (e != null)
		{
			final JSONObject o = jw.convert(e);
			if (o != null)
			{
				theBuffer = new ByteArrayIOStream((new JSONObjHelper(o).getInputStream()));
			}
		}
		else if (getNotification() != null && !UrlUtil.isReturnCodeOK(getHttpRC()))
		{
			final JSONObjHelper o = new JSONObjHelper(new JSONObject());
			o.setInt("rc", getHttpRC());
			o.setString("error", getNotification());
			theBuffer = new ByteArrayIOStream(o.getInputStream());
		}
	}

	@Override
	public String getContentType()
	{
		if (isJSON())
		{
			return UrlUtil.APPLICATION_JSON;
		}
		else
		{
			return super.getContentType();
		}
	}

	public static JSONWriter getJW()
	{
		if (theJW == null)
		{
			theJW = new JSONWriter();
			theJW.setTypeSafe(true);
			theJW.setWantArray(false);
			theJW.addStringArray(AttributeName.TYPES);
			theJW.addStringArray(AttributeName.VALUELIST);
		}
		return theJW;
	}

	/**
	 * @see org.cip4.bambi.core.HTTPResponse#toString()
	 */
	@Override
	public String toString()
	{
		return super.toString() + " json=" + isJSON();
	}

}