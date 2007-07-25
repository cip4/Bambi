/**
 * 
 */
package org.cip4.bambi;

import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.ifaces.IJMFSubscribable;
import org.cip4.jdflib.node.JDFNode;


/**
 * @author prosirai
 *
 */
public interface ISignalDispatcher
{

    /**
     * add a subscription
     * returns the channelID of the new subscription, null if snafu
     * @param subMess the subscription message - one of query or registration
     * @param queueEntryID the associated QueueEntryID, may be null.
     * @return the channelID of the subscription, if successful, else null
     */
    public String addSubscription(IJMFSubscribable subMess, String queueEntryID);

    /**
     * add a subscription
     * returns the channelID of the new subscription, null if snafu
     * @param node the node to search for inline jmfs
     * @param queueEntryID the associated QueueEntryID, may be null.
     * @return the channelIDs of the subscriptions, if successful, else null
     */
    public VString addSubscriptions(JDFNode node, String queueEntryID);

    /**
     * remove a know subscription by channelid
     * @param channelID the channelID of the subscription to remove
     */
    public void removeSubScription(String channelID);

    /**
     * remove a know subscription by channelid
     * @param queueEntryID the queueEntryID of the subscriptions to remove
     */
    public void removeSubScriptions(String queueEntryID);

    /**
     * trigger a subscription based on queuentryID
     * @param the queuentryid of the active queueentry
     * @param queueEntryID the queuentryid of the active queueentry
     * @param workStepID the workStepID of the active task
     * @param amount the amount produced since the last call, 0 if unknown, -1 for a global trigger
     */
    public void triggerQueueEntry(String queueEntryID, String workStepID, int amount );

    /**
     * trigger a subscription based on channelID
     * @param channelID the channelid of the channel to trigger
     * @param queueEntryID the queuentryid of the active queueentry
     * @param workStepID the workStepID of the active task
     * @param amount the amount produced since the last call, 0 if unknown, -1 for a global trigger
     */
    public void triggerChannel(String channelID, String queueEntryID, String workStepID, int amount);
    
    /**
     * set the active filter ids to workstepid and queuentryid, 
     * @param queueEntryID the active queuentry to set, may be null=no filter
     * @param workStepID the active workstepid to set, may be null=no filter
     */
    public void setActiveIDs(String queueEntryID, String workStepID);

}
