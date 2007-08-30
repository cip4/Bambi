/**
 * 
 */
package org.cip4.bambi.servlets;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cip4.bambi.AbstractDevice;
import org.cip4.bambi.CustomDevice;
import org.cip4.bambi.SimDevice;

/**
 * mother of all Bambi servlets
 * @author boegerni
 *
 */
public abstract class AbstractBambiServlet extends HttpServlet {
	
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
	}
	
	/**
	 * display an error on  error.jsp
	 * @param errorMsg short message describing the error
	 * @param errorDetails detailed error info
	 * @param request required to forward the page
	 * @param response required to forward the page
	 */
	protected void showErrorPage(String errorMsg, String errorDetails, HttpServletRequest request, HttpServletResponse response)
	{
		request.setAttribute("errorOrigin", this.getClass().getName());
		request.setAttribute("errorMsg", errorMsg);
		request.setAttribute("errorDetails", errorDetails);

		try {
			request.getRequestDispatcher("/error.jsp").forward(request, response);
		} catch (ServletException e) {
			System.err.println("failed to show error.jsp");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("failed to show error.jsp");
			e.printStackTrace();
		}
	}
	
	/**
	 * get a Device instance from a given object
	 * @param oDev
	 * @return null if oDev is null or invalid, otherwise the SimDevice or CustomDevice.
	 */
	protected AbstractDevice getDeviceFromObject(Object oDev) {
		if (oDev == null)
			return null;
			
		AbstractDevice dev;
		if (oDev instanceof SimDevice)
			dev = (SimDevice)oDev;
		else if (oDev instanceof CustomDevice)
			dev = (CustomDevice)oDev;
		else		
			return null;
		return dev;
	}
}
