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

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.main_et_freq) EditText mFreq_et;
    @BindView(R.id.main_et_length) EditText mLength_et;
    @BindView(R.id.main_et_vf) EditText mVf_et;
    @BindView(R.id.freq_units_spinner) Spinner mFreqUnits_spinner;
    @BindView(R.id.length_unit_spinner) Spinner mLengthUnits_spinner;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mFreq_et.setOnEditorActionListener(myEditorChangeListener);
        mLength_et.setOnEditorActionListener(myEditorChangeListener);
        mVf_et.setOnEditorActionListener(myEditorChangeListener);

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

    private TextView.OnEditorActionListener myEditorChangeListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        }
    };

    private void freqSpinnerChanged(long id) {
        Log.d(TAG, "freqSpinnerChanged: index = " + id);
    }

    private void lengthSpinnerChanged(long id) {
        Log.d(TAG, "lengthSpinnerChanged: index = " + id);
    }

    void hideKeyboard(View view)    {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
