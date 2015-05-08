package cs.washington.mobileaccessibility.portrait;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.*;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.*;
import android.graphics.Bitmap.Config.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.Camera.ErrorCallback;
import android.media.AudioManager;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.*;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.os.Vibrator;
import android.view.View;
import android.view.View.*;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import android.speech.tts.TextToSpeech;

// ----------------------------------------------------------------------

public class FrameFaces extends Activity implements OnGestureListener, OnDoubleTapListener
{ 
    private static final String TAG = "FindFaces";
    public GraphicsView view;
	
	static boolean mPreviewRunning = false;
	public static boolean objectFound = false;
	private GestureDetector mGestureDetector;
	private static final int SWIPE_MIN_DISTANCE = 80;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private Bitmap image;
	Circle[] circleFacesArray;
	Vibrator myVibrate = null;
	boolean takeFinalPhoto = false; 
	Canvas canvas;
	static Paint contrastPaint;
	Bitmap faceContrast;
	boolean readyToDrawCircles = false;
	
	private TextToSpeech myTTS = null;
   
	//@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		// Hide the window title and status bar.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//No notification bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
		WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		
		Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		//Log.i("touch", "DISPLAY H***: " + display.getHeight() + " DISPLAY W:***: " + display.getWidth() + " PIXEL FORMAT: " + display.getPixelFormat());
		
		//setContentView(R.layout.contrast_layout);
		
		//view=(GraphicsView)findViewById(R.id.graphics);
		//setContentView(R.layout.contrast_layout);
		//setContentView(view);
		
		
		//holder = (SurfaceHolder) view.getHolder();
		//holder.addCallback(this);
		//holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		
		mPreviewRunning = false;
		//view.setPreviewRunning(false);
		circleFacesArray = null; //want to reset to null before pic taken		
				
		//view.requestFocus();

		final Context context = this.getApplicationContext();
		//view = new GraphicsView(context);
		//setContentView(R.layout.contrast_layout);
		//view = (GraphicsView) findViewById(R.id.graphics);
		
		//view.setFocusable(true);
		//view.setFocusableInTouchMode(true);
		view = new GraphicsView(context);
		setContentView(view);

