package org.cip4.bambi.workers.manual;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.IDevice;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.workers.core.AbstractWorkerServlet;

public class ManualWorkerServlet extends AbstractWorkerServlet {
	protected static final Log log = LogFactory.getLog(ManualWorkerServlet.class.getName());
	private static final long serialVersionUID = 431025409853435322L;
	
	@Override
	protected IDevice buildDevice(IDeviceProperties prop) {
		ManualDevice dev=new ManualDevice(prop);
		return dev;
	}

	@Override
	protected void showDevice(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			request.getRequestDispatcher("DeviceInfo").forward(request, response);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	@Override
	protected AbstractDevice getDeviceFromObject(Object dev) {
		return dev==null ? null : (ManualDevice)dev;
	}
	
	
}
