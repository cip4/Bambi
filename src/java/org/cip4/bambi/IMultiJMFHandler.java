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
public interface IMultiJMFHandler
{
    /**
     * adds multiple message handlers to a handler
     * @param handler the dispatcher to add handlers to 
     */
    public void addHandlers(IJMFHandler dispatcher);

}
