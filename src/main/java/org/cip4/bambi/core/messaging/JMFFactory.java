/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2019 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */

package org.cip4.bambi.core.messaging;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.net.HTTPDetails;
import org.cip4.jdflib.util.thread.MyMutex;

/**
 * factory for sending JMF messages
 *
 * @author boegerni
 */
public class JMFFactory {

	private final Log log = BambiLogFactory.getLog(JMFFactory.class);

	// end of inner class CallURL

    private static JMFFactory theFactory = null;
    private static MyMutex factoryMutex = new MyMutex();
    final HashMap<CallURL, MessageSender> senders = new HashMap<>();
    private int nThreads = 0;
    private final boolean shutdown = false;
    private final HashMap<EnumType, IMessageOptimizer> optimizers;
    private final long startTime;
    private boolean zapp500;
    private boolean logLots;

    /**
     * @return the logLots
     */
    public boolean isLogLots() {
        return logLots;
    }

    /**
     * @param logLots the logLots to set
     */
    public void setLogLots(final boolean logLots) {
        this.logLots = logLots;
    }

    /**
     *
     */
    private JMFFactory() // all static
    {
        super();
        optimizers = new HashMap<>();
        startTime = System.currentTimeMillis();
        zapp500 = false;
        logLots = false;
    }

    /**
     * @return the zapp500
     */
    public boolean isZapp500() {
        return zapp500;
    }

    /**
     * @param zapp500 the zapp500 to set
     */
    public void setZapp500(final boolean zapp500) {
        this.zapp500 = zapp500;
    }

    /**
     * @return
     */
    public static JMFFactory getJMFFactory() {
        synchronized (factoryMutex) {
            if (theFactory == null) {
                theFactory = new JMFFactory();
            }
        }
        return theFactory;
    }

    /**
     *
     */
    public static void shutdown() {
        if (theFactory != null) {
            theFactory.shutDown(null, true);
            if (theFactory.senders.size() > 0) {
                ThreadUtil.sleep(1234);
                theFactory.shutDown(null, false);
            }
            theFactory = null;
        }
    }

    /**
     * sends a mime or zip multipart message to a given URL
     *
     * @param jmf
     * @param jdf
     * @param url      the URL to send the JMF to
     * @param handler
     * @param callback
     * @param md
     * @param deviceID
     * @return
     */
    public boolean send2URL(final JDFJMF jmf, final JDFNode jdf, final String url, final IResponseHandler handler, final IConverterCallback callback, final MIMEDetails md, final String deviceID) {
        if (shutdown) {
            return false;
        }

        boolean ok = true;
        if (jmf == null) {
            log.error("failed to send JDFMessage, jmf is null");
            ok = false;
        } else if (jdf == null) {
            log.error("failed to send JDFMessage, jdf is null");
            ok = false;
        } else if (url == null) {
            log.error("failed to send JDFMessage, URL is null");
            ok = false;
        }
        if (!ok)
            return false;

        if (deviceID != null) {
            jmf.setSenderID(deviceID);
        }

        final MessageSender ms = getCreateMessageSender(url);
        return ms.queueMessage(jmf, jdf, handler, url, callback, md);
    }

    /**
     * POSTS a empty url message to a given URL
     *
     * @param url      the URL to send the JMF to
     * @param handler
     * @param callback
     * @return
     */
    public boolean send2URL(final String url, final IResponseHandler handler, final IConverterCallback callback) {
        if (shutdown) {
            return false;
        }

        if (url == null) {
            log.error("failed to send empty post, URL is null");
            return false;
        }

        final MessageSender ms = getCreateMessageSender(url);
        return ms.queuePost(handler, url, callback);
    }

    /**
     * sends a JMF message to a given URL
     *
     * @param jmf      the message to send
     * @param url      the URL to send the JMF to
     * @param handler
     * @param callback
     * @param senderID the senderID of the caller
     * @return true if successfully queued
     */
    public boolean send2URL(final JDFJMF jmf, final String url, final IResponseHandler handler, final IConverterCallback callback, final String senderID) {
        return send2URL(jmf, url, handler, callback, senderID, null);
    }

    /**
     * sends a JMF message to a given URL synchronously
     *
     * @param jmf          the message to send
     * @param url          the URL to send the JMF to
     * @param callback
     * @param senderID     the senderID of the caller
     * @param milliSeconds timeout to wait
     * @return the response if successful, otherwise null
     */
    public HttpURLConnection send2URLSynch(final JDFJMF jmf, final String url, final IConverterCallback callback, final String senderID, final int milliSeconds) {
        final MessageResponseHandler handler = new MessageResponseHandler((String) null);
        send2URL(jmf, url, handler, callback, senderID);
        handler.waitHandled(milliSeconds, 10000, true);
        return handler.getConnection();
    }

    /**
     * sends a JMF message to a given URL sychronusly
     *
     * @param jmf          the message to send
     * @param url          the URL to send the JMF to
     * @param callback
     * @param senderID     the senderID of the caller
     * @param milliSeconds timeout to wait
     * @return the response if successful, otherwise null
     */
    public JDFResponse send2URLSynchResp(final JDFJMF jmf, final String url, final IConverterCallback callback, final String senderID, final int milliSeconds) {
        final MessageResponseHandler handler = new MessageResponseHandler((String) null);
        send2URL(jmf, url, handler, callback, senderID);
        handler.waitHandled(milliSeconds, 10000, true);
        final HttpURLConnection uc = handler.getConnection();
        if (uc != null) {
            final JDFDoc d = JDFDoc.parseStream(handler.getBufferedStream());
            if (d != null) {
                final JDFJMF root = d.getJMFRoot();
                return root == null ? null : root.getResponse(0);
            }
        }
        return null;
    }

