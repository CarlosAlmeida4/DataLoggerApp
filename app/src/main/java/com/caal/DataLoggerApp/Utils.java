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


import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;
import android.widget.NumberPicker;

import java.text.DateFormat;
import java.util.Date;

public class Utils {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
    static private final int DATA_RATE_INDEX = 3;//FIXME: might not bee required
    public static final int FILE_MAX_LINES = 10000; //Max lines is the maximum number of lines per file written

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    static String[] fillDataRate(){
        String[] displayedValues = new String[DATA_RATE_INDEX];
        /* Datarates in milliseconds */
        displayedValues[0] = "10";
        displayedValues[1] = "100";
        displayedValues[2] = "1000";
        return displayedValues;
    }

    static void dataRateConstructor(String[] displayedValues,NumberPicker dataRate){
        dataRate.setMinValue(0);
        dataRate.setMaxValue(DATA_RATE_INDEX - 1);
        dataRate.setDisplayedValues(displayedValues);
    }
}
