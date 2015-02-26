package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by pletcha on 2/9/15.
 */
public class ForecastFragment extends Fragment{

    private String LOG_TAG = "ForecastFragment";

    public static int LOGLEVEL = 2;
    public static boolean ERROR = LOGLEVEL > 0;
    public static boolean WARN = LOGLEVEL > 1;
    public static boolean VERBOSE = LOGLEVEL >= 2;

    private ArrayAdapter<String> mForecastAdapter;
    private View rootView;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // need to call this in order for onCreateOptionsMenu to be invoked
        setHasOptionsMenu(true);
    }

        // no longer see the dummy code.
    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflaters instantiate an XML layout file into its corresponding view  objects
        // in this case a layout view with one "child"; a ListView
        rootView = inflater.inflate(R.layout.fragment_main, container, false);



        // the ListView object knows about how to layout a list of items
        // the adapter knows about how to view individual items (in a TextView, here)
        // rootView is fragment_main xml document


        // this adapter knows how to display individual items and is intially given
        // an empty list of items to display. The list is actual updated onStart();
        mForecastAdapter =
                // context, id of list item layout (a file), id of text view for item, list of items
                new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                           R.id.list_item_forecast_textview,
                        new ArrayList<String>());
        // searches rootView.mChildren array for object matching ID rather than creating
        // a new one.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l ) {

                String forecast = (String) adapterView.getItemAtPosition(i);
                // String mItemText = mForecastAdapter.getItem(i);  works also
                // Toast.makeText(getActivity(), mItemText, Toast.LENGTH_SHORT).show();
                Intent intent;
                intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });   // need a listener here



        return rootView;
    }

    /* fragments need their own Options Menu callback. */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater mInf) {
        mInf.inflate(R.menu.forecastfragment, menu);
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

    private void updateWeather() {
        // only executable once on this object. Start by getting access to the persistent
        // settings values in SharePreferences
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Now recover the saved location value or alternatively the default location
        // and use it to query the website
        String pCode = shared.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));  // default is "12561,USA"
       // String uCode = shared.getString("units", "metric");  // default metric

        pCode = checkZipCode(pCode);  // puts ",USA" at end of a bare zipcode
//        new FetchWeatherTask().execute(pCode,uCode);
        new FetchWeatherTask().execute(pCode);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem mItem) {
        int id = mItem.getItemId();
        // we need to set the preferred zipcode to some constant or the one specified through the
        // settings interface. We only need to update it
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        /*
        if (id == R.id.action_map) {
           // get the current preferred location and launch a map until with this preference
            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = shared.getString(getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default));  // default is "12561,USA"
            location = checkZipCode(location);  // puts ",USA" at end of a bare zipcode

            // launch map with this location
            String uri =  "http://maps.google.co.in/maps?q=" + location;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
            return true;
        }
        */
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent;
            int recCode = 3;  // just an example
            intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(mItem);
    }
