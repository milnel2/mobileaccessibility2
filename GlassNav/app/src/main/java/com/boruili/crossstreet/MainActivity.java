package com.boruili.crossstreet;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is the main activity class that create the UI setting and getting location updating
 * It reads in accelerometer, compass and GPS location readings
 * It aslo beeps when the user is off to the initial direction
 */
public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    private View mView;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senMagnetic;
    private Sensor senGyro;
    private AudioManager audio;

    // Used for UI Cards
    private List<CardBuilder> cards;
    private CardScrollView mCardScroller;
    private CardScrollAdapter adapter;

    // Used for getting location
    private Criteria criteria;
    private LocationManager mLocationManager;
    private List<String> providers;
    private Location location;

    private File fileLocation;
    private File fileCompass;
    private File fileAcc;
    private File path;
    private FileOutputStream os;
    private int count;
    private double x_total = 0;
    private double z_total = 0;
    private double x_ave, z_ave;



    private final SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            String outString = "\""+currentDateTimeString+"\","+x+","+y+","+z+"\n";


            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                cards.get(0).setText("x = " + x + ", y =  " + y + ", z = " + z);
                try {
                    os = new FileOutputStream(fileAcc, true);
                    os.write(outString.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                x_total += x;
                z_total += z;
                count ++;
                System.out.println(count);
                // beep the glass for moving
                if(count == 30) {
                    x_ave = x_total/30;
                    z_ave = z_total/30;
                    audio.playSoundEffect(Sounds.TAP);
                    audio.playSoundEffect(Sounds.TAP);
                    audio.playSoundEffect(Sounds.TAP);

                }
                if(count > 30) {
                    if(x < x_ave - 4) {
                        audio.playSoundEffect(Sounds.TAP);
                        audio.playSoundEffect(Sounds.TAP);

                    } else if(x > x_ave + 4) {
                        audio.playSoundEffect(Sounds.ERROR);
                    }
                }
                cards.get(1).setText("x = " + x + ", y =  " + y + ", z = " + z);
                try {
                    os = new FileOutputStream(fileCompass, true);
                    os.write(outString.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) cards.get(3).setText("x = " + x + ", y =  " + y + ", z = " + z);


            adapter.notifyDataSetChanged();

        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("Location", "Location Changed");
            if(location != null) {

                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                double longitude = location.getLongitude();
                double latitude = location.getLatitude();

                String outString = "\""+currentDateTimeString+"\","+longitude+","+latitude+"\n";
                try {
                    os = new FileOutputStream(fileLocation, true);
                    os.write(outString.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                cards.get(2).setText("longitude = " + longitude + ", latitude =  " + latitude);

                adapter.notifyDataSetChanged();
                Log.d(TAG, "longitude = " + longitude + ", latitude =  " + latitude);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);


        createCards();
        mCardScroller = new CardScrollView(this);
        adapter = new CardScrollAdapter() {
            @Override
            public int getCount() {
                return cards.size();
            }

            @Override
            public Object getItem(int position) {
                return cards.get(position);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return cards.get(position).getView(convertView, parent);
            }

            @Override
            public int getPosition(Object item) {
                if (mView.equals(item)) {
                    return 0;
                }
                return AdapterView.INVALID_POSITION;
            }
        };

        mCardScroller.setAdapter(adapter);
        mCardScroller.activate();
        setContentView(mCardScroller);

        senSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senMagnetic = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        senGyro = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        senSensorManager.registerListener(mSensorListener, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(mSensorListener, senMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(mSensorListener, senGyro, SensorManager.SENSOR_DELAY_NORMAL);

        // Getting the current location
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        providers = mLocationManager.getProviders(criteria, true);

        // print providers
        Log.d(TAG, "" + providers.size());

        for (String provider : providers) {
            Log.d(TAG, provider);
        }

        mLocationManager.requestLocationUpdates(providers.get(1), 1000, 0, locationListener);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Log.d(TAG, providers.get(0));
        mLocationManager.requestLocationUpdates(providers.get(0), 1000, 0, locationListener);

        try {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                Log.d("GPS", "GPS");
            }
            if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                Log.d("NetWork", "Connected");
            }// no network provider is enabled
           } else {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    mLocationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                    Log.d("Network", "Network");
                    if (mLocationManager != null) {
                        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            Log.d(TAG, "" + location.getLatitude());
                            Log.d(TAG, "" + location.getLongtitude());
                        }
                    }
                }
                //get the location by gps
                if (isGPSEnabled) {
                    if (location == null) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 0, locationListener);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mLocationManager != null) {location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                            }
                        }
                    }
                }
           }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Plays disallowed sound to indicate that TAP actions are not supported.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.DISALLOWED);

            }
        });

        displayData();
        setContentView(mCardScroller);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
        senSensorManager.registerListener(mSensorListener, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(mSensorListener, senMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(mSensorListener, senGyro, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
        senSensorManager.unregisterListener(mSensorListener);
    }

    private void createCards() {
        cards = new ArrayList<>();

        cards.add(new CardBuilder(this, CardBuilder.Layout.TEXT).setFootnote("Accelerometer"));
        cards.add(new CardBuilder(this, CardBuilder.Layout.TEXT).setFootnote("Magnetic"));
        cards.add(new CardBuilder(this, CardBuilder.Layout.TEXT).setFootnote("Location"));
        cards.add(new CardBuilder(this, CardBuilder.Layout.TEXT).setFootnote("Gyro"));
    }

    private void displayData() {
        // print out data
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        java.util.Date date= new java.util.Date();

        fileLocation = new File(path, "Data_Location" + new Timestamp(date.getTime()) +".txt");
        fileAcc = new File(path, "Data_Accelerometer" + new Timestamp(date.getTime()) +".txt");
        fileCompass = new File(path, "Data_Compass" + new Timestamp(date.getTime()) +".txt");

        cards.get(2).setText(path.toString());
}
