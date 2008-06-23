/*--------------------------------------------------------------------------------------------------
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2006 The International Cooperation for the Integration of
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

package org.cip4.bambi.core;

import org.cip4.bambi.core.messaging.IMultiJMFHandler;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.ifaces.IJMFSubscribable;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;


/**
 * @author prosirai
 *
 */
public interface ISignalDispatcher extends IMultiJMFHandler, IGetHandler
{

    /**
     * add a subscription
     * returns the channelID of the new subscription, null if snafu
     * @param subMess the subscription message - one of query or registration
     * @param queueEntryID the associated QueueEntryID, may be null.
     * @return the channelID of the subscription, if successful, else null
     */
    public String addSubscription(IJMFSubscribable subMess, String queueEntryID);

    /**
     * add a subscription
     * returns the channelID of the new subscription, null if snafu
     * @param node the node to search for inline jmfs
     * @param queueEntryID the associated QueueEntryID, may be null.
     * @return the channelIDs of the subscriptions, if successful, else null
     */
    public VString addSubscriptions(JDFNode node, String queueEntryID);

    /**
     * remove a know subscription by channelid
     * @param channelID the channelID of the subscription to remove
     */
    public void removeSubScription(String channelID);

    /**
     * remove a know subscription by queueEntryID
     * @param queueEntryID the queueEntryID of the subscriptions to remove
     * @param url url of subscriptions to zapp
     */
    public void removeSubScriptions(String queueEntryID, String url);

    /**
     * trigger a subscription based on queuentryID
     * @param the queuentryid of the active queueentry
     * @param queueEntryID the queuentryid of the active queueentry
     * @param workStepID the workStepID of the active task
     * @param amount the amount produced since the last call, 0 if unknown, -1 for a global trigger
     */
    public void triggerQueueEntry(String queueEntryID, String workStepID, int amount );

    /**
     * trigger a subscription based on channelID
     * @param channelID the channelid of the channel to trigger
     * @param queueEntryID the queuentryid of the active queueentry
     * @param workStepID the workStepID of the active task
     * @param amount the amount produced since the last call, 0 if unknown, -1 for a global trigger
     */
    public void triggerChannel(String channelID, String queueEntryID, String workStepID, int amount);
    
    /**
     * stop the dispatcher thread
     */
    public void shutdown();
    /**
     * flush any waiting messages
     */
    public void flush();

    /**
     * find subscriptions in a message and add them if appropriate
     * @param m
     * @param resp
     * @param dispatcher
     */
    public void findSubscription(JDFMessage m, JDFResponse resp);
     
}
