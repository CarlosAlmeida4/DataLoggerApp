package com.caal.DataLoggerApp.LocationServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.util.Log;

import com.caal.DataLoggerApp.FileGenerator;

import java.io.IOException;

public class FileWriteRunnableLocation  implements Runnable{
    public static final String TAG = "FileWriteRunnableLocation";

    FileGenerator local_fileGenerator = new FileGenerator();
    LocationStorage local_locationStorage = new LocationStorage();
    String local_trackname;
    BroadcastReceiver.PendingResult local_pendingResult;
    Context local_context;
    int localiterator = 0;

    /* This runnable class constructors are the same as the FileGenerator storeTrack methods*/


    /**
     * Stubbed, but to be used with location
     *  Constructor for accelerometer file writer Runnable
     * @param fileGenerator - File generator object
     * @param trackName - track name
     * @param locationStorage - accelStorage Object with data to be written
     * @param context - Context, required to write to a file
     * @param iterator - file iterator, To number the created files
     */
    public FileWriteRunnableLocation(FileGenerator fileGenerator, String trackName,
                                     LocationStorage locationStorage, Context context, int iterator)
    {
        local_fileGenerator = fileGenerator;
        local_locationStorage = locationStorage;
        local_trackname = trackName;
        local_context = context;
        localiterator = iterator;
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        Log.d(TAG, "Writing file number " + localiterator);
        try {
            local_fileGenerator.storeTrack(local_trackname, local_context, localiterator , local_locationStorage.mLocationList,true);
            Log.d(TAG, "-------------------------------------------I finished Writing the file number: " + localiterator);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"Error Writing file");
        }
    }
}