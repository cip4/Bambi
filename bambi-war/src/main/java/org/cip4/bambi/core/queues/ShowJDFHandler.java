/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2017 The International Cooperation for the Integration of
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
package org.cip4.bambi.core.queues;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.proxy.AbstractProxyDevice;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.EnumActivation;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.JDFSpawn;
import org.cip4.jdflib.util.UrlUtil;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 *
 * 13.01.2009
 */
public class ShowJDFHandler extends ShowHandler
{
	/**
	 * @param device
	 */
	public ShowJDFHandler(final AbstractDevice device)
	{
		super(device);
	}

	/**
	 * @param request
	 */
	@Override
	protected boolean isMyRequest(final ContainerRequest request)
	{
		boolean b = request.isMyContext("showJDF");
		if (b)
		{
			final String jobPartID = request.getParameter(AttributeName.JOBPARTID);
			b = jobPartID == null;
		}
		return b;
	}

	/**
	 * @param request
	 * @param f
	 */
	@Override
	protected XMLResponse processFile(final ContainerRequest request, final File f)
	{
		final boolean callback = request.getBooleanParam("Callback");
		final boolean raw = request.getBooleanParam("raw");
		final boolean repair = request.getBooleanParam("repair");
		XMLResponse r = new XMLResponse(null);

		InputStream is = FileUtil.getBufferedInputStream(f);
		if (repair || !raw || callback && (_parentDevice instanceof AbstractProxyDevice))
		{
			JDFDoc doc = JDFDoc.parseStream(is);
			doc = prepareRoot(doc, request);

			if (callback)
			{
				final IConverterCallback call = ((AbstractProxyDevice) _parentDevice).getSlaveCallback();
				if (call != null)
				{
					call.updateJDFForExtern(doc);
				}
			}
			if (repair)
			{
				JDFNode n = doc.getJDFRoot();
				if (n != null)
				{
					while (true)
					{
						JDFNode unspawned = new JDFSpawn(n).unSpawn(null);
						if (unspawned == null)
							break;
					}
				}
			}
			if (doc != null)
			{
				r.setXML(doc.getRoot());
				is = null;
			}
		}
		else if (is != null)
		{
			processRaw(r, is);
		}

		final boolean bJDF = request.getBooleanParam(QueueProcessor.isJDF);
		r.setContentType(bJDF ? UrlUtil.VND_JDF : UrlUtil.TEXT_XML);
		return r;
	}

	protected void processRaw(XMLResponse r, InputStream is)
	{
		try
		{
			IOUtils.copy(is, r.getOutputStream());
		}
		catch (IOException e)
		{
			log.error("Cannot copy to response:", e);
		}
	}

	/**
	 * copy all move all partitioned common nodeinfo status values into the root
	 *
	 * @param doc
	 * @param request
	 * @return
	 */
	@Override
	protected JDFDoc prepareRoot(JDFDoc doc, final ContainerRequest request)
	{
		doc = super.prepareRoot(doc, request);
		if (doc == null)
		{
			return null;
		}

		final JDFNode n = doc.getJDFRoot();
		final VElement v = n.getvJDFNode(null, null, false);
		for (int i = 0; i < v.size(); i++)
		{
			final JDFNode n2 = (JDFNode) v.get(i);
			final JDFNodeInfo ni = n2.getNodeInfo();
			// get the status from leaves only
			final EnumNodeStatus s = n2.getVectorPartStatus(ni == null ? null : ni.getPartMapVector(false));
			if (s != null)
			{
				n2.setStatus(s);
			}
		}
		updateStatusFromChildren(v);
		updateActivation(request, n);
		return doc;
	}

	/**
	 *
	 * update the activation based on the request parameter
	 * this is needed in case we serve the JDF of a requestqueueentry via http
	 * @param request
	 * @param n
	 */
	private void updateActivation(final ContainerRequest request, final JDFNode n)
	{
		final EnumActivation activation = EnumActivation.getEnum(request.getParameter("activation"));
		if (activation != null)
			n.setActivation(activation);
	}

	/**
	 * @param v
	 */
	private void updateStatusFromChildren(final VElement v)
	{
		boolean mod = true;
		while (mod)
		{
			mod = false;
			for (int i = 0; i < v.size(); i++)
			{
				final JDFNode n2 = (JDFNode) v.get(i);
				final VElement v2 = n2.getvJDFNode(null, null, true);
				if (v2 != null && v2.size() > 0)
				{
					EnumNodeStatus s2 = ((JDFNode) v2.get(0)).getStatus();
					for (int ii = 1; ii < v2.size(); ii++)
					{
						final JDFNode n3 = (JDFNode) v2.get(ii);
						final EnumNodeStatus s3 = n3.getStatus();
						if (s3 != s2)
						{
							s2 = EnumNodeStatus.Part;
							break;
						}
					}
					if (s2 != null && !ContainerUtil.equals(s2, n2.getStatus()))
					{
						mod = true;
						n2.setStatus(s2);
					}
				}
			}
		}
	}
}
