package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private String LOG_TAG = "MainActivity";

    public static int LOGLEVEL = 2;
    public static boolean ERROR = LOGLEVEL > 0;
    public static boolean WARN = LOGLEVEL > 1;
    public static boolean VERBOSE = LOGLEVEL >= 2;

    String mForecastJsonStr = null;
    Bundle mSavedInstanceState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (WARN) Log.w("ForecastFragment", "entering onCreate ");

        // saved state to be shared with Handler callback
        mSavedInstanceState = savedInstanceState;

        setContentView(R.layout.activity_main);

        // make sure default values are set in shared prefs
       // PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        // this thread connects to weather site and recovers weather forecast
   //      connectToWeather.start();

        // This code gets replaced with a thread launch that initiates a URL connection
        // if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        // }
        Log.v(LOG_TAG,"on Create");

    }


    private String checkZipCode(String s) {
        if (s.length() != 5)
            return s;
        if (s.length() == 5) {  // zipcode length
            try {
                int i = Integer.parseInt(s) ;
                s += ",USA";
            } catch (NumberFormatException nfe) { }
        }
        return s;
    }

    public boolean openPreferredLocationInMap() {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        String location = shared.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));  // default is "12561,USA"
        location = checkZipCode(location);  // puts ",USA" at end of a bare zipcode

        // launch map with this location
        String uri =  "http://maps.google.co.in/maps?q=" + location;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
        else
            Log.d(LOG_TAG, "Couldn't call "+ location + " on google maps");
        return true;
    }


            @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent;
            intent = new Intent(this, SettingsActivity.class);
          //  intent.putExtra(Intent.EXTRA_TEXT, forecast);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(LOG_TAG,"on Restart");
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_TAG,"on Start");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG,"on Pause");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG,"on Stop");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG,"on Destroy");
    }







}