/*
    @Override
    public void onActivityResult(int reqCode,int retCode, Intent intent) {
        super.onActivityResult( reqCode, retCode,  intent);
        // set up local variable
        String result;
        if ((reqCode == 3) & (retCode == Activity.RESULT_OK)
                &  intent.hasExtra(getString(R.string.pref_location_key))) {
            result = intent.getStringExtra(getString(R.string.pref_location_key));
        } else {
            result = "10101, USA";
        }
        new FetchWeatherTask().execute(result);
    }
*/


    public class FetchWeatherTask extends AsyncTask<String,Void,String[]> {
        public String[] doInBackground(String... codes) {

            // no zipcode, nothing to do
            if (codes.length == 0)
                return (String[])null;


            String postalCode = codes[0];

            /*  refactoring to fetch units in formatHighLows()
            if (codes.length < 2)
                return (String[])null;
            String units = codes[1];
            */

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            if (WARN)  Log.w("ForecastFragment", "entering connectToWeather ");

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String serverUnits = "metric";
            int cnt = 7;
            try {

                // daily may fail to do what I want; if so, remove it
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                // Build our URL with Uri.Builder
                Uri bURI =  Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, postalCode)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, serverUnits)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(cnt))
                        .build();   // doesn't work with "daily"

                URL myUrl = new URL(bURI.toString());
                if (VERBOSE) Log.v("ForecastFragment", bURI.toString());
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                ;

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) myUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                if ( urlConnection == null ) {
                    return (String[]) null;
                }
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
                if (VERBOSE) Log.v("ForecastFragment", forecastJsonStr);
            } catch (IOException e) {
                if (ERROR) Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            if (ERROR) Log.e("ForecastFragment", "Error closing stream", e);
                        }
                    }
                }

            }
            String[] wData = {"bad location"};
            try {
                if (forecastJsonStr != null) {
                    // refactor units to later
                  //  wData = getWeatherDataFromJson(forecastJsonStr, 7, units);
                    wData = getWeatherDataFromJson(forecastJsonStr, 7);
                }
            } catch (JSONException jse) {
                if (ERROR) Log.e("ForecastFragment", "Unable to Parse JSON Document");
                jse.printStackTrace();
            } finally {
                if ( forecastJsonStr == null ) {
                    if (ERROR) Log.e("ForecastFragment", "Failed to Connect to WeatherSite");
                    return (String[]) null;
                }
            }
            if (VERBOSE) Log.v("ForecastFragment",wData[0]);
            return wData;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            /*
            if (strings != null) {
                weekForecast.clear();
                weekForecast.addAll(Arrays.asList(strings));
                mForecastAdapter.notifyDataSetChanged();
            }
            */
            if (strings != null) {
                mForecastAdapter.clear();
                for(String dayForecastStr : strings) {
                    mForecastAdapter.add(dayForecastStr);
                }
                // New data is back from the server.  Hooray!
            }
        }
    /*        // this adapter knows how to display individual items and knows where to find the items
            mForecastAdapter =
                    // context, id of list item layout (a file), id of text view for item, list of items
                    new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                            //   R.id.list_item_forecast_textview,
                            weekForecast);

// searches rootView.mChildren array for object matching ID rather than creating
            // a new one.
            ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
            listView.setAdapter(mForecastAdapter);
            */


        /* The date/time conversion code is going to be moved outside the asynctask later,
  * so for convenience we're breaking it out into its own method now.
  */
        private String getReadableDateString(long time){
// Because the API returns a unix timestamp (measured in seconds),
// it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date); // .toString();
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
// For presentation, assume the user doesn't care about tenths of a degree.

            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPrefs.getString(
                    getString(R.string.pref_units_key),
                    getString(R.string.pref_units_metric));

            if (unitType.equals(getString(R.string.pref_units_imperial))) {
                high = (high*1.8)+32;
                low = (low*1.8)+32;
            } else if (unitType.equals(getString(R.string.pref_units_metric))) {
                // do nothing
            } else {
                Log.d(LOG_TAG, "Unit Type not found: " + unitType);
            }
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /*  no longer used
        private double m2I(double m) {
            double i = m*9.0/5.0 + 32.0;
            return i;
        }
        */
        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy: constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays, String units)
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

// These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
// For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

// Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

// The date/time is returned as a long. We need to convert that
// into something human-readable, since most people won't read "1400356800" as
// "this saturday".
                long dateTime = dayForecast.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);

// description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

// Temperatures are in a child object called "temp". Try not to name variables
// "temp" when working with temperature. It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);
                /*   get rid of units here; push to formatHighLows()
                if (units.equals("imperial"))
                    high = m2I(high);
                if (units.equals("imperial"))
                    low = m2I(low);
                */

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }
            /*
            for (String s: resultStrs) {
                Log.v("ForecastFragment","Forecast Entry: "+ s);
            }
            */
            return resultStrs;
        }


    }
}
