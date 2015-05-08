package cs.washington;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.widget.ImageView.ScaleType;

public class PlaceView extends Activity {
	private CatApplication application;
	private ImageView picField;
	private TextToSpeech mTts;
	private DbManager mdb;

	final static String TAG = "Pic2Speech_Place"; // For error logging

	private String picloc;
	//private String picname;

	//private int[] lefts;
	//private int[] tops;
	
    public void onAttachedToWindow() {
    	super.onAttachedToWindow();
    	Window window = getWindow();
    	window.setFormat(PixelFormat.RGBA_8888);
    }
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.application = (CatApplication) this.getApplication();
		mdb = this.application.getDatabaseManager();
		setContentView(R.layout.place);
		mTts = this.application.getTextToSpeech();

		picField = (ImageView)	findViewById(R.id.placepic);
		Button picButton = (Button) findViewById(R.id.place_pic_button);
		picButton.setVisibility(8);
		
		String place = null;
		Bundle extras = getIntent().getExtras();
        place = extras.getString("sourceImage");
        
    	picloc = place;
    	BitmapFactory.Options op = new BitmapFactory.Options();
		op.inSampleSize = 4;
    	Bitmap myBitmap = BitmapFactory.decodeFile(place, op);
    	picField.setImageBitmap(myBitmap);
    	Cursor c = mdb.fetchButtonsForPlace(place);
    	int total = 0;
    	Context context = getApplicationContext();	
    	if(c != null){
			total = c.getCount();
			for(int i = 0; i < total; i++){
				final FrameLayout container = new FrameLayout(context);
        	    FrameLayout layout = (FrameLayout) findViewById(R.id.select_frame);
        	    layout.addView(container);
        	    final ImageButton button = new ImageButton(context);
        	    button.setAlpha(0.4f);
        	    button.setId(555);
        	    final String tex = c.getString(5);
        	    final String au = c.getString(6);
        	    button.setId(c.getInt(0));
        	    button.setOnClickListener(new OnClickListener()
		        {
					String audio = au;
					String text = tex;
		            @Override
		            public void onClick(View v)
		            {
		            	if(audio == null){
							mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
						}else{
							MediaPlayer mp = new MediaPlayer();
			                try {
								mp.setDataSource(audio);
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalStateException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
			                try {
								mp.prepare();
							} catch (IllegalStateException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
			                mp.start();
						}
		               
		            }

		        });
        	    
        	    button.setLayoutParams(new LayoutParams(c.getInt(3), c.getInt(4)));
        	    container.setPadding(c.getInt(1),
        	                          c.getInt(2),
        	                          0,
        	                          0);
        	    container.addView(button);
        	    c.moveToNext();
        	    //Log.d(TAG, "Add button: Left: " + curLeft + " Top: " + curTop);  
			}
		}
    }
}
