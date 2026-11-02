/*
 * The CIP4 Software License, Version 1.0
 */
package org.cip4.bambi.workers.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Vector;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestProp;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.XMLDevice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConsoleDeviceTest extends BambiTestCaseBase
{
	private BambiTestProp testProp;

	@Before
	public void setUp()
	{
		testProp = new BambiTestProp();
		cleanActionFixtures(testProp);
	}

	@After
	public void tearDown()
	{
		cleanActionFixtures(testProp);
	}

	@Test
	public void testConstruct()
	{
		final ConsoleDevice console = new ConsoleDevice(testProp);
		assertNotNull(console);
		final Vector<ConsoleDevice.PhaseAction> loadedActions = console.getActions(-1);
		assertTrue(loadedActions == null || loadedActions.isEmpty());
	}

	@Test
	public void testActionLoaderNoFile()
	{
		cleanActionFixtures(testProp);
		final ConsoleDevice console = new ConsoleDevice(testProp);
		final Vector<ConsoleDevice.PhaseAction> loadedActions = console.getActions(-1);
		assertTrue(loadedActions == null || loadedActions.isEmpty());
	}

	@Test
	public void testGetActionsRoot()
	{
		createActionsFile(testProp);
		final ConsoleDevice console = new ConsoleDevice(testProp);

		final Vector<ConsoleDevice.PhaseAction> rootActions = console.getActions(-1);
		assertNotNull(rootActions);
		assertEquals(3, rootActions.size());
	}

	@Test
	public void testGetActionsNext()
	{
		createActionsFile(testProp);
		final ConsoleDevice console = new ConsoleDevice(testProp);

		final Vector<ConsoleDevice.PhaseAction> nextActions = console.getActions(0);
		assertNotNull(nextActions);
		assertEquals(2, nextActions.size());
	}

	@Test
	public void testGetActionsOutOfRange()
	{
		createActionsFile(testProp);
		final ConsoleDevice console = new ConsoleDevice(testProp);

		assertNull(console.getActions(99));
	}

	@Test
	public void testGetXMLDeviceSetup()
	{
		final ConsoleDevice console = new ConsoleDevice(testProp);
		final ContainerRequest req = createRequest("showDevice");
		req.setParameter("setup", "true");

		final XMLDevice xmlDevice = console.getXMLDevice(false, req);
		assertNotNull(xmlDevice);
		assertTrue(xmlDevice instanceof XMLConsoleDevice);
	}

	@Test
	public void testGetXMLDeviceProc()
	{
		createActionsFile(testProp);
		final ConsoleDevice console = new ConsoleDevice(testProp);
		final ContainerRequest req = createRequest("showDevice");

		final XMLDevice xmlDevice = console.getXMLDevice(true, req);
		assertNotNull(xmlDevice);
		assertTrue(xmlDevice instanceof XMLConsoleDevice);
	}

	@Test
	public void testGetXSLT()
	{
		final ConsoleDevice console = new ConsoleDevice(testProp);
		final ContainerRequest req = createRequest("showDevice");

		final String xslt = console.getXSLT(req);
		assertNotNull(xslt);
		assertEquals("/legacy/showDevice.xsl", xslt);
	}

	private ContainerRequest createRequest(final String context)
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/bambi/" + context);
		return req;
	}

	private void cleanActionFixtures(final BambiTestProp prop)
	{
		if (prop == null)
		{
			return;
		}
		deleteActionFixtures(prop.getConfigDir());
		deleteActionFixtures(getCachedConfigDir(prop));
	}

	private File getCachedConfigDir(final BambiTestProp prop)
	{
		return new File(prop.getBaseDir(), "config");
	}

	private void deleteActionFixtures(final File dir)
	{
		if (dir == null || !dir.isDirectory())
		{
			return;
		}
		final File[] actionFiles = dir.listFiles((f, name) -> name != null && name.startsWith("actions") && name.endsWith(".xml"));
		if (actionFiles == null)
		{
			return;
		}
		for (final File actionFile : actionFiles)
		{
			if (actionFile.isFile() && !actionFile.delete())
			{
				actionFile.deleteOnExit();
			}
		}
	}

	private void createActionsFile(final BambiTestProp prop)
	{
		try
		{
			final File configDir = prop.getConfigDir();
			configDir.mkdirs();
			final File actionsFile = new File(configDir, "actions_" + prop.getDeviceID() + ".xml");
			final String xml = "<Actions>"
					+ "<PhaseAction NodeStatus='Waiting' NodeStatusDetails='wait' DeviceStatus='Idle' DeviceStatusDetails='idle' DescriptiveName='Start' Next='1 2'/>"
					+ "<PhaseAction NodeStatus='InProgress' NodeStatusDetails='running' DeviceStatus='Idle' DeviceStatusDetails='idle' DescriptiveName='Run' Next='2'/>"
					+ "<PhaseAction NodeStatus='Completed' NodeStatusDetails='done' DeviceStatus='Idle' DeviceStatusDetails='idle' DescriptiveName='End'/>"
					+ "</Actions>";
			Files.writeString(actionsFile.toPath(), xml, StandardCharsets.UTF_8);
		}
		catch (final IOException x)
		{
			throw new IllegalStateException("unable to write actions fixture", x);
		}
	}
}