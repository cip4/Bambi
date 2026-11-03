package org.cip4.bambi.core.messaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.cip4.bambi.BambiTestCaseBase;
import org.junit.Test;

public class TriggerTest extends BambiTestCaseBase
{
	private static class TriggerStub extends Trigger
	{
		private final boolean waitValue;

		TriggerStub(final String channelID, final int amount, final boolean waitValue)
		{
			super(channelID, amount);
			this.waitValue = waitValue;
		}

		@Override
		public boolean waitQueued(final int milliseconds)
		{
			return waitValue;
		}
	}

	@Test
	public void testWaitQueuedArrayBranches()
	{
		assertTrue(Trigger.waitQueued(null, 5));
		assertFalse(Trigger.waitQueued(new Trigger[] { new TriggerStub("c1", 1, false) }, 5));
		assertTrue(Trigger.waitQueued(new Trigger[] { new TriggerStub("c1", 1, true), new TriggerStub("c2", 2, true) }, 5));
	}

	@Test
	public void testWaitQueuedUnblocksWhenQueued() throws Exception
	{
		final Trigger trigger = new Trigger("channelAsync", 1);
		final CountDownLatch started = new CountDownLatch(1);
		final AtomicBoolean waitResult = new AtomicBoolean(false);

		final Thread waiter = new Thread(() -> {
			started.countDown();
			waitResult.set(trigger.waitQueued(42));
		});
		waiter.start();

		assertTrue(started.await(1, TimeUnit.SECONDS));
		trigger.setQueued();
		waiter.join(1000);

		assertTrue(waitResult.get());
	}
}
