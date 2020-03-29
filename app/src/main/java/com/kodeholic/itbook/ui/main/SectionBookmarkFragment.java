package com.kodeholic.itbook.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.kodeholic.itbook.R;
import com.kodeholic.itbook.common.BookManager;
import com.kodeholic.itbook.common.BitmapCacheManager;
import com.kodeholic.itbook.common.MyIntent;
import com.kodeholic.itbook.common.data.Bookmark;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.base.BookItemViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SectionBookmarkFragment extends SectionFragment {
    public static final String TAG = SectionBookmarkFragment.class.getSimpleName();

    private BookmarkAdapter mAdapter;
    private ItemTouchHelper mTouchHelper;

    private RecyclerView mListView;
    private TextView     tv_no_result;

    public static SectionBookmarkFragment newInstance(SectionsPagerAdapter.TabInfo info) {
        Log.d(TAG, "newInstance() - " + info.index);
        SectionBookmarkFragment fragment = new SectionBookmarkFragment();
        fragment.setSectionInfo(info);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View root = inflater.inflate(R.layout.fragment_bookmark, container, false);


        //adapter
        mAdapter = new BookmarkAdapter(new ArrayList<Bookmark>());

        //list..
        mListView = root.findViewById(R.id.ll_list);
        //mListView.setHasFixedSize(true);
        mListView.setAdapter(mAdapter);

        //drag..
        ItemTouchHelper.Callback callback = new ItemMoveCallback(mAdapter);
        mTouchHelper = new ItemTouchHelper(callback);
        mTouchHelper.attachToRecyclerView(mListView);

        //no result
        tv_no_result = root.findViewById(R.id.tv_no_result);

        //view를 갱신한다.
        updateView("onCreateView");

        return root;
    }

    @Override
    public void onEvent(int event, Object o) {
        Log.d(TAG, "onEvent() - event: " + event);
        if (event == MyIntent.Event.BOOKMARK_REFRESHED) {
            updateView("BOOKMARK_REFRESHED");
        }
    }

    /**
     * 화면을 갱신한다.
     * @param f
     */
    private void updateView(String f) {
        Log.d(TAG, "updateView() - f: " + f);
        final List<Bookmark> results = BookManager.getInstance(mContext).getBookmarkCopies();
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.setData(results);
                mAdapter.notifyDataSetChanged();

                if (results.size() == 0) {
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

    public class BookmarkAdapter extends RecyclerView.Adapter<BookItemViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
        private List<Bookmark> data;

        public BookmarkAdapter(List<Bookmark> data) {
            this.data = data;
        }

        public void setData(List<Bookmark> data) {
            this.data = data;
        }

        @Override
        public BookItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_book, parent, false);
            return new BookItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final BookItemViewHolder holder, final int position) {
            final Bookmark item = data.get(position);
            holder.tv_title.setText(item.getTitle());
            holder.tv_subtitle.setText(item.getSubTitle());
            holder.tv_isbn13.setText("(" + item.getIsbn13() + ")");
            holder.tv_price.setText(item.getPrice());
            //이미지를 view에 붙인다.
            BitmapCacheManager.getInstance(mContext).loadBitmap(
                    item.getImage(),
                    holder.iv_image,
                    TAG);

            //각종 이벤트를 등록한다.
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
            holder.rowView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mTouchHelper.startDrag(holder);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public void onRowMoved(int fromPosition, int toPosition) {
            Log.d(TAG, "onRowMoved() - fromPosition: " + fromPosition + ", toPosition: " + toPosition);
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(data, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(data, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onRowSelected(BookItemViewHolder myViewHolder) {
            Log.d(TAG, "onRowSelected() - myViewHolder: " + myViewHolder.tv_isbn13.getText());
            myViewHolder.rowView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onRowClear(BookItemViewHolder myViewHolder) {
            Log.d(TAG, "onRowClear() - myViewHolder: " + myViewHolder.tv_isbn13.getText());
            myViewHolder.rowView.setBackgroundColor(Color.WHITE);

            //정렬 순서를 교정한다.
            BookManager.getInstance(mContext).adjustBookmarkOrder(data);
        }
    }

    public static class ItemMoveCallback extends ItemTouchHelper.Callback {
        private final ItemTouchHelperContract mAdapter;

        public ItemMoveCallback(ItemTouchHelperContract adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder instanceof BookItemViewHolder) {
                    BookItemViewHolder myViewHolder = (BookItemViewHolder) viewHolder;
                    mAdapter.onRowSelected(myViewHolder);
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            if (viewHolder instanceof BookItemViewHolder) {
                BookItemViewHolder myViewHolder = (BookItemViewHolder) viewHolder;
                mAdapter.onRowClear(myViewHolder);
            }
        }

        public interface ItemTouchHelperContract {
            void onRowMoved(int fromPosition, int toPosition);
            void onRowSelected(BookItemViewHolder myViewHolder);
            void onRowClear(BookItemViewHolder myViewHolder);
        }
    }
}