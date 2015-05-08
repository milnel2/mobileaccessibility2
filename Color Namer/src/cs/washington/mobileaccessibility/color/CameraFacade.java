package cs.washington.mobileaccessibility.color;

import java.io.IOException;

import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.util.Log;
import android.view.SurfaceHolder;

/*
 * This class encapsulates all the stuff that involves the camera,
 * the canvas/surfaceholder/magic stuff that I do not understand.
 * 
 * I wrote most of the stuff here, and I have no idea what is going on.
 * I'm sure that I started out with some sample code from the Android website,
 * and then added a bunch of patches.  Not it seems to work.  Go figure.
 * 
 * Maybe someone should draw a state diagram, to figure out all the possible
 * states we can be in.
 * 
 * This class creates and destroys the camera, and also tells the camera where
 * to put its previews.
 * 
 * This class gets several types of messages
 *  - it's the SurfaceHolder.Callback, so it gets messages when the Surface
 *  (whatever that is) is created, changed, or destroyed
 *  - it also gets notified when the activity resumes or pauses, since those
 *  are good times to turn the camera on and off.  Very good times, indeed.
 *  
 */
public class CameraFacade implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder; // we get this from the SurfaceView, and hardly ever use it
	private Camera mCamera; // here it is!
	private int mx;
	private int my;
	// this is true between SurfaceCreated and SurfaceDestroyed
	private boolean surfaceExists;
	// this next parameter is true between a call of mCamera.startPreview() and mCamera.stopPreview
	private boolean mPreviewRunning;

	public CameraFacade(SurfaceHolder holder) {
		mHolder = holder;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
		mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mCamera = null;
		mx = my = 0;
		surfaceExists = mPreviewRunning = false;
	}




	// this gets called onResume.  Originally, it was called only if the
	// data had been loaded completely, which I do not one hundred percent
	// understand.  So now it is called.
	public void onResume() {
    	if(surfaceExists && mCamera == null) {
    		startCamera();
    		// you would think that setCameraParameters() should wait for surfaceChanged
    		// to be called, but what the hell this works apparently
    		setCameraParameters();
    	}
	}
	
	public void onPause() {
    	if(mCamera != null) {
    		if(mPreviewRunning) {
    			mCamera.stopPreview();
    			mPreviewRunning = false;
    		}
    		
    		mCamera.release();
    		mCamera = null;
    	}
	}

	public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
    	Log.i("ColorNamer","Surface Created");
    	if(mHolder == null)
    		mHolder = holder;
    	startCamera();
    	surfaceExists = true;
		
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
    	Log.i("ColorNamer","call to surfaceChanged()");
    	if(mCamera == null)
    		return;
    	mx = width;
    	my = height;
    	setCameraParameters();
    	

	}
	
    private void startCamera() {
    	if(mCamera == null)
    		mCamera = Camera.open();
    	try {
        	mCamera.setPreviewDisplay(mHolder); // throws IOException
        	mCamera.setErrorCallback(new ErrorCallback() {
        		public void onError(int code, Camera c) {
        			if(code == Camera.CAMERA_ERROR_SERVER_DIED)
        				Log.e("ColorNamer","The camera server died");
        			else
        				Log.e("ColorNamer","Unknown camera error");
        		}
        	});
        }
        catch(IOException ioe) {
        	mCamera.release();
        	mCamera = null;
        }
        mPreviewRunning = false;
    }
	
    private void setCameraParameters() {
    	Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mx, my);
        parameters.remove("whitebalance"); // theoretically, this might do something helpful
        mCamera.setParameters(parameters);
        if(!mPreviewRunning)
        	mCamera.startPreview();
        mPreviewRunning = true;
    }

	public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
    	//
    	// (Too bad that the CameraDevice object ceased to exist,
    	// several versions of Android ago)
		//
		// (Presumably I am quoting some sample code above)
    	

    	surfaceExists = false;
    	Log.i("ColorNamer","Surface destroyed! mPreviewRunning = " + mPreviewRunning);
    	if(mCamera == null) {
    		mPreviewRunning = false; // it probably should've been already anyways
    		return;
    	}
    	if(mPreviewRunning) {
    		mCamera.stopPreview();
    		mPreviewRunning = false;
    	}
    	Log.i("ColorNamer","We've called stopPreview() (perhaps), but not yet released the camera");
        mCamera.release();
        mCamera = null;
        mHolder = null;
	}
	
	
    // The user tapped the screen, so start requesting previews
    // from the camera
	// OR, we're turning OFF the preview mechanism
    public void getPreview(Camera.PreviewCallback callback) {
    	if(mCamera == null)
    		return;
    	mCamera.setPreviewCallback(callback);
    	Log.i("ColorNamer",callback==null?"Stopping previews":"Starting to request preview frames");
    }
	
	
}
