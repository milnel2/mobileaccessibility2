package cs.washington.mobileaccessibility.vbraille;

import java.util.HashMap;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
/*
 * Currently this is just a demo
 * Consequently all the program's logic is in this
 * class, even though that sort of thing wouldn't
 * make sense for a general view thing
 */
public class BrailleView extends View {
	
	TextToSpeech tts;
	MorseVibrator mv;
	boolean morseMode = false;
        int mode; //default is letter mode
        int sentencePos;
        String spaceDots = "111111";
        int previousMode = 0;
        boolean sentenceOver = false;
        HashMap<String, String> params = new HashMap();
	
	public BrailleView(Context context) {
		super(context);
		tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {

			public void onInit(int arg0) {
				tts.speak("Start", 0, null);
			}
			
		});
		//OnUtteranceCompletedListener listener = 
		tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener()
		{
			public void onUtteranceCompleted(String uttID)
			{
				if(uttID == "done speaking letter")
					Log.v("TouchVibe","DONE speaking letter");
			}
			
		});
		
		//BrailLearn   *****************************************
    	longPressDuration = 3000;
    	//lastRegion = -1;
    	mPaint = new Paint();
    	mPaint.setColor(Color.WHITE);
    	setBackgroundColor(Color.BLACK);
    	//BrailleLearn *****************************************
    	
    	setClickable(true);
		rand = new Random();
		mode=0;
		sentencePos=0;
		randomizeSymbol();
		vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		mv = new MorseVibrator(vibe);
	    setClickable(true);
	    setFocusable(true);
	    setFocusableInTouchMode(true);
	    requestFocus();
	}
	
	private int vibrating = 0;
	private Vibrator vibe;
	private int whichSymbol;
	private Random rand;
	public static final String [] dots = 
	{"100000","101000","110000","110100","100100","111000","111100","101100",
		"011000","011100","100010","101010","110010","110110","100110","111010",
		"111110","101110","011010","011110","100011","101011","011101","110011",
	 "110111","100111","000000"
	};
	public static final String [] symbols =
	{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
	 "p","q","r","s","t","u","v","w","x","y","z","SPACE"};
	
	public static final String [] nato =
	{"alpha","bravo","charlie","delta","echo","foxtrot","golf","hotel","india","juliet","kilo",
		"lima","mike","november","oscar","papa","quebec","romeo","sierra","tango","uniform",
		"victor","whiskey","xray","yankee","zulu"
	};
	
    // public static final String [] sentence1 = {"y","o","u","SPACE","h","a","v","e","SPACE","t","h","r","e","e","SPACE","e","m","a","i","l","s"};
	
    public static final String [] sentence1 = {"b", "u", "s", "SPACE", "f","o","u","r","SPACE","i","s","SPACE","h","e","r","e"};

	private void randomizeSymbol() {
		int oldWhichSymbol = whichSymbol;
		do {
		whichSymbol = rand.nextInt(26);
		} while(whichSymbol == oldWhichSymbol);
		Log.v("TouchVibe","Selected symbol " + symbols[whichSymbol] + " which is " + dots[whichSymbol]);
		//here you can update the screen, see how done in braillelearn
	}

    private void sentenceSymbol(){
        //dont care about last, just want to up position of sentence
        if(sentenceOver==true)
	    {
		tts.speak("Sentence is over", 0, null);
		return;
	    }
        int h=0;
        boolean found = false; 
        while(h<27 && found == false)
	    {
		Log.i("Vibrator Braille","h is " + h);
		Log.i("Vibrator Braille","symbols[h] is " + symbols[h]);
		Log.i("Vibrator Braille","sentence1[pos] is " + sentence1[sentencePos]);
		if(symbols[h].equals(sentence1[sentencePos]))
		    {
			whichSymbol = h;
			Log.i("VBRaille", "whichSymbol is now ***** from sS" + whichSymbol + "which is letter" + symbols[whichSymbol]);
			found = true;
		    }
		h++;
	    }
	
	Log.i("VBraille", "sentence position is " + sentencePos + "and sentence1 length is " + sentence1.length);
        if(sentencePos >= sentence1.length){
	    tts.speak("Sentence is over", 0, null);
	    sentenceOver = true;
	}
        else{
	    sentencePos++;
	}
    }
	private static final long [] patternCorner = {40,40};
	private static final long [] patternMiddle = {0,1000};
	
	private void setVibration(int value) {
		if(value != vibrating) {
			switch(value) {
			case 1:
				Log.i("Vibrator Braille","Doing corner pattern");
				vibe.vibrate(patternCorner,0);
				break;
			case -1:
				Log.i("Vibrator Braille","Doing middle pattern");
				vibe.vibrate(patternMiddle,0);
				break;
			default:
				vibe.cancel();
				
			}
			vibrating = value;
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		if(keyCode == KeyEvent.KEYCODE_SPACE) {
			if(morseMode) {
				morseMode = false;
				vibe.cancel();
				tts.speak("Entering braille mode", 0, null);
			}
			else {
				morseMode = true;
				vibe.cancel();
				tts.speak("Entering morse mode.  Tap screen to hear pattern.  Use J and F to change speed", 0, null);
			}
		}

		if(keyCode == KeyEvent.KEYCODE_BACK)
		    {
			return true;
		    }

		if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
		    tts.speak("mode is: " + mode, 0, null);
		    if(mode == 0)
			{
			    mode = 1;
			    tts.speak("now in sentence mode", 0, null);
			    //whichSymbol = sentence1[];
			    sentenceSymbol();
			}
		 
                }

		if(keyCode == KeyEvent.KEYCODE_MENU) {
			params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "done speaking letter"); 
			tts.speak("Letter was " + symbols[whichSymbol] + ", as in " + nato[whichSymbol], 0, params);
			randomizeSymbol();
			SystemClock.sleep(2700);
			this.invalidate();
			return true;
		}
    	if(keyCode == KeyEvent.KEYCODE_F) {
    		if(speed > 15) {
    			speed -= 10;
    			mv.vibrate("s", speed);
    		}
    		else
    			mv.vibrate("t",30);
    		return true;
    	}
    	if(keyCode == KeyEvent.KEYCODE_J) {
    		speed += 10;
    		mv.vibrate("s", speed);
    		return true;
    	}
		
		return false;
	}
	
	
	int speed = 100;
	
	
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		if(morseMode) {
			switch(me.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mv.vibrate(symbols[whichSymbol], speed);
			}
			return true;
		}
		//Log.v("TouchVibe","TouchEvent called w/ action " + me.getAction() + " and (x,y)=(" + me.getX() + "," + me.getY() + ")");
		switch(me.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			double x = me.getX();
			double y = me.getY();
			x/=getWidth();
			y/=getHeight();
			//Log.v("TouchVibe","New (x,y)=(" + x + "," + y + ")");
			int doit = 0;
			for(int i = 0; i < 2; i++) {
				for(int j = 0; j < 3; j++) {
					
					if(dots[whichSymbol].charAt(i+2*j)=='0')
						continue;
			
					if(((int) (2*x)) == i && ((int) (3*y)) == j)
						doit = (j==1)?(-1):(1);
						
				}
			}
			setVibration(doit);
			break;
		default:
			setVibration(0);
		}
		return true;
	}
	
	//ADD IN GRAPHICS HERE

    private final Paint mPaint;    
    private int xBound;
    private int y1Bound;
    private int y2Bound;
    private int radius;
    private long longPressStartTm;
    private long longPressDuration;
    private String mSymbol;
    
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	xBound = (int) w / 2;
    	y1Bound = (int) h / 3;
    	y2Bound = y1Bound * 2;
    	radius = Math.min(xBound, y1Bound)/4;
    	super.onSizeChanged(w, h, oldw, oldh);
    }
    
    protected void onDraw(Canvas canvas) {
    	//Log.v("TouchVibe", "in on draw" );
    	
    	float x, y;
    	int xUnit = xBound/2;
    	int yUnit = y1Bound/2;

    	mSymbol = dots[whichSymbol];
    	String symbol = mSymbol;
    	
		//if (showNumSign)
			//symbol = numberdots[0];
    	
    	for (int i = 0; i < 6; i++) {
    		if (symbol.charAt(i) == '1') {
    			x = (i % 2 == 0) ? xUnit : xUnit + xBound;
    			y = (i < 2) ? yUnit : (i < 4) ? yUnit + y1Bound : yUnit + y2Bound;
    			canvas.drawCircle(x, y, radius, mPaint);
    		}
    	}
    }
	
	

}
