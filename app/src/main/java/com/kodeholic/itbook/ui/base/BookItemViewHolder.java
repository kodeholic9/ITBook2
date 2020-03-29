package com.kodeholic.itbook.ui.base;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kodeholic.itbook.R;

public class BookItemViewHolder extends RecyclerView.ViewHolder {
    public ImageView iv_image;
    public TextView  tv_title;
    public TextView  tv_subtitle;
    public TextView  tv_isbn13;
    public TextView  tv_price;
    public View      ll_link;
    public View      rowView;

    public BookItemViewHolder(View itemView) {
        super(itemView);

        rowView     = itemView;
        iv_image    = itemView.findViewById(R.id.iv_image);
        tv_title    = itemView.findViewById(R.id.tv_title);
        tv_subtitle = itemView.findViewById(R.id.tv_subtitle);
        tv_isbn13   = itemView.findViewById(R.id.tv_isbn13);
        tv_price    = itemView.findViewById(R.id.tv_price);
        ll_link     = itemView.findViewById(R.id.ll_link);
    }
}