		//be able to change volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		myVibrate = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		mGestureDetector = new GestureDetector(this);
		mGestureDetector.setOnDoubleTapListener(new OnDoubleTapListener()
		{
		  public boolean onDoubleTap(MotionEvent e) 
		  {
			  myTTS.speak("you double tapped", 0, null);
			  //here i want to take photo unless preview not running, then double tap to start preview again
			  if(!mPreviewRunning)
			  {
				  Log.v("blah", "preview not running"); //**
				  if(view.camera!=null)
				  {
					  Log.v("blah", "camera not null"); //**
					  try 
					  {
						  Log.v("blah", "in try for doubletap");
						  view.camera.setPreviewDisplay(view.holder); 
						  view.camera.startPreview();
						  Log.v("blah", "started preview in double tap");
						  mPreviewRunning = true;
						
						  // throws IOException
						  view.camera.setErrorCallback(new ErrorCallback() {
							  public void onError(int code, Camera c) {
								  Log.v("blah", "OnError for camera");
								  if(code == Camera.CAMERA_ERROR_SERVER_DIED)
									  Log.e("cam","The camera server died");
								  else
									  Log.e("cam","Unknown camera error");
							  }
						  });
					  }
					  catch(IOException ioe) {
						  Log.v("blah", "in catch for doubletap");
						  if(view.camera !=null)
						  {
							  view.camera.release();
							  view.camera = null;
						  }
					  }
				 return true;
				} //end if null
				  else
				  Log.v("blah", "camera not null");
				  
				//camera.startPreview();
				//mPreviewRunning = true;
				 return true;
			  }
			  else
				  Log.v("blah", "preview was running");
			  circleFacesArray = null;
			  view.camera.stopPreview();
			  view.camera.takePicture(null, mPictureCallback, mPictureCallback);
			  view.camera.startPreview();
			  //objectFound = false;
			  return true;
		  }
		public boolean onDoubleTapEvent(MotionEvent e) {
			return true;
		}
		public boolean onSingleTapConfirmed(MotionEvent e) 
		{
			return true;
		}
	  });
		myTTS = new TextToSpeech(this, ttsInitListener);
	}

	private TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() 
	{
		public void onInit(int version)
		{
			myTTS.speak("Click the camera button to take a portrait", 0, null);
		}
	};



	@Override
	public boolean onTouchEvent(MotionEvent me) 
	{
		if(circleFacesArray!=null)
		{
			//Log.i("touch", "" + me.getAction());
			switch(me.getAction()) 
			{
		    case MotionEvent.ACTION_DOWN:
		    case MotionEvent.ACTION_MOVE:
		            double x = me.getX();
		            double y = me.getY();
		            
		            Log.i("touch","*****New (x,y) Touch =(" + x + "," + y + ")");
		            x=me.getRawX();
		            y=me.getRawY();
		           
		           
		            Log.i("touch", "\n About to call INANYCIRCLE: x and y are: " + (int)x + ", " + (int)y);
		            if(inAnyCircle((int)x, (int)y, circleFacesArray))
		        	{
		        		Log.i("touch","%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%TOUCHEVENT: in a circle");
		        		myVibrate.vibrate(300);
		        	}
			}
		}
		return mGestureDetector.onTouchEvent(me);
	}


	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(event.getAction() != KeyEvent.ACTION_DOWN)
	        return super.onKeyDown(keyCode, event);
		switch (keyCode) 
		{
			case KeyEvent.KEYCODE_CAMERA:
				myTTS.speak("hit camera button",0, null);
			default:
				return super.onKeyDown(keyCode, event);
		}
		
	}


	protected void onResume()
	{	
		super.onResume();	
	}
	
	protected void onPause()
	{
		super.onPause();
	}
	
	protected void onDestroy()
	{
		myTTS.stop();
		Log.i("cam", "in FrameFaces ondestroy");
		super.onDestroy();
	}
	
	//check to see if x, y and in circle with center_x, center_y, radius
	protected boolean inCircle(int center_x, int center_y, int radius, int x, int y)
	{
		//x and y are the point we are testing
		double ratioX = 848.0/512.0;
		double ratioY = 480.0/384.0;
		double radius1= radius;
		double x1 = center_x * 1.0;
		double y1 = center_y * 1.0;
		radius1 = radius1 * ratioX * 1.0;
		x1 = x1 * ratioX * 1.0;
		y1 = y1 * ratioY * 1.0;
		Log.i("touch","\n INCIRCLE: center_x: " + x1 + " center_y: " + y1 + " radius: " + radius1 + " x: " + x + " y: "+ y);
		Log.i("touch","\n INCIRCLE: regular coords for circle: " + center_x + ", " + center_y + " r: " + radius );
		
		double dist = Math.sqrt(Math.pow((x1-x),2) + Math.pow((y1-y),2));
		if(dist<=radius1)
		{
			Log.i("touch", "IN CIRCLE WOOHOO");
		}
		return dist <= radius1;
	}
	
	//to know where the faces are on the screen, for vibration or other information
	protected boolean inAnyCircle(int x, int y, Circle[] circleArray)
	{
		Log.i("touch","\n inanycircle method");
		for(int i=0; i<circleArray.length; i++)
		{
			Log.i("touch", "i: " + i + " x: " + (int)circleArray[i].center.x + " y: " + (int)circleArray[i].center.y + " radius: " + circleArray[i].radius + " testing x: " + x + " testing y: " + y);
			if (inCircle((int)circleArray[i].center.x, (int) circleArray[i].center.y, circleArray[i].radius, x, y))
				return true;
		}
		return false;
	}
	
	//method to find the framing of the faces, individually and together
	protected void findFraming(Circle[] circleArray, Bitmap bmap)
	{
		//go through all circles, find topmost point, leftmost, rightmost, bottommost
		//topmost must be top of some circle (lowest y)
		int topPointY=3000, bottomPointY=-1000, leftPointX=3000, rightPointX=-1000;
		
		//say out loud the size of the faces in relation to whole image
		int diameter;
		for(int i=0; i<circleArray.length; i++)
		{
			diameter=circleArray[i].radius * 2;
			myTTS.speak("Face " + (i+1) + " is 1 " + (int)(bmap.getWidth()/diameter) + " of the width of screen", TextToSpeech.QUEUE_ADD, null);
			Log.i("framepoints", "diameter over width: " + (int)(bmap.getWidth()/diameter));
		}
		
		for(int i=0; i<circleArray.length; i++)
		{
			Log.i("framepoints", "On Circle: " + i);
			int tempTopY = (int)(circleArray[i].center.y-circleArray[i].radius);
			Log.i("framepoints", "Top Y: " + tempTopY);
			if(tempTopY < topPointY)
				topPointY = tempTopY;
			int tempBottomY = (int)(circleArray[i].center.y+circleArray[i].radius);
			Log.i("framepoints", "Bottom Y: " + tempBottomY);
			if (tempBottomY > bottomPointY)
				bottomPointY = tempBottomY;
		}
		for(int j=0; j<circleArray.length; j++)
		{
			Log.i("framepoints", "On Circle: " + j);
			int tempRightX = (int)(circleArray[j].center.x+circleArray[j].radius);
			Log.i("framepoints", "Right X: " + tempRightX);
			Log.i("framepoints", "Center X: " + (int)circleArray[j].center.x);
			Log.i("framepoints", "Radius: " + (int)circleArray[j].radius);
			if(tempRightX > rightPointX)
				rightPointX = tempRightX;
			int tempLeftX = (int)(circleArray[j].center.x-circleArray[j].radius);
			Log.i("framepoints", "Left X: " + tempLeftX);
			Log.i("framepoints", "Center X: " + (int)circleArray[j].center.x);
			Log.i("framepoints", "Radius: " + (int)circleArray[j].radius);
			if (tempLeftX < leftPointX)
				leftPointX = tempLeftX;
		}

		//print out 4 points you got
		Log.i("framepoints", "topPointY: " + topPointY);
		Log.i("framepoints", "bottomPointY: " + bottomPointY);
		Log.i("framepoints", "leftPointX: " + leftPointX);
		Log.i("framepoints", "rightPointX: " + rightPointX);
		
		if(topPointY <= 0 || topPointY >=bmap.getHeight()-1)
			topPointY = 1;
		if(leftPointX <=0 || leftPointX >= bmap.getWidth()-1)
			leftPointX = 1;
		if(bottomPointY > bmap.getHeight()-1 || bottomPointY <=0)
			bottomPointY = bmap.getHeight()-3;
		if(rightPointX > bmap.getWidth()-1 || rightPointX <=0)
			rightPointX = bmap.getWidth()-3;
		
		Log.i("framepoints AFTER changes", "topPointY: " + topPointY);
		Log.i("framepoints", "bottomPointY: " + bottomPointY);
		Log.i("framepoints", "leftPointX: " + leftPointX);
		Log.i("framepoints", "rightPointX: " + rightPointX);
		
		Log.i("framepoints", "width: " + bmap.getWidth());
		Log.i("framepoints", "height: " + bmap.getHeight());
		
		//now draw framing on bitmap
		for(int a=leftPointX; a < rightPointX; a++)
		{
			//draw top line
			bmap.setPixel(a,topPointY, Color.RED);
			//draw bottom line
			bmap.setPixel(a,bottomPointY, Color.RED);
		}
		for(int b=topPointY; b < bottomPointY; b++)
		{
			bmap.setPixel(leftPointX, b, Color.RED);
			bmap.setPixel(rightPointX, b, Color.RED);
		}
		
		//draw individual faces framings
		//check if they are within bounds of screen!
		for(int k=0; k<circleArray.length; k++)
		{
			int leftPoint = (int) (circleArray[k].center.x - circleArray[k].radius);
			int rightPoint = (int) (circleArray[k].center.x + circleArray[k].radius);
			int topPoint = (int) (circleArray[k].center.y - circleArray[k].radius);
			int bottomPoint = (int) (circleArray[k].center.y + circleArray[k].radius);
			
			if (leftPoint <= 0)
				leftPoint = 0;
			if (rightPoint >= bmap.getWidth())
				rightPoint = bmap.getWidth();
			if(topPoint <= 0)
				topPoint = 0;
			if(bottomPoint >= bmap.getHeight())
				bottomPoint = bmap.getHeight();
			
			for(int v= leftPoint; v<rightPoint; v++)
			{
				//draw top line
				bmap.setPixel(v,topPoint, Color.YELLOW);
				//draw bottom line
				bmap.setPixel(v,bottomPoint, Color.YELLOW);
			}
			for(int v= topPoint; v<bottomPoint; v++)
			{
				//draw top line
				bmap.setPixel(leftPoint,v, Color.YELLOW);
				//draw bottom line
				bmap.setPixel(rightPoint,v, Color.YELLOW);
			}
		}
		
		int spaceTop, spaceBottom, spaceLeft, spaceRight = 0;
		spaceTop = topPointY;
		spaceBottom = bmap.getHeight() - bottomPointY;
		spaceLeft = leftPointX;
		spaceRight = bmap.getWidth() - rightPointX;
		// have within some tolerance so not toooo picky
		//also add in vibrations
		int tolerance = 100;
		float minTopDistance = 1000;
		float minBottomDistance = 1000;
		
		//first, try speaking spacings. then make relative choices.
		//Log.i("touch","circle array is not null: length is " + circleArray.length);
		//these are margins on the bmap not screen
		
		Log.i("touch","Top Margin is " + spaceTop + ". Bottom Margin is " + spaceBottom + ". Left Margin is " + spaceLeft + ". Right Margin is " + spaceRight);
		//first just simple right left up down
		int j;
		boolean allTopHalf = true, allBottomHalf = true, allRightHalf = true, allLeftHalf = true;
		for(j=0; j<circleArray.length; j++)
		{
			//for now just using centers, not radius
			
			if(circleArray[j].center.y > bmap.getHeight()/2)
			{
				allTopHalf = false;
				if ((circleArray[j].center.y - (bmap.getHeight()/2)) < minBottomDistance)
					minBottomDistance = circleArray[j].center.y - (bmap.getHeight()/2); 
			}
			else
			{
				allBottomHalf = false;
				if (((bmap.getHeight()/2)-circleArray[j].center.y) < minTopDistance)
					minTopDistance = (bmap.getHeight()/2) - circleArray[j].center.y;
			}
			if(circleArray[j].center.x > bmap.getWidth()/2)
			{
				allLeftHalf = false;
			}
			else
				allRightHalf = false;
		}
		
		if(allTopHalf)
		{
			if(minTopDistance > tolerance)
			{
				if(allRightHalf)
					myTTS.speak("Move the camera up and right",TextToSpeech.QUEUE_ADD,null);
				else if(allLeftHalf)
					myTTS.speak("Move the camera up and left",TextToSpeech.QUEUE_ADD,null);
				else 
					myTTS.speak("Move the camera up",TextToSpeech.QUEUE_ADD,null);
			}
			else if(minTopDistance <= tolerance)
			{
				if(allRightHalf)
					myTTS.speak("Move the camera right",TextToSpeech.QUEUE_ADD,null);
				else if(allLeftHalf)
					myTTS.speak("Move the camera left",TextToSpeech.QUEUE_ADD,null);
				else
				{
					myTTS.speak("Great! Take a photo",TextToSpeech.QUEUE_ADD,null);
					takeFinalPhoto = true; //after this one is taken
				}
			}
			
		}
		else if(allBottomHalf)
		{
			if(minBottomDistance > tolerance)
			{
				if(allRightHalf)
					myTTS.speak("Move the camera down and right",TextToSpeech.QUEUE_ADD,null);
				else if (allLeftHalf)
					myTTS.speak("Move the camera down and left",TextToSpeech.QUEUE_ADD,null);
				else 
					myTTS.speak("Move the camera down",TextToSpeech.QUEUE_ADD,null);
			}
			else if(minBottomDistance <=tolerance)
			{
				if(allRightHalf)
					myTTS.speak("Move the camera right",TextToSpeech.QUEUE_ADD,null);
				else if (allLeftHalf)
					myTTS.speak("Move the camera left",TextToSpeech.QUEUE_ADD,null);
				else if(minBottomDistance < tolerance)
				{
					myTTS.speak("Great! Take a photo",TextToSpeech.QUEUE_ADD,null);
					takeFinalPhoto = true; //after this one is taken
				}
			}
		}
		else if(allLeftHalf)
		{
			myTTS.speak("Move the camera left",TextToSpeech.QUEUE_ADD,null);
		}
		else if(allRightHalf)
		{
			myTTS.speak("Move the camera right",TextToSpeech.QUEUE_ADD,null);
		}
		else
		{
			myTTS.speak("Great! Take a photo",TextToSpeech.QUEUE_ADD,null);
			takeFinalPhoto = true; //after this one is taken
			//***added 9/9
			view.camera.startPreview();
			mPreviewRunning = true;
			//view.setPreviewRunning(true);
			//you may want to save the photo only here, eventually, though all are interesting for the research.
		}
		//need to add SIZE (like go closer farther)
		
		//now you want the preview in the display again
		/*view.setBackgroundDrawable(null);
		try {
			camera.setPreviewDisplay(holder); // throws IOException
			camera.setErrorCallback(new ErrorCallback() {
			public void onError(int code, Camera c) {
				if(code == Camera.CAMERA_ERROR_SERVER_DIED)
					Log.e("cam","The camera server died");
				else
					Log.e("cam","Unknown camera error");
			}
		});
		}
		catch(IOException ioe) {
		camera.release();
		camera = null;
		}
	  
	  camera.startPreview();
	  mPreviewRunning = true;*/
	}
	
	
	
	private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback()
	{
		public void onPictureTaken(byte[] data, Camera camera) 
		{
			if(data != null)
			{
		
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 4;
	
				image = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				int w = image.getWidth(); //512
				int h = image.getHeight(); //384
				Log.i("cam","width: " + w); 
				Log.i("cam","height: " + h);
				int[] pixels = new int[w*h]; //196,608
				image.getPixels(pixels,0,w,0,0,w,h);
		
				//if taking final picture, you dont need to do all the face recognition
				if(takeFinalPhoto)
				{
					String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
					String saveToPath = extStorageDirectory + "/DCIM/Camera/";
					Log.i("filesystem", "savetopath: " + saveToPath);
					File file = new File(saveToPath, "finalPhoto.jpg");
					OutputStream outStream = null;
					try 
					{
					    outStream = new FileOutputStream(file);
					    image.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
					    outStream.flush();
					    outStream.close();
					   
					}
					catch (IOException e) 
					{
					    // TODO Auto-generated catch block
					    e.printStackTrace();
					} 
					//MediaStore.Images.Media.insertImage(FrameFaces.this.getContentResolver(), image, "FinalImageTaken", "final test image");
					takeFinalPhoto = false;
					myTTS.speak("Ready to prepare for next photo. Tap twice to take a portrait.", TextToSpeech.QUEUE_ADD, null);
					//need to go back to preview here ****
				}//end if takeFinalPhoto
		
				//***
				else
				{
					FaceDetector androidFaces = new FaceDetector(w,h,10);
					Face [] faces = new Face[10];
					int numFaces = androidFaces.findFaces(image, faces);
					PointF point = new PointF(0,0);
					float confidence;
					float eyesDistance;
					//make new bitmap to show face locations
			
					Bitmap faceFramed = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
					faceFramed.setPixels(pixels, 0, w, 0, 0, w, h);
					//faceFramed.eraseColor(Color.YELLOW);
					//Bitmap faceFramed = Bitmap.createBitmap(image, 0, 0, w, h);
					//Bitmap faceFramed = BitmapFactory.decodeByteArray(data,0,data.length,options);
		
					for(int x=0; x<numFaces; x++)
					{
						confidence = faces[x].confidence();
						eyesDistance = faces[x].eyesDistance();
						faces[x].getMidPoint(point); 
						Log.i("cam2", "NumFaces: " + numFaces + " Confidence: " + confidence);
						Log.i("cam2", "Eye Distance: " + eyesDistance);
						Log.i("cam2", "MidPoint X Y: " + point.x + " " + point.y );
						int newX = Math.round(point.x);
						int newY = Math.round(point.y);
						int j = 0;
						int xMove = newX-(int)(eyesDistance/2);
						while(j<eyesDistance)
						{
							faceFramed.setPixel(xMove,newY, Color.BLACK);
							faceFramed.setPixel(xMove, newY + 1, Color.BLACK);
							faceFramed.setPixel(xMove, newY - 1, Color.BLACK);
							j++;
							xMove++;
						}
						Log.i("cam2", "set black pixels");
						
					}
		
					//here do this for low vision- show screen of where faces are in black/white
					//want all black except for white faces
					faceContrast = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
					faceContrast.eraseColor(Color.BLACK);
					//up to here works
					//MediaStore.Images.Media.insertImage(FrameFaces.this.getContentResolver(), faceContrast, "" + new Date().getTime(), "test image");
					canvas = new Canvas(faceContrast);
					contrastPaint = new Paint();
			 
					//have faces array, make into circles array
					circleFacesArray = new Circle[numFaces];
					for(int k=0; k<numFaces; k++)
					{
						//circle has center point and radius
						Log.i("framepoints", "making circle k: " + k);
						faces[k].getMidPoint(point);
						Log.i("framepoints", "midpoint x: " + point.x + "midpoint y: " + point.y );
						//confirmed we are getting the right midpoints
						circleFacesArray[k] = new Circle(point.x, point.y,(int)faces[k].eyesDistance());
					}
		
					//now find framing- want BACKGROUND IMAGE WITH FRAMING UP ON VIEW
					myTTS.speak("There are " + numFaces + " faces",  TextToSpeech.QUEUE_ADD, null);
					if(numFaces > 0)
					{
						findFraming(circleFacesArray, faceFramed);
						readyToDrawCircles = true;				
					}
					//now i want to show this image on a screen- UNTIL WHEN? need a new gesture
					//BitmapDrawable back = new BitmapDrawable(faceFramed);
					//view.setBackgroundDrawable(back);
					
					//BitmapDrawable back = new BitmapDrawable(faceContrast);
					//view.setBackgroundDrawable(back);
					//view.draw(canvas);
					
					if(circleFacesArray != null && readyToDrawCircles)
					{
						view.addVars(circleFacesArray, contrastPaint, faceContrast, mPreviewRunning, view.holder);
						view.draw(canvas);
						Log.v("blah", "just called view.draw");
						view.invalidate();
						Log.v("blah", "just called view.invalidate");
						readyToDrawCircles = false;
						MediaStore.Images.Media.insertImage(FrameFaces.this.getContentResolver(), faceContrast, "" + new Date().getTime(), "test image");
					}
					//sleep and show this image of black and white
					//i just want to get HERE!
					Log.v("blah", "about to sleep");
					SystemClock.sleep(8000);
					Log.v("blah","woke up from sleep");
		
					//view.
					//mPreviewRunning = false;
					//now you can have preview back up
					//**** put this next line back in
					//view.setBackgroundDrawable(null);
					//**change 9/9
					/*try {
						Log.v("blah","about to setpreviewdisplay after sleep");
						camera.setPreviewDisplay(holder); // throws IOException
						camera.setErrorCallback(new ErrorCallback() {
						public void onError(int code, Camera c) {
							if(code == Camera.CAMERA_ERROR_SERVER_DIED)
								Log.e("cam","The camera server died");
							else
								Log.e("cam","Unknown camera error");
						}
					});
					}
					catch(IOException ioe) {
					camera.release();
					camera = null;
					}*/
				  
					camera.startPreview();
					mPreviewRunning = true;
					//view.setPreviewRunning(true);
		
					MediaStore.Images.Media.insertImage(FrameFaces.this.getContentResolver(), faceFramed, "" + new Date().getTime(), "test image");
		
					//myTTS.speak("There are " + numFaces + " faces",  TextToSpeech.QUEUE_ADD, null);
					//((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(300);
					Log.i("cam", "Pixel height: " + h + " Pixel width: " + w );
					//circleFacesArray = null;
		
					String myPath = "/sdcard/DCIM/Camera/testJpg.jpg";
					Bitmap marker2 = Bitmap.createBitmap(image,0,0,w,h);
					try
					{
						File compressTry = new File(myPath);
						FileOutputStream outstream = new FileOutputStream(compressTry);
						marker2.compress(CompressFormat.JPEG,100,outstream);
					}
					catch (IOException ie)
					{
						Log.d("Exception","exception");
					}

					//JUST TO KEEP STILL ON SCREEN
					//camera.startPreview();
					//mPreviewRunning = true;
					
					//want to draw faces on here.
				} //end else?
			} //end if data !=null
		}//end onPictureTaken
	}; //end PictureCallback()

	//NOT USING THESE METHODS BELOW RIGHT NOW- ALL TOUCH METHODS
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		return false;
	}

	public void onLongPress(MotionEvent e)
	{
	}

	public boolean onDoubleTap(MotionEvent e)
	{
		myTTS.speak("double tap", 0, null);
		return true;
	}

	public boolean onSingleTap(MotionEvent e)
	{
		return false;
	}

	public boolean onSingleTapConfirmed()
	{
		myTTS.speak("single tap confirmed", 0, null);
		return true;
	}
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
	{ 
	return false; 
	} 

	public void onShowPress(MotionEvent e) 
	{ 
	}  

	public boolean onDown(MotionEvent e) 
	{ 
		return true;
	} 


	public boolean onTouch(MotionEvent e)
	{
		return true;
	}

	public boolean onSingleTapUp(MotionEvent e)     
	{ 
	return true; 
	} 
	//@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
		return false;

	if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {  //left
	objectFound = false;
	//Log.i("touch","swipe left");
	Toast.makeText(getApplicationContext(), "left", Toast.LENGTH_SHORT).show();
	//go back to picture mode
	} 
	else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {  //right
	//Log.i("touch","swipe left");
	Toast.makeText(getApplicationContext(), "right", Toast.LENGTH_SHORT).show();
	return false;
	}

	return true;
	}
} 



// ----------------------------------------------------------------------

//TODO
//1. Tap screen, save that frame at that point (preview or take picture), save on disk

class Circle{
	PointF center;
	int radius;
	
	Circle(float x,float y, int rad)
	{
		center = new PointF(x,y);
		radius = rad;
	}
}


