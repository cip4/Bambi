package org.cip4.bambi.workers.sim;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractBambiServlet;
import org.cip4.bambi.core.IDevice;
import org.cip4.bambi.core.IDeviceProperties;

public class SimWorkerServlet extends AbstractBambiServlet {
	private static final Log log = LogFactory.getLog(SimWorkerServlet.class.getName());
	private static final long serialVersionUID = 431025409853435322L;
	
	@Override
	protected IDevice buildDevice(IDeviceProperties prop) {
		SimDevice dev=new SimDevice(prop);
        _getHandlers.add(0,dev);
		return dev;
	}	
}
