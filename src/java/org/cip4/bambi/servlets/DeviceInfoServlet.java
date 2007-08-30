package org.cip4.bambi.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.AbstractDevice;
import org.cip4.bambi.CustomDevice;
import org.cip4.bambi.AbstractDeviceProcessor.JobPhase;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;

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
		if (command == null)
		{
			String errDetails = "Parameter 'cmd' is missing or null. Which command is the servlet to execute?";
			showErrorPage("mandatory parameter 'cmd' missing or null", errDetails, request, response);
			return;
		}
		Object oDev = request.getAttribute("device");
		// if dev is null, try to get it from the root device
		if (oDev == null) {
			try {
				response.reset();
				request.getRequestDispatcher("/BambiRootDevice").forward(
						request, response);
			} catch (Exception e) {
				log.error(e);
			}
		}
		if ( oDev == null ) // abort if dev is still null
		{
			String errDetails = "Mandatory parameter 'dev' is missing or null. "+
			"The servlet needs a device to continue.";
			showErrorPage("mandatory parameter 'dev' is missing or null", errDetails, request, response);
			return;
		}
		
		AbstractDevice dev = getDeviceFromObject(oDev);
		if (dev == null)
		{
			String errorDetails = "the device type "+oDev.getClass().getName()+" is unknown"; 
			showErrorPage("unknown device type", errorDetails, request, response);
		}
		
		request.setAttribute("device", dev);
		request.setAttribute("bqu", dev.getQueueFacade());

		if ( command.equals("showDevice") )
		{
			try {
				String devProcClass = dev.getClass().getName();
				if ( ("org.cip4.bambi.SimDevice").equals(devProcClass) )
					request.getRequestDispatcher("/showDevice.jsp").forward(request, response);
				else if ( ("org.cip4.bambi.CustomDevice").equals(devProcClass) )
				{
					JobPhase currentPhase = ((CustomDevice)dev).getCurrentJobPhase();
					request.setAttribute("currentPhase", currentPhase);
					request.getRequestDispatcher("/showCustomDevice.jsp").forward(request, response);
				}
				else
				{
					String errorDetails = "the Device Processor class is '"+devProcClass+
					"', this class is not known by the DeviceInfoServlet";
					showErrorPage("class of device processor is unknown", errorDetails, request, response);
				}

			} catch (Exception e) {
				log.error(e);
			}
		} else if ( command.equals("processNextPhase") )
		{
			if ( !(dev instanceof CustomDevice) )
			{
				String errorDetails="command 'processNextPhase' is not supported for "+dev.getDeviceType();
				showErrorPage("invalid command", errorDetails, request, response);
				return;
			}
			
			JobPhase nextPhase = buildJobPhaseFromRequest(request);
			((CustomDevice)dev).doNextJobPhase(nextPhase);
			
			JobPhase currentPhase = ((CustomDevice)dev).getCurrentJobPhase();
			request.setAttribute("currentPhase", currentPhase);
			request.getRequestDispatcher("/showCustomDevice.jsp").forward(request, response);
		}

	}


	/**
	 * build a new job phase with info from a given request. 
	 * JobPhase parameter 'duration' will remain with its default value 0, since it
	 * is not used in the context of CustomDevice.doNextJobPhase()
	 * @param request request to get the job phase info from
	 * @return the new JobPhase
	 */
	private JobPhase buildJobPhaseFromRequest(HttpServletRequest request) {
		JobPhase newPhase = new JobPhase();
		
		Object devStatus = request.getParameter("DeviceStatus");
		if (devStatus != null) {
			newPhase.deviceStatus = EnumDeviceStatus.getEnum( devStatus.toString() );
		}
		Object devStatusDetails = request.getParameter("DeviceStatusDetails");
		if (devStatusDetails != null) {
			newPhase.deviceStatusDetails = devStatusDetails.toString();
		}
		
		Object nodeStatus = request.getParameter("NodeStatus");
		if (devStatus != null) {
			newPhase.nodeStatus = EnumNodeStatus.getEnum( nodeStatus.toString() );
		}
		Object nodeStatusDetails = request.getParameter("NodeStatusDetails");
		if (nodeStatusDetails != null) {
			newPhase.nodeStatusDetails = nodeStatusDetails.toString();
		}
		
		newPhase.Output_Good = getDoubleFromRequest(request, "Good");
		newPhase.Output_Waste = getDoubleFromRequest(request, "Waste");
		
		return newPhase;
	}

	/**
	 * extract a double attribute from a given request
	 * @param request
	 * @param param
	 * @return
	 */
	private double getDoubleFromRequest(HttpServletRequest request, String param)
	{
		double d = 0.0;
		Object dObj = request.getParameter(param);
		if (dObj != null) {
			try {
				d = Double.valueOf( dObj.toString() ).doubleValue();
			} catch (NumberFormatException ex) {
				log.error("value of attribute '"+param+"' is not a valid double: "+dObj.toString());
			}
			
		}	
		return d;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		log.info("called doPost");
	}

}
