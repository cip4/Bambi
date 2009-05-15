/**
 * 
 */
package org.cip4.bambi.proxy;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.MultiDeviceProperties;

/**
 * @author prosirai
 * 
 */
public class ProxyServlet extends BambiServlet
{

	/**
	 * 
	 */
	public ProxyServlet()
	{
		super();
		log = LogFactory.getLog(ProxyServlet.class.getName());
		log.info("Constructing logger");
	}

	/**
     * 
     */
	private static final long serialVersionUID = -8544678701930337760L;
	private Log log = null;

	@Override
	protected void loadProperties(final ServletContext context, final File config, final String deviceID)
	{
		final MultiDeviceProperties props = new ProxyProperties(context, config);
		createDevices(props, deviceID);
	}

}
