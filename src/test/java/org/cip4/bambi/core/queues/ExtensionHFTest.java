/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2010 The International Cooperation for the Integration of 
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

package org.cip4.bambi.core.queues;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

import jakarta.mail.MessagingException;

import org.cip4.bambi.core.BambiContainerTest;
import org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.extensions.XJDF20;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.junit.Ignore;
import org.junit.Test;

/**
 * test for the various queue processor functions
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * 03.12.2008
 */
public class ExtensionHFTest extends BambiContainerTest
{
	File inputHF = null;
	File outputHF = null;

	/**
	 * @param devProp
	 */
	@Override
	protected void moreSetup(final DeviceProperties devProp)
	{
		inputHF = new File(sm_dirTestData + "ContainerTest/InputHF");
		outputHF = new File(sm_dirTestData + "ContainerTest/OutputHF");
		devProp.setInputHF(inputHF);
		devProp.setOutputHF(outputHF);
	}

	/**
	 * @throws IOException
	 * @throws MessagingException
	 */
    @Test
	@Ignore
	public void testSubmitXJDF_HF() throws IOException, MessagingException
	{
		for (int i = 0; i < 1; i++)
		{
			_theGT.devID = null;
			//			_theGT.devID = "sim001";
			_theGT.assign(null);
			if (i != 0)
			{
				ThreadUtil.sleep(1000);
			}
			System.out.println("Submit " + i);
			final XJDF20 conv = new XJDF20();
			final KElement xjdf = conv.makeNewJDF(_theGT.getNode(), null);

			final File fileInDirectory = FileUtil.getFileInDirectory(inputHF, new File("test.xjdf"));
			xjdf.getOwnerDocument_KElement().write2File(fileInDirectory, 2, false);

			ThreadUtil.sleep(10000);
			assertFalse(fileInDirectory.exists());
		}

	}
}
