/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2026 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.jdflib.node.JDFNode;
import org.junit.Test;

public class RootDeviceTest extends BambiTestCaseBase
{

	@Test
	public void testToString()
	{
		final InputStream resourceAsStream = getClass().getResourceAsStream("/config/devices.xml");
		if (resourceAsStream == null)
		{
			log.fatal("invalid resource stream ");
		}
		final MultiDeviceProperties mp = MultiDeviceProperties.getProperties(resourceAsStream);

		final RootDevice rd = new RootDevice(mp.createDeviceProps(0));
		assertNotNull(rd.toString());
	}

	@Test
	public void testXSLT()
	{
		final RootDevice rd = getRootDevice();
		final ContainerRequest request = new ContainerRequest();
		assertNotNull(rd.getXSLT(request));
		request.setParameter(RootDevice.XSL, "0");
		assertNull(rd.getXSLT(request));
	}

	@Test
	public void testGetDevice()
	{
		final RootDevice rd = getRootDevice();
		assertEquals(rd, rd.getDevice(null));
	}

	@Test
	public void testReset()
	{
		final RootDevice rd = getRootDevice();
		rd.reset();
		assertEquals(rd, rd.getDevice(null));
	}

	@Test
	public void testShutdown()
	{
		final RootDevice rd = getRootDevice();
		rd.shutdown();
		assertEquals(rd, rd.getDevice(null));
	}

	@Test
	public void testNodeFromDoc()
	{
		final RootDevice rd = getRootDevice();
		assertNull(rd.getNodeFromDoc(null));
		assertNull(rd.getNodeFromDoc(JDFNode.createRoot().getOwnerDocument_JDFElement()));
	}

	public RootDevice getRootDevice()
	{
		final InputStream resourceAsStream = getClass().getResourceAsStream("/config/devices.xml");
		final MultiDeviceProperties mp = MultiDeviceProperties.getProperties(resourceAsStream);
		return new RootDevice(mp.createDeviceProps(0));
	}

}
