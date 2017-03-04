package apps.attendencelocation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main Activity";
    public static Context context;
    static MainActivity mInstance;
    private static Intent notificationIntent;
    private static PendingIntent pendingIntent;
    private static AlarmManager alarmManager;

    private static final int MIN_TIME_BW_UPDATES = 1;  // in seconds
    private static final int MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInstance=this;
        context = MainActivity.this;
    }

    private void startUpdate()
    {
        if(AppStatus.getInstance(getApplicationContext()).isOnline()){
            if(GPSchecker()){
                notificationIntent = new Intent(this, LocationService.class);
                pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,0,1000*MIN_TIME_BW_UPDATES,pendingIntent);
                Log.d(TAG,"Update Start");
            }else {
                sendGPSSetting();
            }
        }else {
            Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
        }
    }
    private void stopUpdate(){
        startUpdate();
        alarmManager.cancel(pendingIntent);
        Log.d(TAG,"Update Stop");
    }

    public void startButton(View view){
        startUpdate();
    }
    public void stopButton(View view){
        stopUpdate();
    }
    private void sendGPSSetting(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setMessage("GPS not enabled");
        dialog.setPositiveButton("Enable Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(myIntent);
                //get gps
            }
        });
        dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                finish();

            }
        });
        dialog.show();
    }
    private boolean GPSchecker(){
        Log.d(TAG, "GPSchecker Called " );
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
}
