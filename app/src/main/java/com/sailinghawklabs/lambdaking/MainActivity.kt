package com.sailinghawklabs.lambdaking

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import com.sailinghawklabs.lambdaking.databinding.ActivityMainBinding
import com.sailinghawklabs.lambdaking.databinding.IncludeDataEntryBinding
import com.sailinghawklabs.lambdaking.databinding.IncludeGridResultsBinding
import com.sailinghawklabs.lambdaking.entities.*
import com.sailinghawklabs.lambdaking.tlines.SelectTlineActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val RESULT_CODE_TLINE_VF = 123
        private const val DEFAULT_NUM_DP = 2
        private const val ER_VF_NUM_DP = 3
        private const val PREF_KEY_PRESET_ENTRY = "preset_key"
        private val TAG = MainActivity::class.java.simpleName
        private lateinit var presetEntryState: EntryState
    }

    private lateinit var freqSpinnerAdapter: LinkedHashMapAdapter<Double, String>
    private lateinit var lengthSpinnerAdapter: LinkedHashMapAdapter<Double, String>

    private var epsilonIsMaster = true

    lateinit var mainBinding: ActivityMainBinding
    lateinit var dataBinding: IncludeDataEntryBinding
    lateinit var resultBinding: IncludeGridResultsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: entered")
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        dataBinding = mainBinding.dataEntry
        resultBinding = mainBinding.resultGrid
        setContentView(mainBinding.root)
        setSupportActionBar(mainBinding.includedToolbar.toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        initializeViews()
        enableAdvertisements()

    }

    private fun initializeViews() {
        dataBinding.mainEtFreq.setOnEditorActionListener(myEditorChangeListener)
        dataBinding.mainEtFreq.onFocusChangeListener = myEditTextOnFocusChangeListener

        dataBinding.mainEtLength.setOnEditorActionListener(myEditorChangeListener)
        dataBinding.mainEtLength.onFocusChangeListener = myEditTextOnFocusChangeListener

        dataBinding.mainEtVf.setOnEditorActionListener(myEditorChangeListener)
        dataBinding.mainEtVf.onFocusChangeListener = myEditTextOnFocusChangeListener

        dataBinding.mainEtEr.setOnEditorActionListener(myEditorChangeListener)
        dataBinding.mainEtEr.onFocusChangeListener = myEditTextOnFocusChangeListener
        dataBinding.mainEtEr.setText("1")
        dataBinding.mainTvErLabel.text = Characters.EPSILON_SUB_R

        dataBinding.freqUnitsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                freqSpinnerChanged(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d(TAG, "onNothingSelected: freq spinner")
            }
        }
        dataBinding.lengthUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                lengthSpinnerChanged(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        dataBinding.mainTlList.setOnClickListener {
            val intent = Intent(this, SelectTlineActivity::class.java)
            startActivityForResult(intent, RESULT_CODE_TLINE_VF)
        }

        freqSpinnerAdapter = LinkedHashMapAdapter(this, android.R.layout.simple_spinner_item, FreqUnits)
        freqSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dataBinding.freqUnitsSpinner.adapter = freqSpinnerAdapter

        lengthSpinnerAdapter = LinkedHashMapAdapter(this, android.R.layout.simple_spinner_item, LengthUnits)
        lengthSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dataBinding.lengthUnitSpinner.adapter = lengthSpinnerAdapter

        presetEntryState = loadPresetEntryState()
        setEntryToState(presetEntryState)
        dataBinding.mainEtEr.setAsDefaultValue()
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
        if (!isEntryValid(dataBinding.mainEtFreq, "Frequency")) {
            return false
        }
        if (!isEntryValid(dataBinding.mainEtLength, "Length")) {
            return false
        }
        return if (epsilonIsMaster) {
            isEntryValid(dataBinding.mainEtEr, Characters.EPSILON_SUB_R.toString())
        } else {
            isEntryValid(dataBinding.mainEtVf, "Velocity")
        }
    }

    private fun readEditTexts(): NumericEntryData {
        Log.d("MainActivity", "readEditTexts: epsilonIsMaster=${epsilonIsMaster}")
        val numericEntryData = NumericEntryData(false)

        if (!isEntryValid(dataBinding.mainEtFreq, "Frequency")) {
            dataBinding.mainEtFreq.setText(presetEntryState.freqString)
            hideKeyboard(dataBinding.mainEtFreq)
            dataBinding.mainEtFreq.clearFocus()
        }
        if (!isEntryValid(dataBinding.mainEtLength, "Length")) {
            dataBinding.mainEtLength.setText(presetEntryState.lengthString)
            hideKeyboard(dataBinding.mainEtLength)
            dataBinding.mainEtLength.clearFocus()
        }
        numericEntryData.frequency_Hz = (dataBinding.mainEtFreq.text.toString()).toDouble()
        numericEntryData.cableLength_m = (dataBinding.mainEtLength.text.toString()).toDouble()

        // apply suffix multipliers from the Spinners
        var position = dataBinding.freqUnitsSpinner.selectedItemPosition
        val freqMultEntry = freqSpinnerAdapter.getItem(position)
                ?: throw Exception("MainActivity: readEditTexts: missing freq spinner entry position: $position")
        val freqMultiplier = freqMultEntry.key
        numericEntryData.frequency_Hz *= freqMultiplier

        position = dataBinding.lengthUnitSpinner.selectedItemPosition
        val lengthMultEntry = lengthSpinnerAdapter.getItem(position)
                ?: throw Exception("MainActivity: readEditTexts: missing length spinner entry position: $position")
        val lengthMultiplier = lengthMultEntry.key
        numericEntryData.cableLength_m *= lengthMultiplier
        Log.d(TAG, "calculateResults: frequency= " + numericEntryData.frequency_Hz + "Hz, length="
                + numericEntryData.cableLength_m + "m, Vf = " + numericEntryData.velocityFactor)

        if (!dataBinding.mainEtVf.text.isNullOrEmpty()) {
            numericEntryData.velocityFactor = (Math.max(dataBinding.mainEtVf.text.toString().toDouble(), 0.001))
        }

        if (!dataBinding.mainEtEr.text.isNullOrEmpty()) {
            numericEntryData.epsilon = (Math.max(dataBinding.mainEtEr.text.toString().toDouble(), 0.001))
            numericEntryData.epsilon = (Math.min(numericEntryData.epsilon, 100000.00))
        }

        numericEntryData.valid = true
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
        resultBinding.resultPhaseShiftDeg.text = autoRanger.rangePhase(calcs.phaseShift_deg).toEngineeringString()
        resultBinding.resultDelay.text = autoRanger.rangeTime(calcs.delay_s).toEngineeringString()
        resultBinding.resultElen.text = autoRanger.rangeWavelengths(calcs.num_wavelens).toEngineeringString()

        // speed --------------------------------------------------------------------
        val encoded_speed_m_s = EngineeringNotationTools.encodeMantissa(calcs.velocity_m_s, DEFAULT_NUM_DP)
        var result = encoded_speed_m_s.mantissaString + " " + encoded_speed_m_s.exponentString + "m/s"
        resultBinding.resultSpeedMS.text = result
        val encoded_speed_mi_s = EngineeringNotationTools.encodeMantissa(calcs.velocity_mi_s, DEFAULT_NUM_DP)
        result = encoded_speed_mi_s.mantissaString + " " + encoded_speed_m_s.exponentString + "mi/s"
        resultBinding.resultSpeedMiS.text = result
        val encoded_speed_s_m = EngineeringNotationTools.encodeMantissa(calcs.velocity_s_m, DEFAULT_NUM_DP)
        result = encoded_speed_s_m.mantissaString + " " + encoded_speed_s_m.exponentString + "s/m"
        resultBinding.resultSpeedSM.text = result
        val encoded_speed_s_in = EngineeringNotationTools.encodeMantissa(calcs.velocity_s_in, DEFAULT_NUM_DP)
        result = encoded_speed_s_in.mantissaString + " " + encoded_speed_s_in.exponentString + "s/in"
        resultBinding.resultSpeedSIn.text = result

        // lamdas -------------------------------------------------------------
        resultBinding.resultLambdaM.text = autoRanger.rangeLength(calcs.lambda_m).toEngineeringString()
        resultBinding.resultLambdaFt.text = autoRanger.rangeLengthImperial(calcs.lambda_m).toEngineeringString()
        resultBinding.resultLambda2M.text = autoRanger.rangeLength(calcs.lambda_m / 2).toEngineeringString()
        resultBinding.resultLambdaFt.text = autoRanger.rangeLengthImperial(calcs.lambda_m / 2).toEngineeringString()
        resultBinding.resultLambda4M.text = autoRanger.rangeLength(calcs.lambda_m / 4).toEngineeringString()
        resultBinding.resultLambda4Ft.text = autoRanger.rangeLengthImperial(calcs.lambda_m / 4).toEngineeringString()

        // phase slopes --------------------------------------------------
        result = autoRanger.rangeLength(calcs.phase_slope_m_deg).toEngineeringString() + "/deg"
        resultBinding.resultSlopeMDeg.text = result
        result = autoRanger.rangeLengthImperial(calcs.phase_slope_m_deg).toEngineeringString() + "/deg"
        resultBinding.resultSlopeFtDeg.text = result
        val encodedSlope_deg_m = EngineeringNotationTools.encodeMantissa(1 / calcs.phase_slope_m_deg, DEFAULT_NUM_DP)
        result = encodedSlope_deg_m.mantissaString + " " + encodedSlope_deg_m.exponentString + "deg/m"
        resultBinding.resultSlopeDegM.text = result
        val encodedSlope_deg_ft = EngineeringNotationTools.encodeMantissa(1 / calcs.phase_slope_ft_deg, DEFAULT_NUM_DP)
        result = encodedSlope_deg_ft.mantissaString + " " + encodedSlope_deg_ft.exponentString + "deg/ft"
        resultBinding.resultSlopeDegFt.text = result
        val encodedSlope_deg_hz = EngineeringNotationTools.encodeMantissa(calcs.phase_slope_deg_hz, DEFAULT_NUM_DP)
        result = encodedSlope_deg_hz.mantissaString + " " + encodedSlope_deg_hz.exponentString + "deg/Hz"
        resultBinding.resultSlopeDegHz.text = result
        val encodedSlope_hz_deg = EngineeringNotationTools.encodeMantissa(1 / calcs.phase_slope_deg_hz, DEFAULT_NUM_DP)
        result = encodedSlope_hz_deg.mantissaString + " " + encodedSlope_hz_deg.exponentString + "Hz/deg"
        resultBinding.resultSlopeHzDeg.text = result

        // epsilon ------------------------------------------------------------------
        dataBinding.mainEtVf.setText(String.format(Locale.US, "%.${calcNumDps(calcs.velocityFactor)}f", calcs.velocityFactor))
        dataBinding.mainEtEr.setText(String.format(Locale.US, "%.${calcNumDps(calcs.epsilon_r)}f", calcs.epsilon_r))

        // VSWR ripple spacing -------------------------------------------------------
        val encoded_ripple_hz = EngineeringNotationTools.encodeMantissa(calcs.vswr_ripple_spacing_hz, DEFAULT_NUM_DP)
        result = encoded_ripple_hz.mantissaString + " " + encoded_ripple_hz.exponentString + "Hz spacing"
        resultBinding.resultVswrRipple.text = result
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
            if (v.id == dataBinding.mainEtEr.id) {
                epsilonIsMaster = true
            } else if (v.id == dataBinding.mainEtVf.id) {
                epsilonIsMaster = false
            }
            editTextPostFix(v as EditTextWithClear)
        }
    }
    private val myEditorChangeListener = TextView.OnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (v.id == dataBinding.mainEtEr.id) {
                epsilonIsMaster = true
            } else if (v.id == dataBinding.mainEtVf.id) {
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
                return true
            }
            R.id.mn_main_about -> {
                DialogUtils(this).showAbout()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun capturePresentState(): EntryState {
        val newState = EntryState()
        newState.freqString = dataBinding.mainEtFreq.text.toString()
        newState.lengthString = dataBinding.mainEtLength.text.toString()
        newState.lengthSpinnerIndex = dataBinding.lengthUnitSpinner.getSelectedItemPosition()
        newState.freqUnitsSpinnerIndex = dataBinding.freqUnitsSpinner.getSelectedItemPosition()
        newState.velocityFactorString = dataBinding.mainEtVf.text.toString()
        return newState
    }

    private fun setEntryToState(entryState: EntryState) {
        dataBinding.mainEtFreq.setText(entryState.freqString)
        dataBinding.mainEtLength.setText(entryState.lengthString)
        dataBinding.mainEtVf.setText(entryState.velocityFactorString)
        epsilonIsMaster = false // use velocityFactor
        dataBinding.mainEtEr.setText("0.4") // set to something
        dataBinding.freqUnitsSpinner.setSelection(entryState.freqUnitsSpinnerIndex)
        dataBinding.lengthUnitSpinner.setSelection(entryState.lengthSpinnerIndex)
        refreshDisplay()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode == $requestCode")
        if (requestCode == RESULT_CODE_TLINE_VF) {
            if (resultCode == Activity.RESULT_OK) {
                val newVf = data!!.getDoubleExtra("VF", 1.0)
                dataBinding.mainEtVf.setText(newVf.toString())
                hideKeyboard(dataBinding.mainEtVf)
                dataBinding.mainEtVf.clearFocus()
                dataBinding.mainEtEr.clearFocus()
                dataBinding.mainEtLength.clearFocus()
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
        mainBinding.adView.loadAd(adRequest)
        Log.d("MainActivity", "onCreate: ad id = ${mainBinding.adView.adUnitId}")
    }
}