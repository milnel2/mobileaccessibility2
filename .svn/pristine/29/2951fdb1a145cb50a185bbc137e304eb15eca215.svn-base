package LogToFile;

/** This runnable is used by FileLog to offload the
 *  posting of log entries via HTTP from the UI thread
 *  to a worker thread since the posting activity can
 *  block significantly.
 * 
 * @author hollijr
 */
public class HttpPostManager implements Runnable 
{
	// Initialize sleep time in milliseconds
	private static final int sleepTime = 2000;
	
	private FileLog mFileLog;
		
	public HttpPostManager(FileLog filelog) {
		mFileLog = filelog;
	}

	public void run() {
		// Sleep for established time interval
		while (true) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				if (mFileLog.mSentInterrupt)
					return;
			}
			
			// On waking, post any available entries
			if (!mFileLog.mSentInterrupt) mFileLog.postEntries();	
			else return;
		}		
	}


}
