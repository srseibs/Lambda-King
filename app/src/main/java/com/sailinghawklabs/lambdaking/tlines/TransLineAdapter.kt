package com.sailinghawklabs.lambdaking.tlines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sailinghawklabs.lambdaking.R
import kotlinx.android.synthetic.main.tline_item.view.*


class TransLineAdapter(private val mTransmissionLines: List<TransmissionLine>, var mCallback: TlineSelected)
    : RecyclerView.Adapter<TransLineAdapter.ViewHolder>() {

    interface TlineSelected {
        fun tlineSelected(tline: TransmissionLine)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun populate(position: Int) {
            val tline = mTransmissionLines[position]
            itemView.tline_vf.text = tline.velocityFactorString
            itemView.tline_descr.text = tline.description
        }

        init {
            itemView.setOnClickListener {
                val transmissionLine = mTransmissionLines[adapterPosition]
                mCallback.tlineSelected(transmissionLine)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.tline_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.populate(position)
    }

    override fun getItemCount(): Int {
        return mTransmissionLines.size
    }

}