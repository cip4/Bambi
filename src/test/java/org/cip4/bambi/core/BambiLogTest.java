/*
 * The CIP4 Software License, Version 1.0
 */
package org.cip4.bambi.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.logging.Log;
import org.cip4.bambi.BambiTestCaseBase;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class BambiLogTest extends BambiTestCaseBase
{
	@Test
	public void testDebugErrorInfoWithoutThrowablePrefixCaller()
	{
		final Log log = mock(Log.class);
		final BambiLog bambiLog = new BambiLog(log);

		bambiLog.debug("debug-msg");
		bambiLog.error("error-msg");
		bambiLog.info("info-msg");

		final ArgumentCaptor<Object> debugMessage = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<Object> errorMessage = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<Object> infoMessage = ArgumentCaptor.forClass(Object.class);

		verify(log).debug(debugMessage.capture());
		verify(log).error(errorMessage.capture());
		verify(log).info(infoMessage.capture());

		assertPrefixed(debugMessage.getValue().toString(), "debug-msg");
		assertPrefixed(errorMessage.getValue().toString(), "error-msg");
		assertPrefixed(infoMessage.getValue().toString(), "info-msg");
	}

	@Test
	public void testFatalTraceWarnWithThrowablePrefixCaller()
	{
		final Log log = mock(Log.class);
		final BambiLog bambiLog = new BambiLog(log);
		final RuntimeException x = new RuntimeException("boom");

		bambiLog.fatal("fatal-msg", x);
		bambiLog.trace("trace-msg", x);
		bambiLog.warn("warn-msg", x);

		final ArgumentCaptor<Object> fatalMessage = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<Object> traceMessage = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<Object> warnMessage = ArgumentCaptor.forClass(Object.class);

		verify(log).fatal(fatalMessage.capture(), same(x));
		verify(log).trace(traceMessage.capture(), same(x));
		verify(log).warn(warnMessage.capture(), same(x));

		assertPrefixed(fatalMessage.getValue().toString(), "fatal-msg");
		assertPrefixed(traceMessage.getValue().toString(), "trace-msg");
		assertPrefixed(warnMessage.getValue().toString(), "warn-msg");
	}

	@Test
	public void testEnabledFlagsDelegation()
	{
		final Log log = mock(Log.class);
		when(log.isDebugEnabled()).thenReturn(true);
		when(log.isErrorEnabled()).thenReturn(false);
		when(log.isFatalEnabled()).thenReturn(true);
		when(log.isInfoEnabled()).thenReturn(false);
		when(log.isTraceEnabled()).thenReturn(true);
		when(log.isWarnEnabled()).thenReturn(false);

		final BambiLog bambiLog = new BambiLog(log);

		assertTrue(bambiLog.isDebugEnabled());
		assertFalse(bambiLog.isErrorEnabled());
		assertTrue(bambiLog.isFatalEnabled());
		assertFalse(bambiLog.isInfoEnabled());
		assertTrue(bambiLog.isTraceEnabled());
		assertFalse(bambiLog.isWarnEnabled());
	}

	@Test
	public void testAllSingleArgumentLevelMethods()
	{
		final Log log = mock(Log.class);
		final BambiLog bambiLog = new BambiLog(log);

		bambiLog.fatal("fatal-only");
		bambiLog.trace("trace-only");
		bambiLog.warn("warn-only");

		final ArgumentCaptor<Object> fatalMessage = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<Object> traceMessage = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<Object> warnMessage = ArgumentCaptor.forClass(Object.class);

		verify(log).fatal(fatalMessage.capture());
		verify(log).trace(traceMessage.capture());
		verify(log).warn(warnMessage.capture());

		assertPrefixed(fatalMessage.getValue().toString(), "fatal-only");
		assertPrefixed(traceMessage.getValue().toString(), "trace-only");
		assertPrefixed(warnMessage.getValue().toString(), "warn-only");
	}

	private void assertPrefixed(final String msg, final String payload)
	{
		assertTrue(msg.contains("BambiLogTest"));
		assertTrue(msg.contains(" - "));
		assertTrue(msg.endsWith(payload));
	}
}