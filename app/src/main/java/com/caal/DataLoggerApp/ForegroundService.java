/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.caal.DataLoggerApp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Message;
import android.util.Log;

import com.caal.DataLoggerApp.IMUServices.AccelStorage;
import com.caal.DataLoggerApp.IMUServices.FileWriteRunnableIMU;
import com.caal.DataLoggerApp.IMUServices.IMUTimerTask;
import com.caal.DataLoggerApp.LocationServices.FileWriteRunnableLocation;
import com.caal.DataLoggerApp.LocationServices.LocationStorage;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Timer;


/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification associated with that service is removed.
 */
public class ForegroundService extends Service {

    private static final String PACKAGE_NAME =
            "com.caal.DataLoggerApp";

    private static final String TAG = ForegroundService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";

    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;

    /**
     * For timing performance measurement
     */
    private long oldTime = System.nanoTime();

    /**
     * Location array storage
     */
    LocationStorage locationStorage = new LocationStorage();

    /**
     * File generator object declaration
     */
    FileGenerator fileGenerator = new FileGenerator();

    /**
     * Size of the location array
     */
    private int locationFileLine = 0;

    /**
     *  Handler thread variables to deal with the file writing
     */
    private HandlerThread fileWriterHandlerThread = new HandlerThread("LocationFileWriterHandlerThread");
    private Handler fileWriterHandler;


    /* Handler required for the IMUTimerTask */
    private Timer mIMUTimer;
    private Handler mIMUTimerHandler = new Handler();
    /* Timer task to handle the IMU cyclic action */
    private IMUTimerTask mIMUTimerTask;
    /* cyclic call to store IMU data, defaults to 1 second */
    private static int IMUTASK_PERIOD = 1000;

    static int LocationFileIterator = 0;


    public ForegroundService() {
    }

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                long interval = System.nanoTime()-oldTime;
                oldTime = System.nanoTime();
                Log.d(TAG,"Location TASK took : " +  interval/1000000 + "mS\n");
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        /**
         * Handler thread for the FileWriting stuff
         * Its initialized here since if looks like the other handler is also handled here
         */
        fileWriterHandlerThread.start();
        fileWriterHandler = new Handler(fileWriterHandlerThread.getLooper());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        if(!startedFromNotification){
            startIMUTimer();
        }
        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");

            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        savelastIMU2File();
        stopIMUTimer();

