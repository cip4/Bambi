package org.cip4.bambi.core.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.cip4.bambi.BambiTestCase;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.jmf.JDFAbortQueueEntryParams;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFFlushQueueParams;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.junit.Test;

public class QueueModifyHandlerTest extends BambiTestCase
{
	private QueueProcessor getFreshQueueProcessor()
	{
		final QueueProcessor qp = getDevice(false, false).getQueueProcessor();
		qp.getQueue().flush();
		qp.getQueue().setQueueStatus(EnumQueueStatus.Waiting);
		return qp;
	}

	private JDFQueueEntry appendQueueEntry(final QueueProcessor qp, final String qeid, final EnumQueueEntryStatus status)
	{
		final JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID(qeid);
		qe.setQueueEntryStatus(status);
		return qe;
	}

	private JDFCommand createCommand(final EnumType type)
	{
		return JDFJMF.createJMF(EnumFamily.Command, type).getCommand(0);
	}

	private JDFResponse createResponse(final EnumType type)
	{
		return JDFJMF.createJMF(EnumFamily.Response, type).getResponse(0);
	}

	@Test
	public void testAbortQueueEntryMissingParamsReturnsError()
	{
		final QueueProcessor qp = getFreshQueueProcessor();
		final JDFCommand command = createCommand(EnumType.AbortQueueEntry);
		final JDFResponse response = createResponse(EnumType.AbortQueueEntry);

		qp.new AbortQueueEntryHandler().handleMessage(command, response);

		assertEquals(105, response.getReturnCode());
	}

	@Test
	public void testAbortQueueEntryUnknownQueueEntryReturnsError()
	{
		final QueueProcessor qp = getFreshQueueProcessor();
		final JDFCommand command = createCommand(EnumType.AbortQueueEntry);
		command.appendQueueEntryDef().setQueueEntryID("unknown-qe");
		final JDFResponse response = createResponse(EnumType.AbortQueueEntry);

		qp.new AbortQueueEntryHandler().handleMessage(command, response);

		assertEquals(105, response.getReturnCode());
	}

	@Test
	public void testAbortQueueEntryWaitingTransitionsToAborted()
	{
		final QueueProcessor qp = getFreshQueueProcessor();
		final String qeid = "abort-waiting";
		final JDFQueueEntry qe = appendQueueEntry(qp, qeid, EnumQueueEntryStatus.Waiting);
		final JDFCommand command = createCommand(EnumType.AbortQueueEntry);
		final JDFAbortQueueEntryParams params = (JDFAbortQueueEntryParams) command.appendElement(ElementName.ABORTQUEUEENTRYPARAMS);
		params.getCreateQueueFilter().appendQueueEntryDef(qeid);
		final JDFResponse response = createResponse(EnumType.AbortQueueEntry);

		qp.new AbortQueueEntryHandler().handleMessage(command, response);

		assertEquals(0, response.getReturnCode());
		assertEquals(EnumQueueEntryStatus.Aborted, qe.getQueueEntryStatus());
	}

	@Test
	public void testSuspendQueueEntryRunningAndWaitingBranches()
	{
		final QueueProcessor qp = getFreshQueueProcessor();
		final String runningID = "suspend-running";
		final String waitingID = "suspend-waiting";
		final JDFQueueEntry runningQE = appendQueueEntry(qp, runningID, EnumQueueEntryStatus.Running);
		final JDFQueueEntry waitingQE = appendQueueEntry(qp, waitingID, EnumQueueEntryStatus.Waiting);

		final JDFCommand runningCommand = createCommand(EnumType.SuspendQueueEntry);
		runningCommand.appendQueueEntryDef().setQueueEntryID(runningID);
		final JDFResponse runningResponse = createResponse(EnumType.SuspendQueueEntry);
		qp.new SuspendQueueEntryHandler().handleMessage(runningCommand, runningResponse);
		assertEquals(0, runningResponse.getReturnCode());
		assertEquals(EnumQueueEntryStatus.Suspended, runningQE.getQueueEntryStatus());

		final JDFCommand waitingCommand = createCommand(EnumType.SuspendQueueEntry);
		waitingCommand.appendQueueEntryDef().setQueueEntryID(waitingID);
		final JDFResponse waitingResponse = createResponse(EnumType.SuspendQueueEntry);
		qp.new SuspendQueueEntryHandler().handleMessage(waitingCommand, waitingResponse);
		assertEquals(115, waitingResponse.getReturnCode());
		assertEquals(EnumQueueEntryStatus.Waiting, waitingQE.getQueueEntryStatus());
	}

