package com.sailinghawklabs.lambdaking

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.sailinghawklabs.engineeringnotation.EngineeringNotationTools

import com.sailinghawklabs.lambdaking.preferences.SettingsPrefActivity
import com.sailinghawklabs.lambdaking.tlines.SelectTlineActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.include_data_entry.*
import kotlinx.android.synthetic.main.include_grid_results.*
import java.lang.Exception
import java.util.*
import kotlin.collections.LinkedHashMap

class MainActivity : AppCompatActivity() {

    companion object {
        private const val RESULT_CODE_TLINE_VF = 123
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var freqUnits: LinkedHashMap<Double, String>
    private lateinit var freqSpinnerAdapter: LinkedHashMapAdapter<Double, String>
    private lateinit var lengthUnits: LinkedHashMap<Double, String>
    private lateinit var lengthSpinnerAdapter: LinkedHashMapAdapter<Double, String>

    // main values that are entered:
    private var frequency_Hz = 0.0
    private var cableLength_m = 0.0
    private var velocityFactor = 0.0
    private var epsilon = 1.0
    private var mNumDigits = 0

    private var epsilonIsMaster = true

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: entered")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadPreferences()
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        initializeViews()
        enableAdvertisements()

    }

    private fun initializeViews() {
        main_et_freq.setDefaultValue("1.0")
        main_et_freq.setToDefault()
        main_et_length.setDefaultValue("1.0")
        main_et_length.setToDefault()
        main_et_vf.setDefaultValue("1.0")
        main_et_vf.setToDefault()
        main_et_er.setDefaultValue("1.0")
        main_et_er.setToDefault()

        main_et_freq.setOnEditorActionListener(myEditorChangeListener)
        main_et_freq.onFocusChangeListener = myEditTextOnFocusChangeListener

        main_et_length.setOnEditorActionListener(myEditorChangeListener)
        main_et_length.onFocusChangeListener = myEditTextOnFocusChangeListener

        main_et_vf.setOnEditorActionListener(myEditorChangeListener)
        main_et_vf.onFocusChangeListener = myEditTextOnFocusChangeListener

        main_et_er.setOnEditorActionListener(myEditorChangeListener)
        main_et_er.onFocusChangeListener = myEditTextOnFocusChangeListener

        freq_units_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                freqSpinnerChanged(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d(TAG, "onNothingSelected: freq spinner")
            }
        }
        length_unit_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                lengthSpinnerChanged(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        main_tl_list.setOnClickListener {
            val intent = Intent(this, SelectTlineActivity::class.java)
            startActivityForResult(intent, RESULT_CODE_TLINE_VF)
        }

        populateFreqUnits()
        populateLengthUnits()
    }

    private fun populateFreqUnits() {
        freqUnits = LinkedHashMap()

        // populate map with value multipliers to yield Hz
        freqUnits[1e15] = "PHz"
        freqUnits[1e12] = "THz"
        freqUnits[1e9] = "GHz"
        freqUnits[1e6] = "MHz"
        freqUnits[1e3] = "kHz"
        freqUnits[1.0] = "Hz"
        freqUnits[1e-3] = "mHz"
        freqUnits[1e-6] = "uHz"
        freqSpinnerAdapter = LinkedHashMapAdapter(this, android.R.layout.simple_spinner_item, freqUnits)
        freqSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        freq_units_spinner.adapter = freqSpinnerAdapter
        freq_units_spinner.setSelection(freqUnits.values.indexOf("MHz"))
    }

    private fun populateLengthUnits() {
        lengthUnits = LinkedHashMap()

        // populate map with value multipliers to yield m
        lengthUnits[1e3] = "km"
        lengthUnits[1.0] = "m"
        lengthUnits[1e-1] = "cm"
        lengthUnits[1e-3] = "mm"
        lengthUnits[1 / (1000 * 12 * PhysicalConstants.FEET_PER_METER_ft)] = "mil"
        lengthUnits[1 / (12 * PhysicalConstants.FEET_PER_METER_ft)] = "in"
        lengthUnits[1 / PhysicalConstants.FEET_PER_METER_ft] = "ft"
        lengthSpinnerAdapter = LinkedHashMapAdapter(this, android.R.layout.simple_spinner_item, lengthUnits)
        lengthSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        length_unit_spinner.adapter = lengthSpinnerAdapter
        length_unit_spinner.setSelection(lengthUnits.values.indexOf("m"))
    }

    private fun isEntryValid(et: EditText?, fieldName: String): Boolean {
        if (et!!.text.toString().isEmpty()) {
            Log.d(TAG, "checkEntryValidity: ERROR - $fieldName is empty")
            return false
        }
        if (java.lang.Double.valueOf(et.text.toString()) <= 0) {
            Log.d(TAG, "isEntryBad: ERROR - $fieldName is <= 0")
            return false
        }
        return true
    }