        mServiceHandler.removeCallbacksAndMessages(null);
        //Check if there are any location data that requires to be stored
        if(!locationStorage.isListEmpty()){
            //If list is not empty and we got here it means the user stopped the app but didnt shut
            //down the foreground service correctly
            saveLocation2File();
            //TODO: add a handler here to send a meesage to the IMU thread to finish writing the last datapoints
        }
        //Remove also the file write handler
        fileWriterHandler.removeCallbacksAndMessages(null);
        //kill the ThreadHandler
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            fileWriterHandlerThread.quitSafely();
        }
        else{
            fileWriterHandlerThread.quit();
        }
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates(int dataRateInput) {
        IMUTASK_PERIOD = dataRateInput;
        Log.i(TAG, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), ForegroundService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        savelastIMU2File();
        stopIMUTimer();

        Log.i(TAG, "Removing location updates");
        //store last location data
        saveLocation2File();
        //TODO: add a handler here to send a meesage to the IMU thread to finish writing the last datapoints
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, ForegroundService.class);

        CharSequence text = Utils.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        //TODO: removed so it doesnt appear in the log, allowing easier read of other messages
        //Log.i(TAG, "New location: " + location);

        mLocation = location;

        //Store location in the array
        locationFileLine++;
        locationStorage.addToList(location);

        //Save to the file
        if(locationFileLine == Utils.FILE_MAX_LINES){
            saveLocation2File();
            locationFileLine = 0;
        }

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ForegroundService getService() {
            return ForegroundService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Stores the location data to files
     */
    private void saveLocation2File(){
        /*copy the currently running loc storage data to a local variable */
        LocationStorage local_locationStorage = new LocationStorage(locationStorage);
        FileGenerator local_fileGenerator = fileGenerator;
        /* Start the thread with Handler.post*/
        fileWriterHandler.post(new FileWriteRunnableLocation(local_fileGenerator,
                "Test_Track_1000_ms", local_locationStorage,
                this, LocationFileIterator));
        /* Increment iterator */
        LocationFileIterator++;
        /* The data was passed to the Runnable, now we can clean the Storage lists */
        locationStorage.clearLocationStorage();

    }

    private void savelastIMU2File(){
        if(mIMUTimerTask != null){
            if(mIMUTimerTask.getAccelStorage()!=null){
                /*copy the currently running accel storage data to a local variable */
                AccelStorage local_accelStorage = new AccelStorage(mIMUTimerTask.getAccelStorage());
                FileGenerator local_fileGenerator = new FileGenerator();
                /* Start the thread with Handler.post*/
                fileWriterHandler.post(new FileWriteRunnableIMU(local_fileGenerator, "Test_Track_"+
                        IMUTASK_PERIOD + "_ms", local_accelStorage,
                        this, IMUTimerTask.getAccelFileIterator()));
            }
            if(mIMUTimerTask.getLinearAccelStorage()!=null){
                /*copy the currently running accel storage data to a local variable */
                AccelStorage local_accelStorage = new AccelStorage(mIMUTimerTask.getLinearAccelStorage());
                FileGenerator local_fileGenerator = new FileGenerator();
                /* Start the thread with Handler.post*/
                fileWriterHandler.post(new FileWriteRunnableIMU(local_fileGenerator, "Test_Track_Linear"+
                        IMUTASK_PERIOD + "_ms", local_accelStorage,
                        this, IMUTimerTask.getLinearAccelFileIterator()));
            }
            if(mIMUTimerTask.getEarthAccelStorage()!=null){
                /*copy the currently running accel storage data to a local variable */
                AccelStorage local_accelStorage = new AccelStorage(mIMUTimerTask.getEarthAccelStorage());
                FileGenerator local_fileGenerator = new FileGenerator();
                /* Start the thread with Handler.post*/
                fileWriterHandler.post(new FileWriteRunnableIMU(local_fileGenerator, "Test_Track_Earth"+
                        IMUTASK_PERIOD + "_ms", local_accelStorage,
                        this, IMUTimerTask.getEarthAccelFileIterator()));
            }
            if(mIMUTimerTask.getEarthLinearAccelStorage()!=null){
                /*copy the currently running accel storage data to a local variable */
                AccelStorage local_accelStorage = new AccelStorage(mIMUTimerTask.getEarthLinearAccelStorage());
                FileGenerator local_fileGenerator = new FileGenerator();
                /* Start the thread with Handler.post*/
                fileWriterHandler.post(new FileWriteRunnableIMU(local_fileGenerator, "Test_Track_LinearEarth"+
                        IMUTASK_PERIOD + "_ms", local_accelStorage,
                        this, IMUTimerTask.getEarthLinearAccelFileIterator()));
            }
        }
    }

    /**
     * Stops the IMUTimerTask timer
     */
    private void stopIMUTimer(){
        if(mIMUTimer != null){
            mIMUTimer.cancel();
            mIMUTimer.purge();
        }
    }

    /**
     * Starts the IMUTimerTask timer
     */
    private void startIMUTimer(){
        /* The IMUdata class requires access to the foreground context */
        mIMUTimerTask = new IMUTimerTask(mIMUTimerHandler, this, IMUTASK_PERIOD);
        /**
         * Initialize the TimerTask for the IMU services
         * https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
         */
        mIMUTimer = new Timer();
        mIMUTimer.schedule(mIMUTimerTask,1,IMUTASK_PERIOD);
    }
}
