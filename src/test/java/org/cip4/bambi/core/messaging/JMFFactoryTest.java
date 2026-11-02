package org.cip4.bambi.core.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.jdflib.auto.JDFAutoDeviceFilter.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.junit.After;
import org.junit.Test;

public class JMFFactoryTest extends BambiTestCaseBase
{
	private static class DummyOptimizer implements IMessageOptimizer
	{
		@Override
		public optimizeResult optimize(final JDFMessage newMessage, final JDFMessage oldMessage)
		{
			return optimizeResult.noCheck;
		}
	}

	@After
	public void tearDownFactory()
	{
		JMFFactory.shutdown();
	}

	@Test
	public void testSingletonAndDeprecatedAccessorReturnSameInstance()
	{
		final JMFFactory factory = JMFFactory.getInstance();
		assertSame(factory, JMFFactory.getInstance());
		assertSame(factory, JMFFactory.getJMFFactory());
	}

	@Test
	public void testFlagsToStringAndMessageDetailsNullAndReliableSignalPath()
	{
		final JMFFactory factory = JMFFactory.getInstance();
		factory.setLogLots(true);
		factory.setZapp500(true);

		assertTrue(factory.isLogLots());
		assertTrue(factory.isZapp500());
		assertTrue(factory.toString().contains("threads="));

		final MessageDetails nullMessage = new MessageDetails((JDFJMF) null, null, null, null, "http://example.invalid");
		assertEquals("Null_JMF", nullMessage.getName());
		assertNull(nullMessage.getContentType());

		MessageDetails.setAllSignalsReliable(true);
		final JDFJMF signalJmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails reliableSignal = new MessageDetails(signalJmf, null, null, null, "http://example.invalid");
		assertFalse(reliableSignal.isFireForget());
		MessageDetails.setAllSignalsReliable(false);
	}

	@Test
	public void testCallURLNormalizationComparisonAndNullBaseURL()
	{
		final JMFFactory.CallURL normalized = new JMFFactory.CallURL("http://example.com/a/b/c/?x=1");
		final JMFFactory.CallURL base = new JMFFactory.CallURL("http://example.com/a");

		assertEquals(base, normalized);
		assertEquals(0, base.compareTo(normalized));
		assertTrue(base.compareTo(null) > 0);
		assertNull(base.getBaseURL(null));
		assertNotNull(base.toString());
	}

	@Test
	public void testAddAndGetOptimizerWithNullGuards()
	{
		final JMFFactory factory = JMFFactory.getInstance();
		final DummyOptimizer optimizer = new DummyOptimizer();

		factory.addOptimizer(null, optimizer);
		factory.addOptimizer(EnumType.Status, null);
		assertNull(factory.getOptimizer(null));
		assertNull(factory.getOptimizer(EnumType.Status));

		factory.addOptimizer(EnumType.Status, optimizer);
		assertSame(optimizer, factory.getOptimizer(EnumType.Status));
	}

	@Test
	public void testValidationFailuresDoNotQueueNetworkWork()
	{
		final JMFFactory factory = JMFFactory.getInstance();
		assertFalse(factory.send2URL(null, null, null));
		assertFalse(factory.send2URL(null, null, null, null, null));
		assertFalse(factory.send2URL(null, null, "http://example.invalid", null, null, null, "dev"));
		assertFalse(factory.send2URL(null, null, null, null, null, null));
	}

	@Test
	public void testCreateSenderCachingAndShutdownByCallURL()
	{
		final JMFFactory factory = JMFFactory.getInstance();
		assertNull(factory.getCreateMessageSender(null));

		final MessageSender sender1 = factory.getCreateMessageSender("http://example.com/a/b?x=1");
		final MessageSender sender2 = factory.getCreateMessageSender("http://example.com/a");
		assertSame(sender1, sender2);

		final Vector<MessageSender> oneSender = factory.getMessageSenders("http://example.com/a/c?y=2");
		assertEquals(1, oneSender.size());
		assertSame(sender1, oneSender.firstElement());

		factory.shutDown(new JMFFactory.CallURL("http://example.com/a"), false);
		final MessageSender sender3 = factory.getCreateMessageSender("http://example.com/a");
		assertTrue(sender3 != sender1);
	}
}
