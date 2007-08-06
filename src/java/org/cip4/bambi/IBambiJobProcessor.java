package org.cip4.bambi;

/**
 * interface for all Bambi job processors
 * @author boegerni
 *
 */
public interface IBambiJobProcessor extends Runnable {

	public abstract void run();

	/**
	 * stop processing job phases
	 */
	public abstract void stopProcessing();

	/**
	 * continue processing job phases. Remember to send a notify to "this", e.g.: <br>
	 * <code>simJob.resumeProcessing(); <br>
	 * simJob.notify();
	 * </code>
	 */
	public abstract void resumeProcessing();

	/**
	 * abort processing job phases
	 */
	public abstract void abortProcessing();

}