package org.cip4.bambi.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
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
public class DeviceInfoServlet extends AbstractBambiServlet {
	
	private static Log log = LogFactory.getLog(DeviceInfoServlet.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -6128394458416858325L;
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String qCommand = request.getParameter("qcmd");
		if (qCommand != null && qCommand.equals("suspendQueue"))
		{
			String devID = request.getParameter("id");
			String qeID = request.getParameter("qeid");
			if (devID == null || devID.length() == 0 || qeID == null || qeID.length() == 0)
			{
				log.error("can't suspend QueueEntry with DeviceID ="+devID+" and QueueEntryID="+
						qeID+", either DeviceID or QueueEntryID is missing.");
				showErrorPage("can't suspend QueueEntry", "either DeviceID or QueueEntryID is missing", request, response);
				return;
			}
			else
			{
				
			}
		}
		
		String command = request.getParameter("cmd");
		if ( command != null && command.equals("showDevice") )
		{
			Device dev = (Device) request.getAttribute("device");
			// if dev is null, try to get it from the root device
			if (dev == null) {
				try {
					response.reset();
					request.getRequestDispatcher("/BambiRootDevice").forward(
							request, response);
				} catch (Exception e) {
					log.error(e);
				}
			} else {
				request.setAttribute("device", dev);
				request.setAttribute("bqu", dev.getQueueFacade());
				try {
					request.getRequestDispatcher("/showDevice.jsp").forward(
							request, response);
				} catch (Exception e) {
					log.error(e);
				}
			}
		}

	}
	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		log.info("called doPost");
	}

}
