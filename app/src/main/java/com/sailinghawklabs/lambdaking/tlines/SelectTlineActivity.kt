package com.sailinghawklabs.lambdaking.tlines

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sailinghawklabs.lambdaking.R
import com.sailinghawklabs.lambdaking.databinding.ActivitySelectTlineBinding
import com.sailinghawklabs.lambdaking.databinding.TlineItemBinding
import com.sailinghawklabs.lambdaking.tlines.TransLineAdapter.TlineSelected


class SelectTlineActivity : AppCompatActivity(), TlineSelected {

    private lateinit var mAdapter: RecyclerView.Adapter<TransLineAdapter.ViewHolder>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mTlineData: List<TransmissionLine>

    lateinit var activityBinding: ActivitySelectTlineBinding
    lateinit var itemBinding: TlineItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: entered")
        super.onCreate(savedInstanceState)
        activityBinding = ActivitySelectTlineBinding.inflate(layoutInflater)
        itemBinding = activityBinding.selectTlineItem
        setContentView(activityBinding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        registerGoBack()

        itemBinding.tlineVf.text = getString(R.string.velocity_factor_hint)
        itemBinding.tlineVf.typeface = Typeface.DEFAULT_BOLD
        itemBinding.tlineDescr.text = getString(R.string.description)
        itemBinding.tlineDescr.typeface = Typeface.DEFAULT_BOLD
        activityBinding.activityTlineRv.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        activityBinding.activityTlineRv.layoutManager = mLayoutManager
        readTlineFile()
        mAdapter = TransLineAdapter(mTlineData, this)
        activityBinding.activityTlineRv.adapter = mAdapter
    }

    fun readTlineFile() {
        val inputStream = resources.openRawResource(R.raw.transmission_lines)
        mTlineData = TransLineFile.read(inputStream)
        Log.d(
            TAG,
            "readTlineFile: num entries = " + (mTlineData as MutableList<TransmissionLine>?)!!.size
        )
    }

    override fun tlineSelected(tline: TransmissionLine) {
        val vf = tline.velocityFactor
        val returnIntent = Intent()
        returnIntent.putExtra("VF", vf)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun goBack() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    private fun registerGoBack() {
        onBackPressedDispatcher.addCallback(this) {
            goBack()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> goBack()
        }
        return true
    }

    companion object {
        private val TAG = SelectTlineActivity::class.java.simpleName
    }
}