	@Test
	public void testHoldQueueEntryWaitingAndCompletedBranches()
	{
		final QueueProcessor qp = getFreshQueueProcessor();
		final String waitingID = "hold-waiting";
		final String completedID = "hold-completed";
		final JDFQueueEntry waitingQE = appendQueueEntry(qp, waitingID, EnumQueueEntryStatus.Waiting);
		final JDFQueueEntry completedQE = appendQueueEntry(qp, completedID, EnumQueueEntryStatus.Completed);

		final JDFCommand waitingCommand = createCommand(EnumType.HoldQueueEntry);
		waitingCommand.appendQueueEntryDef().setQueueEntryID(waitingID);
		final JDFResponse waitingResponse = createResponse(EnumType.HoldQueueEntry);
		qp.new HoldQueueEntryHandler().handleMessage(waitingCommand, waitingResponse);
		assertEquals(0, waitingResponse.getReturnCode());
		assertEquals(EnumQueueEntryStatus.Held, waitingQE.getQueueEntryStatus());

		final JDFCommand completedCommand = createCommand(EnumType.HoldQueueEntry);
		completedCommand.appendQueueEntryDef().setQueueEntryID(completedID);
		final JDFResponse completedResponse = createResponse(EnumType.HoldQueueEntry);
		qp.new HoldQueueEntryHandler().handleMessage(completedCommand, completedResponse);
		assertEquals(114, completedResponse.getReturnCode());
		assertEquals(EnumQueueEntryStatus.Completed, completedQE.getQueueEntryStatus());
	}

	@Test
	public void testResumeQueueEntryHeldAndCompletedBranches()
	{
		final QueueProcessor qp = getFreshQueueProcessor();
		final String heldID = "resume-held";
		final String completedID = "resume-completed";
		final JDFQueueEntry heldQE = appendQueueEntry(qp, heldID, EnumQueueEntryStatus.Held);
		final JDFQueueEntry completedQE = appendQueueEntry(qp, completedID, EnumQueueEntryStatus.Completed);

		final JDFCommand heldCommand = createCommand(EnumType.ResumeQueueEntry);
		heldCommand.appendQueueEntryDef().setQueueEntryID(heldID);
		final JDFResponse heldResponse = createResponse(EnumType.ResumeQueueEntry);
		qp.new ResumeQueueEntryHandler().handleMessage(heldCommand, heldResponse);
		assertEquals(0, heldResponse.getReturnCode());
		assertEquals(EnumQueueEntryStatus.Waiting, heldQE.getQueueEntryStatus());

		final JDFCommand completedCommand = createCommand(EnumType.ResumeQueueEntry);
		completedCommand.appendQueueEntryDef().setQueueEntryID(completedID);
		final JDFResponse completedResponse = createResponse(EnumType.ResumeQueueEntry);
		qp.new ResumeQueueEntryHandler().handleMessage(completedCommand, completedResponse);
		assertEquals(115, completedResponse.getReturnCode());
		assertEquals(EnumQueueEntryStatus.Completed, completedQE.getQueueEntryStatus());
	}

