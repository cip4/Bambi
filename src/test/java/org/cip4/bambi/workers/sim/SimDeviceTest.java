/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2026 The International Cooperation for the Integration of
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        The International Cooperation for the Integration of
 *        Processes in  Prepress, Press and Postpress (www.cip4.org)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of
 *    Processes in  Prepress, Press and Postpress" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4",
 *    nor may "CIP4" appear in their name, without prior written
 *    permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR
 * THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG
 * copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.workers.sim;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestDevice;
import org.cip4.bambi.BambiTestProp;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.XMLDevice;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.jdflib.auto.JDFAutoResourceQuParams;
import org.cip4.jdflib.core.JDFAudit.EnumAuditType;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.pool.JDFAuditPool;
import org.junit.Test;

public class SimDeviceTest extends BambiTestCaseBase
{

	@Test
	public void testCreate()
	{
		final SimDevice sim = new SimDevice(new BambiTestProp());
		assertNotNull(sim);
	}

	@Test
	public void testResource()
	{
		final SimDevice sim = new SimDevice(new BambiTestProp());
		final IMessageHandler qh = sim.getJMFHandler(null).getMessageHandler("Resource", EnumFamily.Query);
		assertNotNull(qh);
	}

	@Test
	public void testXML() throws IOException
	{
		final BambiTestDevice sim = (BambiTestDevice) getDevice(false, true);
		final JDFNode n = JDFNode.parseFile(sm_dirTestData + "IDPSimplex.jdf");
		final IQueueEntry iqe = sim.setCurrentEntry(n);
		final XMLDevice xd = sim.getXMLDevice(true, new ContainerRequest());
		assertNotNull(xd);
	}

	@Test
	public void testResourceNS()
	{
		final SimDevice sim = new SimDevice(new BambiTestProp());
		final JDFJMF jmf = JDFJMF.parseFile(sm_dirTestData + "config/resinfo.xml");
		final IMessageHandler qh = sim.new ResourceQueryHandler(jmf);
		final JDFJMF q = JDFJMF.createJMF(EnumFamily.Query, EnumType.Resource);
		q.getQuery().getCreateResourceQuParams(0).setScope(JDFAutoResourceQuParams.EScope.Allowed);
		final JDFJMF r = JDFJMF.createJMF(EnumFamily.Query, EnumType.Resource);
		qh.handleMessage(q.getQuery(), r.getCreateResponse(0));
		assertNotNull(KElement.parseString(r.toXML()));
	}

	public static BambiTestDevice getDevice(String devID) throws IOException
	{
		final BambiTestDevice rootDev = (BambiTestDevice) getDevice(true, true);
		rootDev.setSim(true);
		when(rootDev.getDeviceID()).thenReturn(devID);
		final File destDir = rootDev.getCachedConfigDir();
		if (!destDir.exists() || destDir.list().length == 0)
		{
			FileUtils.copyDirectory(new File(sm_dirTestData, "config"), destDir);
		}

		return rootDev;
	}

	@Test
	public void testProcessExisting() throws IOException
	{
		final BambiTestDevice rootDev = SimDeviceTest.getDevice("digi001");
		final JDFNode n = JDFNode.parseFile(sm_dirTestData + "IDPSimplex.jdf");
		final IQueueEntry iqe = rootDev.setCurrentEntry(n);

		final boolean ok = rootDev.doSynchronous(iqe);
		assertTrue(ok);
		n.write2File(sm_dirTestDataTemp + "IDPSimplex.done.jdf");
		final JDFAuditPool auditPool = n.getAuditPool();
		assertNotNull(auditPool.getAudit(0, EnumAuditType.PhaseTime, null, null));
		assertNotNull(auditPool.getAudit(0, EnumAuditType.ProcessRun, null, null));
		assertNotNull(auditPool.getAudit(0, EnumAuditType.ResourceAudit, null, null));
	}

}
