package org.cip4.bambi.workers.sim;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IDevice;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.workers.core.AbstractWorkerServlet;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.util.UrlUtil;

public class SimWorkerServlet extends AbstractWorkerServlet {
	protected static final Log log = LogFactory.getLog(SimWorkerServlet.class.getName());
	private static final long serialVersionUID = 431025409853435322L;
	
	@Override
	protected IDevice buildDevice(IDeviceProperties prop) {
		SimDevice dev=new SimDevice(prop);
        _getHandlers.add(0,dev);
		return dev;
	}

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet_(HttpServletRequest request, HttpServletResponse response)
    {
    	log.info("Processing get request...");
    	
    	String command = request.getParameter("cmd");
        if(command==null)
        {
            command=UrlUtil.getLocalURL(request.getContextPath(),request.getRequestURI());
        }
    	if (command == null || command.length() == 0) {
    		request.setAttribute("devices", getDevices());
    		try {
    			request.getRequestDispatcher("/overview.jsp").forward(request, response);
    		} catch (Exception e) {
    			log.error(e);
    		} 
    	} else if ( command.equals("showDevice") || 
    			command.equals("processNextPhase") || command.equals("finalizeCurrentQE") )
    	{
    		IDevice dev=getDeviceFromRequest(request);
    		if (dev!=null)
    		{
    			request.setAttribute("device", dev);
    			showDevice(request,response);
    		} else {
    			showErrorPage("can't get device", "device ID missing or unknown", request, response);
    			return;
    		}
    	} else if ( command.endsWith("QueueEntry") )  {
    		IDevice dev=getDeviceFromRequest(request);
    		if (dev!=null)
    		{
    			request.setAttribute("device", dev);
    			try {
    				request.getRequestDispatcher("QEController").forward(request, response);
    			} catch (Exception e) {
    				log.error(e);
    			}
    		} else {
    			log.error("can't get device, device ID is missing or unknown");
    		}
    	} else if ( command.equals("showJDFDoc") ) {
    		String qeid=request.getParameter("qeid");
    		if ( (qeid!=null&&qeid.length()>0) ) {
    			String filePath=_devProperties.getJDFDir()+qeid+".jdf";
    			JDFDoc theDoc=JDFDoc.parseFile(filePath);
    			if (theDoc!=null) {
    				writeRawResponse( request,response,theDoc.toXML() );
    			} else {
    				log.error( "cannot parse '"+filePath+"'" );
    				return;
    			}
    		}
    	}
    }
	
}
