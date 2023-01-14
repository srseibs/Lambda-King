package com.sailinghawklabs.lambdaking.tlines

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sailinghawklabs.lambdaking.databinding.TlineItemBinding

class TransLineAdapter(private val mTransmissionLines: List<TransmissionLine>, var mCallback: TlineSelected)
    : RecyclerView.Adapter<TransLineAdapter.ViewHolder>() {

    interface TlineSelected {
        fun tlineSelected(tline: TransmissionLine)
    }

    inner class ViewHolder(val binding:TlineItemBinding ) : RecyclerView.ViewHolder(binding.root) {

        fun populate(position: Int) {
            val tline = mTransmissionLines[position]
            binding.tlineVf.text = tline.velocityFactorString
            binding.tlineDescr.text = tline.description
        }

        init {
            itemView.setOnClickListener {
                val transmissionLine = mTransmissionLines[bindingAdapterPosition]
                mCallback.tlineSelected(transmissionLine)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TlineItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.populate(position)
    }

    override fun getItemCount(): Int {
        return mTransmissionLines.size
    }
}