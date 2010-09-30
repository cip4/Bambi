/*--------------------------------------------------------------------------------------------------
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
 */
package org.cip4.bambi.core.messaging;

import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.resource.JDFNotification;

/**
 * generic dispatcher handler for dispatching to the respective low level processors
 */
public abstract class DispatchHandler extends JMFHandler.AbstractHandler
{

	public DispatchHandler(final String _type, final EnumFamily[] _families)
	{
		super(_type, _families);
	}

	public DispatchHandler(final EnumType _type, final EnumFamily[] _families)
	{
		super(_type, _families);
	}

	public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response, final IJMFHandler[] devs, final boolean checkReturnCode)
	{

		if (devs == null)
		{
			return false;
		}
		boolean b = false;
		JDFNotification notif = (JDFNotification) response.removeChild(ElementName.NOTIFICATION, null, 0);

		final JDFJMF jmfin = inputMessage.getJMFRoot();

		boolean bSignal = false;
		for (int i = 0; i < devs.length; i++)
		{
			final IMessageHandler mh = devs[i].getHandler(inputMessage.getType(), inputMessage.getFamily());
			if (mh != null)
			{
				response.setReturnCode(0);
				boolean b1 = mh.handleMessage(inputMessage, response);
				if (b1 && checkReturnCode)
				{
					final int rc = response.getReturnCode();
					b1 = rc == 0;
				}
				b = b1 || b;
				if (response.hasChildElement(ElementName.NOTIFICATION, null))
				{
					notif = (JDFNotification) response.removeChild(ElementName.NOTIFICATION, null, 0);
				}
				JDFJMF jmfinAfter = inputMessage.getJMFRoot();

				// undo cleanup of pure signals; else npe in 2nd loop...
				if (jmfinAfter == null)
				{
					bSignal = true;
					jmfin.moveElement(inputMessage, null);
				}
				jmfinAfter = response.getJMFRoot();
				if (jmfinAfter == null)
				{
					bSignal = true;
					jmfin.moveElement(response, null);
				}
			}
		}
		// cleanup pure signals
		if (bSignal)
		{
			inputMessage.deleteNode();
			response.deleteNode();
		}
		if (b)
		{
			response.setReturnCode(0);
		}
		else if (notif != null)
		{
			response.moveElement(notif, null);
		}

		return b;
	}
}