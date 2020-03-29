package com.kodeholic.itbook.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kodeholic.itbook.R;
import com.kodeholic.itbook.common.BookManager;
import com.kodeholic.itbook.common.BitmapCacheManager;
import com.kodeholic.itbook.common.MyIntent;
import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.base.BookItemViewHolder;

/**
 * A placeholder fragment containing a simple view.
 */
public class SectionHistoryFragment extends SectionFragment {
    public static final String TAG = SectionHistoryFragment.class.getSimpleName();

    private HistoryAdapter mAdapter;
    private RecyclerView   mListView;
    private TextView       tv_no_result;

    public static SectionHistoryFragment newInstance(SectionsPagerAdapter.TabInfo info) {
        Log.d(TAG, "newInstance() - " + info.index);
        SectionHistoryFragment fragment = new SectionHistoryFragment();
        fragment.setSectionInfo(info);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View root = inflater.inflate(R.layout.fragment_history, container, false);

        //adapter
        mAdapter = new HistoryAdapter(new BookDetail[0]);

        //list..
        mListView = root.findViewById(R.id.ll_list);
        mListView.setHasFixedSize(true);
        mListView.setAdapter(mAdapter);

        //no result
        tv_no_result = root.findViewById(R.id.tv_no_result);

        //view를 갱신한다.
        showLoading("onCreateView");
        BookManager.getInstance(mContext).loadHistory(new BookManager.HistoryListener() {
            @Override
            public void onResult(BookDetail[] results) {
                hideLoading("onCreateView");
                updateView(results, "onCreateView");
            }
        });

        return root;
    }

    @Override
    public void onEvent(int event, Object o) {
        Log.d(TAG, "onEvent() - event: " + event);
        try {
            if (event == MyIntent.Event.HISTORY_REFRESHED) {
                BookManager.getInstance(mContext).loadHistory(new BookManager.HistoryListener() {
                    @Override
                    public void onResult(BookDetail[] results) {
                        updateView(results, "HISTORY_REFRESHED");
                    }
                });
            }
            else if (event == MyIntent.Event.SORT_OPTION_CHANGED) {
                BookManager.getInstance(mContext).loadHistory(new BookManager.HistoryListener() {
                    @Override
                    public void onResult(BookDetail[] results) {
                        updateView(results, "HISTORY_REFRESHED");
                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 화면을 갱신한다.
     * @param f
     */
    private void updateView(final BookDetail[] results, String f) {
        Log.d(TAG, "updateView() - f: " + f);
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.setData(results);
                mAdapter.notifyDataSetChanged();

                if (results.length == 0) {
                    tv_no_result.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.GONE);
                }
                else {
                    tv_no_result.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public class HistoryAdapter extends RecyclerView.Adapter<BookItemViewHolder> {
        private BookDetail[] data;

        public HistoryAdapter(BookDetail[] data) {
            this.data = data;
        }

        public void setData(BookDetail[] data) {
            this.data = data;
        }

        @Override
        public BookItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_book, parent, false);
            return new BookItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final BookItemViewHolder holder, final int position) {
            final BookDetail item = data[position];
            holder.tv_title.setText(item.getTitle());
            holder.tv_subtitle.setText(item.getSubTitle());
            holder.tv_isbn13.setText("(" + item.getIsbn13() + ")");
            holder.tv_price.setText(item.getPrice());
            //이미지를 view에 붙인다.
            BitmapCacheManager.getInstance(mContext).loadBitmap(
                    item.getImage(),
                    holder.iv_image,
                    TAG);
            holder.ll_link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick() - view: " + v + ", position: " + position + ", " + item);
                    MyIntent.goToURL(mContext, item.getUrl());
                }
            });
            holder.rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick() - view: " + v + ", position: " + position + ", " + item);

                    //detail 화면으로 이동한다.
                    MyIntent.startDetailActivity(mContext, MyIntent.Event.DETAIL_REFRESHED, item.getIsbn13(), TAG);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.length;
        }
    }
}