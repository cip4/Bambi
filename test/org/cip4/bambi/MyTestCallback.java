/**
 * 
 */
package org.cip4.bambi;

import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.jdflib.core.JDFDoc;

public class MyTestCallback implements IConverterCallback
{

    public MyTestCallback()
    {
        //
    }
    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IConverterCallback#prepareJDFForBambi(org.cip4.jdflib.core.JDFDoc)
     */
    public void prepareJDFForBambi(JDFDoc doc)
    {
        doc.getJDFRoot().setAttribute("bambi:callback","prepareJDFForBambi",BambiNSExtension.MY_NS);       
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IConverterCallback#prepareJMFForBambi(org.cip4.jdflib.core.JDFDoc)
     */
    public void prepareJMFForBambi(JDFDoc doc)
    {
             doc.getJMFRoot().setAttribute("bambi:callback","prepareJMFForBambi",BambiNSExtension.MY_NS);       
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IConverterCallback#updateJDFForExtern(org.cip4.jdflib.core.JDFDoc)
     */
    public void updateJDFForExtern(JDFDoc doc)
    {
        doc.getJDFRoot().setAttribute("bambi:callback","updateJDFForExtern",BambiNSExtension.MY_NS);       
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IConverterCallback#updateJMFForExtern(org.cip4.jdflib.core.JDFDoc)
     */
    public void updateJMFForExtern(JDFDoc doc)
    {
        doc.getJMFRoot().setAttribute("bambi:callback","updateJMFForExtern",BambiNSExtension.MY_NS);       
    }
    
}