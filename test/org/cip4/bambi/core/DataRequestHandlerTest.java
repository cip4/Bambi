/**
 * The CIP4 Software License, Version 1.0
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
package org.cip4.bambi.core;

import java.io.File;
import java.io.InputStream;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.FileUtil;

/**
 *  
 * @author rainer prosi
 * @date November 4, 2010
 */
public class DataRequestHandlerTest extends BambiTestCase
{
	boolean extern;

	/**
	 * @param devProp
	 */
	@Override
	protected void moreSetup(DeviceProperties devProp)
	{
		if (extern)
			devProp.setDeviceClassName("org.cip4.bambi.core.HandlerDevice");

	}

	/**
	 * 
	 *  
	 * @throws Exception its a test!
	 */
	public void testHandle() throws Exception
	{
		extern = false;
		File f = new File(sm_dirContainer + File.separator + deviceID + File.separator + "JDF" + File.separator + "qeID" + File.separator + "foo.txt");
		FileUtil.createNewFile(f);
		f = FileUtil.streamToFile(new ByteArrayIOStream("blahblah".getBytes()).getInputStream(), f);
		System.out.println(f.getAbsolutePath());
		assertNotNull(f);
		startContainer();
		StreamRequest sr = new StreamRequest((InputStream) null);
		sr.setPost(false);
		sr.setRequestURI("http://dummy:8080/war/data/" + deviceID + "/qeID/foo.txt");
		XMLResponse resp = bambiContainer.processStream(sr);
		assertNotNull(resp);
		InputStream is = resp.getInputStream();
		assertNotNull(is);

		byte bb[] = new byte[20];
		int l = is.read(bb);
		String bbb = new String(bb, 0, l);
		assertEquals("blahblah", bbb);
	}

	/**
	* 
	*  
	* @throws Exception its a test!
	*/
	public void testHandleExtern() throws Exception
	{
		extern = true;
		File f = new File("/tmp/blub/qeID/foo.txt");
		FileUtil.createNewFile(f);
		f = FileUtil.streamToFile(new ByteArrayIOStream("blahblah".getBytes()).getInputStream(), f);
		System.out.println(f.getAbsolutePath());
		assertNotNull(f);
		startContainer();
		StreamRequest sr = new StreamRequest((InputStream) null);
		sr.setPost(false);
		sr.setRequestURI("http://dummy:8080/war/data/" + deviceID + "/qeID/foo.txt");
		XMLResponse resp = bambiContainer.processStream(sr);
		assertNotNull(resp);
		InputStream is = resp.getInputStream();
		assertNotNull(is);

		byte bb[] = new byte[20];
		int l = is.read(bb);
		String bbb = new String(bb, 0, l);
		assertEquals("blahblah", bbb);
	}
}
