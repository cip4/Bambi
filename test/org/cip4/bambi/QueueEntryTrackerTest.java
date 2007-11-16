/*
*
* The CIP4 Software License, Version 1.0
*
*
* Copyright (c) 2001-2007 The International Cooperation for the Integration of 
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

package org.cip4.bambi;

import java.io.File;

import org.cip4.bambi.proxy.IQueueEntryTracker;
import org.cip4.bambi.proxy.QueueEntryTracker;



public class QueueEntryTrackerTest extends BambiTestCase {
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		File configFile=new File( sm_dirTestTemp+"null.tracker" );
		if (configFile.exists() ) {
			configFile.delete();
		}
		
	}
	
	public void testAddEntries() 
	{
		IQueueEntryTracker qt=new QueueEntryTracker(sm_dirTestTemp, null);
		qt.addEntry("in_1", "out_1", "dev_1", "devUrl_1");
		assertEquals( 1, qt.countTracked() );
		
		qt.addEntry("in_1", "out_1", "dev_1", "devUrl_1");
		assertEquals( 1, qt.countTracked() );
	}
	
	public void testRemoveEntries() 
	{
		IQueueEntryTracker qt=new QueueEntryTracker(sm_dirTestTemp, null);
		qt.addEntry("in_1", "out_1", "dev_1", "devUrl_1");		
		qt.addEntry("in_2", "out_2", "dev_2", "devUrl_2");
		assertEquals( 2, qt.countTracked() );
		
		qt.removeEntry("in_3");
		assertEquals( 2, qt.countTracked() );
		
		qt.removeEntry("in_1");
		assertEquals( 1, qt.countTracked() );
	}
	
	public void testPersistResume() {
		{
			IQueueEntryTracker qt=new QueueEntryTracker(sm_dirTestTemp, null);
			qt.addEntry("in_1", "out_1", "dev_1", "devUrl_1");		
			qt.addEntry("in_2", "out_2", "dev_2", "devUrl_2");
		}
		
		IQueueEntryTracker qt=new QueueEntryTracker(sm_dirTestTemp, null);
		assertEquals( 2, qt.countTracked() );
		assertEquals( "out_1",qt.getOutgoingQEID("in_1") );
		assertEquals( "out_2",qt.getOutgoingQEID("in_2") );
		assertEquals( "in_1",qt.getIncomingQEID("out_1") );
		assertEquals( "in_2",qt.getIncomingQEID("out_2") );
		assertEquals( "devUrl_1",qt.getDeviceURL("in_1") );
		assertEquals( "devUrl_2",qt.getDeviceURL("in_2") );
		assertEquals( "dev_1",qt.getDeviceID("in_1") );
		assertEquals( "dev_2",qt.getDeviceID("in_2") );
	}

}
