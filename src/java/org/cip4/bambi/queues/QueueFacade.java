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

package org.cip4.bambi.queues;

import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.servlets.DeviceServlet;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;


/**
 * facade for JDFQueue in Bambi, used for reliable displaying Queues in JSP
 * 
 * @author boegerni
 * 
 */
public class QueueFacade {
	public static class BambiQueueEntry 
	{
		public String queueEntryID=null;
		public EnumQueueEntryStatus queueEntryStatus=null;
		public int queuePriority=0;
		
		protected BambiQueueEntry(String qEntryID, EnumQueueEntryStatus qStatus, int qPriority)
		{
			queueEntryID = qEntryID;
			queueEntryStatus = qStatus;
			queuePriority = qPriority;
		}
	}
	

	private static Log log = LogFactory.getLog(QueueFacade.class.getName());
	private JDFQueue _theQueue = null;

	/**
	 * constructor
	 */
	public QueueFacade(JDFQueue queue)
	{
		_theQueue = queue;
	}

	public String toString()
	{
		return ( _theQueue.toString() );
	}
	
	public String getQueueStatusString()
	{
		return _theQueue.getQueueStatus().getName();
	}
	
	public EnumQueueStatus getQueueStatus() {
		return _theQueue.getQueueStatus();
	}
	
	public Vector getBambiQueueEntryVector()
	{
		log.info("building BambieQueueEntryVector");
		Vector qes = new Vector();
		for (int i = 0; i<_theQueue.getQueueSize();i++)
		{
			JDFQueueEntry jqe = _theQueue.getQueueEntry(i);
			BambiQueueEntry bqe = new BambiQueueEntry( jqe.getQueueEntryID(),
					jqe.getQueueEntryStatus(),jqe.getPriority() );
			qes.add(bqe);
		}
			
		return qes;
	}
	
	public String toHTML()
	{
		String quStr = _theQueue.toXML();
		int pos = quStr.indexOf(">");
		quStr = quStr.substring(pos+2);
		String xsltHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n"
			+ "<?xml-stylesheet type=\"text/xsl\" href=\""+DeviceServlet.xslDir+"queue2html.xsl\"?> \r\n";
		return (xsltHeader+quStr);
	}
	
	/**
	 * count all QueueEntries
	 * @return
	 */
	public int countAll() {
		return _theQueue.getQueueSize();
	}
	
	/**
	 * count all QueueEntries which have the given {@link QueueEntryStatus} 
	 * @param status
	 * @return
	 */
	public int count(EnumQueueEntryStatus status) {
		VElement qev = _theQueue.getQueueEntryVector();
		int count=0;
		for (int i=0;i<qev.size();i++) {
			JDFQueueEntry qe = (JDFQueueEntry) qev.elementAt(i);
			if ( qe!=null&&qe.getQueueEntryStatus().equals(status) ) {
				count++;
			}
		}
		
		return count;
	}
	
	

}