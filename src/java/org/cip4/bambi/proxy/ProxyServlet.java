/**
 * 
 */
package org.cip4.bambi.proxy;

import java.io.File;

import javax.servlet.ServletContext;

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
    private static final long serialVersionUID = -8544678701930337760L;

    @Override
    protected void loadProperties(ServletContext context, File config, String deviceID)
    {
        MultiDeviceProperties props=new ProxyProperties(context,config);
        createDevices(props, deviceID);
    }

}
