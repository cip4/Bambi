/*
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2014 The International Cooperation for the Integration of
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

import java.util.Vector;

import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFAudit;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.elementwalker.FixVersion;
import org.cip4.jdflib.extensions.XJDF20;
import org.cip4.jdflib.extensions.XJDFHelper;
import org.cip4.jdflib.extensions.xjdfwalker.XJDFToJDFConverter;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.EnumUtil;
import org.cip4.jdflib.util.StringUtil;

/**
 * mother of all converters
 */
public class ConverterCallback extends BambiLogFactory implements IConverterCallback
{
	private EnumVersion fixToExtern = null;
	private EnumVersion fixToBambi = null;
	private final Vector<IConverterCallback> postConversionList;

	/**
	 * get the version to modify the version for outgoing jdf and jmf
	 * @return the fixTo
	 */
	public EnumVersion getFixToExtern()
	{
		return fixToExtern;
	}

	/**
	 * 
	 * add an additional converter to this it will be called after the internal conversion
	 * @param cb the IConverterCallback to call
	 */
	public void addConverter(IConverterCallback cb)
	{
		if (cb != null)
		{
			postConversionList.add(cb);
		}
	}

	/**
	 * set the version to modify the version for outgoing jdf and jmf
	 * @param fixTo the fixTo to set
	 */
	public void setFixToExtern(final EnumVersion fixTo)
	{
		this.fixToExtern = fixTo;
	}

	/**
	 * get the version to modify the version for incoming jdf and jmf
	 * @return the fixTo
	 */
	public EnumVersion getFixToBambi()
	{
		return fixToBambi;
	}

