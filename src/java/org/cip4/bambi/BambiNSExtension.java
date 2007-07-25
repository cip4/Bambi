/**
 * 
 */
package org.cip4.bambi;

import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFQueueEntry;

/**
 * @author prosirai
 *
 */
public class BambiNSExtension
{

    private BambiNSExtension(){/* never construct - static class */}
   
    public static final String MY_NS = "www.cip4.org/Bambi";
    public static final String MY_NS_PREFIX = "bambi:";

    public static String docURL="DocURL";
   /**
     * 
     * @param qe the JDFQueueEntry to set
     * @param docURL the queuentryid within the prinect system
     */
    public static void setDocURL(JDFQueueEntry qe, String _docURL)
    {
        setMyNSAttribute(qe,docURL,_docURL);       
    }
    
    /**
     * @param qe the JDFQueueEntry to work on
     * @return
     */
    public static String getDocURL(JDFQueueEntry qe)
    {
        return getMyNSAttribute(qe,docURL);
    }
        
    /**
     * 
     * @param e the element to work on
     * @param attName the local attribute name to set
     * @param attVal the attribute value to set
     */
    private static void setMyNSAttribute(KElement e, String attName,String attVal)
    {
        if(e==null)
        {
            throw new JDFException("setMyNSAttribute: setting on null element");
        }
        e.setAttribute(MY_NS_PREFIX+attName,attVal,MY_NS);       
    }
    
    /**
     * @param e the element to work on
     * @param attName the local attribute name to set
     * @return the attribute value, null if none exists
     * 
     */
    private static String getMyNSAttribute(KElement e, String attName)
    {
        return e==null ? null : e.getAttribute(attName, MY_NS, null);
    }
}
