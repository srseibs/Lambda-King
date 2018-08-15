package com.sailinghawklabs.lambdaking.tlines;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.sailinghawklabs.lambdaking.R;

import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectTlineActivity extends AppCompatActivity implements TransLineAdapter.TlineSelected {
    private static final String TAG = SelectTlineActivity.class.getSimpleName();
    @BindView(R.id.activity_tline_rv) RecyclerView mRecyclerView;
    @BindView(R.id.tline_vf) TextView mTitle_Vf;
    @BindView(R.id.tline_descr) TextView mTitle_descr;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    List<TransmissionLine> mTlineData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: entered");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_tline);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitle_Vf.setText("Vf");
        mTitle_Vf.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle_descr.setText("Description");
        mTitle_descr.setTypeface(Typeface.DEFAULT_BOLD);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        readTlineFile();
        mAdapter = new TransLineAdapter(mTlineData, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    void readTlineFile() {
        InputStream inputStream = getResources().openRawResource(R.raw.transmission_lines);
        mTlineData = TransLineFile.read(inputStream);
        Log.d(TAG, "readTlineFile: num entries = " + mTlineData.size());
    }

    @Override
    public void tlineSelected(TransmissionLine tline) {
        double vf =  tline.getVelocityFactor();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("VF", vf);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: entered");
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}
