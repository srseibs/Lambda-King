package com.sailinghawklabs.lambdaking.tlines;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sailinghawklabs.lambdaking.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransLineAdapter extends RecyclerView.Adapter<TransLineAdapter.ViewHolder>  {

    public interface TlineSelected {
        void tlineSelected(TransmissionLine tline);
    }
    TlineSelected mCallback;

    private List<TransmissionLine> mTransmissionLines;

    public TransLineAdapter(List<TransmissionLine> transmissionLines, TlineSelected selectionListener) {
        mTransmissionLines = transmissionLines;
        mCallback = selectionListener;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tline_vf) TextView mTextView_vf;
        @BindView(R.id.tline_descr) TextView mTextView_descr;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TransmissionLine transmissionLine = mTransmissionLines.get(getAdapterPosition());
                    mCallback.tlineSelected(transmissionLine);
                }
            });
        }

        void populate(final int position) {
            TransmissionLine tline = mTransmissionLines.get(position);
            mTextView_vf.setText(tline.getVelocityFactorString());
            mTextView_descr.setText(tline.getDescription());
        }
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.tline_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.populate(position);
    }

    @Override
    public int getItemCount() {
        return mTransmissionLines.size();
    }
}
