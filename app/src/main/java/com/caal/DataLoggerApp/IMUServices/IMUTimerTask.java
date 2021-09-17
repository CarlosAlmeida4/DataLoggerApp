package com.caal.DataLoggerApp.IMUServices;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.caal.DataLoggerApp.FileGenerator;
import com.caal.DataLoggerApp.Utils;

import java.util.Objects;
import java.util.TimerTask;

public class IMUTimerTask extends TimerTask {
    private Handler mTimerHandler;

    public static final String TAG = "IMUTimerTask";
    public static final int EXAMPLE_TASK = 1;
    static long oldTime;

    /**
     * Size of the IMU array
     */
    private static int IMUFileLine = 0;
    /* NUmber of files created while app is running */
    static int AccelFileIterator = 0;

    /**
     *  Custom classes of this projects
     */
    private IMUdata SensorData;
    private MagStorage magStorage;
    private AccelStorage accelStorage;

    /* cyclic call to store IMU data, defaults to 1 second used in the filename*/
    private static int IMUTASK_PERIOD = 1000;

    /**
     *  Handler thread variables to deal with the file writing
     */
    private HandlerThread fileWriterHandlerThread = new HandlerThread("IMUFileWriterHandlerThread");
    private Handler fileWriterHandler;
    /**
     * Handler for the foreground service shutdown stage
     */
    public Handler ShutdownIMUStorageHandler;
    private boolean isShutdownStage = false;
    private boolean isLastFileWritten = false;
    /* Foreground context is required for file writing */
    Context mForegroundContext;

    public IMUTimerTask(Handler mTimerHandlerReceived, Context ForegroundContext, long Period){
        //Init project specific classes
        SensorData = new IMUdata();
        accelStorage = new AccelStorage();

        mTimerHandler = mTimerHandlerReceived;
        SensorData.IMUdataInit(ForegroundContext);
        mForegroundContext = ForegroundContext;

        IMUTASK_PERIOD = (int) Period;

        /*
         * Handler thread for the FileWriting stuff
         */
        fileWriterHandlerThread.start();
        fileWriterHandler = new Handler(fileWriterHandlerThread.getLooper());
    }

    @Override
    public void run() {
        /*this time handler runs at the time requested in the main activity*/
        mTimerHandler.post(new Runnable() {
            @Override
            public void run() {

                long interval = System.nanoTime()-oldTime;
                oldTime = System.nanoTime();
                Log.d(TAG,"IMU TASK took : " +  interval/1000000 + "mS\n");

                IMUFileLine++;
                String[] ret_val = saveAccel();
                String NotifMessage = " AccelX = " + ret_val[0] + " AccelY = " + ret_val[1] + " AccelZ= " + ret_val[2];
                ret_val = saveMag();
                NotifMessage = NotifMessage  + "\n MagX = " + ret_val[0] + " MagY = " + ret_val[1] + " MagZ= " + ret_val[2];
                Log.d(TAG,NotifMessage);

                //Save to the file and check if its not writing while the shutdown is also writing a file
                if(IMUFileLine == Utils.FILE_MAX_LINES && !isShutdownStage){
                    saveAccel2File();
                    IMUFileLine = 0;
                }
            }
        });

    }

    /**
     * Takes care of the accelerometer data gathering
     *
     * @return 3 strings with the value of X Y and Z accel data
     */
    private String[] saveAccel() {
        String[] ret_val = new String[3];
        ret_val[0] = String.valueOf(SensorData.AccelerometerData[0]);
        ret_val[1] = String.valueOf(SensorData.AccelerometerData[1]);
        ret_val[2] = String.valueOf(SensorData.AccelerometerData[2]);
        Float accelx = new Float(SensorData.AccelerometerData[0]);
        Float accely = new Float(SensorData.AccelerometerData[1]);
        Float accelz = new Float(SensorData.AccelerometerData[2]);
        accelStorage.addToList(accelx, accely, accelz);
        return ret_val;
    }

    /**
     *
     */
    private String[] saveMag(){
        String[] ret_val = new String[3];
        ret_val[0] = String.valueOf(SensorData.MagneticData[0]);
        ret_val[1] = String.valueOf(SensorData.MagneticData[1]);
        ret_val[2] = String.valueOf(SensorData.MagneticData[2]);
        return ret_val;
    }

     private void saveAccel2File(){
        if(accelStorage!=null){
            /*copy the currently running accel storage data to a local variable */
            AccelStorage local_accelStorage = new AccelStorage(accelStorage);

            FileGenerator local_fileGenerator = new FileGenerator();
            /* Start the thread with Handler.post*/
            fileWriterHandler.post(new FileWriteRunnableIMU(local_fileGenerator, "Test_Track_"+
                    IMUTASK_PERIOD + "_ms", local_accelStorage,
                    mForegroundContext, AccelFileIterator));
            /* Increment iterator */
            AccelFileIterator++;
            /* The data was passed to the Runnable, now we can clean the Storage lists */
            accelStorage.clearAccelStorage();
        }
    }

    public AccelStorage getAccelStorage(){
            if(accelStorage == null){
                return null;
            }
            else{
                return accelStorage;
            }


    }

    public static int getAccelFileIterator(){
        return AccelFileIterator;
    }
}