	/**
	 * set the version to modify the version for incoming jdf and jmf
	 * @param fixTo the fixTo to set
	 */
	public void setFixToBambi(final EnumVersion fixTo)
	{
		this.fixToBambi = fixTo;
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#prepareJDFForBambi(org.cip4.jdflib.core.JDFDoc)
	 */
	public ConverterCallback()
	{
		super();
		postConversionList = new Vector<IConverterCallback>();
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#prepareJDFForBambi(org.cip4.jdflib.core.JDFDoc) ensure a JobPartID in the root
	 * @param docIn the incoming JDF Document
	 */
	@Override
	public JDFDoc prepareJDFForBambi(JDFDoc docIn)
	{
		if (docIn == null)
			return docIn;

		JDFDoc doc = importXJDF(docIn);

		final JDFNode n = doc.getJDFRoot();
		if (n == null)
		{
			log.warn("No JDF Node in document - do nothing");
			return doc;
		}
		if (!n.hasAttribute(AttributeName.JOBPARTID))
		{
			log.warn("adding default root JobPartID='root'");
			n.setJobPartID("root");
		}
		if (fixToBambi != null)
		{
			n.fixVersion(fixToBambi);
		}
		fixSubscriptions(n);
		for (IConverterCallback cb : postConversionList)
		{
			JDFDoc docIn2 = doc;
			doc = cb.prepareJDFForBambi(docIn2);
		}
		return doc;
	}

	/**
	 * @param doc
	 * @return
	 */
	protected JDFDoc importXJDF(JDFDoc doc)
	{
		final KElement root = doc.getRoot();
		if (XJDFHelper.XJDF.equals(root.getLocalName()))
		{
			log.info("importing xjdf to Bambi");
			final XJDFToJDFConverter xc = getXJDFImporter();
			if (xc != null)
			{
				doc = xc.convert(root);
				if (doc != null)
				{
					FixVersion fv = new FixVersion((EnumVersion) null);
					fv.walkTree(doc.getRoot(), null);
				}
				else
				{
					log.error("null converted document returned, bailing out");
				}
			}
			else
			{
				log.error("null converter returned, bailing out");
			}
		}
		return doc;
	}

	/**
	 * @param doc
	 * @return
	 */
	protected JDFDoc importXJMF(JDFDoc doc)
	{
		final KElement root = doc.getRoot();
		if (XJDFHelper.XJMF.equals(root.getLocalName()))
		{
			log.info("importing XJMF to Bambi");
			final XJDFToJDFConverter xc = getXJDFImporter();
			if (xc != null)
			{
				doc = xc.convert(root);
				if (doc != null)
				{
					FixVersion fv = new FixVersion((EnumVersion) null);
					fv.walkTree(doc.getRoot(), null);
				}
				else
				{
					log.error("null converted document returned, bailing out");
				}
			}
			else
			{
				log.error("null converter returned, bailing out");
			}
		}
		return doc;
	}

	/**
	 * 
	 * get the importer for JDFD - may be overwritten to set parameters
	 * @return the xjdf to jdf converter
	 */
	protected XJDFToJDFConverter getXJDFImporter()
	{
		final XJDFToJDFConverter xc = new XJDFToJDFConverter(null);
		xc.setConvertUnits(true);
		return xc;
	}

	/**
	 * @param doc
	 * @return
	 */
	protected JDFDoc exportXJDF(JDFDoc doc)
	{
		JDFNode root = doc.getJDFRoot();
		if (root == null)
			return doc;
		log.info("exporting XJDF");
		final XJDF20 xjdf = getXJDFExporter();
		final KElement newRoot = xjdf.makeNewJDF(root, null);
		return new JDFDoc(newRoot.getOwnerDocument());
	}

	/**
	 * @param doc
	 * @return
	 */
	protected JDFDoc exportXJMF(JDFDoc doc)
	{
		JDFJMF jmf = doc.getJMFRoot();
		if (jmf == null)
			return doc;
		log.info("exporting XJMF");
		final XJDF20 xjdf = getXJDFExporter();
		final KElement newJMF = xjdf.makeNewJMF(jmf);
		return new JDFDoc(newJMF.getOwnerDocument());
	}

	/**
	 * 
	 * get the exporter for JDFD - may be overwritten to set parameters
	 * @return
	 */
	protected XJDF20 getXJDFExporter()
	{
		final XJDF20 xjdf = new XJDF20();
		return xjdf;
	}

	/**
	 * make sure that all jobID attributes match the root jobID in any subscriptions
	 * @param n
	 */
	private void fixSubscriptions(final JDFNode n)
	{
		if (n == null)
		{
			return;
		}
		final String jobID = StringUtil.getNonEmpty(n.getJobID(true));
		if (jobID == null)
		{
			log.error("root with no JobID");
			return;
		}
		final Vector<JDFSubscription> vSubs = n.getChildrenByClass(JDFSubscription.class, true, 0);
		if (vSubs != null)
		{
			for (int i = 0; i < vSubs.size(); i++)
			{
				final KElement message = vSubs.get(i).getParentNode_KElement();
				if (message instanceof JDFMessage)
				{
					final VElement v = message.getChildrenByTagName_KElement(null, null, new JDFAttributeMap(AttributeName.JOBID, "*"), false, true, 0);
					if (v != null)
					{
						for (int ii = 0; ii < v.size(); ii++)
						{
							v.get(ii).setAttribute(AttributeName.JOBID, jobID);
						}
					}

				}
			}
		}

	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#prepareJMFForBambi(org.cip4.jdflib.core.JDFDoc)
	 * @param doc the JMF Doc
	 */
	@Override
	public JDFDoc prepareJMFForBambi(JDFDoc doc)
	{
		if (doc == null)
		{
			return null;
		}
		doc = importXJMF(doc);
		final JDFJMF jmf = doc.getJMFRoot();
		if (fixToBambi != null)
		{
			jmf.fixVersion(fixToBambi);
		}
		for (IConverterCallback cb : postConversionList)
		{
			doc = cb.prepareJMFForBambi(doc);
		}
		return doc;
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#updateJDFForExtern(org.cip4.jdflib.core.JDFDoc)
	 * @param doc the JDF doc
	 */

	@Override
	public JDFDoc updateJDFForExtern(JDFDoc doc)
	{
		if (doc == null)
		{
			return null;
		}
		final JDFNode n = doc.getJDFRoot();
		if (fixToExtern != null)
		{
			boolean bXJDF = fixToExtern.equals(EnumVersion.Version_2_0);
			EnumVersion fixVersion = bXJDF ? JDFAudit.getDefaultJDFVersion() : fixToExtern;
			n.fixVersion(fixVersion);
			if (bXJDF)
			{
				doc = exportXJDF(doc);
			}
		}
		for (IConverterCallback cb : postConversionList)
		{
			doc = cb.updateJDFForExtern(doc);
		}
		return doc;
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#updateJMFForExtern(org.cip4.jdflib.core.JDFDoc)
	 * @param doc the JMF doc
	 */
	@Override
	public JDFDoc updateJMFForExtern(JDFDoc doc)
	{
		if (doc == null)
		{
			return null;
		}
		final JDFJMF jmf = doc.getJMFRoot();
		boolean bXJDF = EnumUtil.aLessEqualsThanB(EnumVersion.Version_2_0, jmf.getMaxVersion());
		if (fixToExtern != null || bXJDF)
		{
			bXJDF = bXJDF || EnumUtil.aLessEqualsThanB(EnumVersion.Version_2_0, fixToExtern);
			EnumVersion fixVersion = bXJDF ? JDFAudit.getDefaultJDFVersion() : fixToExtern;
			jmf.fixVersion(fixVersion);
			if (bXJDF)
			{
				doc = exportXJMF(doc);
			}
		}
		for (IConverterCallback cb : postConversionList)
		{
			doc = cb.updateJMFForExtern(doc);
		}
		return doc;
	}
}
