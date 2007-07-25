/**
 * 
 */
package org.cip4.bambi;

/**
 * this interface specifies the interface of a message dispatcher/handler
 * 
 * @author prosirai
 *
 */
public interface IJMFHandler
{
    /**
     * add a message handler
     * @param handler the handler associated with the event
     */
    public void addHandler(IMessageHandler handler);

}
