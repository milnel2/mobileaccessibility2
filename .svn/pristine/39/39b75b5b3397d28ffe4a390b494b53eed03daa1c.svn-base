package LogToFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Deflater;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FileLog {
	
	private static final String URL = "http://vbraille.cs.washington.edu/logentry.php";
		//"http://192.168.1.32:8888/vbraille/logentry.php";

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
	private static final String TAG = "FileLog";
	private static final int MAX_POSTS_BEFORE_SEND = 100;

	private boolean mLogToFileEnabled;  // set false if log file > 1MB or no writeable external storage is available
	private RandomAccessFile mRAF;
	private File mFile;
	private String mFilename;
	private boolean mExternalStorageAvailable = false;
	private boolean mExternalStorageWriteable = false;
	private boolean mFileTooBig = false;
	private Context mContext;
	private ConnectivityManager mConnMgr;
	private ArrayList<String> mPostMsg;
	private String mUser;
	private TelephonyManager mTeleMgr;
	private String mDeviceId = "unknown";
	
	// Thread for HttpPostManager
	private Thread mPoster;
	protected boolean mSentInterrupt = false;
	
	
	public FileLog(Context context, String fn, String user)  
	{
		mContext = context;
		mConnMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		mFilename = fn;
		mLogToFileEnabled = true;
		mPostMsg = new ArrayList<String>(MAX_POSTS_BEFORE_SEND + 1);
		mUser = user;
		mTeleMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		mDeviceId = mTeleMgr.getDeviceId();
		
		// HttpPost thread
		mPoster = new Thread(new HttpPostManager(this));
		mPoster.start();
	}
	
	public boolean isEnabled() 
	{
		return mLogToFileEnabled;
	}
	
	public String getReasonWhyDisabled()
	{
		String msg = "";
		if (!mExternalStorageAvailable)
			msg = "No external storage available.";
		else if (!mExternalStorageWriteable)
			msg = "Do not have permission to write to external storage.";
		else if (mFileTooBig) 
			msg = "Log file exceeds 1MB";
		return msg;
	}
	
	private void storageAvailable() 
	{	
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		    mLogToFileEnabled = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		    mLogToFileEnabled = false;
		}
	} // end storageAvailable()
	
	public boolean openLogFile()
	{
		boolean result = true;
		storageAvailable();
		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			mFile = new File(Environment.getExternalStorageDirectory(), mFilename);
			if (!mFile.exists()) {
				try {
					mFile.createNewFile();
				} catch (IOException e) {
					result = false;
				}		
			}
			
			if (result) {
					try {
						mRAF = new RandomAccessFile(mFile, "rwd");
					} catch (FileNotFoundException e) {
						result = false;
						closeLogFile();
					}
			}

			// If file size is greater than 1GB, disable logging
			long max = 1024L * 1024L * 1024L;
			long len = 0L;
			try {
				len = mRAF.length();
			} catch (IOException e) {
				result = false;
			}
			if (len > max) {
				mLogToFileEnabled = false;
				closeLogFile();
				mFileTooBig = true;
				result = false;
			}
		}
		else result = false;
		return result;
		
	} // end openLogFile()

	public void addEntry(String app, String session, int round, String code, String text) 
	{
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String msg = session + " " + round + " " + sdf.format(now) + " " + code + " " + text + " ";

		// move the actual posting action to a worker thread
		//if (mPostMsg.size() >= MAX_POSTS_BEFORE_SEND) {  // automatically post entries if max is reached
		//	postEntries();  // the method will clear the mPostMsg arraylist if post is successful
		//}
		
		// synchronize access to the mPostMsg arraylist since the app might be adding at the same
	    // time the worker thread is trying to clone/clear it (in postEntries()).
		synchronized(this) {
			mPostMsg.add(msg);
		}
		if (isEnabled()) {  // will be disabled if no writeable external storage or file > 1MB
			msg = mDeviceId + " " + app + " " + msg + "\n";
		    try {
				mRAF.seek(mFile.length());
				// encode using single bytes and keep it short
				mRAF.writeBytes(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.v(TAG, "addEntry");
	} // end addEntry()
	
	public void closeLogFile() 
	{
		if (isEnabled()) {
			try {
				mRAF.close();
			} catch (IOException e) {}
		}
	} // end closeLogFile()
	
	public void stopHttpPostThread() 
	{
		mSentInterrupt = true;
		mPoster.interrupt();
	} 
	
	public int postEntries() {
		Log.v(TAG, "postEntries");
		int returnval = 0;
		
		if (mConnMgr.getActiveNetworkInfo() == null || !mConnMgr.getActiveNetworkInfo().isConnected()) {
			// No active network -- to prevent huge posting if network connection is re-established, clear memory when number
			// of current, unsent postings exceeds 100
			if (mPostMsg.size() >= 100) synchronized(this) {
		    	mPostMsg.clear();
		    }
			returnval = 1;
		}
		
		// if connection is available
		else if (mPostMsg.size() > 0)
		{
		    // Create a new HttpClient and Post Header
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(URL);
		    String text = "";
		    ArrayList<String> postMessages;
		    // synchronize access to the mPostMsg arraylist since the app might be adding at the same
		    // time the worker thread is trying to clone/clear it.
		    synchronized(this) {
		    	postMessages = (ArrayList<String>) mPostMsg.clone();
		    	mPostMsg.clear();
		    }
		    for (int i = 0; i < postMessages.size(); i++) {
		    	text += postMessages.get(i);
		    	Log.v(TAG, postMessages.get(i));
		    }
		    // check to make sure the text was built.  this check is needed in the situation
		    // where the UI thread is in the onPause() method and calls LogFile's addEntry method
		    // just before the worker thread gets the preceding synchronized code (that is, in the
		    // case where the worker thread was just starting the postEntries() method when the UI 
		    // thread called interrupt on it.) In this scenario, the worker thread will catch that 
		    // final log entry and clear the arraylist before the UI thread gets to it. Without 
		    // this check, the UI thread wouldn't have any messages to send and would just send an empty log.
		    if (!text.equals("")) {
			    text = mUser + " " + mDeviceId + " " + text;
			    byte[] compressedText = compressString(text);
		
			    try {
			    	ByteArrayEntity entity = new ByteArrayEntity(compressedText);
			    	httppost.setEntity(entity);
			        Log.v(TAG, "httppost.setEntity");
		
			        // Execute HTTP Post Request
			        HttpResponse response = httpclient.execute(httppost);
			        String responseText = EntityUtils.toString(response.getEntity());	
			        Log.v(TAG, "Response: " + responseText); 
	
			    } catch (ClientProtocolException e) {
			        // TODO Auto-generated catch block
			    	returnval = 2;
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			    	returnval = 3;
			    }
		    }
		}
		else returnval = 4;
		return returnval;
	} 
		
	private byte[] compressString(String stringToCompress) 
	{ 
		Log.i(TAG, "Compressing String " + stringToCompress);
		byte[] input = stringToCompress.getBytes();
		
		// Create the compressor with highest level of compression 
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);
		
		// Give the compressor the data to compress 
		compressor.setInput(input); 
		compressor.finish();
		
		// Create an expandable byte array to hold the compressed data.
		// You cannot use an array that's the same size as the orginal because 
		// there is no guarantee that the compressed data will be smaller than 
		// the uncompressed data.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
		
		// Compress the data 
		byte[] buf = new byte[1024]; 
		while (!compressor.finished()) { 
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count); 
		} 
		
		try { 
			bos.close();
		} catch (IOException e) { } 
		
		// Get the compressed data 
		byte[] compressedData = bos.toByteArray(); 
		
		Log.i(TAG, "Finished to compress string " + stringToCompress);
		return compressedData;
	}
	
}
	

