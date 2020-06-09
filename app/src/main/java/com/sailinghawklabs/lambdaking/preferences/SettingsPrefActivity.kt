package com.sailinghawklabs.lambdaking.preferences

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.sailinghawklabs.lambdaking.R

class SettingsPrefActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, SettingsFragment())
                    .commit()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_main, rootKey)
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_digits))!!)

        }

    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener =
                Preference.OnPreferenceChangeListener { preference, value ->

                    val stringValue = value.toString()

                    if (preference is ListPreference) {
                        // For list preferences, look up the correct display value in
                        // the preference's 'entries' list.
                        val index = preference.findIndexOfValue(stringValue)

                        // Set the summary to reflect the new value.
                        preference.setSummary(
                                if (index >= 0)
                                    preference.entries[index]
                                else
                                    null)

                    } else {
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        preference.summary = stringValue
                    }
                    true
                }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

    }
}