/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2008 The International Cooperation for the Integration of 
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

import java.util.zip.DataFormatException;

import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.util.JDFDate;

/**
 * provides Bambi specific XML extensions for JDF and JMF
 * @author prosirai
 * 
 */
public class BambiNSExtension
{

	/**
	 * 
	 */
	private static final String STATUS_CONTAINER = "StatusContainer";
	private static String slaveURL = "SlaveURL";
	private static String slaveSubmissionTime = "SlaveSubmissionTime";

	protected BambiNSExtension()
	{/* never construct - static class */
	}

	public static final String MY_NS = "www.cip4.org/Bambi";
	public static final String MY_NS_PREFIX = "bambi:";

	/**
	 * 
	 * @param e the element to work on
	 * @param attName the local attribute name to set
	 * @param attVal the attribute value to set
	 */
	public static void setMyNSAttribute(final KElement e, final String attName, final String attVal)
	{
		if (e == null)
		{
			throw new JDFException("setMyNSAttribute: setting on null element");
		}
		e.setAttribute(MY_NS_PREFIX + attName, attVal, MY_NS);
	}

	/**
	 * @param e the element to work on
	 * @param attName the local element name to get
	 * @param iSkip get the nth element
	 * @return the element
	 * 
	 */
	public static KElement getCreateMyNSElement(final KElement e, final String elmName, final int iSkip)
	{
		return e == null ? null : e.getCreateElement(elmName, MY_NS, iSkip);
	}

	/**
	 * @param e the element to work on
	 * @param attName the local element name to get
	 * @param iSkip get the nth element
	 * @return the element
	 * 
	 */
	public static KElement getMyNSElement(final KElement e, final String elmName, final int iSkip)
	{
		return e == null ? null : e.getElement(elmName, MY_NS, iSkip);
	}

	/**
	 * @param e the element to work on
	 * @param attName the local element name to get
	 * @param iSkip get the nth element
	 * @return the element
	 * 
	 */
	public static KElement removeMyNSElement(final KElement e, final String elmName, final int iSkip)
	{
		return e == null ? null : e.removeChild(elmName, MY_NS, iSkip);
	}

	/**
	 * @param e the element to work on
	 * @param attName the local attribute name to set
	 * @return the attribute value, null if none exists
	 * 
	 */
	public static String getMyNSAttribute(final KElement e, final String attName)
	{
		return e == null ? null : e.getAttribute(attName, MY_NS, null);
	}

	/**
	 * @param e the element to work on
	 * @param attName the local attribute name to set
	 * @return the attribute value, null if none exists
	 * 
	 */
	public static String getMyNSString(final String attName)
	{
		return MY_NS_PREFIX + attName;
	}

	/**
	 * 
	 * @param e the element to work on
	 * @param attName the local attribute name to remove
	 */
	public static void removeMyNSAttribute(final KElement e, final String attName)
	{
		if (e == null)
		{
			throw new JDFException("setMyNSAttribute: setting on null element");
		}
		e.removeAttribute(MY_NS_PREFIX + attName, MY_NS);
	}

	/**
	 * 
	 */
	public static KElement getStatusContainer(final JDFQueueEntry qe)
	{
		return getMyNSElement(qe, STATUS_CONTAINER, 0);
	}

	/**
	 * 
	 */
	public static KElement getCreateStatusContainer(final JDFQueueEntry qe)
	{
		return getCreateMyNSElement(qe, STATUS_CONTAINER, 0);
	}

	/**
	 * remove all Bambi specific elements and attributes from the given KElement
	 * @param ke the KElement to clean up
	 */
	public static void removeBambiExtensions(final KElement ke)
	{
		if (ke != null)
		{
			ke.removeExtensions(MY_NS);
		}
	}

	/*** docURL *************************************************************/
	/**
	 * the URL where the JDFDoc can be grabbed
	 */
	public static final String docURL = "DocURL";

	/**
	 * set the location of the JDF
	 * @param ke the KElement to work on
	 * @param docURL the location of the JDF
	 */
	public static void setDocURL(final KElement ke, final String _docURL)
	{
		setMyNSAttribute(ke, docURL, _docURL);
	}

	/**
	 * get the location of the JDF
	 * @param ke the KElement to work on
	 * @return docURL the location of the JDF
	 */
	public static String getDocURL(final KElement ke)
	{
		return getMyNSAttribute(ke, docURL);
	}

	/**
	 * get the associated JDF
	 * @param ke the KElement to work on
	 * @return docURL the location of the JDF
	 */
	public static JDFDoc getDocFromURL(final KElement ke)
	{
		final String url = getMyNSAttribute(ke, docURL);
		return JDFDoc.parseURL(url, null);
	}

	/*** returnURL **********************************************************/
	/**
	 * the URL to send the JDFDoc back to after processing
	 */
	public static final String returnURL = "ReturnURL";

	/**
	 * set the location to send the ReturnQueueEntry to
	 * @param ke the KElement to work on
	 * @param theReturnURL the location to send the ReturnQueueEntry to
	 */
	public static void setReturnURL(final KElement ke, final String theReturnURL)
	{
		setMyNSAttribute(ke, returnURL, theReturnURL);
	}

