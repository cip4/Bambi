package org.cip4.bambi.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.Device;

/**
 * takes care of the needs of showDevices.jsp
 * @author boegerni
 *
 */
public class DeviceInfoServlet extends HttpServlet {
	
	private static Log log = LogFactory.getLog(DeviceInfoServlet.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -6128394458416858325L;
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Device dev = (Device) request.getAttribute("device");
		if (dev == null) {
			log.error("cannot display info for null device");
			return;
		}

		request.setAttribute("device", dev);
		request.setAttribute("bqu", dev.getQueueFacade());
		try {
			request.getRequestDispatcher("/showDevice.jsp").forward(request,response);
		} catch (Exception e) {
			log.error(e);
			}
	}
	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		log.info("called doPost");
	}

}
