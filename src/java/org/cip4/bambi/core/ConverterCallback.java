package org.cip4.bambi.core;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;

/**
 * 
 */

/**
 * @author prosirai
 * empty converter cleanup stub
 */
public class ConverterCallback implements IConverterCallback
{
    private static Log log = LogFactory.getLog(ConverterCallback.class.getName());

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IConverterCallback#prepareJDFForBambi(org.cip4.jdflib.core.JDFDoc)
     */
    public ConverterCallback()
    {
        super();
    }
    public void prepareJDFForBambi(JDFDoc doc)
    {
        //
    }


    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IConverterCallback#prepareJMFForBambi(org.cip4.jdflib.core.JDFDoc)
     */
    public void prepareJMFForBambi(JDFDoc doc)
    {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IConverterCallback#updateJDFForExtern(org.cip4.jdflib.core.JDFDoc)
     */

    public void updateJDFForExtern(JDFDoc doc)
    {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IConverterCallback#updateJMFForExtern(org.cip4.jdflib.core.JDFDoc)
     */
    public void updateJMFForExtern(JDFDoc doc)
    {
        // TODO Auto-generated method stub
    }
}
