/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2026 The International Cooperation for the Integration of
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights
 * reserved.
 */
package org.cip4.bambi.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.jdflib.util.CPUTimer;
import org.junit.Test;

public class BambiLogFactoryTest extends BambiTestCaseBase
{
	private static class Marker
	{
		// marker class for class-based log lookup
	}

	private static class TestLogFactory extends BambiLogFactory
	{
		public TestLogFactory()
		{
			super();
		}

		public TestLogFactory(final Class<?> clazz)
		{
			super(clazz);
		}

		public CPUTimer exposeGlobalTimer()
		{
			return getGlobalTimer();
		}

		public CPUTimer exposeLocalTimer()
		{
			return getLocalTimer();
		}
	}

	@Test
	public void testPrintStackTraceChain()
	{
		final Throwable leaf = new IllegalArgumentException("leaf");
		leaf.setStackTrace(createFrames("leaf", 14));

		final Throwable middle = new RuntimeException("middle", leaf);
		middle.setStackTrace(createFrames("middle", 15));

		final Throwable top = new IllegalStateException("top", middle);
		top.setStackTrace(createFrames("top", 16));

		final String trace = BambiLogFactory.printStackTrace(top, 5);
		assertTrue(trace.contains("caused by:"));
		assertTrue(trace.contains("IllegalStateException top"));
		assertTrue(trace.contains("RuntimeException middle"));
		assertTrue(trace.contains("IllegalArgumentException leaf"));
		assertTrue(trace.contains("..."));
	}

	@Test
	public void testGetLogCreatedDeleted()
	{
		final long createdBefore = BambiLogFactory.countPlus;
		final BambiLogFactory factory = new BambiLogFactory();
		assertNotNull(factory.getLog());
		assertTrue(factory.getCreated() > createdBefore);
		assertTrue(factory.getDeleted() >= 0);
	}

	@Test
	public void testGetTimers()
	{
		final TestLogFactory factory = new TestLogFactory();
		assertNotNull(factory.exposeGlobalTimer());
		assertNotNull(factory.exposeLocalTimer());
	}

	@Test
	public void testGetLogForClass()
	{
		final TestLogFactory factory = new TestLogFactory(Marker.class);
		assertNotNull(factory.getLog());
		assertNotNull(BambiLogFactory.getLog(Marker.class));
	}

	private static StackTraceElement[] createFrames(final String cls, final int size)
	{
		final StackTraceElement[] frames = new StackTraceElement[size];
		for (int i = 0; i < size; i++)
		{
			frames[i] = new StackTraceElement(cls, "m" + i, cls + ".java", i + 1);
		}
		return frames;
	}
}