package com.caal.DataLoggerApp.IMUServices;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class IMUdata implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor Accelerometer,Magnetic,GyroScope,AmbTemp,Humidity;// Sensor declarataion
    /*
        Data structs to use on the sensor change
     */
    public float[] AccelerometerData = new  float[3];
    public float[] MagneticData = new  float[3];
    public float[] GyroscopeData = new  float[3];
    public float[] orientationAngles = new float[3];
    public float AmbTempData;
    public float HumidityData;
    /*
        Rotation Matrix
     */
    private float[] rotationMatrix = new float[9];

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

        /*
            Add this class as the sensor event listeners
         */
        sensorManager.registerListener(this,Accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,Magnetic,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,GyroScope,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,AmbTemp,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,Humidity,SensorManager.SENSOR_DELAY_FASTEST);
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
            default :
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
     *  Update the orientation angles
     */
    private double updateOrientationAngles(){

        SensorManager.getRotationMatrix(rotationMatrix,null, AccelerometerData, MagneticData);
        float[] orientation = SensorManager.getOrientation(rotationMatrix,orientationAngles);
        double degrees = (Math.toDegrees(orientation[0])+360) % 360.0;
        double angle = Math.round(degrees * 100)/100;

        //getDirection(angle);
        return angle;
    }


}

