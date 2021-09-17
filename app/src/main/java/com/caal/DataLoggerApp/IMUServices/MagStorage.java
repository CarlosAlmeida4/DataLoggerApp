package com.caal.DataLoggerApp.IMUServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MagStorage {


    List<Float> MagX;
    List<Float> MagY;
    List<Float> MagZ;
    List<String>  WriteString;
    private long MagIndex;


    /*Constructor to create from another object
     In java, when you do object A = object B you are not copying Object B into
     object A, in fact, you're passing by ref
     To achieve a an actual copy a constructor can be used
     */
    public MagStorage(){
        /* For the object that will be used to store the real accel data */
        /* Just do inits here */
        this.MagX = new ArrayList<Float>();
        this.MagY = new ArrayList<Float>();
        this.MagZ = new ArrayList<Float>();
        this.WriteString = new ArrayList<String>();
        this.MagIndex = 0;
    }
    /* For the local copy */
    public MagStorage(MagStorage MgSt) {
        this.MagX = new ArrayList<Float>(MgSt.MagX);
        this.MagY = new ArrayList<Float>(MgSt.MagY);
        this.MagZ = new ArrayList<Float>(MgSt.MagZ);
        this.WriteString = new ArrayList<String>(MgSt.WriteString);
        this.MagIndex = MgSt.MagIndex;
    }

    public void clearMagStorage(){
        MagX.clear();
        MagY.clear();
        MagZ.clear();
        WriteString.clear();
    }

    public boolean addToList(Float magx,Float magy,Float magz){

        MagX.add(magx);
        MagY.add(magy);
        MagZ.add(magz);
        addToString(magx, magy,magz);
        return true;
    }

    private void addToString(Float magx,Float magy,Float magz){
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String toAdd = "<trkpt X=\"" + magx + "\" Y=\"" + magy + "\" Z=\"" + magz + "\" Index = \"" + MagIndex + "\"></trkpt>\n";
        WriteString.add(toAdd);
        MagIndex++;
    }

}

