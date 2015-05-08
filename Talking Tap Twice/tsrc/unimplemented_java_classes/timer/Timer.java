package unimplemented_java_classes.timer;


import java.util.LinkedList;

/**
 * <h1>Class Overview</h1>
* <p></br></br>This class was designed as a replacement for the javax.swing.Timer, which is not available on the Android platform. It provides most of the functionality provided by the javax.swing.Timer.</br></br></p>
 * 
 * @author Nicole Torcolini
 */
public class Timer 
{
	/**
	 * 
	 */
	protected LinkedList<TimerListener> listenerList;
	private int delay, initialDelay, times;
	private boolean repeats, running, sameThread;
	private Thread counter;
	private Runnable runner;

	/**
	 * Creates a Timer that will notify its listeners every delay milliseconds.
	 * 
	 * If delay is less than or equal to zero the timer will fire as soon as it is started. If listener is not null, it's registered as an action listener on the timer. If sameThread is set to true, the Timer will use the main Thread to count and call fireAction. This is necessary if the actionPerformed method of the TimerListener includes modifying a View. However, the only way to stop the Timer when running this way is for the class owning the TimerListener to have a reference to the Timer which the TimerListener can access to call stop during the actionPerformed method. If a View is not modified, it is preferable to set sameThread to false, so that the Timer will use a separate Thread to count, call fireAction, and start the next count. This allows the Timer to be stopped at any time.
		 * @param delay the number of milliseconds between action events 
		 * @param listener an initial listener; can be null 
		 * @param sameThreads Specifies if the Timer uses the main Thread to count and call fireAction.
		 */
		public Timer(int delay, TimerListener listener, boolean sameThread)
	{
			this.sameThread = sameThread;
		init(delay, listener);
	}

	private void init(int delay, TimerListener listener)
	{
		repeats = true;
		initialDelay = this.delay = delay;
		times = 0;
		listenerList = new LinkedList<TimerListener>();
		if (listener != null) listenerList.add(listener);
		runner = (sameThread) ? new Runnable()
		{

			public void run() 
			{
				waitForSame();
			}
		} :
			new Runnable()
		{

			public void run() 
			{
				waitForDiff();
			}
		};
	}

	/**
	 * Adds an action listener to the Timer.
	 *  
	 * @param listener the listener to add 
	 */
	public void addListener(TimerListener listener)
	{
		listenerList.add(listener);
	}

	/**
	 * Notifies all listeners that have registered interest for notification on this event type. 
	 */
	protected void fireActionPerformed()
	{
		for (TimerListener listener:listenerList)
				listener.actionPerformed();
	}

	/**
	 * Returns true if the Timer is running.
	 */
	public boolean isRunning()
	{
		return running;
	}

	/**
	 * Removes the specified action listener from the Timer.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeListener(TimerListener listener)
	{
		listenerList.remove(listener);
	}

	/**
	 * Restarts the Timer, canceling any pending firings and causing it to fire with its initial delay.
	 */
	public void restart()
	{
		stop();
		start();
	}

	/**
	 *Starts the Timer, causing it to start sending action events to its listeners. 
	 */
	public void start()
	{
		if (running) return;
		running = true;
		times = 0;
		if (sameThread) execute();
		else executions();
	}

	private void executions() 
	{
		counter = new Thread(runner);
		try
		{
			counter.start();
			}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *Stops the Timer, causing it to stop sending action events to its listeners. 
	 */
	public void stop()
	{
		if (running)
		{
			counter.interrupt();
			running = false;
		}
	}

	private void waitForSame()
	{
		synchronized(counter)
		{
			try
			{
				if (times == 0) counter.wait(initialDelay);
				else counter.wait(delay);
			}
			catch (Exception e)
			{
				return;
			}
			times++;
		}
	}

	private void waitForDiff()
	{
		synchronized(counter)
		{
			try
			{
				if (times == 0) counter.wait(initialDelay);
				else counter.wait(delay);
			}
			catch (Exception e)
			{
				return;
			}
			if (!counter.isInterrupted()) fireActionPerformed();
			delay++;
		}
		if ((!repeats) || (counter.isInterrupted())) return;
		executions();
	}

private void execute() 
	{
		counter = new Thread(runner);
		try
		{
			counter.start();
			counter.join();
			if (!counter.isInterrupted()) fireActionPerformed();
			if ((!repeats) || (!running) || (counter.isInterrupted())) return;
			execute();
}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Returns the delay, in milliseconds, between firings of action events.
	 */
	public int getDelay() 
	{
		return delay;
	}

	/**
	 * Sets the Timer's delay, the number of milliseconds between successive action events.
	 * 
	 * @param delay the delay in milliseconds
	 */
	public void setDelay(int delay) 
	{
		initialDelay = this.delay = delay;
	}

	/**
	 * Returns the Timer's initial delay.
	 */
	public int getInitialDelay() 
	{
		return initialDelay;
	}

	/**
	 *Sets the Timer's initial delay, which by default is the same as the between-event delay.
	 * This is used only for the first action event. Subsequent action events are spaced using the delay property.
	 *  
	 * @param initialDelay the delay, in milliseconds, between the invocation of the start method and the first action event fired by this timer 
	 */
	public void setInitialDelay(int initialDelay) 
	{
		this.initialDelay = initialDelay;
	}

	/**
	 * Returns true (the default) if the Timer will send an action event to its listeners multiple times.
	 */
	public boolean isRepeats() 
	{
		return repeats;
	}

	/**
	 * If flag is false, instructs the Timer to send only one action event to its listeners.
	 * 
	 * @param flag specify false to make the timer stop after sending its first action event
	 */
	public void setRepeats(boolean flag) 
	{
		repeats = flag;
	}

	/**
	 * Returns an array of all the action listeners registered on this timer.
	 * 
	 * @return all of the timer's Listeners or an empty array if no listeners are currently registered 
	 */
	public TimerListener[] getListeners() 
	{
		return (TimerListener[]) listenerList.toArray();
	}

}