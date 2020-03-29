package com.kodeholic.itbook.common;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.kodeholic.itbook.database.DatabaseHelper;
import com.kodeholic.itbook.database.TBL_BOOKMARK;
import com.kodeholic.itbook.database.TBL_HISTORY;
import com.kodeholic.itbook.database.TBL_NEW_LIST;
import com.kodeholic.itbook.database.TBL_SEARCH_RESULT;
import com.kodeholic.itbook.lib.util.Log;


public class DatabaseManager {
    public static final String TAG = DatabaseManager.class.getSimpleName();

    private volatile static DatabaseManager sInstance;

    private Context mContext = null;
    private DatabaseHelper mHelper = null;
    private TBL_NEW_LIST mTblNewList;
    private TBL_HISTORY  mTblHistory;
    private TBL_BOOKMARK mTblBookmark;
    private TBL_SEARCH_RESULT mTblSearchResult;

    private DatabaseManager(Context context) {
        mContext = context;

        //데이타베이스 관련 초기화
        mHelper = new DatabaseHelper(context);
        mTblNewList = new TBL_NEW_LIST(mHelper.getWritableDatabase());
        mTblHistory = new TBL_HISTORY(mHelper.getWritableDatabase());
        mTblBookmark= new TBL_BOOKMARK(mHelper.getWritableDatabase());
        mTblSearchResult = new TBL_SEARCH_RESULT(mHelper.getWritableDatabase());
    }

    public static DatabaseManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DatabaseManager.class) {
                if (sInstance == null) {
                    sInstance = new DatabaseManager(context);
                }
            }
        }
        return sInstance;
    }

    public TBL_NEW_LIST getTblNewList() {
        return mTblNewList;
    }

    public TBL_HISTORY getTblHistory() {
        return mTblHistory;
    }

    public TBL_BOOKMARK getTblBookmark() {
        return mTblBookmark;
    }

    public TBL_SEARCH_RESULT getTblSearchResult() {
        return mTblSearchResult;
    }
}
