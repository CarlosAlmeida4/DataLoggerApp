package com.caal.DataLoggerApp;


import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileGenerator {


    public boolean createZipfile(String folderName, Context context) throws IOException {

        //Get date and append to filename
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);

        //Set filepath
        String filepath =  folderName;
        String zipName = folderName + ".zip";

        StringBuilder sb = new StringBuilder();
        sb.append("Test String");


        File f = new File(context.getExternalFilesDir(filepath),zipName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        ZipEntry e = new ZipEntry("mytext.txt");
        out.putNextEntry(e);
        byte[] data = sb.toString().getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();

        out.close();
        return true;
    }
    /**
     * @param trackName - name given to the track, if iteration is desired, trackName should always be the same
     * @param context   - current context
     * @param wString   - String for the Accel data, the string must be made in the class responsible by maintaining the data
     * @return True if all parameters are correctly accepted, false if not
     * @throws IOException - in case of a failure when writing a file
     */
    public boolean storeTrack(String trackName, Context context, List<String> wString) throws IOException {

        boolean ret_val = true;
        String filepath;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String HoursMins = dateFormat.format(date);
        File trackFile;

        String FILE_NAME = "Track_";
        //Get date and append to filename
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        FILE_NAME = FILE_NAME + formattedDate.toString() + "_" + HoursMins.toString() + "_" + trackName;

        /* Create an APX file */
        /* Since this method is overloaded GPX files should not end up here*/
        Log.d("storeTrack", "Data format : " + formattedDate.toString());
        //Set filepath
        filepath =  formattedDate.toString();
        //Append filetype to the name
        FILE_NAME = FILE_NAME + ".Apx";
        trackFile = new File(context.getExternalFilesDir(filepath), FILE_NAME);
        /* Call generation function */
        ret_val = generateApx(trackFile, trackName, wString);

        return ret_val;
    }

    /**
     * @param trackName - name given to the track, if iteration is desired, trackName should always be the same
     * @param context   - current context
     * @param iterator  - If file is iterative give here the iteration
     * @param wString   - String for the Accel data, the string must be made in the class responsible by maintaining the data
     * @return True if all parameters are correctly accepted, false if not
     * @throws IOException - in case of a failure when writing a file
     */
    public boolean storeTrack(String trackName, Context context, int iterator, List<String> wString) throws IOException {

        boolean ret_val = true;
        String filepath;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String HoursMins = dateFormat.format(date);
        File trackFile;

        String FILE_NAME = "Track_";
        //Get date and append to filename
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        FILE_NAME = FILE_NAME + formattedDate.toString() + "_" + HoursMins.toString() + "_" + trackName + "_";

        /* iteration is used, the trackname must contain the iterator */
        FILE_NAME = FILE_NAME + "i" + Integer.toString(iterator);


        /* Create an APX file */
        /* Since this method is overloaded GPX files should not end up here*/
        Log.d("storeTrack", "Data format : " + formattedDate.toString());
        //Set filepath
        filepath =  formattedDate.toString();
        //Append filetype to the name
        FILE_NAME = FILE_NAME + ".Apx";
        trackFile = new File(context.getExternalFilesDir(filepath), FILE_NAME);
        /* Call generation function */
        ret_val = generateApx(trackFile, trackName, wString);

        return ret_val;
    }

    /**
     * @param trackName   - name given to the track, if iteration is desired, trackName should always be the same
     * @param context     - current context
     * @param iterator    - If file is iterative give here the iteration
     * @param points      - Location points from the gps data
     * @param junk        - Junk due to overloading issue, can be anything
     * @return True if all parameters are correctly accepted, false if not
     * @throws IOException - in case of a failure when writing a file
     */
    public boolean storeTrack(String trackName, Context context, int iterator, List<Location> points, boolean junk) throws IOException {

        boolean ret_val = true;
        String filepath;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String HoursMins = dateFormat.format(date);
        File trackFile;

        String FILE_NAME = "Track_";
        //Get date and append to filename
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        FILE_NAME = FILE_NAME + formattedDate.toString() + "_" + HoursMins.toString() + "_" + trackName + "_";

        /* iteration is used, the trackname must contain the iterator */
        FILE_NAME = FILE_NAME + "i" + Integer.toString(iterator);


        /* Create an APX file */
        /* Since this method is overloaded GPX files should not end up here*/
        Log.d("storeTrack", "Data format : " + formattedDate.toString());
        //Set filepath
        filepath = formattedDate.toString();
        //Append filetype to the name
        FILE_NAME = FILE_NAME + ".Gpx";
        trackFile = new File(context.getExternalFilesDir(filepath), FILE_NAME);
        /* Call generation function */
        ret_val = generateGpx(trackFile, trackName, points);

        return ret_val;
    }

    /**
     * @param trackName   - name given to the track, if iteration is desired, trackName should always be the same
     * @param context     - current context
     * @param points      - Location points from the gps data
     * @param junk        - Junk due to overloading issue, can be anything
     * @return True if all parameters are correctly accepted, false if not
     * @throws IOException - in case of a failure when writing a file
     */
    public boolean storeTrack(String trackName, Context context, List<Location> points, boolean junk) throws IOException {

        boolean ret_val = true;
        String filepath;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String HoursMins = dateFormat.format(date);
        File trackFile;

        String FILE_NAME = "Track_";
        //Get date and append to filename
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        FILE_NAME = FILE_NAME + formattedDate.toString() + "_" + HoursMins.toString() + "_" + trackName + "_";

        /* Create an APX file */
        /* Since this method is overloaded GPX files should not end up here*/
        Log.d("storeTrack", "Data format : " + formattedDate.toString());
        //Set filepath
        filepath =  formattedDate.toString();
        //Append filetype to the name
        FILE_NAME = FILE_NAME + ".Gpx";
        trackFile = new File(context.getExternalFilesDir(filepath), FILE_NAME);
        /* Call generation function */
        ret_val = generateGpx(trackFile, trackName, points);

        return ret_val;
    }

    /**
     * @param file    - file that will be created
     * @param name    - name that will be in the file header
     * @param wString - String Lists required to create file
     */
    private static boolean generateApx(File file, String name, List<String> wString) {

        boolean ret_val = false;
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><Apx xmlns=\"MadeUpByCarlos\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"YeahNope\"><trk>\n";
        name = "<name>" + name + "</name><trkseg>\n";

        String segments = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        for (String Stringy : wString) {
            segments += Stringy;
        }

        String footer = "</trkseg></trk></Apx>";

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.append(header);
            writer.append(name);
            writer.append(segments);
            writer.append(footer);
            writer.flush();
            writer.close();
            ret_val = true;
        } catch (IOException e) {
            Log.e("generateAfx", "Error Writting Path", e);
            ret_val = false;
        }

        return ret_val;
    }

    /**
     * @param file   - file that will be created
     * @param name   - name that will be in the file header
     * @param points - List of location points
     */
    private static boolean generateGpx(File file, String name, List<Location> points) {

        boolean ret_val = false;
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        name = "<name>" + name + "</name><trkseg>\n";

        String segments = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        for (Location location : points) {
            segments += "<trkpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude() + "\"><time>" + df.format(new Date(location.getTime())) + "</time></trkpt>\n";
        }

        String footer = "</trkseg></trk></gpx>";

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.append(header);
            writer.append(name);
            writer.append(segments);
            writer.append(footer);
            writer.flush();
            writer.close();
            ret_val = true;
        } catch (IOException e) {
            Log.e("generateGfx", "Error Writting Path", e);
            ret_val = false;
        }
        return ret_val;
    }
}
