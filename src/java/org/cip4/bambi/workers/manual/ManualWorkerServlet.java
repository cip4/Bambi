package org.cip4.bambi.workers.manual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IDevice;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.workers.core.AbstractWorkerServlet;

public class ManualWorkerServlet extends AbstractWorkerServlet {
	protected static final Log log = LogFactory.getLog(ManualWorkerServlet.class.getName());
	private static final long serialVersionUID = 431025409853435322L;
	
	@Override
	protected IDevice buildDevice(IDeviceProperties prop) {
		ManualDevice dev=new ManualDevice(prop);
        _getHandlers.add(0,dev);
		return dev;
	}
		
	
}
