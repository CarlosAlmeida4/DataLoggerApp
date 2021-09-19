package com.caal.DataLoggerApp.IMUServices;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class AccelStorage {


    List<Float> AccelX;
    List<Float> AccelY;
    List<Float> AccelZ;
    List<String>  WriteString;
    private long AccelIndex;


    /*Constructor to create from another object
     In java, when you do object A = object B you are not copying Object B into
     object A, in fact, you're passing by ref
     To achieve a an actual copy a constructor can be used
     */
    public AccelStorage(){
        /* For the object that will be used to store the real accel data */
        /* Just do inits here */
        this.AccelX = new ArrayList<Float>();
        this.AccelY = new ArrayList<Float>();
        this.AccelZ = new ArrayList<Float>();
        this.WriteString = new ArrayList<String>();
        this.AccelIndex = 0;
    }
    /* For the local copy */
    public AccelStorage(AccelStorage AcSt) {
        this.AccelX = new ArrayList<Float>(AcSt.AccelX);
        this.AccelY = new ArrayList<Float>(AcSt.AccelY);
        this.AccelZ = new ArrayList<Float>(AcSt.AccelZ);
        this.WriteString = new ArrayList<String>(AcSt.WriteString);
        this.AccelIndex = AcSt.AccelIndex;
    }

    public void clearAccelStorage(){
        AccelX.clear();
        AccelY.clear();
        AccelZ.clear();
        WriteString.clear();
    }

    public boolean addToList(Float accelx,Float accely,Float accelz,float deltaInMiliSeconds){
        /*if(LocationList.size() == 10){
            return false;
        }*/
        AccelX.add(accelx);
        AccelY.add(accely);
        AccelZ.add(accelz);
        addToString(accelx, accely,accelz,deltaInMiliSeconds);
        return true;
    }

    private void addToString(Float accelx,Float accely,Float accelz,float deltaInMiliSeconds){
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String toAdd = "<trkpt X=\"" + accelx + "\" Y=\"" + accely + "\" Z=\"" + accelz + "\" delta = \""+ deltaInMiliSeconds +"\" Index = \"" + AccelIndex + "\"></trkpt>\n";
        WriteString.add(toAdd);
        AccelIndex++;
    }

}
