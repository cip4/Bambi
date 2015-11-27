/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2014 The International Cooperation for the Integration of 
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
package org.cip4.bambi.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.logging.LogConfigurator;
import org.cip4.jdflib.util.net.ProxyUtil;
import org.cip4.jdflib.util.thread.MyMutex;

/**
 * starter / stopper class when using a windows service
 * see DummyService for an example implementation
 * @author rainer prosi
 * @date Oct 26, 2011
 */
public abstract class JettyService
{
    protected final Log log;
    protected JettyServer theServer = null;
    protected static JettyService theService = null;
    protected MyMutex mutex;

    private class StopExit extends Thread
    {
        /**
         *
         */
        StopExit()
        {
            super("StopExit");
            setDaemon(true);
        }

        /**
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run()
        {
            try
            {
                Thread.sleep(42420);
                log.warn("exiting ungracefully");
                System.exit(2);
            }
            catch (final InterruptedException x)
            {
                //nop
            }
        }
    }

    /**
     * this gets the actual server instance
     * @param args
     * @return
     */
    public abstract JettyServer getServer(String[] args);

    /**
     *
     */
    public JettyService()
    {
        super();
        log = LogFactory.getLog(getClass());
        if (theService == null)
            theService = this;
        else
            log.error("You are creating a second JettyService. This is generally a very bad idea.");
    }

    /**
     *
     * main ... this main obviously won't work but is an example main. see BambiService for a working implementation
     * @param args
     *
     */
    public static void main(String[] args)
    {
        LogConfigurator.configureLog(".", "jetty.log");
        theService = null;
        theService.doMain(args);
    }

    /**
     *
     * start
     * @param args
     */
    public static final void start(String[] args)
    {
        theService.doStart(args);
    }

    /**
     *
     * main ...
     * @param args
     */
    public static final void stop(String[] args)
    {
        theService.doStop(args);
    }

    /**
     *
     * @return true if we are in a jetty service environment
     *
     */
    public static final boolean isJettyEnvironment()
    {
        return theService != null;
    }

    /**
     *
     *
     * @param args
     * @return
     */
    public int doMain(String[] args)
    {
        log.info("main() called, # args: " + args.length);
        VString vArgs = new VString();
        for (String arg : args)
        {
            log.info("arg: " + arg);
            vArgs.add(arg.toLowerCase());
        }

        String arg0 = args.length > 0 ? args[0] : "## no parameters ##";
        if (vArgs.contains("start") || args.length == 0)
        {
            return doStart(args);
        }
        else if (vArgs.contains("stop"))
        {
            int i = doStop(args);
            if (i == 0)
                log.info("exiting normally after stop: " + i);
            else
                log.warn("abnormal exit" + i);
            new StopExit().start();
            return i;
        }
        else
        {
            log.error("Unknown parameter: " + arg0);
            return 1;
        }
    }

    /**
     * start the actual server
     *
     * @param args
     * @return
     */
    protected int doStart(String[] args)
    {
        if (theServer != null)
        {
            log.error("server already started - ignoring start");
            return 2;
        }
        theServer = getLicensedServer(args);
        if (theServer == null)
        {
            log.error("server couldn't start");
            return 1;
        }
        else
        {
            ProxyUtil.setUseSystemDefault(true);
            theServer.start();
            mutex = new MyMutex();
            return 0;
        }
    }

    /**
     *
     * @param args
     * @return
     */
    protected JettyServer getLicensedServer(String[] args)
    {
        if (args == null)
            args = new String[] {};
        if (isLicensed(args))
        {
            return getServer(args);
        }
        else
        {
            log.warn("not licensed - retrieving nonlicensed server");
            return getNullServer(args);
        }
    }

    /**
     *
     * overwrite this if you want a different unlicensed server implementation. null is also an option in case you want to fail miserably
     * @param args
     * @return
     */
    protected JettyServer getNullServer(String[] args)
    {
        return new VString(args).contains("-nonull") ? null : new NullServer();
    }

    /**
     * you can overwrite this for a license check and return a null server in case licensing fails
     * this implementation simply checks for the non-existence of an environment variable JettyNoStart
     *
     * @param args
     * @return
     */
    protected boolean isLicensed(String[] args)
    {
        boolean isLicensed = System.getenv("JettyNoStart") == null;
        log.info("checking license - isLicensed=" + isLicensed);
        return isLicensed;
    }

    /**
     * wait for the server to stop - useful to keep the main thread alive
     *
     */
    public void waitStopped()
    {
        if (mutex != null)
        {
            log.info("waiting for server stop");
            ThreadUtil.wait(mutex, 0);
        }
    }

    /**
     * stop the actual server - note: calls exit so MUST be called last
     * @param args
     * @return
     */
    protected int doStop(String[] args)
    {
        if (theServer == null)
        {
            log.error("server already stopped - ignoring stop");
            return 2;
        }
        theServer.stop();
        theServer = null;
        ThreadUtil.notifyAll(mutex);
        return 0;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getClass().getCanonicalName();
    }
}