    private fun checkAllEntries(): Boolean {
        return (isEntryValid(main_et_freq, "Frequency")
                && isEntryValid(main_et_length, "Length")
                && isEntryValid(main_et_vf, "Velocity"))
    }

    private fun readEditTexts(): Boolean {
        if (!checkAllEntries()) return false

        // parse the entry EditTexts, since they should be OK to read now without error
        frequency_Hz = (main_et_freq.text.toString()).toDouble()
        cableLength_m = (main_et_length.text.toString()).toDouble()
        velocityFactor = (main_et_vf.text.toString()).toDouble()
        epsilon = (main_et_er.text.toString()).toDouble()

        // apply suffix multipliers fron the Spinners
        var position = freq_units_spinner.selectedItemPosition
        val freqMultEntry = freqSpinnerAdapter.getItem(position)
        if (freqMultEntry == null) {
            throw Exception("MainActivity: readEditTexts: missing freq spinner entry position: $position")
        }
        val freqMultiplier = freqMultEntry.key
        frequency_Hz *= freqMultiplier

        position = length_unit_spinner.selectedItemPosition
        val lengthMultEntry = lengthSpinnerAdapter.getItem(position)
        if (lengthMultEntry == null) {
            throw Exception("MainActivity: readEditTexts: missing length spinner entry position: $position")
        }
        val lengthMultiplier = lengthMultEntry.key
        cableLength_m *= lengthMultiplier
        Log.d(TAG, "calculateResults: frequency= " + frequency_Hz + "Hz, length=" + cableLength_m + "m, Vf = " + velocityFactor)
        return true
    }

    private fun calculateResults() {
        if (!readEditTexts()) return
        val autoRanger = AutoRanger(mNumDigits)

        val calcs = Calculator.execute(frequency_Hz, cableLength_m, velocityFactor, epsilon, epsilonIsMaster)

        // -------------------------------------------------------------------------------------
        // Adjust the units and display results
        // auto-units: *s, *m, ft|in|mi|mil|yd,
        result_phase_shift_deg.text = autoRanger.rangePhase(calcs.phaseShift_deg).toEngineeringString()
        result_delay.text = autoRanger.rangeTime(calcs.delay_s).toEngineeringString()
        result_elen.text = autoRanger.rangeWavelengths(calcs.num_wavelens).toEngineeringString()


        // speed --------------------------------------------------------------------
        val encoded_speed_m_s = EngineeringNotationTools.encodeMantissa(calcs.velocity_m_s, mNumDigits)
        var result = encoded_speed_m_s.mantissaString + " " + encoded_speed_m_s.exponentString + "m/s"
        result_speed_m_s.text = result
        val encoded_speed_mi_s = EngineeringNotationTools.encodeMantissa(calcs.velocity_mi_s, mNumDigits)
        result = encoded_speed_mi_s.mantissaString + " " + encoded_speed_m_s.exponentString + "mi/s"
        result_speed_mi_s.text = result
        val encoded_speed_s_m = EngineeringNotationTools.encodeMantissa(calcs.velocity_s_m, mNumDigits)
        result = encoded_speed_s_m.mantissaString + " " + encoded_speed_s_m.exponentString + "s/m"
        result_speed_s_m.text = result
        val encoded_speed_s_in = EngineeringNotationTools.encodeMantissa(calcs.velocity_s_in, mNumDigits)
        result = encoded_speed_s_in.mantissaString + " " + encoded_speed_s_in.exponentString + "s/in"
        result_speed_s_in.text = result


        // lamdas -------------------------------------------------------------
        result_lambda_m.text = autoRanger.rangeLength(calcs.lambda_m).toEngineeringString()
        result_lambda_ft.text = autoRanger.rangeLengthImperial(calcs.lambda_m).toEngineeringString()
        result_lambda_2_m.text = autoRanger.rangeLength(calcs.lambda_m / 2).toEngineeringString()
        result_lambda_2_ft.text = autoRanger.rangeLengthImperial(calcs.lambda_m / 2).toEngineeringString()
        result_lambda_4_m.text = autoRanger.rangeLength(calcs.lambda_m / 4).toEngineeringString()
        result_lambda_4_ft!!.text = autoRanger.rangeLengthImperial(calcs.lambda_m / 4).toEngineeringString()

        // phase slopes --------------------------------------------------
        result_slope_m_deg.text = autoRanger.rangeLength(calcs.phase_slope_m_deg).toEngineeringString() + "/deg"
        result_slope_ft_deg.text = autoRanger.rangeLengthImperial(calcs.phase_slope_m_deg).toEngineeringString() + "/deg"
        val encodedSlope_deg_m = EngineeringNotationTools.encodeMantissa(1 / calcs.phase_slope_m_deg, mNumDigits)
        result = encodedSlope_deg_m.mantissaString + " " + encodedSlope_deg_m.exponentString + "deg/m"
        result_slope_deg_m.text = result
        val encodedSlope_deg_ft = EngineeringNotationTools.encodeMantissa(1 / calcs.phase_slope_ft_deg, mNumDigits)
        result = encodedSlope_deg_ft.mantissaString + " " + encodedSlope_deg_ft.exponentString + "deg/ft"
        result_slope_deg_ft.text = result
        val encodedSlope_deg_hz = EngineeringNotationTools.encodeMantissa(calcs.phase_slope_deg_hz, mNumDigits)
        result = encodedSlope_deg_hz.mantissaString + " " + encodedSlope_deg_hz.exponentString + "deg/Hz"
        result_slope_deg_hz.text = result
        val encodedSlope_hz_deg = EngineeringNotationTools.encodeMantissa(1 / calcs.phase_slope_deg_hz, mNumDigits)
        result = encodedSlope_hz_deg.mantissaString + " " + encodedSlope_hz_deg.exponentString + "Hz/deg"
        result_slope_hz_deg.text = result

        // epsilon ------------------------------------------------------------------
        //result_epsilon.text = String.format(Locale.US, "%." + mNumDigits + "f (relative)", calcs.epsilon_r)
        if (epsilonIsMaster) {
            main_et_vf.setText(String.format(Locale.US, "%.${mNumDigits}f", calcs.velocityFactor))
        } else {
            main_et_er.setText(String.format(Locale.US, "%.${mNumDigits}f", calcs.epsilon_r))
        }

        // VSWR ripple spacing -------------------------------------------------------
        val encoded_ripple_hz = EngineeringNotationTools.encodeMantissa(calcs.vswr_ripple_spacing_hz, mNumDigits)
        result = encoded_ripple_hz.mantissaString + " " + encoded_ripple_hz.exponentString + "Hz spacing"
        result_vswr_ripple.text = result
    }

