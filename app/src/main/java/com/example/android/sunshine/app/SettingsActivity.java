package com.example.android.sunshine.app;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import static android.preference.Preference.OnPreferenceChangeListener;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file. This loads XML layout and
        // replaces the normal load of layout (setContentView)
        addPreferencesFromResource(R.xml.pref_general);
        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        // here we associate ("bind") a preference "summary" to the location preference whose
        // key value is "location". This is an EditTextPreference.This "binding" happens by
        // attaching a change listener to the EditTextPreference and firing the listener right
        // away to initialize the summary
        /*
        String s = getString(R.string.pref_location_key);
        Preference p = findPreference(s);
        bindPreferenceSummaryToValue(p);
        s = "units";
        Preference p = findPreference(s);
        bindPreferenceSummaryToValue(p);
        */
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));

    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this); // callback is this.onPreferenceChange()
        // Trigger the listener immediately with the preference's
        // current value. Future callbacks will happen automatically
        //THe sharedPreference is a "persistent" version of things where values are held.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
                        // "key" is "location"|"units", "" is default if key missing
    }

    // this is the listener callback that we must override.
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            // preference summary is the value tht is displayed in the original tap area
            preference.setSummary(stringValue);   // this is just a search value, I think
        }
        /*
        // at this point we want to retrieve the value stored and set up "refresh"
        Intent resultIntent = new Intent();
        // Add extras to this intent.
        resultIntent.putExtra(getString(R.string.pref_location_key), stringValue.toCharArray());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        */
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
 //       getPreferenceScreen().getSharedPreferences()
 //               .registerOnSharedPreferenceChangeListener((Preference.OnPreferenceChangeListener) this);
    }

    @Override
    protected void onPause() {
        super.onPause();
 //       getPreferenceScreen().getSharedPreferences()
 //               .unregisterOnSharedPreferenceChangeListener((OnPreferenceChangeListener) this);
    }

}