	/**
	 * get the location to send the ReturnQueueEntry to
	 * @param ke the KElement to work on
	 * @return the location to send the ReturnQueueEntry to
	 */
	public static String getReturnURL(final KElement ke)
	{
		return getMyNSAttribute(ke, returnURL);
	}

	/*** returnJMF *********************************************************/
	/**
	 * the URL to send the ReturnJMF to
	 */
	public static final String returnJMF = "ReturnJMF";

	/**
	 * set the location to send the ReturnJMF to
	 * @param ke the KElement to work on
	 * @param theReturnJMF the location to send the ReturnJMF to
	 */
	public static void setReturnJMF(final KElement ke, final String theReturnJMF)
	{
		setMyNSAttribute(ke, returnJMF, theReturnJMF);
	}

	/**
	 * get the location to send the ReturnJMF to
	 * @param ke the KElement to work on
	 * @return the location to send the ReturnJMF to
	 */
	public static String getReturnJMF(final KElement ke)
	{
		return getMyNSAttribute(ke, returnJMF);
	}

	/*** deviceID *********************************************************/
	/**
	 * the ID of the device processing the QueueEntry
	 */
	public static final String deviceID = "DeviceID";

	/**
	 * set the ID of the device processing the QueueEntry
	 * @param ke the KElement to work on
	 * @param theDeviceID the ID of the device processing the QueueEntry
	 */
	public static void setDeviceID(final KElement ke, final String theDeviceID)
	{
		setMyNSAttribute(ke, deviceID, theDeviceID);
	}

	/**
	 * get the ID of the device processing the QueueEntry
	 * @param ke the KElement to work on
	 * @return the ID of the device processing the QueueEntry
	 */
	public static String getDeviceID(final KElement ke)
	{
		return getMyNSAttribute(ke, deviceID);
	}

	/*** deviceURL *********************************************************/
	/**
	 * the URL of the device processing the QueueEntry
	 */
	public static final String deviceURL = "DeviceURL";

	/**
	 * set the URL of the device processing the QueueEntry
	 * @param ke the KElement to work on
	 * @param theDeviceID the URL of the device processing the QueueEntry
	 */
	public static void setDeviceURL(final KElement ke, final String theDeviceURL)
	{
		setMyNSAttribute(ke, deviceURL, theDeviceURL);
	}

	/**
	 * get the URL of the device processing the QueueEntry
	 * @param ke the KElement to work on
	 * @return the URL of the device processing the QueueEntry
	 */
	public static String getDeviceURL(final KElement ke)
	{
		return getMyNSAttribute(ke, deviceURL);
	}

	// /////////////////////////////////////////////////////////////////////

	/**
	 * the queue entry tracking subelement
	 */
	public static final String slaveQueueEntryID = "SlaveQueueEntryID";

	/**
	 * @param qe
	 * @param outputQEID
	 */
	public static void setSlaveQueueEntryID(final KElement qe, final String outputQEID)
	{
		setMyNSAttribute(qe, slaveQueueEntryID, outputQEID);
	}

	/**
	 * @param qe the queuentry to check
	 */
	public static String getSlaveQueueEntryID(final KElement qe)
	{
		return getMyNSAttribute(qe, slaveQueueEntryID);
	}

	/**
	 * @param q
	 * @param slaveID
	 * @return the QueueEntry with the matching slave qeid
	 */
	public static JDFQueueEntry getSlaveQueueEntry(final JDFQueue q, final String slaveID)
	{
		if (KElement.isWildCard(slaveID))
		{
			return null;
		}

		final VElement v = q.getQueueEntryVector();
		if (v == null)
		{
			return null;
		}
		final int size = v.size();
		for (int i = 0; i < size; i++)
		{
			final JDFQueueEntry qe = (JDFQueueEntry) v.elementAt(i);
			if (slaveID.equals(qe.getAttribute(slaveQueueEntryID, MY_NS, null)))
			{
				return qe;
			}
		}
		return null;
	}

	/**
	 * @param root
	 * @param slaveURL
	 */
	public static void setSlaveURL(final KElement root, final String _slaveURL)
	{
		setMyNSAttribute(root, slaveURL, _slaveURL);
	}

	/**
	 * @param qe
	 * @param date
	 */
	public static void setSlaveSubmissionTime(final JDFQueueEntry qe, final JDFDate date)
	{
		setMyNSAttribute(qe, slaveSubmissionTime, date == null ? null : date.getDateTimeISO());
	}

	/**
	 * @param qe
	 * @param date
	 * @return the submission time to the slave
	 */
	public static JDFDate getSlaveSubmissionTime(final JDFQueueEntry qe)
	{
		final String s = getMyNSAttribute(qe, slaveSubmissionTime);
		try
		{
			return s == null ? null : new JDFDate(s);
		}
		catch (final DataFormatException e)
		{
			return null;
		}
	}

}
