package org.cip4.bambi.core.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.jdflib.jmf.JDFAcknowledge;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AcknowledgeMapTest extends BambiTestCaseBase
{
	private AcknowledgeMap acknowledgeMap;

	@Before
	public void setUpAcknowledgeMap() throws Exception
	{
		acknowledgeMap = AcknowledgeMap.getMap();
		resetAcknowledgeMap();
	}

	@After
	public void tearDownAcknowledgeMap() throws Exception
	{
		resetAcknowledgeMap();
	}

	@Test
	public void testGetMapSingletonIdentity()
	{
		assertSame(AcknowledgeMap.getMap(), AcknowledgeMap.getMap());
	}

	@Test
	public void testHandleAcknowledgeUnknownChannelSetsError()
	{
		final JDFResponse response = createResponse();
		final JDFAcknowledge acknowledge = createAcknowledge("unknown-" + System.nanoTime());
		assertTrue(acknowledgeMap.handleMessage(acknowledge, response));
		assertEquals(6, response.getReturnCode());
	}

	@Test
	public void testPutAndHandleAcknowledgeMatchedHandler()
	{
		final String refID = "match-" + System.nanoTime();
		final TestResponseHandler handler = new TestResponseHandler(true, false);
		acknowledgeMap.addHandler(refID, handler);
		assertTrue(acknowledgeMap.handleMessage(createAcknowledge(refID), createResponse()));
		assertEquals(1, handler.handleCalls);
		assertNotNull(handler.finalMessage);
		assertEquals(refID, ((JDFAcknowledge) handler.finalMessage).getrefID());
	}

	@Test
	public void testDoneHandledDedupeIgnoresSecondAcknowledge()
	{
		final String refID = "dedupe-" + System.nanoTime();
		final TestResponseHandler handler = new TestResponseHandler(true, false);
		acknowledgeMap.addHandler(refID, handler);
		assertTrue(acknowledgeMap.handleMessage(createAcknowledge(refID), createResponse()));
		final JDFResponse secondResponse = createResponse();
		assertTrue(acknowledgeMap.handleMessage(createAcknowledge(refID), secondResponse));
		assertEquals(1, handler.handleCalls);
		assertEquals(0, secondResponse.getReturnCode());
	}

	@Test
	public void testCleanupRemovesExpiredAbortedEntries() throws Exception
	{
		final String abortedRefID = "aborted-" + System.nanoTime();
		final String activeRefID = "active-" + System.nanoTime();
		setLastCleanup(System.currentTimeMillis());
		acknowledgeMap.addHandler(abortedRefID, new TestResponseHandler(true, true));
		final TestResponseHandler activeHandler = new TestResponseHandler(true, false);
		acknowledgeMap.addHandler(activeRefID, activeHandler);
		assertEquals(2, getHandlerMap().size());

		setLastCleanup(System.currentTimeMillis() - 3600001L);
		final String triggerRefID = "trigger-" + System.nanoTime();
		acknowledgeMap.addHandler(triggerRefID, new TestResponseHandler(true, false));

		assertFalse(getHandlerMap().containsKey(abortedRefID));
		assertTrue(getHandlerMap().containsKey(activeRefID));
		assertTrue(getDoneHandled().contains(abortedRefID));
		final JDFResponse unknownResponse = createResponse();
		assertTrue(acknowledgeMap.handleMessage(createAcknowledge(abortedRefID), unknownResponse));
		assertEquals(0, unknownResponse.getReturnCode());

		assertTrue(acknowledgeMap.handleMessage(createAcknowledge(activeRefID), createResponse()));
		assertEquals(1, activeHandler.handleCalls);
	}

	@Test
	public void testHandleMessageNullInputThrowsNPE()
	{
		try
		{
			acknowledgeMap.handleMessage(null, createResponse());
			fail("Expected NullPointerException for null acknowledge input");
		}
		catch (final NullPointerException expected)
		{
			// expected: AcknowledgeMap assumes non-null input message
		}
	}

	@Test
	public void testAcknowledgeThreadConstructAndSynchronousHelpers()
	{
		final TestMessageHandler baseHandler = new TestMessageHandler();
		final AcknowledgeThread acknowledgeThread = new AcknowledgeThread(baseHandler, getDevice(false, true));
		acknowledgeThread.setWaitForSych(5);

		assertTrue(acknowledgeThread.isAcknowledge());
		assertFalse(acknowledgeThread.isSubScribable());
		assertEquals(baseHandler.getMessageType(), acknowledgeThread.getMessageType());
		assertEquals(baseHandler.getFamilies()[0], acknowledgeThread.getFamilies()[0]);
		assertTrue(acknowledgeThread.toString().contains("AcknowledgeThread"));

		final JDFMessage inputMessage = JDFJMF.createJMF(EnumFamily.Query, EnumType.Status).getQuery(0);
		assertTrue(acknowledgeThread.handleMessage(inputMessage, createResponse()));
		assertEquals(1, baseHandler.handleCalls);
	}

	private JDFResponse createResponse()
	{
		return JDFJMF.createJMF(EnumFamily.Response, EnumType.Status).getResponse();
	}

	private JDFAcknowledge createAcknowledge(final String refID)
	{
		final JDFAcknowledge acknowledge = JDFJMF.createJMF(EnumFamily.Acknowledge, EnumType.Status).getAcknowledge(0);
		acknowledge.setrefID(refID);
		acknowledge.setID("ack-" + refID);
		return acknowledge;
	}

	private void resetAcknowledgeMap() throws Exception
	{
		final Field mapField = AcknowledgeMap.class.getDeclaredField("theMap");
		mapField.setAccessible(true);
		((Map<?, ?>) mapField.get(acknowledgeMap)).clear();

		final Field doneHandledField = AcknowledgeMap.class.getDeclaredField("doneHandled");
		doneHandledField.setAccessible(true);
		((Set<?>) doneHandledField.get(acknowledgeMap)).clear();

		setLastCleanup(0L);
	}

	private void setLastCleanup(final long value) throws Exception
	{
		final Field lastCleanupField = AcknowledgeMap.class.getDeclaredField("lastCleanup");
		lastCleanupField.setAccessible(true);
		lastCleanupField.setLong(acknowledgeMap, value);
	}

	@SuppressWarnings("unchecked")
	private Map<String, IResponseHandler> getHandlerMap() throws Exception
	{
		final Field mapField = AcknowledgeMap.class.getDeclaredField("theMap");
		mapField.setAccessible(true);
		return (Map<String, IResponseHandler>) mapField.get(acknowledgeMap);
	}

	@SuppressWarnings("unchecked")
	private Set<String> getDoneHandled() throws Exception
	{
		final Field doneHandledField = AcknowledgeMap.class.getDeclaredField("doneHandled");
		doneHandledField.setAccessible(true);
		return (Set<String>) doneHandledField.get(acknowledgeMap);
	}

	private static class TestMessageHandler implements IMessageHandler
	{
		private int handleCalls;

		@Override
		public boolean isSubScribable()
		{
			return true;
		}

		@Override
		public boolean isAcknowledge()
		{
			return false;
		}

		@Override
		public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
		{
			handleCalls++;
			return true;
		}

		@Override
		public String getMessageType()
		{
			return "Status";
		}

		@Override
		public EnumFamily[] getFamilies()
		{
			return new EnumFamily[] { EnumFamily.Query };
		}
	}

	private static class TestResponseHandler implements IResponseHandler
	{
		private final boolean handleResult;
		private final boolean aborted;
		private int handleCalls;
		private JDFMessage finalMessage;

		private TestResponseHandler(final boolean handleResult, final boolean aborted)
		{
			this.handleResult = handleResult;
			this.aborted = aborted;
		}

		@Override
		public boolean handleMessage()
		{
			handleCalls++;
			return handleResult;
		}

		@Override
		public HttpURLConnection getConnection()
		{
			return null;
		}

		@Override
		public void setConnection(final HttpURLConnection con)
		{
		}

		@Override
		public void setCallBack(final IConverterCallback _callBack)
		{
		}

		@Override
		public void setBufferedStream(final ByteArrayIOStream bis)
		{
		}

		@Override
		public void waitHandled(final int wait1, final int wait2, final boolean abortTimeOut)
		{
		}

		@Override
		public boolean isAborted()
		{
			return aborted;
		}

		@Override
		public void setMessage(final JDFMessage response)
		{
			this.finalMessage = response;
		}

		@Override
		public JDFResponse getResponse()
		{
			return null;
		}

		@Override
		public JDFMessage getFinalMessage()
		{
			return finalMessage;
		}
	}
}
