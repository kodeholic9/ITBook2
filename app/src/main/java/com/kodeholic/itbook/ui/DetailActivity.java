package com.kodeholic.itbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kodeholic.itbook.R;
import com.kodeholic.itbook.common.BookManager;
import com.kodeholic.itbook.common.BitmapCacheManager;
import com.kodeholic.itbook.common.MyIntent;
import com.kodeholic.itbook.common.PopupManager;
import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.lib.util.JSUtil;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.base.IBase;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements IBase, View.OnClickListener {
    public static final String TAG = DetailActivity.class.getSimpleName();

    private static final int VIEW_TYPE_LOW  = 0;
    private static final int VIEW_TYPE_MID  = 1;
    private static final int VIEW_TYPE_HIGH = 2;
    private static final int VIEW_TYPE_EDIT = 3;

    public class NV {
        public String name;
        public String value;
        public int    viewType;
        public NV() { }
        public NV(String name, String value, int viewType) {
            this.name = name;
            this.value= value != null ? value : "";
            this.viewType = viewType;
        }
    }

    private Context    mContext;
    private List<NV>   mNVList;
    private BookDetail mDetail;

    private DetailAdapter mAdapter;
    private RecyclerView  mListView;
    private ProgressBar pb_loading;
    private TextView    tv_label;
    private ImageView   iv_image;
    private Button      bt_exit;
    private TextView    tv_bookmark;

    private NameValueItemViewHolder mNoteViewHolder;
    private String mNewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mContext = this;
        mNVList  = new ArrayList<>();

        //view를 초기화한다.
        initView();

        //LB를 등록한다.
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyIntent.Action.DETAIL_ACTION);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mControlRecevier, filter);

        //Intent를 체크하고, 정합 오류인 경우 Activity를 종료시킨다.
        onProcess(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //loading을 종료한다.
        hideLoading();

        //LB를 해제한다.
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mControlRecevier);
    }

    @Override
    public void onBackPressed() {
        if (checkNoteAndShowDialog()) {
            return;
        }
        super.onBackPressed();
    }

    /**
     * Intent를 처리한다.
     * @param intent
     */
    private void onProcess(Intent intent) {
        //intent 유효성 여부 검사
        if (intent == null) {
            Log.e(TAG, "onProcess(). Intent is null!!");
            return ;
        }

        if (!MyIntent.Action.DETAIL_ACTION.equals(intent.getAction())) {
            Log.e(TAG, "onProcess(). Invalid Action!! - " + intent.getAction());
            return ;
        }

        try {
            mDetail  = JSUtil.json2Object(intent.getStringExtra(MyIntent.Extra.BOOK_DETAIL), BookDetail.class);
            mNewNote = mDetail.getNote();
            synchronized (mNVList) {
                mNVList.clear();
                mNVList.add(new NV("Title", mDetail.getTitle(), VIEW_TYPE_LOW));
                mNVList.add(new NV("SubTitle", mDetail.getSubTitle(), VIEW_TYPE_LOW));
                mNVList.add(new NV("authors", mDetail.getAuthors(), VIEW_TYPE_LOW));
                mNVList.add(new NV("publisher", mDetail.getPublisher(), VIEW_TYPE_LOW));
                mNVList.add(new NV("language", mDetail.getLanguage(), VIEW_TYPE_LOW));
                mNVList.add(new NV("isbn10", mDetail.getIsbn10(), VIEW_TYPE_LOW));
                mNVList.add(new NV("isbn13", mDetail.getIsbn13(), VIEW_TYPE_LOW));
                mNVList.add(new NV("pages", "" + mDetail.getPages(), VIEW_TYPE_LOW));
                mNVList.add(new NV("year", "" + mDetail.getYear(), VIEW_TYPE_LOW));
                mNVList.add(new NV("rating", "" + mDetail.getRating(), VIEW_TYPE_LOW));
                mNVList.add(new NV("desc", mDetail.getDesc(), VIEW_TYPE_HIGH));
                mNVList.add(new NV("price", mDetail.getPrice(), VIEW_TYPE_LOW));
                mNVList.add(new NV("image", mDetail.getImage(), VIEW_TYPE_LOW));
                mNVList.add(new NV("url", mDetail.getUrl(), VIEW_TYPE_LOW));
                mNVList.add(new NV("note", mDetail.getNote(), VIEW_TYPE_EDIT));
            }

            //View를 갱신한다.
            updateView("onProcess");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (mDetail == null) {
            Log.e(TAG, "onProcess() - mDetail is NULL! stop activity!!");
            finish();
        }

        return;
    }

    /**
     * Array 목록을 반환한다.
     * @return
     */
    private NV[] toArray() {
        synchronized (mNVList) {
            return mNVList.toArray(new NV[0]);
        }
    }

    private void initView() {
        pb_loading = findViewById(R.id.pb_loading);
        tv_label   = findViewById(R.id.tv_label);
        iv_image   = findViewById(R.id.iv_image);
        bt_exit    = findViewById(R.id.bt_exit);
        tv_bookmark= findViewById(R.id.tv_bookmark);

        //onclick
        bt_exit.setOnClickListener(this);
        tv_bookmark.setOnClickListener(this);

        //adapter
        mAdapter = new DetailAdapter(toArray());

        //list..
        mListView = findViewById(R.id.ll_list);
        mListView.setAdapter(mAdapter);
    }

    private void updateView(String f) {
        Log.d(TAG, "updateView() - f: " + f);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });
    }
    private void updateView() {
        if (mDetail == null) {
            return;
        }
        tv_label.setText(mDetail.getTitle());

        //북마크 버튼 label
        boolean isBookmark = BookManager.getInstance(mContext).isBookmark(mDetail.getIsbn13());
        Log.d(TAG, "updateView() - isBookmark: " + isBookmark);
        if (!isBookmark) {
            tv_bookmark.setText("Add Bookmark");
        }
        else {
            tv_bookmark.setText("Remove Bookmark");
        }

        //이미지를 view에 붙인다.
        BitmapCacheManager.getInstance(mContext).loadBitmap(
                mDetail.getImage(),
                iv_image,
                TAG);

        //작성중인 Note를 복원한다.
        if (mNoteViewHolder != null && mNoteViewHolder.ed_input != null) {
            mNewNote = mNoteViewHolder.ed_input.getText().toString();
        }
        mAdapter.setData(toArray());
        mAdapter.notifyDataSetChanged();
    }

    private BroadcastReceiver mControlRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onProcess(intent);
        }
    };

    @Override
    public void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb_loading.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb_loading.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_exit:
                if (checkNoteAndShowDialog()) {
                    break;
                }
                finish();
                break;

            case R.id.tv_bookmark:
                if (!BookManager.getInstance(mContext).isBookmark(mDetail.getIsbn13())) {
                    BookManager.getInstance(mContext).putBookmark(mDetail, TAG);
                    PopupManager.getInstance(mContext).showToast("Bookmark Added");
                }
                else {
                    BookManager.getInstance(mContext).delBookmark(mDetail.getIsbn13(), TAG);
                    PopupManager.getInstance(mContext).showToast("Bookmark Removed");
                }
                updateView("tv_bookmark");
                break;
        }
    }

    public class DetailAdapter extends RecyclerView.Adapter<NameValueItemViewHolder> {
        private NV[] data;
        public DetailAdapter(NV[] data) {
            this.data = data;
        }

        public void setData(NV[] data) {
            this.data = data;
        }

        @Override
        public int getItemViewType(int position) {
            // loader can't be at position 0
            // loader can only be at the last position
            return data[position].viewType;
        }

        @Override
        public NameValueItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    (viewType == VIEW_TYPE_LOW)
                            ? R.layout.list_item_name_value_low : (viewType == VIEW_TYPE_MID)
                            ? R.layout.list_item_name_value_mid : (viewType == VIEW_TYPE_HIGH)
                            ? R.layout.list_item_name_value_high: R.layout.list_item_name_value_edit,
                    parent,
                    false);

            NameValueItemViewHolder viewHolder = new NameValueItemViewHolder(itemView, viewType);
            if (viewHolder.viewType == VIEW_TYPE_EDIT) {
                mNoteViewHolder = viewHolder;
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final NameValueItemViewHolder holder, final int position) {
            final NV item = data[position];
            holder.tv_name.setText(item.name);
            holder.tv_value.setText(item.value);
            if (holder.ed_input != null) {
                holder.ed_input.setText(mNewNote);
            }
        }

        @Override
        public int getItemCount() {
            return data.length;
        }
    }

    public class NameValueItemViewHolder extends RecyclerView.ViewHolder {
        public int      viewType;
        public TextView tv_name;
        public TextView tv_value;
        public EditText ed_input;

        public NameValueItemViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            this.tv_name  = itemView.findViewById(R.id.tv_name);
            this.tv_value = itemView.findViewById(R.id.tv_value);
            this.ed_input = itemView.findViewById(R.id.ed_input);
        }
    }

    private boolean checkNoteAndShowDialog() {
        if (mNoteViewHolder != null && mNoteViewHolder.ed_input != null) {
            String newNote = mNoteViewHolder.ed_input.getText().toString();
            String oldNote = mDetail.getNote() != null ? mDetail.getNote() : "";
            Log.d(TAG, "newNote: " + newNote);
            Log.d(TAG, "oldNote: " + oldNote);
            if (!newNote.equals(oldNote)) {
                showDialog(mDetail.getIsbn13(), newNote);
                return true;
            }
        }

        return false;
    }

    private void showDialog(final String isbn13, final String note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Notes have changed. Do you want to save?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BookManager.getInstance(mContext).saveNote(isbn13, note, TAG);
                        finish();
                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.show();
    }
}