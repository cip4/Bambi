package org.cip4.bambi.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;

/**
 * 
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
	 * @see org.cip4.bambi.core.IConverterCallback#prepareJDFForBambi(org.cip4.jdflib.core.JDFDoc)
	 * @param doc
	 */
	public void prepareJDFForBambi(JDFDoc doc)
	{
		//
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#prepareJMFForBambi(org.cip4.jdflib.core.JDFDoc)
	 * @param doc
	 */
	public void prepareJMFForBambi(JDFDoc doc)
	{
		// TODO Auto-generated method stub
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#updateJDFForExtern(org.cip4.jdflib.core.JDFDoc)
	 * @param doc
	 */

	public void updateJDFForExtern(JDFDoc doc)
	{
		// TODO Auto-generated method stub
	}

	/**
	 * @see org.cip4.bambi.core.IConverterCallback#updateJMFForExtern(org.cip4.jdflib.core.JDFDoc)
	 * @param doc
	 */
	public void updateJMFForExtern(JDFDoc doc)
	{
		// TODO Auto-generated method stub
	}
}
