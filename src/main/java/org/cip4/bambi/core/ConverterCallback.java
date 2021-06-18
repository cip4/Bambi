/*
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFAudit;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.elementwalker.FixVersion;
import org.cip4.jdflib.extensions.XJDF20;
import org.cip4.jdflib.extensions.XJDFConstants;
import org.cip4.jdflib.extensions.xjdfwalker.XJDFToJDFConverter;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.EnumUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.lib.jdf.jsonutil.JSONWriter;

/**
 * mother of all converters
 */
public class ConverterCallback extends BambiLogFactory implements IConverterCallback, Cloneable
{
	private static final String REMOVE_JOB_ID = "RemoveJobID";
	private static final String FIX_TO_BAMBI = "FixToBambi";
	static final String FIX_TO_EXTERN = "FixToExtern";
	static final String IS_JSON = "isJSON";

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ConverterCallback clone()
	{
		return new ConverterCallback(this);
	}

	private EnumVersion fixToExtern = null;
	private EnumVersion fixToBambi = null;
	private Vector<IConverterCallback> postConversionList;
	private boolean removeJobIDFromSubs;
	private boolean isJSON;

	/**
	 * copy ctor
	 *
	 * @param other
	 */
	public ConverterCallback(final ConverterCallback other)
	{
		this();
		if (other != null)
		{
			fixToBambi = other.fixToBambi;
			fixToExtern = other.fixToExtern;
			postConversionList = other.postConversionList;
			removeJobIDFromSubs = other.removeJobIDFromSubs;
			isJSON = other.isJSON;
		}
	}

	/**
	 * get the version to modify the version for outgoing jdf and jmf
	 *
	 * @return the fixTo
	 */
	public EnumVersion getFixToExtern()
	{
		return fixToExtern;
	}

	/**
	 *
	 * add an additional converter to this it will be called after the internal conversion
	 *
	 * @param cb the IConverterCallback to call
	 */
	public void addConverter(final IConverterCallback cb)
	{
		if (cb != null)
		{
			postConversionList.add(cb);
		}
	}

	/**
	 * set the version to modify the version for outgoing jdf and jmf
	 *
	 * @param fixTo the fixTo to set
	 */
	public void setFixToExtern(final EnumVersion fixTo)
	{
		this.fixToExtern = fixTo;
	}

	/**
	 * get the version to modify the version for incoming jdf and jmf
	 *
	 * @return the fixTo
	 */
	public EnumVersion getFixToBambi()
	{
		return fixToBambi;
	}

	/**
	 * set the version to modify the version for incoming jdf and jmf
	 *
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
		postConversionList = new Vector<>();
		setRemoveJobIDFromSubs(false);
		isJSON = false;
	}

	/**
	 *
	 * @return
	 */
	public boolean isRemoveJobIDFromSubs()
	{
		return removeJobIDFromSubs;
	}

