package com.sailinghawklabs.lambdaking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sailinghawklabs.engineeringnotation.EngineeringNotationTools;
import com.sailinghawklabs.lambdaking.preferences.SettingsPrefActivity;
import com.sailinghawklabs.lambdaking.tlines.SelectTlineActivity;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {
    public static int RESULT_CODE_TLINE_VF = 123;

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.main_et_freq) EditTextWithClear mFreq_et;
    @BindView(R.id.main_et_length) EditTextWithClear mLength_et;
    @BindView(R.id.main_et_vf) EditTextWithClear mVf_et;
    @BindView(R.id.freq_units_spinner) Spinner mFreqUnits_spinner;
    @BindView(R.id.length_unit_spinner) Spinner mLengthUnits_spinner;

    @BindView(R.id.result_phase_shift_deg) TextView tv_result_phase_shift_deg;
    @BindView(R.id.result_delay) TextView tv_result_delay_s;
    @BindView(R.id.result_elen) TextView tv_result_elen;
    @BindView(R.id.result_epsilon) TextView tv_result_epsilon;

    @BindView(R.id.result_speed_m_s) TextView tv_result_speed_m_s;
    @BindView(R.id.result_speed_mi_s) TextView tv_result_speed_mi_s;
    @BindView(R.id.result_speed_s_m) TextView tv_result_speed_s_m;
    @BindView(R.id.result_speed_s_mi) TextView tv_result_speed_s_in;

    @BindView(R.id.result_lambda_m) TextView tv_result_lambda_m;
    @BindView(R.id.result_lambda_ft) TextView tv_result_lambda_ft;
    @BindView(R.id.result_lambda_2_m) TextView tv_result_lambda_2_m;
    @BindView(R.id.result_lambda_2_ft) TextView tv_result_lambda_2_ft;
    @BindView(R.id.result_lambda_4_m) TextView tv_result_lambda_4_m;
    @BindView(R.id.result_lambda_4_ft) TextView tv_result_lambda_4_ft;
    @BindView(R.id.result_slope_m_deg) TextView tv_result_slope_m_deg;
    @BindView(R.id.result_slope_ft_deg) TextView tv_result_slope_ft_deg;
    @BindView(R.id.result_slope_deg_m) TextView tv_result_slope_deg_m;
    @BindView(R.id.result_slope_deg_in) TextView tv_result_slope_deg_ft;
    @BindView(R.id.result_slope_deg_hz) TextView tv_result_slope_deg_hz;
    @BindView(R.id.result_slope_hz_deg) TextView tv_result_slope_hz_deg;
    @BindView(R.id.result_vswr_ripple) TextView tv_result_vswr_spacing_hz;

    @BindView(R.id.main_tl_list) TextView tv_tranLine_list_button;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private AdView mAdView;

    LinkedHashMap<Double, String> freqUnits;
    LinkedHashMapAdapter<Double, String> freqSpinnerAdapter;
    LinkedHashMap<Double, String> lengthUnits;
    LinkedHashMapAdapter<Double, String> lengthSpinnerAdapter;

    // main values that are entered:
    double frequency_Hz;
    double cableLength_m;
    double velocityFactor;

    int mNumDigits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: entered");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        loadPreferences();

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(getString(R.string.app_name));
        }

        Double value1 = Double.parseDouble("77.00");
        Double value2 = Double.parseDouble(" 77");

        populateFreqUnits();
        populateLengthUnits();

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mFreq_et.setDefaultValue("1.0");
        mLength_et.setDefaultValue("1.0");
        mVf_et.setDefaultValue("1.0");

        mFreq_et.setOnEditorActionListener(myEditorChangeListener);
        mLength_et.setOnEditorActionListener(myEditorChangeListener);
        mVf_et.setOnEditorActionListener(myEditorChangeListener);

        mFreq_et.setOnFocusChangeListener(myEditTextOnFocusChangeListener);
        mLength_et.setOnFocusChangeListener(myEditTextOnFocusChangeListener);
        mVf_et.setOnFocusChangeListener(myEditTextOnFocusChangeListener);

        mFreqUnits_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                freqSpinnerChanged(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected: freq spinner");
            }
        });
        mLengthUnits_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                lengthSpinnerChanged(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @OnClick(R.id.main_tl_list)
    void callTransmissionLinePicker() {
        Intent intent = new Intent(this, SelectTlineActivity.class);
        startActivityForResult(intent, RESULT_CODE_TLINE_VF);
    }


    private void populateFreqUnits() {
        freqUnits = new LinkedHashMap<Double, String>();

        // populate map with value multipliers to yield Hz
        freqUnits.put( 1e15, "PHz");
        freqUnits.put(1e12, "THz");
        freqUnits.put(1e9, "GHz") ;
        freqUnits.put(1e6, "MHz");
        freqUnits.put(1e3, "kHz");
        freqUnits.put(1.0, "Hz");
        freqUnits.put(1e-3, "mHz");
        freqUnits.put(1e-6, "uHz");

        freqSpinnerAdapter = new LinkedHashMapAdapter<Double, String>(this, android.R.layout.simple_spinner_item, freqUnits);
        freqSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFreqUnits_spinner.setAdapter(freqSpinnerAdapter);

        mFreqUnits_spinner.setSelection(3); // TODO: find a more elegant way to do this

    }

    private void populateLengthUnits() {
        lengthUnits = new LinkedHashMap<Double, String>();

        // populate map with value multipliers to yield m
        lengthUnits.put(1e3, "km");
        lengthUnits.put(1.0, "m");
        lengthUnits.put(1e-1, "cm");
        lengthUnits.put(1e-3, "mm");
        lengthUnits.put(1/(1000 * 12 * PhysicalConstants.FEET_PER_METER_ft), "mil");
        lengthUnits.put(1/(12 * PhysicalConstants.FEET_PER_METER_ft), "in");
        lengthUnits.put(1/PhysicalConstants.FEET_PER_METER_ft, "ft");

        lengthSpinnerAdapter = new LinkedHashMapAdapter<Double, String>(this, android.R.layout.simple_spinner_item, lengthUnits);
        lengthSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLengthUnits_spinner.setAdapter(lengthSpinnerAdapter);
        mLengthUnits_spinner.setSelection(1);
    }

    private boolean isEntryValid(EditText et, String fieldName) {
        if (et.getText().toString().isEmpty()) {
            Log.d(TAG, "checkEntryValidity: ERROR - " + fieldName + " is empty");
            return false;
        }

        if (Double.valueOf(et.getText().toString()) <= 0) {
            Log.d(TAG, "isEntryBad: ERROR - " + fieldName + " is <= 0");
            return false;
        }
        return true;
    }

    private boolean checkAllEntries() {
        return isEntryValid(mFreq_et, "Frequency")
                && isEntryValid(mLength_et, "Length")
                && isEntryValid(mVf_et, "Velocity");

    }

    private boolean readEditTexts() {
        if (! checkAllEntries())
            return false;

        // parse the entry EditTexts, since they should be OK to read now without error
        frequency_Hz = Double.valueOf(mFreq_et.getText().toString());
        cableLength_m = Double.valueOf(mLength_et.getText().toString());
        velocityFactor = Double.valueOf(mVf_et.getText().toString());

        // apply suffiz multipliers fron the Spinners
        int position =  mFreqUnits_spinner.getSelectedItemPosition();
        Map.Entry<Double, String> freqMultEntry = freqSpinnerAdapter.getItem(position);
        Double freqMultiplier = freqMultEntry.getKey();
        frequency_Hz *= freqMultiplier;

        position = mLengthUnits_spinner.getSelectedItemPosition();
        Map.Entry<Double, String> lengthMultEntry = lengthSpinnerAdapter.getItem(position);
        Double lengthMultiplier = lengthMultEntry.getKey();
        cableLength_m *= lengthMultiplier;
        Log.d(TAG, "calculateResults: frequency= " + frequency_Hz + "Hz, length="  + cableLength_m + "m, Vf = " + velocityFactor);
        return true;

    }

    private void calculateResults() {
        if (! readEditTexts())
            return;


        AutoRanger autoRanger = new AutoRanger(mNumDigits);


        // auto-units: *s, *m, ft|in|mi|mil|yd,

        // (1) Velocity = F * Lambda  [m/s]
        // (2) Velocity = c * Vp [m/s]
        // (3) Lambda = c * Vp  / F  [m]    From (1) and (2)
        // (4) Lambda/2, Lambda/4 [m]       From (3)
        // (5) PhaseShift = 360 * CableLength / Lambda  [m]

        // calculate some basic intermediate results
        Double velocity_m_s = PhysicalConstants.SPEED_OF_LIGHT_mps * velocityFactor;
        Double velocity_mi_s = velocity_m_s /  (PhysicalConstants.FEET_PER_MILE_ft * PhysicalConstants.FEET_PER_METER_ft);
        Double velocity_s_m = 1/velocity_m_s;
        Double velocity_s_in =  velocity_s_m / (12 * PhysicalConstants.FEET_PER_METER_ft);
        Double lambda_m = velocity_m_s / frequency_Hz;
        Double phaseShift_deg = 360.0 * cableLength_m / lambda_m ;
        Double delay_s = cableLength_m / velocity_m_s;

        //   wavelen_m = CL/2
        //   f*wavelen_m = velocity_m_s
        //   f = 2 * velocity_m_s / CL;

        //  https://www.microwaves101.com/encyclopedias/cable-length-rule-of-thumb
        Double vswr_ripple_spacing_hz = velocity_m_s / (2.0 * cableLength_m);



        Double num_wavelens = cableLength_m / lambda_m;
        Double phase_slope_m_deg = cableLength_m / phaseShift_deg;
        Double phase_slope_ft_deg = phase_slope_m_deg * PhysicalConstants.FEET_PER_METER_ft;
        Double phase_slope_deg_hz = phaseShift_deg / frequency_Hz;
        Double epsilon_r = 1 / (velocityFactor * velocityFactor);


        tv_result_phase_shift_deg.setText(autoRanger.rangePhase(phaseShift_deg).toEngineeringString());
        tv_result_delay_s.setText(autoRanger.rangeTime(delay_s).toEngineeringString());
        tv_result_elen.setText(autoRanger.rangeWavelengths(num_wavelens).toEngineeringString());


        // speed --------------------------------------------------------------------
        EngineeringNotationTools.MantissaExponent encoded_speed_m_s = EngineeringNotationTools.encodeMantissa(velocity_m_s, mNumDigits);
        String result = encoded_speed_m_s.mantissaString + " " + encoded_speed_m_s.exponentString + "m/s";
        tv_result_speed_m_s.setText(result);

        EngineeringNotationTools.MantissaExponent encoded_speed_mi_s = EngineeringNotationTools.encodeMantissa(velocity_mi_s, mNumDigits);
        result = encoded_speed_mi_s.mantissaString + " " + encoded_speed_m_s.exponentString + "mi/s";
        tv_result_speed_mi_s.setText(result);

        EngineeringNotationTools.MantissaExponent encoded_speed_s_m = EngineeringNotationTools.encodeMantissa(velocity_s_m, mNumDigits);
        result = encoded_speed_s_m.mantissaString + " " + encoded_speed_s_m.exponentString + "s/m";
        tv_result_speed_s_m.setText(result);

        EngineeringNotationTools.MantissaExponent encoded_speed_s_in = EngineeringNotationTools.encodeMantissa(velocity_s_in, mNumDigits);
        result = encoded_speed_s_in.mantissaString + " " + encoded_speed_s_in.exponentString + "s/in";
        tv_result_speed_s_in.setText(result);


        // lamdas -------------------------------------------------------------
        tv_result_lambda_m.setText(autoRanger.rangeLength(lambda_m).toEngineeringString());
        tv_result_lambda_ft.setText(autoRanger.rangeLengthImperial(lambda_m).toEngineeringString());

        tv_result_lambda_2_m.setText(autoRanger.rangeLength(lambda_m/2).toEngineeringString());
        tv_result_lambda_2_ft.setText(autoRanger.rangeLengthImperial(lambda_m/2).toEngineeringString());
        tv_result_lambda_4_m.setText(autoRanger.rangeLength(lambda_m/4).toEngineeringString());
        tv_result_lambda_4_ft.setText(autoRanger.rangeLengthImperial(lambda_m/4).toEngineeringString());

        // phase slopes --------------------------------------------------
        tv_result_slope_m_deg.setText(autoRanger.rangeLength(phase_slope_m_deg).toEngineeringString()+"/deg");
        tv_result_slope_ft_deg.setText(autoRanger.rangeLengthImperial(phase_slope_m_deg).toEngineeringString()+"/deg");

        EngineeringNotationTools.MantissaExponent encodedSlope_deg_m = EngineeringNotationTools.encodeMantissa(1/phase_slope_m_deg, mNumDigits);
        result = encodedSlope_deg_m.mantissaString + " " + encodedSlope_deg_m.exponentString + "deg/m";
        tv_result_slope_deg_m.setText(result);

        EngineeringNotationTools.MantissaExponent encodedSlope_deg_ft = EngineeringNotationTools.encodeMantissa(1/phase_slope_ft_deg, mNumDigits);
        result = encodedSlope_deg_ft.mantissaString + " " + encodedSlope_deg_ft.exponentString + "deg/ft";
        tv_result_slope_deg_ft.setText(result);

        EngineeringNotationTools.MantissaExponent encodedSlope_deg_hz = EngineeringNotationTools.encodeMantissa(phase_slope_deg_hz, mNumDigits);
        result = encodedSlope_deg_hz.mantissaString + " " + encodedSlope_deg_hz.exponentString + "deg/Hz";
        tv_result_slope_deg_hz.setText(result);

        EngineeringNotationTools.MantissaExponent encodedSlope_hz_deg = EngineeringNotationTools.encodeMantissa(1/ phase_slope_deg_hz, mNumDigits);
        result = encodedSlope_hz_deg.mantissaString + " " + encodedSlope_hz_deg.exponentString + "Hz/deg";
        tv_result_slope_hz_deg.setText(result);

        // epsilon ------------------------------------------------------------------
        tv_result_epsilon.setText(String.format(Locale.US, "%." + mNumDigits + "f (relative)", epsilon_r));

        // VSWR ripple spacing -------------------------------------------------------
        EngineeringNotationTools.MantissaExponent encoded_ripple_hz = EngineeringNotationTools.encodeMantissa(vswr_ripple_spacing_hz, mNumDigits);
        result = encoded_ripple_hz.mantissaString + " " + encoded_ripple_hz.exponentString + "Hz spacing";
        tv_result_vswr_spacing_hz.setText(result);

    }

    private TextView.OnFocusChangeListener myEditTextOnFocusChangeListener = new TextView.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (! hasFocus) {
                editTextPostFix((EditTextWithClear) v);
            }
        }
    };

    private TextView.OnEditorActionListener myEditorChangeListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                editTextPostFix((EditTextWithClear) v);
                return true;
            }
            return false;
        }
    };


    private void editTextPostFix(EditTextWithClear et) {
        hideKeyboard(et);
        et.clearFocus();
        if (et.getText().toString().isEmpty()) {
            et.setToDefault();
        }
        calculateResults();
    }

    private void freqSpinnerChanged(long id) {
        Log.d(TAG, "freqSpinnerChanged: index = " + id);
        calculateResults();
    }

    private void lengthSpinnerChanged(long id) {
        Log.d(TAG, "lengthSpinnerChanged: index = " + id);
        calculateResults();
    }

    void hideKeyboard(View view)    {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: menu = " + menu.toString());
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: item = " + item.getTitle().toString());

        switch (item.getItemId()) {
            case R.id.mn_main_settings:
                startActivity(new Intent(MainActivity.this, SettingsPrefActivity.class));
                return true;

            case R.id.mn_main_help:
                // startActivity(new Intent(MainActivity.this, HelpActivity.class));
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String numDigitsString = prefs.getString(getString(R.string.pref_key_digits), getString(R.string.pref_def_digits));
        mNumDigits = Integer.parseInt(numDigitsString);
        Log.d(TAG, "loadPreferences: nNumDigits set to : " + mNumDigits);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: entered");
        loadPreferences();
        calculateResults();
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode == " + requestCode);

        if (requestCode == RESULT_CODE_TLINE_VF) {
            if (resultCode == Activity.RESULT_OK) {
                double newVf = data.getDoubleExtra("VF", 1.0);
                mVf_et.setText(String.valueOf(newVf));
                calculateResults();
            }
        }
    }
}
