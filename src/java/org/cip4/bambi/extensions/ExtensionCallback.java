/**
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
			toJDFConverter.createProduct = false;
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

}
