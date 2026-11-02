package org.cip4.bambi.workers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestProcessor;
import org.cip4.bambi.BambiTestProp;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.resource.process.JDFEmployee;
import org.junit.Test;

public class UIModifiableDeviceTest extends BambiTestCaseBase
{
	private static class TestUIModifiableDevice extends UIModifiableDevice
	{
		private JobPhase currentJobPhase;
		private boolean processNextPhaseCalled;

		private TestUIModifiableDevice()
		{
			super(new BambiTestProp(true));
		}

		@Override
		protected AbstractDeviceProcessor buildDeviceProcessor()
		{
			return new BambiTestProcessor(EnumQueueEntryStatus.Completed);
		}

		@Override
		public JobPhase getCurrentJobPhase()
		{
			return currentJobPhase;
		}

		public void setCurrentJobPhase(final JobPhase currentJobPhase)
		{
			this.currentJobPhase = currentJobPhase;
		}

		public JobPhase buildJobPhase(final ContainerRequest request)
		{
			return buildJobPhaseFromRequest(request);
		}

		@Override
		protected XMLResponse processNextPhase(final ContainerRequest request)
		{
			processNextPhaseCalled = true;
			return showDevice(request, false);
		}

		public boolean isProcessNextPhaseCalled()
		{
			return processNextPhaseCalled;
		}
	}

	private TestUIModifiableDevice createDevice()
	{
		return new TestUIModifiableDevice();
	}

	private JDFEmployee createEmployee(final String personalID)
	{
		final JDFEmployee employee = (JDFEmployee) new JDFDoc("Employee").getRoot();
		employee.setPersonalID(personalID);
		return employee;
	}

	private ContainerRequest createRequest(final String context, final UIModifiableDevice device)
	{
		final ContainerRequest request = new ContainerRequest();
		request.setRequestURI("http://host/bambi/" + context + "/" + device.getDeviceID());
		return request;
	}

	private boolean hasEmployee(final Vector<JDFEmployee> employees, final String personalID)
	{
		if (employees == null)
		{
			return false;
		}
		for (final JDFEmployee employee : employees)
		{
			if (employee != null && employee.matches(personalID))
			{
				return true;
			}
		}
		return false;
	}

	@Test
	public void testBuildJobPhaseStatus()
	{
		final TestUIModifiableDevice device = createDevice();
		final ContainerRequest request = createRequest("processNextPhase", device);
		request.setParameter("DeviceStatus", EnumDeviceStatus.Running.getName());
		request.setParameter("NodeStatus", EnumNodeStatus.Completed.getName());

		final JobPhase phase = device.buildJobPhase(request);
		assertEquals(EnumDeviceStatus.Running, phase.getDeviceStatus());
		assertEquals(EnumNodeStatus.Completed, phase.getNodeStatus());
		assertEquals(0, phase.getDurationMillis());
	}

	@Test
	public void testBuildJobPhaseAmounts()
	{
		final TestUIModifiableDevice device = createDevice();
		final ContainerRequest request = createRequest("processNextPhase", device);
		request.setParameter("Res0", "Component");
		request.setParameter("Speed0", "3600");
		request.setParameter("Waste0", "false");
		request.setParameter("Res1", "Media");
		request.setParameter("Speed1", "1800");
		request.setParameter("Waste1", "true");
		request.setParameter("Res2", "ExposedMedia");
		request.setParameter("Speed2", "600");
		request.setParameter("Waste2", "false");

		final JobPhase phase = device.buildJobPhase(request);
		assertEquals(3, phase.getPhaseAmountResourceNames().size());
		assertEquals(3600.0, phase.getOutputSpeed("Component", null), 0.001);
		assertFalse(phase.getOutputAmountIsGood("Media"));
		assertTrue(phase.getOutputAmountIsGood("ExposedMedia"));
	}

	@Test
	public void testBuildJobPhaseDuration()
	{
		final TestUIModifiableDevice device = createDevice();
		final JobPhase current = new JobPhase();
		current.setDurationMillis(4200);
		device.setCurrentJobPhase(current);

		final ContainerRequest explicitRequest = createRequest("processNextPhase", device);
		explicitRequest.setParameter(AttributeName.DURATION, "7.5");
		final JobPhase explicitDuration = device.buildJobPhase(explicitRequest);
		assertEquals(7000, explicitDuration.getDurationMillis());

		final ContainerRequest inheritedRequest = createRequest("processNextPhase", device);
		final JobPhase inheritedDuration = device.buildJobPhase(inheritedRequest);
		assertEquals(4200, inheritedDuration.getDurationMillis());
	}

	@Test
	public void testHandleLoginIn()
	{
		final TestUIModifiableDevice device = createDevice();
		final JDFEmployee employee = createEmployee("emp-login");
		final Vector<JDFEmployee> knownEmployees = new Vector<JDFEmployee>();
		knownEmployees.add(employee);
		device.employees = device.new EmployeeList(knownEmployees);

		final ContainerRequest request = createRequest("login", device);
		request.setParameter(AttributeName.PERSONALID, "emp-login anyName");
		final XMLResponse response = device.handleGet(request);
		assertNotNull(response);
		assertTrue(hasEmployee(device.getStatusListener().getStatusCounter().getEmpoyees(), "emp-login"));
	}

	@Test
	public void testHandleLoginOut()
	{
		final TestUIModifiableDevice device = createDevice();
		final JDFEmployee employee = createEmployee("emp-logout");
		final Vector<JDFEmployee> knownEmployees = new Vector<JDFEmployee>();
		knownEmployees.add(employee);
		device.employees = device.new EmployeeList(knownEmployees);
		device.getStatusListener().addEmployee(employee);

		final ContainerRequest request = createRequest("login", device);
		request.setParameter(AttributeName.PERSONALID, "emp-logout user");
		request.setParameter("inout", "logout");
		final XMLResponse response = device.handleGet(request);
		assertNotNull(response);
		assertFalse(hasEmployee(device.getStatusListener().getStatusCounter().getEmpoyees(), "emp-logout"));
	}

	@Test
	public void testHandleGetRouting()
	{
		final TestUIModifiableDevice device = createDevice();
		final ContainerRequest processRequest = createRequest("processNextPhase", device);
		final XMLResponse processResponse = device.handleGet(processRequest);
		assertNotNull(processResponse);
		assertTrue(device.isProcessNextPhaseCalled());

		final ContainerRequest unknownRequest = createRequest("unknownContext", device);
		final XMLResponse unknownResponse = device.handleGet(unknownRequest);
		assertNull(unknownResponse);
	}
}
