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

package org.cip4.bambi.messaging;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.servlets.DeviceServlet;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.w3c.dom.Document;

/**
 * factory for creating JMF messages
 * 
 * @author boegerni
 * 
 */
public class JMFFactory {
	
	private static Log log = LogFactory.getLog(JMFFactory.class.getName());
	
	private static JDFJMF createJMF(EnumFamily family, EnumType type)
	{
		JDFDoc doc = new JDFDoc(ElementName.JMF);
		JDFJMF jmf = doc.getJMFRoot();

		if (family==EnumFamily.Command)
		{
			jmf.appendCommand(type);
		} else if (family==EnumFamily.Query)
		{
			jmf.appendQuery(type); 
		} 
		
		return jmf;
	}
	
	public static JDFJMF buildSuspendQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = createJMF(EnumFamily.Command, EnumType.SuspendQueueEntry);
		jmf.getCommand(0).appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}
	
	public static JDFJMF buildResumeQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = createJMF(EnumFamily.Command, EnumType.ResumeQueueEntry);
		jmf.getCommand(0).appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}
	
	public static JDFJMF buildAbortQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = createJMF(EnumFamily.Command, EnumType.AbortQueueEntry);
		jmf.getCommand(0).appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}
	
	public static JDFJMF buildRemoveQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = createJMF(EnumFamily.Command, EnumType.RemoveQueueEntry);
		jmf.getCommand(0).appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}
	
	public static JDFJMF buildStatus()
	{
		JDFJMF jmf = createJMF(EnumFamily.Query, EnumType.Status);
		return jmf;
	}
	
	/**
	 * send a message to Bambi
	 * @param jmf the message to send
	 * @param subDeviceId the Bambi sub-device (null for root device, "device001" for BambiRootDevice/device001 etc.)
	 * @return the response
	 */
	public static JDFResponse send2Bambi(JDFJMF jmf, String subDeviceId)
	{
		Properties properties = new Properties();
		FileInputStream in=null;
		String targetURL=null;
		try {
			in = new FileInputStream(DeviceServlet.configDir+"Bambi.properties");
			properties.load(in);
			JDFJMF.setTheSenderID(properties.getProperty("SenderID"));
			targetURL= properties.getProperty("BambiURL")+"/"+properties.getProperty("RootDeviceID");
			if (subDeviceId!=null && subDeviceId.length()>0)
				targetURL += "/"+subDeviceId;
			in.close();
		} catch (IOException e) {
			log.error("failed to load Bambi properties: \r\n"+e.getMessage());
			return null;
		}
		Document dd = jmf.getOwnerDocument();
		JDFDoc doc = new JDFDoc(dd);
		JDFDoc respDoc = doc.write2URL(targetURL);
		if (respDoc==null || respDoc.toString().length()<10)
		{
			log.error("no response received");
			return null;
		}
		return respDoc.getJMFRoot().getResponse(0);
	}
	
}