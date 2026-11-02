/**
 *
 */
package org.cip4.bambi.messenger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestProp;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class MessengerDeviceTest extends BambiTestCaseBase
{
	private static class MessengerDeviceForTest extends MessengerDevice
	{
		MessengerDeviceForTest(final IDeviceProperties prop)
		{
			super(prop);
		}

		@Override
		protected QueueProcessor buildQueueProcessor()
		{
			return Mockito.mock(QueueProcessor.class);
		}

		AbstractDeviceProcessor callMessengerBuildDeviceProcessor()
		{
			return super.buildDeviceProcessor();
		}

		QueueProcessor callMessengerBuildQueueProcessor()
		{
			return super.buildQueueProcessor();
		}

		void callMessengerReloadQueue()
		{
			super.reloadQueue();
		}

		void callMessengerAddJobHandlers()
		{
			super.addJobHandlers();
		}
	}

	@Test
	public void testConstruct()
	{
		assertThrows(NullPointerException.class, () -> new MessengerDevice(new BambiTestProp()));
		final MessengerDeviceForTest safeDevice = new MessengerDeviceForTest(new BambiTestProp());
		assertNotNull(safeDevice);
	}

	@Test
	public void testBuildDeviceProcessorNull()
	{
		final MessengerDeviceForTest device = new MessengerDeviceForTest(new BambiTestProp());
		assertNull(device.callMessengerBuildDeviceProcessor());
	}

	@Test
	public void testBuildQueueProcessorNull()
	{
		final MessengerDeviceForTest device = new MessengerDeviceForTest(new BambiTestProp());
		assertNull(device.callMessengerBuildQueueProcessor());
	}

	@Test
	public void testCanAcceptNull()
	{
		final MessengerDeviceForTest device = new MessengerDeviceForTest(new BambiTestProp());
		assertNull(device.canAccept(null, null));
	}

	@Test
	public void testGetNodeFromDocNull()
	{
		final MessengerDeviceForTest device = new MessengerDeviceForTest(new BambiTestProp());
		assertNull(device.getNodeFromDoc(null));
	}

	@Test
	public void testReloadQueueNoop()
	{
		final MessengerDeviceForTest device = new MessengerDeviceForTest(new BambiTestProp());
		device.callMessengerReloadQueue();
	}

	@Test
	public void testAddHandlersNoop()
	{
		final MessengerDeviceForTest device = new MessengerDeviceForTest(new BambiTestProp());
		device.callMessengerAddJobHandlers();
	}

}
