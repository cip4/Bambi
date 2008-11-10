/**
 * 
 */
package org.cip4.bambi.core;

import java.io.File;

import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.util.QueueHotFolderListener;
import org.cip4.jdflib.util.UrlUtil;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 *
 */
public class QueueHFListener implements QueueHotFolderListener
{
	/**
	 * 
	 */
	private IConverterCallback _callBack = null;
	private final QueueProcessor queueProc;

	/**
	 * @param qProc 
	 * @param callBackClass
	 */
	public QueueHFListener(QueueProcessor qProc, IConverterCallback callBackClass)
	{
		queueProc = qProc;
		_callBack = callBackClass;
	}

	/**
	 * @see org.cip4.jdflib.util.QueueHotFolderListener#submitted(org.cip4.jdflib.jmf.JDFJMF)
	 * @param submissionJMF
	 */
	public void submitted(JDFJMF submissionJMF)
	{
		AbstractDevice.log.info("HFListner:submitted");
		JDFCommand command = submissionJMF.getCommand(0);

		if (_callBack != null)
			_callBack.prepareJMFForBambi(submissionJMF.getOwnerDocument_JDFElement());

		JDFQueueSubmissionParams qsp = command.getQueueSubmissionParams(0);

		JDFDoc doc = qsp.getURLDoc();
		if (doc == null)
		{
			AbstractDevice.log.warn("could not process JDF File");
		}
		else
		{
			if (_callBack != null)
				_callBack.prepareJDFForBambi(doc);

			JDFQueueEntry qe = queueProc.addEntry(command, null, doc);
			if (qe == null)
				AbstractDevice.log.warn("_theQueue.addEntry returned null");
			final String tmpURL = qsp.getURL();
			final File tmpFile = UrlUtil.urlToFile(tmpURL);
			if (tmpFile != null)
			{
				if (!tmpFile.delete())
					AbstractDevice.log.warn("failed to delete temporary file " + tmpFile.getAbsolutePath());
			}
		}
	}
}