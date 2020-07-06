package org.cip4.bambi.core.messaging;

import org.apache.commons.logging.Log;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFSignal;

import java.util.List;

class SenderQueueOptimizer {

    private final Log log = BambiLogFactory.getLog(MessageSender.class);

    private final MessageSender messageSender;

    /**
     * Custom constructor. Accepting a MessageSender object for initializing.
     */
    public SenderQueueOptimizer(MessageSender messageSender) {
        super();
        this.messageSender = messageSender;
    }

    /**
     * @param jmf
     */
    protected void optimize(final JDFJMF jmf) {
        final VElement messages = jmf == null ? null : jmf.getMessageVector(null, null);
        if (messages != null) {
            messageSender.checkedJMF++;
            for (final KElement m : messages) {
                optimizeMessage((JDFMessage) m);
            }
        }
    }

    /**
     * @param newMessage
     */
    private void optimizeMessage(final JDFMessage newMessage) {
        if (!(newMessage instanceof JDFSignal)) {
            return;
        }
        final JDFMessage.EnumType typ = newMessage.getEnumType();
        if (typ == null) {
            return;
        }
        final IMessageOptimizer opt = messageSender.myFactory.getOptimizer(typ);
        if (opt == null) {
            return;
        }
        messageSender.checked++;
        final List<MessageDetails> tail = messageSender._messages.getTailClone();
        if (tail != null) {
            for (int i = tail.size() - 1; i >= 0; i--) {
                final MessageDetails messageDetails = tail.get(i);
                if (messageDetails == null) {
                    log.warn("empty message in tail...");
                    break;
                }
                final JDFJMF jmf = messageDetails.jmf;
                if (jmf == null) {
                    continue; // don't optimize mime packages
                }
                final VElement v2 = jmf.getMessageVector(null, null);
                if (v2 == null) {
                    continue;
                }
                for (int ii = v2.size() - 1; ii >= 0; ii--) {
                    final JDFMessage mOld = (JDFMessage) v2.get(ii);
                    if (mOld instanceof JDFSignal) {
                        final IMessageOptimizer.optimizeResult res = opt.optimize(newMessage, mOld);
                        if (res == IMessageOptimizer.optimizeResult.remove) {
                            removeMessage(mOld, messageDetails);
                        } else if (res == IMessageOptimizer.optimizeResult.cont) {
                            return; // we found a non matching message and must stop optimizing
                        }
                    }
                }
            }
        }
    }

    /**
     * @param old
     * @param messageDetails
     */
    private void removeMessage(final JDFMessage old, final MessageDetails messageDetails) {
        synchronized (messageSender._messages) {
            final JDFJMF jmf = old.getJMFRoot();
            jmf.removeChild(old);
            messageSender.removedHeartbeat++;
            if (messageSender.myFactory.isLogLots() || messageSender.removedHeartbeat < 10 || messageSender.removedHeartbeat % 1000 == 0) {
                log.info("removed redundant " + old.getType() + " " + old.getLocalName() + " Message ID= " + old.getID() + " Sender= " + old.getSenderID() + "# " + messageSender.removedHeartbeat + " / "
                        + messageSender.checked);
            }
            final VElement v = jmf.getMessageVector(null, null);
            if (v == null || v.size() == 0) {
                final boolean zapped = messageSender._messages.remove(messageDetails);
                if (zapped) {
                    messageSender.removedHeartbeatJMF++;
                    if (messageSender.myFactory.isLogLots() || messageSender.removedHeartbeatJMF < 10 || messageSender.removedHeartbeatJMF % 1000 == 0) {
                        log.info("removed redundant jmf # " + messageSender.removedHeartbeatJMF + " ID: " + jmf.getID() + " total checked: " + messageSender.checkedJMF);
                    }
                } else {
                    log.warn("could not remove redundant jmf # " + messageSender.removedHeartbeatJMF + " ID: " + jmf.getID() + " total checked: " + messageSender.checkedJMF);
                }
            }
        }
    }
}
