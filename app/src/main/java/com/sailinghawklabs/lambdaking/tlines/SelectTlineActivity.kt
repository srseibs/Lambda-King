package com.sailinghawklabs.lambdaking.tlines

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sailinghawklabs.lambdaking.R
import com.sailinghawklabs.lambdaking.tlines.TransLineAdapter.TlineSelected
import kotlinx.android.synthetic.main.activity_select_tline.*
import kotlinx.android.synthetic.main.tline_item.*


class SelectTlineActivity : AppCompatActivity(), TlineSelected {

    private lateinit var mAdapter: RecyclerView.Adapter<TransLineAdapter.ViewHolder>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mTlineData: List<TransmissionLine>

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: entered")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_tline)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        tline_vf.text = "Vf"
        tline_vf.typeface = Typeface.DEFAULT_BOLD
        tline_descr.text = "Description"
        tline_descr.typeface = Typeface.DEFAULT_BOLD
        activity_tline_rv.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        activity_tline_rv.layoutManager = mLayoutManager
        readTlineFile()
        mAdapter = TransLineAdapter(mTlineData, this)
        activity_tline_rv.adapter = mAdapter
    }

    fun readTlineFile() {
        val inputStream = resources.openRawResource(R.raw.transmission_lines)
        mTlineData = TransLineFile.read(inputStream)
        Log.d(TAG, "readTlineFile: num entries = " + (mTlineData as MutableList<TransmissionLine>?)!!.size)
    }

    override fun tlineSelected(tline: TransmissionLine) {
        val vf = tline.velocityFactor
        val returnIntent = Intent()
        returnIntent.putExtra("VF", vf)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: entered")
        super.onBackPressed()
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    companion object {
        private val TAG = SelectTlineActivity::class.java.simpleName
    }
}