	/**
	 *
	 * @param removeJobIDFromSubs
	 */
	public void setRemoveJobIDFromSubs(final boolean removeJobIDFromSubs)
	{
		this.removeJobIDFromSubs = removeJobIDFromSubs;
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#prepareJDFForBambi(org.cip4.jdflib.core.JDFDoc) ensure a JobPartID in the root
	 * @param docIn the incoming JDF Document
	 */
	@Override
	public JDFDoc prepareJDFForBambi(final JDFDoc docIn)
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
		for (final IConverterCallback cb : postConversionList)
		{
			final JDFDoc docIn2 = doc;
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
		if (XJDFConstants.XJDF.equals(root.getLocalName()))
		{
			log.info("importing xjdf to Bambi");
			final XJDFToJDFConverter xc = getXJDFImporter();
			if (xc != null)
			{
				doc = xc.convert(root);
				if (doc != null)
				{
					final FixVersion fv = new FixVersion((EnumVersion) null);
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
		if (XJDFConstants.XJMF.equals(root.getLocalName()))
		{
			log.info("importing XJMF to Bambi");
			final XJDFToJDFConverter xc = getXJDFImporter();
			if (xc != null)
			{
				doc = xc.convert(root);
				if (doc != null)
				{
					final FixVersion fv = new FixVersion((EnumVersion) null);
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
	 *
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
	protected JDFDoc exportXJDF(final JDFDoc doc)
	{
		final JDFNode root = doc == null ? null : doc.getJDFRoot();
		if (root == null)
			return doc;
		log.info("exporting XJDF");
		final XJDF20 xjdf = getXJDFExporter();
		final KElement newRoot = xjdf.makeNewJDF(root, null);
		return newRoot == null ? null : new JDFDoc(newRoot.getOwnerDocument());
	}

	/**
	 * @param doc
	 * @return
	 */
	protected JDFDoc exportXJMF(final JDFDoc doc)
	{
		final JDFJMF jmf = doc == null ? null : doc.getJMFRoot();
		if (jmf == null)
			return doc;
		final XJDF20 xjdf = getXJDFExporter();
		if (xjdf.isAbstractMessage())
		{
			log.info("exporting XJMF");
		}
		final KElement newJMF = xjdf.makeNewJMF(jmf);
		return newJMF == null ? null : new JDFDoc(newJMF.getOwnerDocument());
	}

	/**
	 *
	 * get the exporter for JDFD - may be overwritten to set parameters
	 *
	 * @return
	 */
	protected XJDF20 getXJDFExporter()
	{
		final XJDF20 xjdf = new XJDF20();
		return xjdf;
	}

	/**
	 *
	 * get the exporter for JSON - may be overwritten to set parameters
	 *
	 * @return
	 */
	protected JSONWriter getJSONWriter()
	{
		return ServletContainer.getJSONWriter();
	}

	/**
	 * make sure that all jobID attributes match the root jobID in any subscriptions
	 *
	 * @param n
	 */
	protected void fixSubscriptions(final JDFNode n)
	{
		if (n == null)
		{
			return;
		}
		final String jobID = removeJobIDFromSubs ? null : StringUtil.getNonEmpty(n.getJobID(true));
		fixSubscriptions(n, jobID);
	}

	/**
	 *
	 * @param n
	 * @param jobID
	 */
	protected void fixSubscriptions(final JDFElement n, final String jobID)
	{
		if (jobID == null && !removeJobIDFromSubs)
		{
			log.warn("root subscription with no JobID: ");
			return;
		}
		final List<JDFSubscription> vSubs = n.getChildArrayByClass(JDFSubscription.class, true, 0);
		if (vSubs != null)
		{
			final JDFAttributeMap attMap = new JDFAttributeMap(AttributeName.JOBID, "*");
			if (removeJobIDFromSubs)
			{
				attMap.put(AttributeName.JOBPARTID, "*");
				attMap.put(AttributeName.QUEUEENTRYID, "*");
			}
			for (final JDFSubscription sub : vSubs)
			{
				final KElement message = sub.getParentNode_KElement();
				if (message instanceof JDFMessage)
				{
					final VElement v = message.getChildrenByTagName_KElement(null, null, attMap, false, false, 0);
					if (v != null)
					{
						for (final KElement e : v)
						{
							if (removeJobIDFromSubs)
							{
								e.setAttribute(AttributeName.JOBPARTID, null);
								e.setAttribute(AttributeName.JOBID, null);
								e.setAttribute(AttributeName.QUEUEENTRYID, null);
								e.removeChildren(ElementName.PART, null, null);
							}
							else
							{
								e.setAttribute(AttributeName.JOBID, jobID);
							}
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
		for (final IConverterCallback cb : postConversionList)
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
		final JDFNode n = doc == null ? null : doc.getJDFRoot();
		if (n == null)
		{
			return null;
		}
		if (fixToExtern != null)
		{
			final boolean bXJDF = isXJDF();
			final EnumVersion fixVersion = bXJDF ? JDFAudit.getDefaultJDFVersion() : fixToExtern;
			n.fixVersion(fixVersion);
			if (bXJDF)
			{
				doc = exportXJDF(doc);
			}
		}
		for (final IConverterCallback cb : postConversionList)
		{
			doc = cb.updateJDFForExtern(doc);
		}
		return doc;
	}

	/**
	 *
	 * @return
	 */
	boolean isXJDF()
	{
		final boolean bXJDF = fixToExtern == null ? false : EnumUtil.aLessEqualsThanB(EnumVersion.Version_2_0, fixToExtern);
		return bXJDF;
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
			final EnumVersion fixVersion = bXJDF ? JDFAudit.getDefaultJDFVersion() : fixToExtern;
			jmf.fixVersion(fixVersion);
			if (bXJDF)
			{
				doc = exportXJMF(doc);
			}
		}
		for (final IConverterCallback cb : postConversionList)
		{
			doc = cb.updateJMFForExtern(doc);
		}
		return doc;
	}

	/**
	 *
	 * @see org.cip4.bambi.core.IConverterCallback#getJMFExternStream(org.cip4.jdflib.core.JDFDoc)
	 */
	@Override
	public InputStream getJMFExternStream(final JDFDoc doc)
	{
		final JDFDoc doc2 = updateJMFForExtern(doc);
		if (isJSON)
		{
			return getJSONWriter().getStream(doc2.getRoot());
		}
		return writeToStream(doc2);
	}

	protected InputStream writeToStream(final JDFDoc doc2)
	{
		if (doc2 != null)
		{
			final ByteArrayIOStream bos = new ByteArrayIOStream();
			try
			{
				doc2.write2Stream(bos, 2, false);
			}
			catch (final IOException e)
			{
				return null;
			}
			return bos.getInputStream();
		}
		return null;
	}

	/**
	 *
	 * @see org.cip4.bambi.core.IConverterCallback#getJDFExternStream(org.cip4.jdflib.core.JDFDoc)
	 */
	@Override
	public InputStream getJDFExternStream(final JDFDoc doc)
	{
		final JDFDoc doc2 = updateJDFForExtern(doc);
		return writeToStream(doc2);
	}

	/**
	 *
	 * @see org.cip4.bambi.core.IConverterCallback#getJDFContentType()
	 */
	@Override
	public String getJDFContentType()
	{
		return isXJDF() ? UrlUtil.VND_XJDF : UrlUtil.VND_JDF;
	}

	/**
	 *
	 * @see org.cip4.bambi.core.IConverterCallback#getJMFContentType()
	 */
	@Override
	public String getJMFContentType()
	{
		if (isXJDF())
			return isJSON() ? UrlUtil.VND_XJMF_J : UrlUtil.VND_XJMF;
		else
			return UrlUtil.VND_JMF;
	}

	@Override
	public JDFAttributeMap getCallbackDetails()
	{
		final JDFAttributeMap m = new JDFAttributeMap();
		m.put(IS_JSON, isJSON);
		m.putNotNull(FIX_TO_EXTERN, fixToExtern);
		m.putNotNull(FIX_TO_BAMBI, fixToBambi);
		m.put(REMOVE_JOB_ID, removeJobIDFromSubs);
		return m;
	}

	@Override
	public void setCallbackDetails(final JDFAttributeMap map)
	{
		if (map != null)
		{
			setJSON(StringUtil.parseBoolean(map.get(IS_JSON), false));
			removeJobIDFromSubs = StringUtil.parseBoolean(map.get(REMOVE_JOB_ID), true);
			final String f2b = map.get(FIX_TO_BAMBI);
			if (!StringUtil.isEmpty(f2b))
				fixToBambi = EnumVersion.getEnum(f2b);
			final String f2e = map.get(FIX_TO_EXTERN);
			if (!StringUtil.isEmpty(f2e))
				fixToExtern = EnumVersion.getEnum(map.get(FIX_TO_EXTERN));
		}
	}

	/**
	 * @return the isJSON
	 */
	public boolean isJSON()
	{
		return isJSON;
	}

	/**
	 * @param isJSON the isJSON to set
	 */
	public void setJSON(final boolean isJSON)
	{
		this.isJSON = isJSON;
	}

}