    /**
     * sends a mime multipart package to a given URL synchronously
     *
     * @param mp           the mime multipart to send
     * @param url          the URL to send the JMF to
     * @param callback
     * @param md
     * @param senderID
     * @param milliSeconds
     * @return the response if successful, otherwise null
     */
    public HttpURLConnection send2URLSynch(final JDFJMF jmf, final JDFNode jdf, final String url, final IConverterCallback callback, final MIMEDetails md, final String senderID,
                                           final int milliSeconds) {
        final MessageResponseHandler handler = new MessageResponseHandler((String) null);
        send2URL(jmf, jdf, url, handler, callback, md, senderID);
        handler.waitHandled(milliSeconds, 10000, true);
        return handler.getConnection();
    }

    /**
     * @param cu         the callURL to shut down, if null all of them
     * @param graceFully
     */
    public void shutDown(final CallURL cu, final boolean graceFully) {
        if (cu == null) // null = all
        {
            log.info("shutting down all senders ");
            final Vector<CallURL> keySet = ContainerUtil.getKeyVector(senders);
            if (keySet != null) {
                for (final CallURL s : keySet) {
                    shutDown(s, graceFully);
                }
            }
            log.info("completed shutting down all senders ");
        } else
        // individual url
        {
            final MessageSender ms = senders.get(cu);
            if (ms != null) {
                log.info("shutting down sender " + cu.getBaseURL() + (graceFully ? " gracefully" : " forced"));
                ms.shutDown(graceFully);
            } else {
                log.warn("no sender to shut down: " + cu.getBaseURL() + (graceFully ? " gracefully" : " forced"));
            }
            senders.remove(cu);
        }
    }

    /**
     * @param typ message type
     * @param opt the optimizer to call
     */
    public void addOptimizer(final EnumType typ, final IMessageOptimizer opt) {
        if (typ != null && opt != null) {
            optimizers.put(typ, opt);
        }
    }

    /**
     * @param typ
     * @return
     */
    public IMessageOptimizer getOptimizer(final EnumType typ) {
        return typ == null ? null : optimizers.get(typ);
    }

    /**
     * @param url the url to match, if null all
     * @return Vector of all known message senders matching url; null if none match
     */
    public Vector<MessageSender> getMessageSenders(String url) {
        url = UrlUtil.normalize(url);
        Vector<MessageSender> v = ContainerUtil.toValueVector(senders, true);
        if (StringUtil.getNonEmpty(url) == null) {
            return v;
        }

        if (v != null) {
            for (int i = v.size() - 1; i >= 0; i--) {
                final MessageSender messageSender = v.get(i);
                if (!messageSender.matchesURL(url)) {
                    v.remove(i);
                }
            }
            if (v.size() == 0) {
                v = null;
            }
        }
        return v;
    }

    /**
     * get an existing MessageSender or create it if it does not exist for a given url or callback
     *
     * @param url the URL to send a message to, if null use the fire&forget sender
     * @return the MessageSender that will queue and dispatch the message
     */
    public MessageSender getCreateMessageSender(final String url) {
        if (url == null) {
            log.warn("attempting to retrieve MessageSender for null");
            return null;
        }
        if (shutdown) {
            log.warn("attempting to retrieve MessageSender after shutdown for " + url);
            return null;
        }
        final CallURL callURL = new CallURL(url);

        synchronized (senders) {
            MessageSender messageSender = senders.get(callURL);
            if (messageSender != null && !messageSender.isRunning()) {
                shutDown(callURL, false);
                log.info("removed idle message sender " + callURL.url);
                messageSender = null;
            }
            if (messageSender == null) {
                cleanIdleSenders();
                messageSender = new MessageSender(callURL);
                messageSender.setStartTime(startTime);
                messageSender.setJMFFactory(this);
                senders.put(callURL, messageSender);
                final String name = "MessageSender_" + nThreads++ + "_" + callURL.getBaseURL();
                final Thread thread = new Thread(messageSender, name);
                log.info("creating new message sender: " + name);
                thread.setDaemon(false);
                thread.start();
            }
            return messageSender;
        }
    }

    /**
     * cleanup idle threads
     */
    private void cleanIdleSenders() {
        synchronized (senders) {
            final Vector<MessageSender> vRemove = new Vector<>();
            for (final MessageSender ms : senders.values()) {
                if (!ms.isRunning()) {
                    vRemove.add(ms);
                }
            }
            for (final MessageSender ms : vRemove) {
                ms.shutDown(false);
                log.info("removing idle message sender " + ms.getCallURL().getBaseURL());
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "JMFFactory : threads=" + nThreads + " zapp500=" + zapp500 + " logLots=" + logLots + " Senders: " + senders;
    }

    /**
     * sends a JMF message to a given URL
     *
     * @param jmf      the message to send
     * @param url      the URL to send the JMF to
     * @param handler
     * @param callback
     * @param senderID the senderID of the caller
     * @return true if successfully queued
     */
    public boolean send2URL(final JDFJMF jmf, final String url, final IResponseHandler handler, final IConverterCallback callback, final String senderID, final HTTPDetails det) {
        if (shutdown) {
            return false;
        }
        if (jmf == null) {
            log.error("failed to send JDFMessage, message is null");
            return false;
        } else if (url == null) {
            log.error("failed to send JDFMessage, target URL is null");
            return false;
        }

        final MessageSender ms = getCreateMessageSender(url);
        if (senderID != null) {
            jmf.setSenderID(senderID);
        }
        return ms.queueMessage(jmf, handler, url, callback, det);
    }
}