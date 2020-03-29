package com.kodeholic.itbook.ui.base;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kodeholic.itbook.R;

public class LoadingViewHolder extends RecyclerView.ViewHolder {
    public ProgressBar pb_loading;
    public View        rowView;

    public LoadingViewHolder(View itemView) {
        super(itemView);

        rowView    = itemView;
        pb_loading = itemView.findViewById(R.id.pb_loading);
        pb_loading.setVisibility(View.GONE);
    }

    public void showLoading() {
        pb_loading.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        pb_loading.setVisibility(View.GONE);
    }
}
