package apps.attendencelocation;

import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static android.content.ContentValues.TAG;
import static apps.attendencelocation.MainActivity.context;

import android.os.CountDownTimer;

/**
 * Created by ashrafiqubal on 07/01/17.
 */

public class LocationService extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "LocationService";

    //LocationManager locationManager;
    private static final int MIN_TIME_BW_UPDATES = 1000 * 60 * 5, MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    public boolean isGPSEnabled = false, isNetworkEnabled = false, canGetLocation = false;
    private Location mLastLocation;
    double latitude,longitude;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 1;
    private static int FATEST_INTERVAL = 1; // 2 sec
    private static int DISPLACEMENT = 100; // 100 meters

    private boolean isGPSOn = false;

    @Override
    public void onReceive(Context mContext, Intent intent) {
        if(GPSchecker()){
            buildGoogleApiClient();
            //Log.d("sendlocation", "it will appear in Log Console");
            connectToGoogleAPI();
            createLocationRequest();
            turnGPSOn();
            isGPSOn = true;
        }else {
            //sendGPSSetting();
            isGPSOn = false;
        }
        // code for send location to srver
    }
    private boolean GPSchecker(){
        //Log.d(TAG, "GPSchecker Called " );
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        if( !gps_enabled && !network_enabled){
            return false;
        }else {
            return true;
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }


    /**
     * Method to display the location on UI
     * */
    private void getLocation() {
        if((ContextCompat.checkSelfPermission(MainActivity.context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            //Log.d(TAG,"getLocation called, Location Reveived");
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                Log.d(TAG,"getLocation called, LatLong Set "+latitude+"  "+longitude);
                //Toast.makeText(MainActivity.context,latitude+"  "+longitude,Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(MainActivity.context,"Unable to fetch location. Try again",Toast.LENGTH_LONG).show();
            }
        }else {
            //Toast.makeText(MainActivity.context,"Please close and restart the app",Toast.LENGTH_LONG).show();
        }
    }
    private void connectToGoogleAPI(){
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    private void disconnectToGoogleAPI(){
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            //Log.d(TAG, "disconnectToGoogleAPI  "+mGoogleApiClient.isConnected());
        }
    }
    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }
    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        //Log.d(TAG,"onConnected Called");
        //Log.d(TAG, "onConnected Called "+mGoogleApiClient.isConnected());
        //Log.d(TAG, "GoogleApiClient Connected  " +mGoogleApiClient.isConnected());
        startLocationUpdates();
        delaySec(1);
    }
    @Override
    public void onConnectionSuspended(int arg0) {
        Log.d(TAG,"OnConnectionSuspended Called");
        mGoogleApiClient.connect();
    }
    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        Log.d(TAG,"onLocationChanged Called");
    }
    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        //Log.d(TAG,"createLocationRequest called successfully" );

    }
    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        if(ContextCompat.checkSelfPermission(MainActivity.context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            try{
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
                //Log.d(TAG,"startLocationUpdates called succesfully" );
            }catch (Exception e){
                Log.d(TAG,"Error:1 "+e.getMessage());
            }
        }else {
            //Toast.makeText(MainActivity.context,"Please close and restart the app",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            //Log.d(TAG,"StopLocationUpdates Called");
        }catch (Exception e){
            Log.d(TAG,"Error:2 "+e.getMessage());
        }

    }
    public void delaySec(int sec){
        new CountDownTimer(1000*sec, 1000*sec) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                //Log.d(TAG, "delaySec GoogleApiClient Connected  " +mGoogleApiClient.isConnected());
                if (mGoogleApiClient.isConnected()) {
                    getLocation();
                    stopLocationUpdates();
                    disconnectToGoogleAPI();
                }
            }

        }.start();
    }
    private void turnGPSOn(){
        String provider = Settings.Secure.getString(MainActivity.context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            MainActivity.mInstance.sendBroadcast(poke);
        }
    }
    private void turnGPSOff(){
        String provider = Settings.Secure.getString(MainActivity.context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            MainActivity.mInstance.sendBroadcast(poke);
        }
    }
}
