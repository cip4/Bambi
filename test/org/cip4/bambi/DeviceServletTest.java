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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import javax.mail.MessagingException;
import javax.mail.Multipart;

import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.bambi.workers.core.AbstractWorkerServlet;
import org.cip4.bambi.workers.sim.SimWorkerServlet;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.JDFResourceLink.EnumUsage;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.EnumType;
import org.cip4.jdflib.resource.process.JDFFileSpec;
import org.cip4.jdflib.resource.process.JDFRunList;
import org.cip4.jdflib.resource.process.prepress.JDFColorSpaceConversionParams;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

public class DeviceServletTest extends BambiTestCase {
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		abortRemoveAll(simWorkerUrl);
	}
	
	private JDFResponse singleMIMESubmit(String jobID) throws MalformedURLException, IOException, MessagingException
    {
        JDFDoc d1=new JDFDoc("JMF");
        d1.setOriginalFileName("JMF.jmf");
        JDFJMF jmf=d1.getJMFRoot();
        JDFCommand com=(JDFCommand) jmf.appendMessageElement(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.SubmitQueueEntry);
      
        com.appendQueueSubmissionParams().setURL("TheJDF");
        
        JDFDoc doc=new JDFDoc("JDF");
        doc.setOriginalFileName("JDF.jdf");  
        JDFNode n=doc.getJDFRoot();
        n.setJobID(jobID);
        n.setType(EnumType.ColorSpaceConversion);
        JDFColorSpaceConversionParams cscp=(JDFColorSpaceConversionParams) n.addResource(ElementName.COLORSPACECONVERSIONPARAMS, null, EnumUsage.Input, null, null, null, null);
        JDFFileSpec fs0=cscp.appendFinalTargetDevice();
        fs0.setURL(StringUtil.uncToUrl(sm_dirTestData+File.separator+"test.icc",true));
        JDFRunList rl=(JDFRunList)n.addResource(ElementName.RUNLIST, null, EnumUsage.Input, null, null, null, null);
        rl.addPDF(StringUtil.uncToUrl(sm_dirTestData+File.separator+"url3.pdf",false), 0, -1);

        Multipart m=MimeUtil.buildMimePackage(d1,doc);
        
        JDFDoc[] d2=MimeUtil.getJMFSubmission(m);
        assertNotNull(d2);
        assertEquals(d2[0].getJMFRoot().getCommand(0).getQueueSubmissionParams(0).getURL(), "cid:JDF.jdf");
        assertEquals(d2[1].getJDFRoot().getEnumType(),EnumType.ColorSpaceConversion);
        
        // now serialize to file and reread - should still work
        HttpURLConnection uc=MimeUtil.writeToURL(m, simWorkerUrl);
        MimeUtil.writeToFile(m, sm_dirTestTemp+"testMime.mjm");
        assertEquals(uc.getResponseCode(), 200);
        UrlUtil.UrlPart[] parts=UrlUtil.getURLParts(uc);
        assertEquals(parts.length, 1);
        InputStream is=parts[0].inStream;
        JDFDoc docResp=new JDFParser().parseStream(is);
        assertNotNull(docResp);
        JDFJMF jmf2=docResp.getJMFRoot();
        assertNotNull(jmf2);
        JDFResponse r=(JDFResponse) jmf2.getMessageElement(EnumFamily.Response,JDFMessage.EnumType.SubmitQueueEntry,0);
        assertNotNull(r);
        assertEquals( 0,r.getReturnCode() );
        JDFQueueEntry qe=r.getQueueEntry(0);
        String devQEntryID=qe.getQueueEntryID();
        assertNotSame(devQEntryID,"");
        return r;
    }
    
	
	private boolean removeDir(File path)
	{
		if( path.exists() ) {
		      File[] files = path.listFiles();
		      for(int i=0; i<files.length; i++) {
		         if(files[i].isDirectory()) {
		           removeDir( files[i] );
		         } else {
		           files[i].delete();
		         }
		      }
		    }
		    return( path.delete() );
	}

	public void testAddDevice()
	{
		AbstractWorkerServlet d = new SimWorkerServlet();
		assertEquals( 0,d.getDeviceQuantity() ); 
		MultiDeviceProperties dp = new MultiDeviceProperties( null,new File(sm_dirTestData+"test_devices.xml") );
		assertNotNull( d.createDevice(dp.getDevice("device001")) );
        assertNotNull( d.createDevice(dp.getDevice("device002")) );
		assertNull( d.createDevice(dp.getDevice("device002")) );
		assertEquals( 2,d.getDeviceQuantity() );
		assertNotNull( d.getDevice("device001") );
		
		removeDir( new File("nulljmb") );
	}
	
	public void testRemoveDevice()
	{
		AbstractWorkerServlet d = new SimWorkerServlet();
		assertEquals( 0,d.getDeviceQuantity() );
		MultiDeviceProperties dp = new MultiDeviceProperties( null,new File(sm_dirTestData+"test_devices.xml") );
		assertNotNull( d.createDevice(dp.getDevice("device001")) );
        assertNotNull( d.createDevice(dp.getDevice("device002")) );
		assertEquals( 2,d.getDeviceQuantity() );
		assertTrue( d.removeDevice("device001") );
		assertNull( d.getDevice("device001") );
		assertEquals(1, d.getDeviceQuantity() );
		assertFalse( d.removeDevice("device001") );
		
		removeDir( new File("nulljmb") );
	}

    public void testMimeSubmit() throws Exception
    {
        JDFResponse resp=singleMIMESubmit("SingleMIME");
        assertNotNull( resp );
        assertEquals( 0,resp.getReturnCode() );
        String qeid=resp.getQueue(0).getQueueEntry(0).getQueueEntryID();
        assertNotNull( qeid );
        assertTrue( qeid!="" );
    }

    public void testMultiSubmit() throws Exception
    {
    	VString qeids=new VString();
    	
    	long t=System.currentTimeMillis();
    	for(int i=0;i<55;i++) {
    		long t1=System.currentTimeMillis();
    		System.out.println("Pre submit, "+i);
    		JDFResponse resp=singleMIMESubmit("Job"+i);
    		long t2=System.currentTimeMillis();
    		System.out.println("Post submit, "+i+" single: "+(t2-t1)+" total: "+(t2-t));
    		
    		assertNotNull( resp );
            assertEquals( 0,resp.getReturnCode() );
            String qeid=resp.getQueueEntry(0).getQueueEntryID();
            assertNotNull( qeid );
            assertTrue( qeid!="" );
            qeids.add( qeid );   		
    	}
    }
    
    public void testCreateDevicesFromFile() {
    	AbstractWorkerServlet d = new SimWorkerServlet();
    	assertEquals( 0,d.getDeviceQuantity() );
    	File f=new File(simConfigDir+"devices.xml");
    	d.createDevicesFromFile(f);
    	assertEquals( 2,d.getDeviceQuantity() );
    	
    	removeDir( new File("nulljmb") );
    }
    
}
