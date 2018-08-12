package com.sailinghawklabs.lambdaking;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.main_et_freq) EditTextWithClear mFreq_et;
    @BindView(R.id.main_et_length) EditTextWithClear mLength_et;
    @BindView(R.id.main_et_vf) EditTextWithClear mVf_et;
    @BindView(R.id.freq_units_spinner) Spinner mFreqUnits_spinner;
    @BindView(R.id.length_unit_spinner) Spinner mLengthUnits_spinner;

    @BindView(R.id.result_phase_shift_deg) TextView tv_result_phase_shift_deg;
    @BindView(R.id.result_delay) TextView tv_result_delay_s;
    @BindView(R.id.result_elen) TextView tv_result_elen;
    @BindView(R.id.result_speed_m_s) TextView tv_result_speed_m_s;
    @BindView(R.id.result_speed_mi_s) TextView tv_result_speed_mi_s;

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
    @BindView(R.id.result_epsilon) TextView tv_result_epsilon;

    private AdView mAdView;

    LinkedHashMap<Double, String> freqUnits;
    LinkedHashMapAdapter<Double, String> freqSpinnerAdapter;
    LinkedHashMap<Double, String> lengthUnits;
    LinkedHashMapAdapter<Double, String> lengthSpinnerAdapter;

    // main values that are entered:
    double frequency_Hz;
    double cableLength_m;
    double velocityFactor;

    AutoRanger mAutoRanger;
    final int DEFAULT_NUM_DIGITS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mAutoRanger = new AutoRanger(DEFAULT_NUM_DIGITS);

        populateFreqUnits();
        populateLengthUnits();

        String temp = "hello";

        tv_result_phase_shift_deg.setText(temp);

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

        // auto-units: *s, *m, ft|in|mi|mil|yd,

        // (1) Velocity = F * Lambda  [m/s]
        // (2) Velocity = c * Vp [m/s]
        // (3) Lambda = c * Vp  / F  [m]    From (1) and (2)
        // (4) Lambda/2, Lambda/4 [m]       From (3)
        // (5) PhaseShift = 360 * CableLength / Lambda  [m]

        // calculate some basic intermediate results
        Double velocity_m_s = PhysicalConstants.SPEED_OF_LIGHT_mps * velocityFactor;
        Double velocity_mi_s = velocity_m_s /  (PhysicalConstants.FEET_PER_MILE_ft * PhysicalConstants.FEET_PER_METER_ft);
        Double lambda_m = velocity_m_s / frequency_Hz;
        Double phaseShift_deg = 360.0 * cableLength_m / lambda_m ;
        Double delay_s = cableLength_m / velocity_m_s;
        Double num_wavelens = cableLength_m / lambda_m;
        Double phase_slope_m_deg = cableLength_m / phaseShift_deg;
        Double phase_slope_ft_deg = phase_slope_m_deg * PhysicalConstants.FEET_PER_METER_ft;
        Double phase_slope_deg_hz = phaseShift_deg / frequency_Hz;
        Double epsilon_r = 1 / (velocityFactor * velocityFactor);


        tv_result_phase_shift_deg.setText(mAutoRanger.rangePhase(phaseShift_deg).toEngineeringString());
        tv_result_delay_s.setText(mAutoRanger.rangeTime(delay_s).toEngineeringString());
        tv_result_elen.setText(mAutoRanger.rangeWavelengths(num_wavelens).toEngineeringString());


        // speed --------------------------------------------------------------------
        EngineeringNotationTools.MantissaExponent encoded_speed_m_s = EngineeringNotationTools.encodeMantissa(velocity_m_s, DEFAULT_NUM_DIGITS);
        String result = encoded_speed_m_s.mantissaString + " " + encoded_speed_m_s.exponentString + "m/s";

        tv_result_speed_m_s.setText(result);
        tv_result_speed_mi_s.setText(String.format(Locale.US, "%." + DEFAULT_NUM_DIGITS + "f mi/s", velocity_mi_s));


        // lamdas -------------------------------------------------------------
        tv_result_lambda_m.setText(mAutoRanger.rangeLength(lambda_m).toEngineeringString());
        tv_result_lambda_ft.setText(mAutoRanger.rangeLengthImperial(lambda_m).toEngineeringString());

        tv_result_lambda_2_m.setText(mAutoRanger.rangeLength(lambda_m/2).toEngineeringString());
        tv_result_lambda_2_ft.setText(mAutoRanger.rangeLengthImperial(lambda_m/2).toEngineeringString());
        tv_result_lambda_4_m.setText(mAutoRanger.rangeLength(lambda_m/4).toEngineeringString());
        tv_result_lambda_4_ft.setText(mAutoRanger.rangeLengthImperial(lambda_m/4).toEngineeringString());

        // phase slopes --------------------------------------------------
        tv_result_slope_m_deg.setText(mAutoRanger.rangeLength(phase_slope_m_deg).toEngineeringString()+"/deg");
        tv_result_slope_ft_deg.setText(mAutoRanger.rangeLengthImperial(phase_slope_m_deg).toEngineeringString()+"/deg");

        EngineeringNotationTools.MantissaExponent encodedSlope_deg_m = EngineeringNotationTools.encodeMantissa(1/phase_slope_m_deg, DEFAULT_NUM_DIGITS);
        result = encodedSlope_deg_m.mantissaString + " " + encodedSlope_deg_m.exponentString + "deg/m";
        tv_result_slope_deg_m.setText(result);

        EngineeringNotationTools.MantissaExponent encodedSlope_deg_ft = EngineeringNotationTools.encodeMantissa(1/phase_slope_ft_deg, DEFAULT_NUM_DIGITS);
        result = encodedSlope_deg_ft.mantissaString + " " + encodedSlope_deg_ft.exponentString + "deg/ft";
        tv_result_slope_deg_ft.setText(result);

        EngineeringNotationTools.MantissaExponent encodedSlope_deg_hz = EngineeringNotationTools.encodeMantissa(phase_slope_deg_hz, DEFAULT_NUM_DIGITS);
        result = encodedSlope_deg_hz.mantissaString + " " + encodedSlope_deg_hz.exponentString + "deg/Hz";
        tv_result_slope_deg_hz.setText(result);

        EngineeringNotationTools.MantissaExponent encodedSlope_hz_deg = EngineeringNotationTools.encodeMantissa(1/ phase_slope_deg_hz, DEFAULT_NUM_DIGITS);
        result = encodedSlope_hz_deg.mantissaString + " " + encodedSlope_hz_deg.exponentString + "Hz/deg";
        tv_result_slope_hz_deg.setText(result);

        // epsilon ------------------------------------------------------------------
        tv_result_epsilon.setText(String.format(Locale.US, "%." + DEFAULT_NUM_DIGITS + "f (relative)", epsilon_r));

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

}