	@Test
	public void testRemoveQueueEntryWaitingSuccessAndRunningError()
	{
		final QueueProcessor qp = getFreshQueueProcessor();
		final String waitingID = "remove-waiting";
		final String runningID = "remove-running";
		final JDFQueueEntry waitingQE = appendQueueEntry(qp, waitingID, EnumQueueEntryStatus.Waiting);
		final JDFQueueEntry runningQE = appendQueueEntry(qp, runningID, EnumQueueEntryStatus.Running);

		final JDFCommand waitingCommand = createCommand(EnumType.RemoveQueueEntry);
		waitingCommand.appendQueueEntryDef().setQueueEntryID(waitingID);
		final JDFResponse waitingResponse = createResponse(EnumType.RemoveQueueEntry);
		qp.new RemoveQueueEntryHandler().handleMessage(waitingCommand, waitingResponse);
		assertEquals(0, waitingResponse.getReturnCode());
		assertEquals(EnumQueueEntryStatus.Removed, waitingQE.getQueueEntryStatus());

		final JDFCommand runningCommand = createCommand(EnumType.RemoveQueueEntry);
		runningCommand.appendQueueEntryDef().setQueueEntryID(runningID);
		final JDFResponse runningResponse = createResponse(EnumType.RemoveQueueEntry);
		qp.new RemoveQueueEntryHandler().handleMessage(runningCommand, runningResponse);
		assertEquals(106, runningResponse.getReturnCode());
		assertEquals(EnumQueueEntryStatus.Running, runningQE.getQueueEntryStatus());
	}

	@Test
	public void testFlushQueueAllAndFiltered()
	{
		final QueueProcessor qp = getFreshQueueProcessor();
		appendQueueEntry(qp, "flush-1", EnumQueueEntryStatus.Waiting);
		appendQueueEntry(qp, "flush-2", EnumQueueEntryStatus.Waiting);

		final JDFCommand filteredCommand = createCommand(EnumType.FlushQueue);
		final JDFFlushQueueParams params = (JDFFlushQueueParams) filteredCommand.appendElement(ElementName.FLUSHQUEUEPARAMS);
		params.getCreateQueueFilter().appendQueueEntryDef("flush-1");
		final JDFResponse filteredResponse = createResponse(EnumType.FlushQueue);
		qp.new FlushQueueHandler().handleMessage(filteredCommand, filteredResponse);
		assertEquals(0, filteredResponse.getReturnCode());
		assertEquals(1, qp.getQueue().getQueueSize());

		final JDFCommand allCommand = createCommand(EnumType.FlushQueue);
		final JDFResponse allResponse = createResponse(EnumType.FlushQueue);
		qp.new FlushQueueHandler().handleMessage(allCommand, allResponse);
		assertEquals(0, allResponse.getReturnCode());
		assertEquals(0, qp.getQueue().getQueueSize());
	}

	@Test
	public void testModifyQueueStatusHoldAndResume()
	{
		final QueueProcessor qp = getFreshQueueProcessor();
		final JDFCommand holdCommand = createCommand(EnumType.HoldQueue);
		final JDFResponse holdResponse = createResponse(EnumType.HoldQueue);
		qp.new HoldQueueHandler().handleMessage(holdCommand, holdResponse);
		assertEquals(0, holdResponse.getReturnCode());
		assertEquals(EnumQueueStatus.Held, qp.getQueue().getQueueStatus());

		final JDFCommand resumeCommand = createCommand(EnumType.ResumeQueue);
		final JDFResponse resumeResponse = createResponse(EnumType.ResumeQueue);
		qp.new ResumeQueueHandler().handleMessage(resumeCommand, resumeResponse);
		assertEquals(0, resumeResponse.getReturnCode());
		assertEquals(EnumQueueStatus.Waiting, qp.getQueue().getQueueStatus());
	}

	@Test
	public void testOpenCloseQueueHandlers()
	{
		final QueueProcessor qp = getFreshQueueProcessor();
		final JDFCommand closeCommand = createCommand(EnumType.CloseQueue);
		final JDFResponse closeResponse = createResponse(EnumType.CloseQueue);
		qp.new CloseQueueHandler().handleMessage(closeCommand, closeResponse);
		assertEquals(0, closeResponse.getReturnCode());
		assertEquals(EnumQueueStatus.Closed, qp.getQueue().getQueueStatus());

		final JDFCommand openCommand = createCommand(EnumType.OpenQueue);
		final JDFResponse openResponse = createResponse(EnumType.OpenQueue);
		qp.new OpenQueueHandler().handleMessage(openCommand, openResponse);
		assertEquals(0, openResponse.getReturnCode());
		assertNotEquals(EnumQueueStatus.Closed, qp.getQueue().getQueueStatus());
	}
}
