package cs.washington.mobileaccessibility.portrait;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import cs.washington.mobileaccessibility.portrait.Circle;


public class GraphicsView extends SurfaceView implements SurfaceHolder.Callback{
		
		
		Circle [] ca;
		Bitmap bmp;
		static SurfaceHolder holder;
		static Camera camera;
		
		
		public GraphicsView(Context context)
		{
			super(context);
			this.setFocusable(true);
			this.setFocusableInTouchMode(true);
			holder = this.getHolder();
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);	
	    }
		
		public GraphicsView(Context context, AttributeSet attr)
		{
			super(context,attr);
			this.setFocusable(true);
			this.setFocusableInTouchMode(true);
			holder = this.getHolder();
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		
		public GraphicsView(Context context, AttributeSet attr, int defStyle)
		{
			super(context,attr,defStyle);
			this.setFocusable(true);
			this.setFocusableInTouchMode(true);
			holder = this.getHolder();
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	
		public void invalidate()
		{
			super.invalidate();
		}
		
	public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
		 try {
	            // This case can actually happen if the user opens and closes the camera too frequently.
	            // The problem is that we cannot really prevent this from happening as the user can easily
	            // get into a chain of activites and tries to escape using the back button.
	            // The most sensible solution would be to quit the entire EPostcard flow once the picture is sent.
	            if(camera == null)
	            	camera = Camera.open();
	            Log.v("blah", "at least opened camera");
	            camera.setPreviewDisplay(holder);
	            Log.v("blah", "camera setpreviewDisplay in GraphicsView surfaceCreated");
	            Log.v("blah", "camera opened in GraphicsView surfaceCreated");
	        } catch(Exception e) {
	            //finish();
	        	  Log.v("blah", "camera EXCEPTION not opened in GraphicsView surfaceCreated");
	        	  if(camera !=null)
	        	  {
	        	  camera.release();
	        	  camera = null;
	        	  }
	        	  else
	        		  camera = camera.open();
	            //return;
	        }
	        return;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
    	Log.v("blah", "calling SURFACE DESTROYED");
        if(camera!=null && FrameFaces.mPreviewRunning)
    	{
        	Log.v("blah", "about to stop preview in destroyed");
        	camera.stopPreview();
        	camera.release();
        	FrameFaces.mPreviewRunning = false;
    	}
        else if(camera!=null)
        {
        	Log.v("blah","just release camera in destroyed");
        	camera.release();
        }
       
       
        //** 9/9
        //camera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
    	Log.v("blah", "in surfaceChanged!!");
    	if(camera == null)
    	{
    		camera = Camera.open();
    	}
    	
        if(camera !=null)
        {
        	Camera.Parameters parameters = camera.getParameters();
        
        	// this is for DROID- switch back
        	//parameters.setPreviewSize(1280, 720);
        	//parameters.setPreviewSize(480,320); //this one is for Nexus One
        	/*List<Size> myList = parameters.getSupportedPreviewSizes();
        	for(int i=0; i<myList.size();i++)
        	{
        		int width = myList.get(i).width;
        		int height = myList.get(i).height;
        		Log.v("sizePreview","w x h = " + width + " x " + height);
        	}*/
        	
        	parameters.setPreviewSize(640,480);
        	//640 by 480 OR 800x480
        	camera.setParameters(parameters);
        	// **changed 9/9
        	//parameters.setPreviewSize(w, h);
        	//camera.setParameters(parameters);
        	camera.startPreview();
        	FrameFaces.mPreviewRunning = true;
        }
    }
		
    	//might take this out.
    	protected void addVars(Circle[] circleArr, Paint paint, Bitmap bmp, boolean bool, SurfaceHolder holder)
    	{
    		ca = circleArr;
    		//p = paint;
    		bmp = bmp;
    		//mPreview = bool;
    		//FrameFaces.holder = holder;
    	}
		
		protected void onDraw(Canvas canvas)
		{
			//not sure i need this next line
			super.onDraw(canvas);
			//have circle array- now paint onto canvas
			Log.v("blah", "in ONDRAW!!");
			FrameFaces.contrastPaint.setColor(Color.WHITE);
			Log.v("blah","in onDraw- about to draw faces");
			Log.v("blah","circleFacesArrayLength: " + ca.length);
			Matrix mx = new Matrix();
			mx.reset();
				
			canvas.drawBitmap(bmp, mx, FrameFaces.contrastPaint);
				
				for(int x=0; x< ca.length; x++)
				{
					
					//for each face, paint these pixels
					//find center, and then make circle with radius eyesDistance
					PointF centerCircle = ca[x].center;
					int radiusCircle = ca[x].radius;
					//now draw white circle
					canvas.drawCircle(centerCircle.x, centerCircle.y, radiusCircle, FrameFaces.contrastPaint);
				}
			
			//readyToDrawCircles = false;
			Log.v("blah", "leaving ONDRAW!!");
			//put faceContract in gallery to check
			
		
		}//end onDraw
	
} //end graphicsview class