    private val myEditTextOnFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        if (!hasFocus) {
            if (v.id == main_et_er.id) {
                epsilonIsMaster = true
            } else if (v.id == main_et_vf.id) {
                epsilonIsMaster = false
            }
            editTextPostFix(v as EditTextWithClear)
        }
    }
    private val myEditorChangeListener = TextView.OnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (v.id == main_et_er.id) {
                epsilonIsMaster = true
            } else if (v.id == main_et_vf.id) {
                epsilonIsMaster = false
            }
            editTextPostFix(v as EditTextWithClear)
            return@OnEditorActionListener true
        }
        false
    }

    private fun editTextPostFix(et: EditTextWithClear) {
        hideKeyboard(et)
        et.clearFocus()
        if (et.text.toString().isEmpty()) {
            et.setToDefault()
        }
        calculateResults()
    }

    private fun freqSpinnerChanged(id: Long) {
        Log.d(TAG, "freqSpinnerChanged: index = $id")
        calculateResults()
    }

    private fun lengthSpinnerChanged(id: Long) {
        Log.d(TAG, "lengthSpinnerChanged: index = $id")
        calculateResults()
    }

    fun hideKeyboard(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu: menu = $menu")
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: item = " + item.title.toString())
        when (item.itemId) {
            R.id.mn_main_settings -> {
                startActivity(Intent(this@MainActivity, SettingsPrefActivity::class.java))
                return true
            }
            R.id.mn_main_help ->                 // startActivity(new Intent(MainActivity.this, HelpActivity.class));
                return true
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val numDigitsString = prefs.getString(getString(R.string.pref_key_digits), getString(R.string.pref_def_digits))
        mNumDigits = numDigitsString!!.toInt()
        Log.d(TAG, "loadPreferences: nNumDigits set to : $mNumDigits")
    }

    override fun onResume() {
        Log.d(TAG, "onResume: entered")
        loadPreferences()
        calculateResults()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode == $requestCode")
        if (requestCode == RESULT_CODE_TLINE_VF) {
            if (resultCode == Activity.RESULT_OK) {
                val newVf = data!!.getDoubleExtra("VF", 1.0)
                main_et_vf.setText(newVf.toString())
                epsilonIsMaster = false
                calculateResults()
            }
        }
    }

    private fun enableAdvertisements() {
        MobileAds.initialize(this) {
            Log.d("MainActivity", "MobileAds initialization returned")
        }

        // add physical device(s) as Test Devices for development (not release)
        if (BuildConfig.DEBUG) {
            val config = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf("28B5489C26A46FD308BFB094FC7F36D8"))
                    .build()
            MobileAds.setRequestConfiguration(config)

        }
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        Log.d("MainActivity", "onCreate: ad id = ${adView.adUnitId}")
    }
}