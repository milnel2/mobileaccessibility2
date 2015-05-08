package android_talking_software.applications.calculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android_talking_software.development.talking_tap_twice.R;

public class Main extends Activity implements OnInitListener
{
	TextToSpeech tts;
	String hello = "Hello, I am the Android Talking Calculator!";

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main4calculator);
		tts = new TextToSpeech(this, this);
Intent i = new Intent(this, ATC.class);
startActivity(i);
	}

	public void onInit(int status) 
	{
		if (status == TextToSpeech.SUCCESS)    	tts.speak(hello, TextToSpeech.QUEUE_ADD, null);
	}

	@Override
	protected void onDestroy() 
	{
		if (tts != null) 
		{
			tts.stop();            
			tts.shutdown();        
		}
		super.onDestroy();
	}

}