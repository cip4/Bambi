/**
 * 
 */
package org.cip4.bambi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFQueueEntry;

/**
 * @author prosirai
 *
 */
public class QueueEntry implements IQueueEntry
{
    private static Log log = LogFactory.getLog(QueueEntry.class.getName());
   
    private JDFDoc theDoc;
    private JDFQueueEntry theQueueEntry;
    
    public QueueEntry(JDFDoc doc, JDFQueueEntry qe)
    {
        super();
        log.info("constructing new QueueEntry");
        theDoc=doc;
        theQueueEntry=qe;
        if(theDoc==null || theQueueEntry==null)
            log.error("null elements in queuentry");
    }

    public JDFDoc getJDF()
    {
        return theDoc;
    }
    public JDFQueueEntry getQueueEntry()
    {
        return theQueueEntry;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String s="[QueueEntry: ] \nQueueEntry : ";
        s+=theQueueEntry==null ?"null \n" : theQueueEntry.getQueueEntryID() + "\n"+theQueueEntry.toString();
        s+="\n Doc: "+theDoc==null ?"null \n": theDoc.toString();
        return s;
    }
    
}
