package org.cip4.bambi.workers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestDevice;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.resource.process.JDFEmployee;
import org.junit.Test;

public class XMLWorkerDeviceTest extends BambiTestCaseBase
{
	private BambiTestDevice createDevice()
	{
		return (BambiTestDevice) getDevice(false, true);
	}

	private JDFEmployee createEmployee(final String personalID)
	{
		final JDFEmployee employee = (JDFEmployee) new JDFDoc("Employee").getRoot();
		employee.setPersonalID(personalID);
		return employee;
	}

	private ContainerRequest createRequest(final String context, final WorkerDevice device)
	{
		final ContainerRequest request = new ContainerRequest();
		request.setRequestURI("http://host/bambi/" + context + "/" + device.getDeviceID());
		return request;
	}

	private boolean containsEmployee(final VElement employees, final String personalID)
	{
		for (int i = 0; i < employees.size(); i++)
		{
			final KElement employee = employees.get(i);
			if (personalID.equals(employee.getAttribute(AttributeName.PERSONALID)))
			{
				return true;
			}
		}
		return false;
	}

	@Test
	public void testConstructAddsEmployees()
	{
		final BambiTestDevice device = createDevice();
		device.employees = device.new EmployeeList(new Vector<JDFEmployee>());
		final JDFEmployee loggedIn = createEmployee("emp-login");
		device.getStatusListener().addEmployee(loggedIn);

		final XMLWorkerDevice xmlWorkerDevice = new XMLWorkerDevice(device, true, createRequest("showDevice", device));
		final KElement root = xmlWorkerDevice.getRoot();
		assertEquals("XMLDevice", root.getNodeName());
		assertEquals("true", root.getAttribute("login"));
		assertNotNull(root.getAttribute(AttributeName.TYPEEXPRESSION));
		assertTrue(containsEmployee(root.getChildElementVector("Employee", null), "emp-login"));
	}

	@Test
	public void testKnownEmployees()
	{
		final BambiTestDevice device = createDevice();
		final JDFEmployee loggedIn = createEmployee("emp-logged");
		final JDFEmployee knownOnly = createEmployee("emp-known-only");

		final Vector<JDFEmployee> knownEmployees = new Vector<JDFEmployee>();
		knownEmployees.add(loggedIn);
		knownEmployees.add(knownOnly);
		device.employees = device.new EmployeeList(knownEmployees);
		device.getStatusListener().addEmployee(loggedIn);

		final XMLWorkerDevice xmlWorkerDevice = new XMLWorkerDevice(device, true, createRequest("showDevice", device));
		final KElement knownRoot = xmlWorkerDevice.getRoot().getElement("KnownEmployees", null, 0);
		assertNotNull(knownRoot);
		final VElement employees = knownRoot.getChildElementVector("Employee", null);
		assertEquals(1, employees.size());
		assertTrue(containsEmployee(employees, "emp-known-only"));
	}

	@Test
	public void testGetParentDevice()
	{
		final BambiTestDevice device = createDevice();
		device.employees = device.new EmployeeList(new Vector<JDFEmployee>());
		final XMLWorkerDevice xmlWorkerDevice = new XMLWorkerDevice(device, false, createRequest("showDevice", device));
		assertSame(device, xmlWorkerDevice.getParentDevice());
	}
}
