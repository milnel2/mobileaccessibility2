package edu.uw_cse.ma.accessiblemenu;

import java.util.Locale;
import android.app.Application;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;


public class GlobalState extends Application {
	private static TextToSpeech sTts;
	private static Context sContext;
	private static OnInitListener sListener;
	
	protected void killTTS()
	{
		if (sTts != null) {
			sTts.stop();
			sTts.shutdown();
		}
		sTts = null;
	}
	
	protected static TextToSpeech getTTS()
	{
		return sTts;
	}
	
	protected TextToSpeech createTTS(Context context, OnInitListener listener) 
	{
		sContext = context;
		sListener = listener;
		killTTS();
		sTts = new TextToSpeech(context, new OnInitListener() {

			@Override
			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS) {
					sTts.setSpeechRate((float) 1.25);  // use phone setting instead, automatic in 2.1+
			        sTts.setLanguage(Locale.US);
					sTts.addEarcon("ding", sContext.getPackageName(), R.raw.ding);
					sTts.addEarcon("duobong", sContext.getPackageName(), R.raw.duobong);
					sTts.addEarcon("suboptions", sContext.getPackageName(), R.raw.duobong); 
					sTts.addEarcon("flingdown", sContext.getPackageName(), R.raw.flingdown);
					sTts.addEarcon("flingup", sContext.getPackageName(), R.raw.flingup);
				}
				if (sListener != null) sListener.onInit(status);
				sContext = null;
				sListener = null;
			}
			
		});
		return sTts;
	}
	
	protected static void configureTTS(Context context)
	{
		sTts.setSpeechRate((float) 1.25);  // use phone setting instead, automatic in 2.1+
        sTts.setLanguage(Locale.US);
		sTts.addEarcon("ding", context.getPackageName(), R.raw.ding);
		sTts.addEarcon("duobong", context.getPackageName(), R.raw.duobong);
		sTts.addEarcon("suboptions", context.getPackageName(), R.raw.duobong); 
		sTts.addEarcon("flingdown", context.getPackageName(), R.raw.flingdown);
		sTts.addEarcon("flingup", context.getPackageName(), R.raw.flingup);
	}
	
}
