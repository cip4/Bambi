package org.cip4.bambi.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.node.JDFNode;

/**
 * mother of all converters
 */
public class ConverterCallback implements IConverterCallback
{
	private static Log log = LogFactory.getLog(ConverterCallback.class.getName());
	private EnumVersion fixToExtern = null;
	private EnumVersion fixToBambi = null;

	/**
	 * get the version to modify the version for outgoing jdf and jmf
	 * @return the fixTo
	 */
	public EnumVersion getFixToExtern()
	{
		return fixToExtern;
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
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#prepareJDFForBambi(org.cip4.jdflib.core.JDFDoc) ensure a JobPartID in the root
	 * @param doc the incoming JDF Document
	 */
	public JDFDoc prepareJDFForBambi(final JDFDoc doc)
	{
		final JDFNode n = doc.getJDFRoot();
		if (n != null && !n.hasAttribute(AttributeName.JOBPARTID))
		{
			log.warn("adding default root JobPartID='root'");
			n.setJobPartID("root");
		}
		if (fixToBambi != null)
		{
			n.fixVersion(fixToBambi);
		}
		return doc;
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#prepareJMFForBambi(org.cip4.jdflib.core.JDFDoc)
	 * @param doc the JMF Doc
	 */
	public JDFDoc prepareJMFForBambi(final JDFDoc doc)
	{
		if (doc == null)
		{
			return null;
		}
		final JDFJMF jmf = doc.getJMFRoot();
		if (fixToBambi != null)
		{
			jmf.fixVersion(fixToBambi);
		}
		return doc;
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#updateJDFForExtern(org.cip4.jdflib.core.JDFDoc)
	 * @param doc the JDF doc
	 */

	public JDFDoc updateJDFForExtern(final JDFDoc doc)
	{
		if (doc == null)
		{
			return null;
		}
		final JDFNode n = doc.getJDFRoot();
		if (fixToExtern != null)
		{
			n.fixVersion(fixToExtern);
		}
		return doc;
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#updateJMFForExtern(org.cip4.jdflib.core.JDFDoc)
	 * @param doc the JMF doc
	 */
	public JDFDoc updateJMFForExtern(final JDFDoc doc)
	{
		if (doc == null)
		{
			return null;
		}
		final JDFJMF jmf = doc.getJMFRoot();
		if (fixToExtern != null)
		{
			jmf.fixVersion(fixToExtern);
		}
		return doc;
	}
}
