package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DetailActivity extends ActionBarActivity {

  //  private ShareActionProvider shareActionProvider ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    /** Defines a default (dummy) share intent to initialize the action provider.
     * However, as soon as the actual content to be used in the intent
     * is known or changes, you must update the share intent by again calling
     * mShareActionProvider.setShareIntent()
     */

    private String checkZipCode(String s) {
        if (s.length() != 5)
            return s;
        if (s.length() == 5) {  // zipcode length
            try {
                int i = Integer.parseInt(s);
                s += ",USA";
            } catch (NumberFormatException nfe) {
            }
        }
        return s;
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
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_map) {
            // get the current preferred location and launch a map until with this preference
            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
            String location = shared.getString(getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default));  // default is "12561,USA"
            location = checkZipCode(location);  // puts ",USA" at end of a bare zipcode

            // launch map with this location
//            String uri =  "http://maps.google.co.in/maps?q=" + location;
            String uri =  "http://maps.google.com/maps?q=" + location;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = "DetaiLFragment";
        private static final String FORECAST_SHARING_HASHTAG = " #SunshineApp";
        // need a way to pass the detail forecast to the share intent
        private String mForecastStr;


        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();

            if (intent != null & intent.hasExtra(Intent.EXTRA_TEXT))
                mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);

            final TextView textView = (TextView) rootView.findViewById(R.id.detail_text);
            textView.setText(mForecastStr);

            return rootView;
        }

        /* helper for creating a share intent when we create the options menu */
        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARING_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate menu resource file.
            inflater.inflate(R.menu.menu_detail, menu);

            // Locate MenuItem with ShareActionProvider
            MenuItem item = menu.findItem(R.id.menu_item_share);

            // Fetch and store ShareActionProvider
            ShareActionProvider mShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is Null??");
            }

        }
    }
}

