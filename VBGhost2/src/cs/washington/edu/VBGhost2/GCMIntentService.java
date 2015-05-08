package cs.washington.edu.VBGhost2;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService{
	private Toast toast;
	Handler mMainThreadHandler = null;
	
    public GCMIntentService() {
        super();
        mMainThreadHandler = new Handler();
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.d("GCM", "RECIEVED A MESSAGE");
        Log.d("GCM", intent.getStringExtra("message"));
        // Get the data from intent and send to notificaion bar
       // generateNotification(context, intent.getStringExtra("message"));
        
        displayMessage(context, intent.getStringExtra("message"));
    }
    

    public void generateNotification(Context arg2, String message){
    	//new AlertDialog.Builder(this).setTitle("New Message").setMessage(message).setPositiveButton("OK", null).show();
    	//displayToast(message);
    	//Notification notification = new Notification();
    	Intent notificationIntent = new Intent(this, LoadingAct.class);
    	generateNotification2(arg2, message, "VBGhost", notificationIntent);
    }
    
    public void displayMessage(Context arg2, String message){
    	Intent intent = new Intent(this, ReceiveMessage.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.putExtra("message", message);
    	startActivity(intent);
    }
    
    public static void generateNotification2(Context context, String msg, String title, Intent intent) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, title, when);
        notification.setLatestEventInfo(context, title, msg,
                PendingIntent.getActivity(context, 0, intent, 0));
        
        notification.flags|= notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS;
        notification.vibrate=new long[] {100L, 100L, 200L, 500L};
        //notification.flags |= Notification.FLAG_AUTO_CANCEL;

    //    SharedPreferences settings = Prefs.get(context);
        int notificatonID = 1;
        NotificationManager nm =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificatonID, notification);
      //  notification.defaults |= Notification.DEFAULT_SOUND;
        Utils.notificationReceived=true;
        Utils.notiMessage = msg;
        Utils.notiTitle = "Ghost";
       // PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
       // WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
       // wl.acquire(1000);

    }
    
	public void displayToast(String msg) {
		toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.show();
	}

    @Override
    protected void onError(Context context, String errorId) {
    	Log.v("GCM", "onError: " + errorId);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.d("RegID: ", registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
    }
}