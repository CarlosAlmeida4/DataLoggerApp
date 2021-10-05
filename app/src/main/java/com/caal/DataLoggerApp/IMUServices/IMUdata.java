package com.caal.DataLoggerApp.IMUServices;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class IMUdata implements SensorEventListener {
    public static final String TAG = "IMUdata";

    private SensorManager sensorManager;
    // Sensor declaration
    private Sensor Accelerometer,Magnetic,GyroScope,AmbTemp,Humidity,Gravity,linearAccelerometer;
    /*
        Data structs to use on the sensor change
     */
    public float[] AccelerometerData = new  float[3];
    public float[] MagneticData = new  float[3];
    public float[] GyroscopeData = new  float[3];
    public float[] orientationAngles = new float[3];
    public float[] GravityData = new float[3];
    public float[] linearAccelData = new float[3];
    public float[] earthAccelData = new float[3];
    public float[] linearEarthAccelData = new float[3];

    public float AmbTempData;
    public float HumidityData;



    public void IMUdataInit(Context context){
        //Initialize everything related with sensors
        /*
            Initialize the sensor manager
         */
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

        /*
            Initialize the sensors
         */
        Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        GyroScope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        AmbTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        Humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        Gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        /*
            Add this class as the sensor event listeners
         */
        sensorManager.registerListener(this,Accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,Magnetic,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,GyroScope,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,AmbTemp,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,Humidity,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,Gravity,sensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,linearAccelerometer,sensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * Called when there is a new sensor event.  Note that "on changed"
     * is somewhat of a misnomer, as this will also be called if we have a
     * new reading from a sensor with the exact same sensor values (but a
     * newer timestamp).
     *
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     *
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values,0,AccelerometerData,0,event.values.length);
                earthAccelData = calculateEarthAccelData(AccelerometerData);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values,0,MagneticData,0,event.values.length);
                break;
            case Sensor.TYPE_GYROSCOPE:
                System.arraycopy(event.values,0,GyroscopeData,0,event.values.length);
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                AmbTempData = event.values[0];
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                HumidityData = event.values[0];
                break;
            case Sensor.TYPE_GRAVITY:
                System.arraycopy(event.values,0,GravityData,0,event.values.length);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values,0,linearAccelData,0,event.values.length);
                linearEarthAccelData = calculateEarthAccelData(linearAccelData);
                break;
            default:
                break;
        }

    }

    /**
     * Called when the accuracy of the registered sensor has changed.  Unlike
     * onSensorChanged(), this is only called when this accuracy value changes.
     *
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     *  Calculate the acceleration in the earth coordinates system
     */
    private float[] calculateEarthAccelData(float[]InputData){

        float[] R = new float[16], I = new float[16], earthAcc = new float[16];
        SensorManager.getRotationMatrix(R,I, GravityData, MagneticData);
        float[] relativacc = new float[4];
        float[] inv = new float[16];
        relativacc[0]=InputData[0];
        relativacc[1]=InputData[1];
        relativacc[2]=InputData[2];
        relativacc[3]=0;
        android.opengl.Matrix.invertM(inv, 0, R, 0);
        android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, relativacc, 0);
        //Log.d(TAG, "earthAccelData : X = " + earthAcc[0] + " Y  = " + earthAcc[1] + " Z = " + earthAcc[2]);
        //Log.d(TAG, "GravityData : X = " + GravityData[0] + " Y  = " + GravityData[1] + " Z = " + GravityData[2]);
        //Log.d(TAG, "MagneticData : X = " + MagneticData[0] + " Y  = " + MagneticData[1] + " Z = " + MagneticData[2]);
        //Log.d(TAG, "AccelerometerData : X = " + AccelerometerData[0] + " Y  = " + AccelerometerData[1] + " Z = " + AccelerometerData[2]);
        return earthAcc;
    }


}

