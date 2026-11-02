/*
 * The CIP4 Software License, Version 1.0
 */
package org.cip4.bambi.workers.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilderFactory;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestProp;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.XMLDevice;
import org.cip4.bambi.workers.JobPhase;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ConsoleDeviceProcessorTest extends BambiTestCaseBase
{
	private BambiTestProp prop;
	private ConsoleDevice device;

	@Before
	public void setUpDevice()
	{
		prop = new BambiTestProp();
		createActionsFile(prop);
		device = new ConsoleDevice(prop);
	}

	@Test
	public void testConstruct()
	{
		final ConsoleDeviceProcessor processor = (ConsoleDeviceProcessor) device.buildDeviceProcessor();
		assertNotNull(processor);
	}

	@Test
	public void testInit()
	{
		final ConsoleDeviceProcessor processor = (ConsoleDeviceProcessor) device.buildDeviceProcessor();
		processor.setParent(device);
		final StatusListener statusListener = new StatusListener(null, device.getDeviceID(), null);
		processor.init(device.getQueueProcessor(), statusListener, prop);
		assertNotNull(processor.getStatusListener());
	}

	@Test
	public void testDoNextPhaseFinal()
	{
		final ConsoleDeviceProcessor processor = (ConsoleDeviceProcessor) device.getProcessor(null, 0);
		assertNotNull(processor);

		final JobPhase phase = new JobPhase();
		phase.setDeviceStatus(EnumDeviceStatus.Idle);
		phase.setNodeStatus(EnumNodeStatus.Completed);

		final ContainerRequest req = createRequest("processNextPhase");
		req.setParameter("pos", "99");
		processor.doNextPhase(phase, req);

		assertEquals(99, processor.currentAction);
		assertEquals(EnumQueueEntryStatus.Completed, processor.processDoc(null, null));
	}

	@Test
	public void testXMLConsoleProcessorFill()
	{
		final ContainerRequest req = createRequest("showDevice");
		final XMLDevice xmlDevice = device.getXMLDevice(true, req);
		assertNotNull(xmlDevice);

		final String xml = xmlDevice.toXML();
		assertNotNull(xml);

		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		final Document doc;
		try
		{
			doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
		}
		catch (final Exception x)
		{
			throw new IllegalStateException("unable to parse XMLConsoleDevice output", x);
		}

		assertTrue(doc.getElementsByTagNameNS("*", "Processor").getLength() > 0);
		assertTrue(doc.getElementsByTagNameNS("*", "PhaseAction").getLength() > 0);
	}

	private ContainerRequest createRequest(final String context)
	{
		final ContainerRequest req = new ContainerRequest();
		req.setRequestURI("http://host/bambi/" + context);
		return req;
	}

	private void createActionsFile(final BambiTestProp testProp)
	{
		try
		{
			final File configDir = testProp.getConfigDir();
			configDir.mkdirs();
			final File actionsFile = new File(configDir, "actions_" + testProp.getDeviceID() + ".xml");
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