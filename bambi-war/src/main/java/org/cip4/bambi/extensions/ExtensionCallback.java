/*
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

package org.cip4.bambi.extensions;

import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.ConverterCallback;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.extensions.XJDF20;
import org.cip4.jdflib.extensions.xjdfwalker.XJDFToJDFConverter;
import org.cip4.jdflib.node.JDFNode;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * Apr 5, 2009
 */
public class ExtensionCallback extends ConverterCallback
{

	/**
	 * 
	 */
	public ExtensionCallback()
	{
		super();
	}

	/**
	 * 
	 * @param other
	 */
	public ExtensionCallback(ExtensionCallback other)
	{
		super(other);
	}

	/**
	 * 
	 */
	private static final String SUBMIT_VERSION = "SubmitVersion";

	/**
	 * @see org.cip4.bambi.core.ConverterCallback#prepareJDFForBambi(org.cip4.jdflib.core.JDFDoc)
	 */
	@Override
	public JDFDoc prepareJDFForBambi(JDFDoc doc)
	{
		doc = super.prepareJDFForBambi(doc);
		if (doc != null)
		{
			final XJDFToJDFConverter toJDFConverter = new XJDFToJDFConverter(null);
			toJDFConverter.setCreateProduct(false);
			if (toJDFConverter.canConvert(doc.getRoot()))
			{
				doc = toJDFConverter.convert(doc.getRoot());
				final JDFNode n = doc.getJDFRoot();
				BambiNSExtension.setMyNSAttribute(n, SUBMIT_VERSION, "2.0");
			}
		}
		return doc;
	}

	/**
	 * @see org.cip4.bambi.core.ConverterCallback#updateJDFForExtern(org.cip4.jdflib.core.JDFDoc)
	 */
	@Override
	public JDFDoc updateJDFForExtern(JDFDoc doc)
	{
		doc = super.updateJDFForExtern(doc);

		if (doc != null)
		{
			final JDFNode root = doc.getJDFRoot();
			if ("2.0".equals(BambiNSExtension.getMyNSAttribute(root, SUBMIT_VERSION)))
			{
				final XJDF20 x2 = new XJDF20();
				final KElement e = x2.makeNewJDF(root, null);
				doc = new JDFDoc(e.getOwnerDocument());
			}
		}
		return doc;

	}

	/**
	 * @see org.cip4.bambi.core.ConverterCallback#clone()
	 */
	@Override
	protected ExtensionCallback clone()
	{
		return new ExtensionCallback(this);
	}

}
