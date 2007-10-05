package org.cip4.bambi.workers.sim;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IDevice;
import org.cip4.bambi.workers.core.AbstractDevice;
import org.cip4.bambi.workers.core.AbstractWorkerServlet;
import org.cip4.bambi.workers.core.MultiDeviceProperties.DeviceProperties;

public class SimWorkerServlet extends AbstractWorkerServlet {
	protected static Log log = LogFactory.getLog(SimWorkerServlet.class.getName());
	private static final long serialVersionUID = 431025409853435322L;
	
	protected IDevice buildDevice(DeviceProperties prop) {
		SimDevice dev=new SimDevice(prop);
		return dev;
	}

	protected void showDevice(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			request.getRequestDispatcher("DeviceInfo").forward(request, response);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	protected void setAppDir() {
		_appDir=System.getProperty("catalina.base")+"/webapps/SimWorker/";
	}

	protected AbstractDevice getDeviceFromObject(Object dev) {
		if (dev!=null) {
			return (SimDevice)dev;
		} else {
			return null;
		}
	}
	
	
}
