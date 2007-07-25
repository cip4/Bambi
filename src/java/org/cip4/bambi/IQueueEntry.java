/**
 * 
 */
package org.cip4.bambi;

import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFQueueEntry;

/**
 * @author prosirai
 *
 */
public interface IQueueEntry
{

    public JDFDoc getJDF();
    public JDFQueueEntry getQueueEntry();
    
}
