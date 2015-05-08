package cs.washington.edu.buddies;

import android.os.Handler;

/* Used when multiple toasts are displayed sequentially.  
 * Use this Runnable with a worker thread in order to
 * stop sequential displays of toasts if they aren't 
 * needed (e.g., if the user starts a new activity, 
 * toasts associated with the old activity that would
 * normally be queued aren't needed). Interrupting
 * the worker thread stops the subsequent but no longer
 * needed toasts from being displayed.
 */
public class ToastDisplayer implements Runnable {
	
	// Initialize sleep time in milliseconds
	private static final int sleepTime = 2000;

	private Handler mHandler;
	private ToastDisplay mCaller;
	private int mRepeat;
	
	
	// Create runnable for posting back to game to update status
	final Runnable mDisplayToast = new Runnable() {
		public void run() {
			mCaller.displayToast();
		}
	};
	
	
	public ToastDisplayer(ToastDisplay caller, Handler handler, int repeat) {
		mHandler = handler;
		mCaller = caller;
		mRepeat = repeat;
	}

	@Override
	public void run() {
		int i = 0;
		while (i < mRepeat) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				return;
			}
			
			mHandler.post(mDisplayToast);
		}		
	}
}
