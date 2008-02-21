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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;


/**
 * container for the properties of several Bambi devices
 * 
 * @author boegerni
 */
public class MultiDeviceProperties
{
    /**
     * properties for a single device
     * @author boegerni
     *
     */
    private KElement root;

    public class DeviceProperties implements IDeviceProperties {
        /**
         * constructor
         */
        private KElement devRoot;

        protected DeviceProperties(KElement _devRoot) {
            devRoot=_devRoot;
        }


        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getDeviceURL()
         */
        public String getDeviceURL() {
            return devRoot.getAttribute("DeviceURL",null,null);
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClass()
         */
        public IConverterCallback getCallBackClass()
        {
            String _callBackName=getCallBackClassName();

            if(_callBackName!=null)
            {
                try
                {
                    Class c=Class.forName(_callBackName);
                    return (IConverterCallback) c.newInstance();
                }
                catch (Exception x)
                {
                    log.error("Cannot instantiate callback class: "+_callBackName);
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClassName()
         */
        public String getCallBackClassName()
        {
            return devRoot.getAttribute("CallBackName",null,null);        
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getDeviceID()
         */
        public String getDeviceID() {
            return devRoot.getAttribute("DeviceID",null,null);        
        }


        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getProxyURL()
         */
        public String getProxyControllerURL() {
            return devRoot.getAttribute("ProxyURL",null,null);        
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getDeviceType()
         */
        public String getDeviceType() {
            return devRoot.getAttribute("DeviceType",null,null);        
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#toString()
         */
        @Override
        public String toString() {
            return "[ DeviceProperties: "+devRoot.toString()+"]";           
        }

        private File getFile(String file)
        {
           final String fil=devRoot.getAttribute(file,null,null);
           return fil==null ? getRootFile(file) : new File(fil);
        } 

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getDeviceErrorHF()
         */
        public File getErrorHF()
        {
           return getFile("ErrorHF");
        }


        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getDeviceOutputHF()
         */
        public File getOutputHF()
        {
            return getFile("OutputHF");
        }


        /**
         * @return
         */
        public File getInputHF()
        {
            return getFile("InputHF");
        }


        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getBaseDir()
         */
        public File getBaseDir()
        {
            File fBase=getAppDir();
            File f= getRootFile("BaseDir");
            return FileUtil.getFileInDirectory(fBase, f);
        }
        
        public File getAppDir()
        {
            return getRootFile("AppDir");
        }


  
        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getJDFDir()
         */
        public File getJDFDir()
        {
            File fBase=getBaseDir();
            File f= getFile("JDFDir");
            return FileUtil.getFileInDirectory(fBase, f);
        }


        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getSlaveDeviceID()
         */
        public String getSlaveDeviceID()
        {
            String s= devRoot.getAttribute("SlaveDeviceID",null,null);
            if(s!=null)
                return s;
            s=getSlaveURL();
            if(s!=null)
                s=StringUtil.token(s, -1, "/");
            return s;
        }


        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getSlaveErrorHF()
         */
        public File getSlaveErrorHF()
        {
            return getFile("SlaveErrorHF");
        }


        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getSlaveInputHF()
         */
        public File getSlaveInputHF()
        {
            return getFile("SlaveInputHF");
        }


        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getSlaveOutputHF()
         */
        public File getSlaveOutputHF()
        {
            return getFile("SlaveOutputHF");
        }


        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getSlaveURL()
         */
        public String getSlaveURL()
        {
            return devRoot.getAttribute("SlaveURL",null,null);
        }


        /**
         * get the tracked resource - defaults to "Output"
         * 
         * @see org.cip4.bambi.core.IDeviceProperties#getTrackResource()
         */
        public String getTrackResource()
        {
            return devRoot.getAttribute("TrackResource",null,"Output");
        }


        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getMaxPush()
         */
        public int getMaxPush()
        {
            return devRoot.getIntAttribute("MaxPush", null, 0);
        }



    }

    private static final Log log = LogFactory.getLog(MultiDeviceProperties.class.getName());

    /**
     * create device properties for the devices defined in the config file
     * @param appDir     the location of the web application in the server
     * @param configFile the config file
     */
    public MultiDeviceProperties(File baseDir, File configFile) {

        JDFParser p = new JDFParser();
        XMLDoc doc = p.parseFile(FileUtil.getFileInDirectory(baseDir, configFile));
        root = doc==null ? null : doc.getRoot();
        if (root==null) {
            log.fatal( "failed to parse "+configFile+", root is null" );
        }
        else
        {
            root.setAttribute("AppDir", baseDir.getAbsolutePath());
        }
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IMultiDeviceProperties#count()
     */
    public int count() {
        return root.numChildElements(ElementName.DEVICE, null);
    }

     /* (non-Javadoc)
     * @see org.cip4.bambi.core.IMultiDeviceProperties#getDevice(java.lang.String)
     */
    public IDeviceProperties getDevice(String deviceID) {
        KElement dev=root.getChildWithAttribute(ElementName.DEVICE, AttributeName.DEVICEID, null, deviceID, 0, true);
        return dev==null ? null : this.new DeviceProperties(dev);
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IMultiDeviceProperties#toString()
     */
    @Override
    public String toString() {
        return "[ MultiDeviceProperties: "+root.toString()+"]";           
    }

    /**
     * @return
     */
    public File getAppDir()
    {
        return getRootFile("AppDir");
    }
    
    public File getBaseDir()
    {
        File fBase=getAppDir();
        File f= getRootFile("BaseDir");
        return FileUtil.getFileInDirectory(fBase, f);
     }

    /**
     * @return
     */
    public File getJDFDir()
    {
        File fBase=getBaseDir();
        File f= getRootFile("JDFDir");
        return FileUtil.getFileInDirectory(fBase, f);
    }

    /**
     * @param string
     * @return
     */
    protected File getRootFile(String file)
    {
       final String fil=root.getAttribute(file,null,null);
       return fil==null ? null : new File(fil);
    }

    /**
     * @return
     */
    public VString getDeviceIDs()
    {
       VElement v=root.getChildElementVector(ElementName.DEVICE, null);
       VString vs=new VString();
       for(int i=0;i<v.size();i++)
       {
           vs.add(v.elementAt(i).getAttribute(AttributeName.DEVICEID));
       }
       return vs;
    }

}