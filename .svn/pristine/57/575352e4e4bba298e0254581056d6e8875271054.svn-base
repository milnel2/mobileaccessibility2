package cs.washington.mobileaccessibility.camera;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.tts.TTS;

import java.io.IOException;
import java.util.Date;

// ----------------------------------------------------------------------

public class SimpleCamera extends Activity { 
	private static final String TAG = "SimpleCamera";
	
    private Preview mPreview;    
    private TTS myTts;
    private OpenCV opencv = new OpenCV();
    
    /**
     * This listens for the click
     */
    private View.OnClickListener mClickListener = new View.OnClickListener() {

		//@Override
		public void onClick(View v) {
			if(v.equals(mPreview)) {
				mPreview.takePicture(null, null, mJpeg);
			}
		}
    	
    };
    
    /**
     * This is the callback for when the jpeg image is ready.
     * You can also make one as a callback for when the raw
     * image is ready.  If there is not enough memory for the
     * raw image though the byte[] will be null.
     */
    private Camera.PictureCallback mJpeg = new Camera.PictureCallback() {

		//@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 8;
	
			Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length, options);
			int w = image.getWidth();
			int h = image.getHeight();
			int[] pixels = new int[w*h];
			image.getPixels(pixels,0,w,0,0,w,h);

			byte[] picData = opencv.findContours(pixels,0,data.length);
			Bitmap newPic = BitmapFactory.decodeByteArray(picData,0,picData.length);
			
MediaStore.Images.Media.insertImage(SimpleCamera.this.getContentResolver(), newPic, "" + new Date().getTime(), "test image");
			

			// TODO: whatever you want to do with the image
			
			Log.d(TAG, "i got the image");
image.recycle();
		}
    	
    };

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title and status bar.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN); 


        // Create our Preview view and set it as the content of our activity.
        mPreview = new Preview(this);        
        setContentView(mPreview);
        
		
        mPreview.setOnClickListener(mClickListener);
		myTts = new TTS(this, ttsInitListener, true);

    }

	private TTS.InitListener ttsInitListener = new TTS.InitListener() {
		public void onInit(int version) {
		myTts.speak("Tap the screen to take a picture", 0, null);
		}
	};


}

// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;

    Preview(Context context) {
        super(context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open();
        try {
           mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
            // TODO: add more exception handling logic here
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(w, h);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }
    
    /**
     * A wrapper around the camera. The parameters are the same as Camera.takePicture()
     * @param shutter
     * @param raw
     * @param jpeg
     */
    public void takePicture(Camera.ShutterCallback shutter, Camera.PictureCallback raw, Camera.PictureCallback jpeg) {
    	mCamera.stopPreview();
		mCamera.takePicture(shutter, raw, jpeg);
		mCamera.startPreview();
    }
}