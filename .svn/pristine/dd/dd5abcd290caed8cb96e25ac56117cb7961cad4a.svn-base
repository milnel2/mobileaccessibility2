package android_talking_software.development.talking_tap_twice;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

/**
*<h1>Class Overview</h1>
 * <p></br></br>This class is used to provide sound and haptic feedback. The idea is that a class which implements android_talking_software.development.talking_tap_twice.event.TouchListener will have a FeedBack provider whose feedback method is invoked when the processTap or processEntry method of the owner class is invoked.</br></br></p>
 * 
 * @author Nicole
 *
 */
public class FeedBackProvider 
{
	private Vibrator vibe;
	private  AudioManager  manager;
	private  SoundPool pool;
private 	int streamVolume;
private int play;
		
	/**
	 * @param context
	 */
	public FeedBackProvider(Context context)
	{
		vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		pool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
				streamVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
				play = pool.load(context, R.raw.click, 1);
		}

	/**
	 * Plays a sound and performs haptic feedback.
	 */
	public void feedBack() 
	{
		vibe.vibrate(100);
	pool.play(play, streamVolume, streamVolume, 1, 0, 1f);
			}

}