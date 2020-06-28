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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.sailinghawklabs.engineeringnotation.EngineeringNotationTools
import com.sailinghawklabs.lambdaking.entities.*
import com.sailinghawklabs.lambdaking.tlines.SelectTlineActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.include_data_entry.*
import kotlinx.android.synthetic.main.include_grid_results.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    companion object {
        private const val RESULT_CODE_TLINE_VF = 123
        private const val DEFAULT_NUM_DP = 2
        private const val ER_VF_NUM_DP = 3
        private const val PREF_KEY_PRESET_ENTRY = "preset_key"
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var freqSpinnerAdapter: LinkedHashMapAdapter<Double, String>
    private lateinit var lengthSpinnerAdapter: LinkedHashMapAdapter<Double, String>

    private var epsilonIsMaster = true

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: entered")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        initializeViews()
        enableAdvertisements()
    }

    private fun initializeViews() {
        main_et_freq.setOnEditorActionListener(myEditorChangeListener)
        main_et_freq.onFocusChangeListener = myEditTextOnFocusChangeListener

        main_et_length.setOnEditorActionListener(myEditorChangeListener)
        main_et_length.onFocusChangeListener = myEditTextOnFocusChangeListener

        main_et_vf.setOnEditorActionListener(myEditorChangeListener)
        main_et_vf.onFocusChangeListener = myEditTextOnFocusChangeListener

        main_et_er.setOnEditorActionListener(myEditorChangeListener)
        main_et_er.onFocusChangeListener = myEditTextOnFocusChangeListener
        main_et_er.setText("1")
        main_tv_erLabel.text = Characters.EPSILON_SUB_R

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

        freqSpinnerAdapter = LinkedHashMapAdapter(this, android.R.layout.simple_spinner_item, FreqUnits)
        freqSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        freq_units_spinner.adapter = freqSpinnerAdapter

        lengthSpinnerAdapter = LinkedHashMapAdapter(this, android.R.layout.simple_spinner_item, LengthUnits)
        lengthSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        length_unit_spinner.adapter = lengthSpinnerAdapter
        setEntryToState(loadPresetEntryState())
        main_et_er.setAsDefaultValue()
    }

    private fun loadPresetEntryState(): EntryState {
        val preferences = getPreferences(Context.MODE_PRIVATE)
        val defaultState = EntryState.getDefault().serialize()
        val storedStateString = preferences.getString(PREF_KEY_PRESET_ENTRY, defaultState)
        return EntryState.create(storedStateString ?: defaultState)
    }

    private fun storePresetEntryState(state: EntryState) {
        val preferences = getPreferences(Context.MODE_PRIVATE)
        preferences.edit().putString(PREF_KEY_PRESET_ENTRY, state.serialize()).apply()
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
        if (!isEntryValid(main_et_freq, "Frequency")) {
            return false
        }
        if (!isEntryValid(main_et_length, "Length")) {
            return false
        }
        return if (epsilonIsMaster) {
            isEntryValid(main_et_er, Characters.EPSILON_SUB_R.toString())
        } else {
            isEntryValid(main_et_vf, "Velocity")
        }
    }

    private fun readEditTexts(): NumericEntryData {
        Log.d("MainActivity", "readEditTexts: epsilonIsMaster=${epsilonIsMaster}")
        if (!checkAllEntries()) {
            Log.d("MainActivity", "readEditTexts: failed checkAllEntries")
            return NumericEntryData(false)
        }

        val numericEntryData = NumericEntryData()

        numericEntryData.frequency_Hz = (main_et_freq.text.toString()).toDouble()
        numericEntryData.cableLength_m = (main_et_length.text.toString()).toDouble()

        if (!main_et_vf.text.isNullOrEmpty()) {
            numericEntryData.velocityFactor = (Math.max(main_et_vf.text.toString().toDouble(), 0.001))
        }

        if (!main_et_er.text.isNullOrEmpty()) {
            numericEntryData.epsilon = (Math.max(main_et_er.text.toString().toDouble(), 0.001))
            numericEntryData.epsilon = (Math.min(numericEntryData.epsilon, 100000.00))
        }

        // apply suffix multipliers fron the Spinners
        var position = freq_units_spinner.selectedItemPosition
        val freqMultEntry = freqSpinnerAdapter.getItem(position)
                ?: throw Exception("MainActivity: readEditTexts: missing freq spinner entry position: $position")
        val freqMultiplier = freqMultEntry.key
        numericEntryData.frequency_Hz *= freqMultiplier

        position = length_unit_spinner.selectedItemPosition
        val lengthMultEntry = lengthSpinnerAdapter.getItem(position)
                ?: throw Exception("MainActivity: readEditTexts: missing length spinner entry position: $position")
        val lengthMultiplier = lengthMultEntry.key
        numericEntryData.cableLength_m *= lengthMultiplier
        Log.d(TAG, "calculateResults: frequency= " + numericEntryData.frequency_Hz + "Hz, length="
                + numericEntryData.cableLength_m + "m, Vf = " + numericEntryData.velocityFactor)
        return numericEntryData
    }

    private fun refreshDisplay() {
        Log.d("MainActivity", "refreshDisplay: ")
        val numericEntryData = readEditTexts()
        if (!numericEntryData.valid) {
            Log.d("MainActivity", "refreshDisplay: numeric entry invalid - abort")
            return
        }
        val autoRanger = AutoRanger(DEFAULT_NUM_DP)
        val calcs =
                Calculator.execute(numericEntryData, epsilonIsMaster)

        // -------------------------------------------------------------------------------------
        // Adjust the units and display results
        // auto-units: *s, *m, ft|in|mi|mil|yd,
        result_phase_shift_deg.text = autoRanger.rangePhase(calcs.phaseShift_deg).toEngineeringString()
        result_delay.text = autoRanger.rangeTime(calcs.delay_s).toEngineeringString()
        result_elen.text = autoRanger.rangeWavelengths(calcs.num_wavelens).toEngineeringString()

        // speed --------------------------------------------------------------------
        val encoded_speed_m_s = EngineeringNotationTools.encodeMantissa(calcs.velocity_m_s, DEFAULT_NUM_DP)
        var result = encoded_speed_m_s.mantissaString + " " + encoded_speed_m_s.exponentString + "m/s"
        result_speed_m_s.text = result
        val encoded_speed_mi_s = EngineeringNotationTools.encodeMantissa(calcs.velocity_mi_s, DEFAULT_NUM_DP)
        result = encoded_speed_mi_s.mantissaString + " " + encoded_speed_m_s.exponentString + "mi/s"
        result_speed_mi_s.text = result
        val encoded_speed_s_m = EngineeringNotationTools.encodeMantissa(calcs.velocity_s_m, DEFAULT_NUM_DP)
        result = encoded_speed_s_m.mantissaString + " " + encoded_speed_s_m.exponentString + "s/m"
        result_speed_s_m.text = result
        val encoded_speed_s_in = EngineeringNotationTools.encodeMantissa(calcs.velocity_s_in, DEFAULT_NUM_DP)
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
        result = autoRanger.rangeLength(calcs.phase_slope_m_deg).toEngineeringString() + "/deg"
        result_slope_m_deg.text = result
        result = autoRanger.rangeLengthImperial(calcs.phase_slope_m_deg).toEngineeringString() + "/deg"
        result_slope_ft_deg.text = result
        val encodedSlope_deg_m = EngineeringNotationTools.encodeMantissa(1 / calcs.phase_slope_m_deg, DEFAULT_NUM_DP)
        result = encodedSlope_deg_m.mantissaString + " " + encodedSlope_deg_m.exponentString + "deg/m"
        result_slope_deg_m.text = result
        val encodedSlope_deg_ft = EngineeringNotationTools.encodeMantissa(1 / calcs.phase_slope_ft_deg, DEFAULT_NUM_DP)
        result = encodedSlope_deg_ft.mantissaString + " " + encodedSlope_deg_ft.exponentString + "deg/ft"
        result_slope_deg_ft.text = result
        val encodedSlope_deg_hz = EngineeringNotationTools.encodeMantissa(calcs.phase_slope_deg_hz, DEFAULT_NUM_DP)
        result = encodedSlope_deg_hz.mantissaString + " " + encodedSlope_deg_hz.exponentString + "deg/Hz"
        result_slope_deg_hz.text = result
        val encodedSlope_hz_deg = EngineeringNotationTools.encodeMantissa(1 / calcs.phase_slope_deg_hz, DEFAULT_NUM_DP)
        result = encodedSlope_hz_deg.mantissaString + " " + encodedSlope_hz_deg.exponentString + "Hz/deg"
        result_slope_hz_deg.text = result

        // epsilon ------------------------------------------------------------------
        main_et_vf.setText(String.format(Locale.US, "%.${calcNumDps(calcs.velocityFactor)}f", calcs.velocityFactor))
        main_et_er.setText(String.format(Locale.US, "%.${calcNumDps(calcs.epsilon_r)}f", calcs.epsilon_r))

        // VSWR ripple spacing -------------------------------------------------------
        val encoded_ripple_hz = EngineeringNotationTools.encodeMantissa(calcs.vswr_ripple_spacing_hz, DEFAULT_NUM_DP)
        result = encoded_ripple_hz.mantissaString + " " + encoded_ripple_hz.exponentString + "Hz spacing"
        result_vswr_ripple.text = result
    }

    fun calcNumDps(value: Double): Int {
        return when {
            value < 10 -> {
                3
            }
            value < 100 -> {
                2
            }
            value < 1000 -> {
                1
            }
            else -> {
                0
            }
        }
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
        Log.d("MainActivity", "editTextPostFix: ${et.id}")
        hideKeyboard(et)
        et.clearFocus()
        if (et.text.toString().isEmpty()) {
            et.setToDefault()
        }
        refreshDisplay()
    }

    private fun freqSpinnerChanged(id: Long) {
        Log.d(TAG, "freqSpinnerChanged: index = $id")
        refreshDisplay()
    }

    private fun lengthSpinnerChanged(id: Long) {
        Log.d(TAG, "lengthSpinnerChanged: index = $id")
        refreshDisplay()
    }

    private fun hideKeyboard(view: View) {
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
            R.id.mn_main_help -> {
                DialogUtils(this).showHelp()
                return true
            }
            R.id.mn_preset -> {
                setEntryToState(loadPresetEntryState())
                return true
            }
            R.id.mn_save -> {
                storePresetEntryState(capturePresentState())
                return true
            }
            R.id.mn_initialize -> {
                setEntryToState(EntryState.getDefault())
            }
            R.id.mn_main_about -> {
                DialogUtils(this).showAbout()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun capturePresentState(): EntryState {
        val newState = EntryState()
        newState.freqString = main_et_freq.text.toString()
        newState.lengthString = main_et_length.text.toString()
        newState.lengthSpinnerIndex = length_unit_spinner.getSelectedItemPosition()
        newState.freqUnitsSpinnerIndex = freq_units_spinner.getSelectedItemPosition()
        newState.velocityFactorString = main_et_vf.text.toString()
        return newState
    }

    private fun setEntryToState(entryState: EntryState) {
        main_et_freq.setText(entryState.freqString)
        main_et_length.setText(entryState.lengthString)
        main_et_vf.setText(entryState.velocityFactorString)
        epsilonIsMaster = false // use velocityFactor
        main_et_er.setText("0.4") // set to something
        freq_units_spinner.setSelection(entryState.freqUnitsSpinnerIndex)
        length_unit_spinner.setSelection(entryState.lengthSpinnerIndex)
        refreshDisplay()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode == $requestCode")
        if (requestCode == RESULT_CODE_TLINE_VF) {
            if (resultCode == Activity.RESULT_OK) {
                val newVf = data!!.getDoubleExtra("VF", 1.0)
                main_et_vf.setText(newVf.toString())
                epsilonIsMaster = false
                refreshDisplay()
            }
        }
    }

    private fun enableAdvertisements() {
        // add physical device(s) as Test Devices for development (not release)
        if (BuildConfig.DEBUG) {
            val config = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf("28B5489C26A46FD308BFB094FC7F36D8"))
                    .build()
            MobileAds.setRequestConfiguration(config)

        }

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        Log.d("MainActivity", "onCreate: ad id = ${adView.adUnitId}")
    }
}