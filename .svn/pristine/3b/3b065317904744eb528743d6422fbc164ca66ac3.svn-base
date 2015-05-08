package cs.washington.edu.buddies;

import android.os.Handler;

/* Runnable for thread that sleeps and wakes, 
 * checking the status of the pet upon waking.
 */
public class StatusMonitor implements Runnable {
	
	// Initialize sleep time in milliseconds
	private static final int sleepTime = 5000;
	
	private Animal mAnimal;
	private Handler mHandler;
	private Game mGame;
	
	// Create runnable for posting back to game to update status
	final Runnable mUpdateStatus = new Runnable() {
		public void run() {
			mGame.onStatusChange();
		}
	};
	
	public StatusMonitor(Animal animal, Handler handler, Game game) {
		mAnimal = animal;
		mHandler = handler;
		mGame = game;
	}

	@Override
	public void run() {
		// Sleep for established time interval
		while (true) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				if (mGame.sentInterrupt)
					return;
			}
			
			// On waking, check the status of the animal
			boolean notify = mAnimal.checkStatus();
			if (notify) {
				mHandler.post(mUpdateStatus);
			}
		}		
	}
}
