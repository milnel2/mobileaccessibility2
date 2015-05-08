package cs.washington.edu.softkeypad;
import android.app.Application;
import android.speech.tts.TextToSpeech;


public class GlobalState extends Application {
	private TextToSpeech mTts;
	
	public void setTTS(TextToSpeech tts)
	{
		mTts = tts;
	}
	
	public TextToSpeech getTTS()
	{
		return mTts;
	}
}
