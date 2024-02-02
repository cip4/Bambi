/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */

package org.cip4.bambi.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestDevice;
import org.cip4.bambi.core.queues.QueueEntry;
import org.cip4.bambi.workers.WorkerDeviceProcessor;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.node.JDFNode;
import org.junit.Test;

public class AbstractDeviceProcessorTest extends BambiTestCaseBase
{

	/**
	 * @throws Exception
	 */
	@Test
	public void testToString() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		final WorkerDeviceProcessor devProc = device.buildDeviceProcessor();
		assertNotNull(devProc.toString());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testActive() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		final WorkerDeviceProcessor devProc = device.buildDeviceProcessor();
		devProc._doShutdown = false;
		devProc.setCurrentQE(null);
		assertFalse(devProc.isActive());
		devProc.setCurrentQE(new QueueEntry(JDFNode.createRoot(), (JDFQueueEntry) JDFElement.createRoot(ElementName.QUEUEENTRY)));
		assertTrue(devProc.isActive());
		devProc.shutdown();
		assertFalse(devProc.isActive());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testFillCurrent() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setFinalStatus(EnumQueueEntryStatus.Aborted);
		final WorkerDeviceProcessor devProc = device.buildDeviceProcessor();
		for (int i = 0; i < 111; i++)
			devProc.fillCurrentQE();

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testfinalizeProcessDocNull() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		device.setFinalStatus(EnumQueueEntryStatus.Aborted);
		final WorkerDeviceProcessor devProc = device.buildDeviceProcessor();
		assertNotNull(devProc.processDoc(null, null));

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testProcessExistingQueueEntry() throws Exception
	{
		final BambiTestDevice device = new BambiTestDevice();
		int n = 0;
		for (final Object o : EnumQueueEntryStatus.getEnumList())
		{
			final EnumQueueEntryStatus qes = (EnumQueueEntryStatus) o;
			device.setFinalStatus(qes);
			final WorkerDeviceProcessor devProc = device.getNewProcessor();
			if (devProc.processExistingQueueEntry())
				n++;
		}
		assertEquals(3, n);
	}

}
