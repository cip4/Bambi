package org.cip4.bambi.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.node.JDFNode;

/**
 * mother of all converters
 */
public class ConverterCallback implements IConverterCallback
{
	private static Log log = LogFactory.getLog(ConverterCallback.class.getName());

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
	public void prepareJDFForBambi(final JDFDoc doc)
	{
		final JDFNode n = doc.getJDFRoot();
		if (n != null && !n.hasAttribute(AttributeName.JOBPARTID))
		{
			log.warn("adding default root JobPartID='root'");
			n.setJobPartID("root");
		}
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#prepareJMFForBambi(org.cip4.jdflib.core.JDFDoc)
	 * @param doc the JMF Doc
	 */
	public void prepareJMFForBambi(final JDFDoc doc)
	{
		// empty stub
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#updateJDFForExtern(org.cip4.jdflib.core.JDFDoc)
	 * @param doc the JDF doc
	 */

	public void updateJDFForExtern(final JDFDoc doc)
	{
		// empty stub
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#updateJMFForExtern(org.cip4.jdflib.core.JDFDoc)
	 * @param doc the JMF doc
	 */
	public void updateJMFForExtern(final JDFDoc doc)
	{
		// empty stub
	}
}
