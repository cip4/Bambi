package org.cip4.bambi.core.messaging;

import org.cip4.bambi.core.ConverterCallback;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.UrlUtil;

public class MyTestCallback extends ConverterCallback
{

	public MyTestCallback()
	{
		super();
	}

	public MyTestCallback(final ConverterCallback other)
	{
		super(other);
	}

	JDFAttributeMap m;

	/**
	 * @see org.cip4.bambi.core.ConverterCallback#getJMFContentType()
	 */
	@Override
	public String getJMFContentType()
	{
		return UrlUtil.VND_JMF;
	}

	@Override
	public JDFAttributeMap getCallbackDetails()
	{
		return new JDFAttributeMap("a", "b");
	}

	@Override
	public void setCallbackDetails(final JDFAttributeMap map)
	{
		m = map;
	}

}