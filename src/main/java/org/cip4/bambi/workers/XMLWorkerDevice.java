package org.cip4.bambi.workers;

import java.util.Iterator;
import java.util.Vector;

import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.XMLDevice;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.resource.process.JDFEmployee;

/**
 * @author Rainer Prosi
 */
public class XMLWorkerDevice extends XMLDevice
{

	/**
	 * XML representation of this simDevice fore use as html display using an XSLT
	 * @param addProcs if true, add processor elements
	 * @param request
	 * @param workerDevice TODO
	 */
	public XMLWorkerDevice(WorkerDevice workerDevice, final boolean addProcs, final ContainerRequest request)
	{
		super(workerDevice, addProcs, request);
		final KElement deviceRoot = getRoot();
		deviceRoot.setAttribute(AttributeName.TYPEEXPRESSION, getParentDevice().getProperties().getTypeExpression());
		deviceRoot.setAttribute("login", true, null);
		addEmployees();
		addKnownEmployees();
	}

	/**
	 * 
	 */
	private void addKnownEmployees()
	{
		final KElement deviceRoot = getRoot();
		final KElement knownEmps = deviceRoot.appendElement("KnownEmployees");
		final Vector<JDFEmployee> vEmpLoggedIn = getParentDevice().getStatusListener().getStatusCounter().getEmpoyees();
		final Vector<JDFEmployee> vEmp = getParentDevice().employees.vEmp;
		for (int i = 0; i < vEmp.size(); i++)
		{
			final JDFEmployee knownEmp = vEmp.get(i);
			boolean bAdd = true;
			if (vEmpLoggedIn != null)
			{
				for (JDFEmployee employee : vEmpLoggedIn)
				{
					if (knownEmp.matches(employee))
					{
						bAdd = false;
						break;
					}

				}
			}
			if (bAdd)
			{
				knownEmps.copyElement(knownEmp, null);
			}
		}
	}

	/**
	 * add the currently logged employees, duh
	 */
	private void addEmployees()
	{
		final KElement deviceRoot = getRoot();
		final Vector<JDFEmployee> vEmp = getParentDevice().getStatusListener().getStatusCounter().getEmpoyees();
		for (final Iterator<JDFEmployee> iterator = vEmp.iterator(); iterator.hasNext();)
		{
			final JDFEmployee employee = iterator.next();
			deviceRoot.copyElement(employee, null);
		}
	}

	/**
	 * 
	 * @see org.cip4.bambi.core.XMLDevice#getParentDevice()
	 */
	@Override
	protected WorkerDevice getParentDevice()
	{
		return (WorkerDevice) super.getParentDevice();
	}
}