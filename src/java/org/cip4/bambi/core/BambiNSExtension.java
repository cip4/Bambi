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


import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;

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

    protected BambiNSExtension(){/* never construct - static class */}
   
    public static final String MY_NS = "www.cip4.org/Bambi";
    public static final String MY_NS_PREFIX = "bambi:";
    
    /**
     * 
     * @param e the element to work on
     * @param attName the local attribute name to set
     * @param attVal the attribute value to set
     */
    protected static void setMyNSAttribute(KElement e, String attName,String attVal)
    {
        if(e==null)
        {
            throw new JDFException("setMyNSAttribute: setting on null element");
        }
        e.setAttribute(MY_NS_PREFIX+attName,attVal,MY_NS);       
    }
    
    /**
     * @param e the element to work on
     * @param attName the local element name to get
     * @param iSkip get the nth element
     * @return the element
     * 
     */
    protected static KElement getCreateMyNSElement(KElement e, String elmName, int iSkip)
    {
        return e==null ? null : e.getCreateElement(elmName, MY_NS, iSkip);
    }
    /**
     * @param e the element to work on
     * @param attName the local element name to get
     * @param iSkip get the nth element
     * @return the element
     * 
     */
    protected static KElement getMyNSElement(KElement e, String elmName, int iSkip)
    {
        return e==null ? null : e.getElement(elmName, MY_NS, iSkip);
    }
    /**
     * @param e the element to work on
     * @param attName the local element name to get
     * @param iSkip get the nth element
     * @return the element
     * 
     */
    protected static KElement removeMyNSElement(KElement e, String elmName, int iSkip)
    {
        return e==null ? null : e.removeChild(elmName, MY_NS, iSkip);
    }
    /**
     * @param e the element to work on
     * @param attName the local attribute name to set
     * @return the attribute value, null if none exists
     * 
     */
    protected static String getMyNSAttribute(KElement e, String attName)
    {
        return e==null ? null : e.getAttribute(attName, MY_NS, null);
    }
    
    /**
     * 
     * @param e the element to work on
     * @param attName the local attribute name to remove
     */
    protected static void removeMyNSAttribute(KElement e, String attName)
    {
        if(e==null)
        {
            throw new JDFException("setMyNSAttribute: setting on null element");
        }
        e.removeAttribute(MY_NS_PREFIX+attName,MY_NS);       
    }
    
    /**
     * 
     */
    public static KElement getStatusContainer(JDFQueueEntry qe)
    {
       return getMyNSElement(qe,STATUS_CONTAINER,0);
    }
    /**
     * 
     */
    public static KElement getCreateStatusContainer(JDFQueueEntry qe)
    {
       return getCreateMyNSElement(qe,STATUS_CONTAINER,0);
    }
    /**
     * remove all Bambi specific elements and attributes from the given KElement
     * @param ke the KElement to clean up
     */
    public static void removeBambiExtensions(KElement ke)
    {
        if(ke!=null)
            ke.removeExtensions(MY_NS);
    }

    /*** docURL *************************************************************/
    /**
     * the URL where the JDFDoc can be grabbed
     */
    public static final String docURL="DocURL";
   /**
     * set the location of the JDF
     * @param ke the KElement to work on
     * @param docURL the location of the JDF
     */
    public static void setDocURL(KElement ke, String _docURL)
    {
        setMyNSAttribute(ke,docURL,_docURL);       
    }
    
    /**
     * get the location of the JDF
     * @param ke the KElement to work on
     * @return docURL the location of the JDF
     */
    public static String getDocURL(KElement ke)
    {
        return getMyNSAttribute(ke,docURL);
    }
        
    /**
     * get the associated JDF
     * @param ke the KElement to work on
     * @return docURL the location of the JDF
     */
    public static JDFDoc getDocFromURL(KElement ke)
    {
        String url=getMyNSAttribute(ke,docURL);
        return JDFDoc.parseURL(url, null);
    }
   /*** returnURL **********************************************************/
    /**
     * the URL to send the JDFDoc back to after processing
     */
    public static final String returnURL="ReturnURL";
    /**
      * set the location to send the ReturnQueueEntry to
      * @param ke the KElement to work on
      * @param theReturnURL the location to send the ReturnQueueEntry to
      */
     public static void setReturnURL(KElement ke, String theReturnURL)
     {
         setMyNSAttribute(ke,returnURL,theReturnURL);       
     }
     
     /**
      * get the location to send the ReturnQueueEntry to
      * @param ke the KElement to work on
      * @return the location to send the ReturnQueueEntry to
      */
     public static String getReturnURL(KElement ke)
     {
         return getMyNSAttribute(ke,returnURL);
     }
     
     /*** returnJMF *********************************************************/
     /**
      * the URL to send the ReturnJMF to
      */
     public static final String returnJMF="ReturnJMF";
     /**
       * set the location to send the ReturnJMF to
       * @param ke the KElement to work on
       * @param theReturnJMF the location to send the ReturnJMF to
       */
      public static void setReturnJMF(KElement ke, String theReturnJMF)
      {
          setMyNSAttribute(ke,returnJMF,theReturnJMF);       
      }
      
      /**
       * get the location to send the ReturnJMF to
       * @param ke the KElement to work on
       * @return the location to send the ReturnJMF to
       */
      public static String getReturnJMF(KElement ke)
      {
          return getMyNSAttribute(ke,returnJMF);
      }
    
      /*** deviceID *********************************************************/
      /**
       * the ID of the device processing the QueueEntry
       */
      public static final String deviceID="DeviceID";
      /**
        * set the ID of the device processing the QueueEntry
        * @param ke the KElement to work on
        * @param theDeviceID the ID of the device processing the QueueEntry
        */
       public static void setDeviceID(KElement ke, String theDeviceID)
       {
           setMyNSAttribute(ke,deviceID,theDeviceID);       
       }
       
       /**
        * get the ID of the device processing the QueueEntry
        * @param ke the KElement to work on
        * @return the ID of the device processing the QueueEntry
        */
       public static String getDeviceID(KElement ke)
       {
           return getMyNSAttribute(ke,deviceID);
       }
       
       /*** deviceURL *********************************************************/
       /**
        * the URL of the device processing the QueueEntry
        */
       public static final String deviceURL="DeviceURL";
       /**
         * set the URL of the device processing the QueueEntry
         * @param ke the KElement to work on
         * @param theDeviceID the URL of the device processing the QueueEntry
         */
        public static void setDeviceURL(KElement ke, String theDeviceURL)
        {
            setMyNSAttribute(ke,deviceURL,theDeviceURL);       
        }
        
        /**
         * get the URL of the device processing the QueueEntry
         * @param ke the KElement to work on
         * @return the URL of the device processing the QueueEntry
         */
        public static String getDeviceURL(KElement ke)
        {
            return getMyNSAttribute(ke,deviceURL);
        }
        
///////////////////////////////////////////////////////////////////////
        
        /**
         * the queue entry tracking subelement
         */
        public static final String devQueueEntryID="DeviceQueueEntryID";

         /**
         * @param qe
         * @param outputQEID
         */
        public static void setDeviceQueueEntryID(KElement qe, String outputQEID)
        {
            setMyNSAttribute(qe, devQueueEntryID, outputQEID);        
        }
        /**
         * @param qe the queuentry to check
         */
        public static String  getDeviceQueueEntryID(KElement qe)
        {
            return getMyNSAttribute(qe, devQueueEntryID);        
        }
        
        /**
         * @param qt
         * @param outputQEID
         */
        public static JDFQueueEntry  getDeviceQueueEntry(JDFQueue q, String deviceQueueEntryID)
        {
            if(KElement.isWildCard(deviceQueueEntryID))
                return null;
            
            VElement v=q.getQueueEntryVector();
            if(v==null)
                return null;
            for(int i=0;i<v.size();i++)
            {
                JDFQueueEntry qe=(JDFQueueEntry)v.elementAt(i);
                if(deviceQueueEntryID.equals(qe.getAttribute(devQueueEntryID, MY_NS, null)))
                    return qe;
            }
            return null;
        }
   
}
