package com.sailinghawklabs.lambdaking.preferences;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;

import com.sailinghawklabs.lambdaking.R;


public class SettingsPrefActivity extends AppCompatPreferenceActivity {
    private static final String TAG = SettingsPrefActivity.class.getSimpleName();


    //Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
    //    Uri.fromParts("package", getPackageName(), null));
    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //startActivity(intent);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MainPreferenceFragment()).commit();

    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            // notification preference change listeners to update the Summary fields
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_digits)), getActivity().getString(R.string.pref_def_digits));

//            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_midmorning)), getActivity().getString(R.string.pref_def_midmorning));
//            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_lunch)), getActivity().getString(R.string.pref_def_lunch));
//            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_afternoon)), getActivity().getString(R.string.pref_def_afternoon));
//            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_dinner)), getActivity().getString(R.string.pref_def_dinner));
//            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_bedtime)), getActivity().getString(R.string.pref_def_bedtime));


//            Preference button = findPreference(getString(R.string.pref_key_settings_link));
//
//            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                            Uri.fromParts("package", getActivity().getPackageName(), null));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                    return true;
//                }
//            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference, String defaultValue) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), defaultValue));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = (String) newValue;
            Log.d(TAG, "onPreferenceChange: key:" + preference.getKey() + ", summary: " + stringValue);


            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }  else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };



}
