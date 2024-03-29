/*--------------------------------------------------------------------------------------------------
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2022 The International Cooperation for the Integration of
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
 */
package org.cip4.bambi.messaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.core.messaging.StatusSignalComparator;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoMISDetails.EnumDeviceOperationMode;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFJobPhase;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.node.JDFActivity;
import org.cip4.jdflib.resource.JDFDevice;
import org.cip4.jdflib.resource.JDFEvent;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.JDFDate;
import org.junit.Test;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class StatusSignalComparatorTest extends BambiTestCase
{
	/**
	 * 
	 */
	@Test
	public void testIsSameStatusSignal()
	{
		final JMFBuilder b = new JMFBuilder();
		final JDFJMF jmf = b.createJMF(EnumFamily.Signal, EnumType.Status);
		final JDFSignal signal = jmf.getSignal(0);
		final JDFDeviceInfo di = signal.getCreateDeviceInfo(0);
		di.setDeviceID("d1");
		final JDFJMF jmf2 = (JDFJMF) jmf.clone();
		final JDFSignal signal2 = jmf2.getSignal(0);
		final JDFDeviceInfo di2 = signal2.getCreateDeviceInfo(0);
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		final JDFJobPhase jp = di.appendJobPhase();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		final JDFJobPhase jp2 = di2.appendJobPhase();
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		jp.setStatus(EnumNodeStatus.Waiting);
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		jp2.setStatus(EnumNodeStatus.Waiting);
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
	}

	/**
	 * 
	 */
	@Test
	public void testIsSameStatusSignalActivity()
	{
		final JMFBuilder b = new JMFBuilder();
		final JDFJMF jmf = b.createJMF(EnumFamily.Signal, EnumType.Status);
		final JDFSignal signal = jmf.getSignal(0);
		final JDFDeviceInfo di = signal.getCreateDeviceInfo(0);
		di.setDeviceID("d1");
		final JDFJMF jmf2 = (JDFJMF) jmf.clone();
		final JDFSignal signal2 = jmf2.getSignal(0);
		final JDFDeviceInfo di2 = signal2.getCreateDeviceInfo(0);
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		final JDFJobPhase jp = di.appendJobPhase();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		final JDFJobPhase jp2 = di2.appendJobPhase();
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		JDFActivity a = jp.appendActivity();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		jp2.appendActivity();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		a.deleteNode();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
	}

	/**
	 * 
	 */
	@Test
	public void testIsSameStatusSignalNotification()
	{
		final JMFBuilder b = new JMFBuilder();
		final JDFJMF jmf = b.createJMF(EnumFamily.Signal, EnumType.Status);
		final JDFSignal signal = jmf.getSignal(0);
		final JDFDeviceInfo di = signal.getCreateDeviceInfo(0);
		di.setDeviceID("d1");
		final JDFJMF jmf2 = (JDFJMF) jmf.clone();
		final JDFSignal signal2 = jmf2.getSignal(0);
		final JDFDeviceInfo di2 = signal2.getCreateDeviceInfo(0);
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		final JDFJobPhase jp = di.appendJobPhase();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		final JDFJobPhase jp2 = di2.appendJobPhase();
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		JDFNotification a = signal.appendNotification();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		signal2.appendNotification();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		a.deleteNode();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
	}

	/**
	 * 
	 */
	@Test
	public void testIsSameStatusSignalEvent()
	{
		final JMFBuilder b = new JMFBuilder();
		final JDFJMF jmf = b.createJMF(EnumFamily.Signal, EnumType.Status);
		final JDFSignal signal = jmf.getSignal(0);
		final JDFDeviceInfo di = signal.getCreateDeviceInfo(0);
		di.setDeviceID("d1");
		final JDFJMF jmf2 = (JDFJMF) jmf.clone();
		final JDFSignal signal2 = jmf2.getSignal(0);
		final JDFDeviceInfo di2 = signal2.getCreateDeviceInfo(0);
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		final JDFJobPhase jp = di.appendJobPhase();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		final JDFJobPhase jp2 = di2.appendJobPhase();
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		JDFEvent e = di.appendEvent();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		di2.appendEvent();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		e.deleteNode();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
	}

	/**
	 * 
	 */
	@Test
	public void testIsSameStatusSignalIdle()
	{
		final JMFBuilder b = new JMFBuilder();
		final JDFJMF jmf = b.createJMF(EnumFamily.Signal, EnumType.Status);
		final JDFSignal signal = jmf.getSignal(0);
		final JDFDeviceInfo di1 = signal.getCreateDeviceInfo(0);
		di1.setDeviceID("d1");
		final JDFJMF jmf2 = (JDFJMF) jmf.clone();
		final JDFSignal signal2 = jmf2.getSignal(0);
		final JDFDeviceInfo di2 = signal2.getCreateDeviceInfo(0);

		assertTrue(di1.isSamePhase(di2, false));
		final JDFDate date = new JDFDate();
		di1.setIdleStartTime(date);
		di2.setIdleStartTime(date);
		assertTrue(di1.isSamePhase(di2, false));
		di1.setDeviceStatus(EnumDeviceStatus.Idle);
		di2.setDeviceStatus(EnumDeviceStatus.Idle);
		assertTrue(di1.isSamePhase(di2, false));
		di1.setDeviceOperationMode(EnumDeviceOperationMode.Productive);
		di2.setDeviceOperationMode(EnumDeviceOperationMode.Productive);
		assertTrue(di1.isSamePhase(di2, false));
		final JDFDevice dev = di1.appendDevice();
		dev.setDeviceID("d1");
		dev.setDeviceType("foo");
		di2.copyElement(dev, null);
		assertTrue(di1.isSamePhase(di2, false));
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		final JDFJobPhase jp = di1.appendJobPhase();
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		final JDFJobPhase jp2 = di2.appendJobPhase();
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		jp.setStatus(EnumNodeStatus.Waiting);
		assertFalse(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
		jp2.setStatus(EnumNodeStatus.Waiting);
		assertTrue(new StatusSignalComparator().isSameStatusSignal(signal, signal2));
	}

}
