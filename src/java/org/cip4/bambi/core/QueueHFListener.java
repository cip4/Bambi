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
public class QueueHFListener extends BambiLogFactory implements QueueHotFolderListener
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
	public QueueHFListener(final QueueProcessor qProc, final IConverterCallback callBackClass)
	{
		queueProc = qProc;
		_callBack = callBackClass;
	}

	/**
	 * @see org.cip4.jdflib.util.QueueHotFolderListener#submitted(org.cip4.jdflib.jmf.JDFJMF)
	 * @param submissionJMF
	 */
	public void submitted(final JDFJMF submissionJMF)
	{
		log.info("HFListner:submitted");
		final JDFCommand command = submissionJMF.getCommand(0);

		if (_callBack != null)
		{
			_callBack.prepareJMFForBambi(submissionJMF.getOwnerDocument_JDFElement());
		}

		final JDFQueueSubmissionParams qsp = command.getQueueSubmissionParams(0);

		final JDFDoc doc = qsp.getURLDoc();
		if (doc == null)
		{
			log.warn("could not process JDF File");
		}
		else
		{
			if (_callBack != null)
			{
				_callBack.prepareJDFForBambi(doc);
			}

			final JDFQueueEntry qe = queueProc.addEntry(command, null, doc);
			if (qe == null)
			{
				log.warn("_theQueue.addEntry returned null");
			}
			final String tmpURL = qsp.getURL();
			final File tmpFile = UrlUtil.urlToFile(tmpURL);
			if (tmpFile != null)
			{
				if (!tmpFile.delete())
				{
					log.warn("failed to delete temporary file " + tmpFile.getAbsolutePath());
				}
			}
		}
	}
}