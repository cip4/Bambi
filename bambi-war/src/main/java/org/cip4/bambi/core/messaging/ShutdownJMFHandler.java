/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2012 The International Cooperation for the Integration of 
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
package org.cip4.bambi.core.messaging;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiContainer;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.auto.JDFAutoShutDownCmdParams.EnumShutDownType;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFShutDownCmdParams;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * jmf handler for the shutdown methof
 * @author rainer prosi
 * @date June 5, 2012
 */
public class ShutdownJMFHandler extends AbstractHandler {

	private final AbstractDevice device;
	private boolean killContainer;

	private static ServiceKiller theServiceKiller = null;

	/**
	 * 
	 * thread to kill mama service
	 * @author rainer prosi
	 * @date Jun 5, 2012
	 */
	private class ServiceKiller extends Thread {
		/**
		 * 
		 */
		private ServiceKiller() {
			super("ContainerKillerThread");
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			log.info("Contemplating Harakiri");
			ThreadUtil.sleep(1000);
			log.info("Committing Harakiri");
			long t0 = System.currentTimeMillis();
			BambiContainer container = BambiContainer.getInstance();
			if (container == null) {
				log.error("cannot retrieve container, bailing out");
			} else {
				container.shutDown();
			}
			// if (JettyService.isJettyEnvironment()) {
			// JettyService.stop(new String[] {});
			// }
			log.info("Digital Nirvana!!! after " + (System.currentTimeMillis() - t0) + " milliseconds");
		}
	}

	/**
	 * 
	 * @param device
	 */
	public ShutdownJMFHandler(AbstractDevice device) {
		super(EnumType.ShutDown, new EnumFamily[] { EnumFamily.Command });
		this.device = device;
		killContainer = false;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
	 */
	@Override
	public boolean handleMessage(JDFMessage inputMessage, JDFResponse response) {
		JDFShutDownCmdParams scp = inputMessage.getShutDownCmdParams(0);
		EnumShutDownType shutDownType = scp == null ? EnumShutDownType.StandBy : scp.getShutDownType();
		if (EnumShutDownType.Full.equals(shutDownType)) {
			log.info("shutting down; " + device.getDeviceType() + " ID=" + device.getDeviceID());
			device.shutdown();
			if (killContainer && theServiceKiller == null) {
				theServiceKiller = new ServiceKiller();
				theServiceKiller.start();
			}
			return true;
		} else {
			JMFHandler.errorResponse(response, "Standby shutdown not handled " + device.getDeviceType() + " ID=" + device.getDeviceID(), 101, EnumClass.Error);
			return true;
		}
	}

	/**
	 * Setter for killContainer
	 * @param bZapp if true; die MF die!
	 */
	public void setKillContainer(boolean bZapp) {
		this.killContainer = bZapp;
	}
}
