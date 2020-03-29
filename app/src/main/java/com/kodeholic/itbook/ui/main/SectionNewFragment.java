package com.kodeholic.itbook.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kodeholic.itbook.R;
import com.kodeholic.itbook.common.BookManager;
import com.kodeholic.itbook.common.BitmapCacheManager;
import com.kodeholic.itbook.common.MyIntent;
import com.kodeholic.itbook.common.PopupManager;
import com.kodeholic.itbook.common.data.Book;
import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.lib.http.HttpResponse;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.base.BookItemViewHolder;


/**
 * A placeholder fragment containing a simple view.
 */
public class SectionNewFragment extends SectionFragment {
    public static final String TAG = SectionNewFragment.class.getSimpleName();

    private NewListAdapter mAdapter;
    private RecyclerView   mListView;
    private SwipeRefreshLayout mPullToRefresh;

    public static SectionNewFragment newInstance(SectionsPagerAdapter.TabInfo info) {
        Log.d(TAG, "newInstance() - " + info.index);
        SectionNewFragment fragment = new SectionNewFragment();
        fragment.setSectionInfo(info);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View root = inflater.inflate(R.layout.fragment_new_list, container, false);

        //adapter
        mAdapter = new NewListAdapter(BookManager.getInstance(mContext).getNewResultToArray());

        //list..
        mListView = root.findViewById(R.id.ll_list);
        mListView.setHasFixedSize(true);
        mListView.setAdapter(mAdapter);

        mPullToRefresh = root.findViewById(R.id.ll_refresh);
        mPullToRefresh.setOnRefreshListener(mRefreshListener);
        mPullToRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        return root;
    }

    private void updateView(String f) {
        Log.d(TAG, "updateView() - f: " + f);
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.setData(BookManager.getInstance(mContext).getNewResultToArray());
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onEvent(int event, Object o) {
        Log.d(TAG, "onEvent() - event: " + event);
        if (event == MyIntent.Event.NEW_LIST_REFRESHED) {
            updateView("NEW_LIST_REFRESHED");
        }
    }

    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            BookManager.getInstance(mContext).loadNewList(new BookManager.Listener() {
                @Override
                public void onResponse(HttpResponse response) {
                    mPullToRefresh.post(new Runnable() {
                        @Override
                        public void run() {
                            mPullToRefresh.setRefreshing(false);
                        }
                    });
                }
            }, "onRefresh");
        }
    };

    public class NewListAdapter extends RecyclerView.Adapter<BookItemViewHolder> {
        private Book[] data;
        private boolean detailStarting = false;

        public NewListAdapter(Book[] data) {
            this.data = data;
        }

        public void setData(Book[] data) {
            this.data = data;
        }

        @Override
        public BookItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_book, parent, false);
            return new BookItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final BookItemViewHolder holder, final int position) {
            final Book item = data[position];
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

                    //반복 호출되는 상황 예외처리...
                    synchronized (this) {
                        if (detailStarting) {
                            Log.d(TAG, "already detailStarting!!");
                            return;
                        }
                        detailStarting = true;
                    }

                    //detail 조회후, 화면으로 이동한다.
                    showLoading("onClick");
                    BookManager.getInstance(mContext).loadDetail(item.getIsbn13(), new BookManager.DetailListener() {
                        @Override
                        public void onResult(BookDetail result) {
                            hideLoading("onClick");
                            synchronized (this) {
                                detailStarting = false;
                            }
                            if (result == null) {
                                PopupManager.getInstance(mContext).showToast("Failed to load detail!");
                                return;
                            }
                            MyIntent.startDetailActivity(mContext, MyIntent.Event.DETAIL_REFRESHED, result.getIsbn13(), TAG);
                        }
                    }, TAG);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.length;
        }
    }
}