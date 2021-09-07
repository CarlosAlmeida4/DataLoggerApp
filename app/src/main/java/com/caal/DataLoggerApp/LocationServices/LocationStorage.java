package com.caal.DataLoggerApp.LocationServices;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationStorage {

    /**
     * List of location data
     */
    public List<Location> mLocationList;

    public LocationStorage()
    {
        mLocationList = new ArrayList<Location>();
    }

    public LocationStorage(LocationStorage LocSt)
    {
        mLocationList = new ArrayList<Location>(LocSt.mLocationList);
    }

    public void clearLocationStorage(){
        mLocationList.clear();
    }

    /**
     *  Add location object to the location list
     * @param location - location object
     * @return add to list result
     */
    public boolean addToList(Location location){
        /*if(LocationList.size() == 10){
            return false;
        }*/
        boolean ret_val = mLocationList.add(location);
        if(ret_val==true){
            return true;
        }
        else{
            return false;
        }

    }

    /**
     *  Checks if the list is empy
     * @return True if the list is empty
     */
    public boolean isListEmpty(){
        return mLocationList.isEmpty();
    }

}
