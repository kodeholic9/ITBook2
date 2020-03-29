package com.kodeholic.itbook.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kodeholic.itbook.lib.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = DatabaseHelper.class.getSimpleName();

    /*** DB 상수 정의 *******************************/
    public static final int DB_VERSION = 1;

    //
    public static final String DB_NAME = "itbook.db";

    public Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate()");
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade() - oldVersion: " + oldVersion + ", newVersion: " + newVersion);
        //oldVersion
        switch (oldVersion) {
//            case 1: upgradeXTo2(db); // no break
//            case 2: upgrade2To3(db); // no break
//            case 3: upgrade3To4(db); // no break
//            case 4: upgrade4To5(db); // no break
//            case 5: upgrade5To6(db); // no break
//            case 6: upgrade6To7(db); // no break
//            case 7: upgrade7To8(db); // no break
//            case 8: upgrade8To9(db); // no break
//            case 9: upgrade9To10(db); break;
        }
    }

    /**
     * 전체 TABLE을 생성한다.
     * @param db
     */
    private void createTables(SQLiteDatabase db) {
        execSQL(db, TBL_NEW_LIST.CREATE, "createTables");
        execSQL(db, TBL_HISTORY.CREATE, "createTables");
        execSQL(db, TBL_BOOKMARK.CREATE, "createTables");
        execSQL(db, TBL_SEARCH_RESULT.CREATE, "createTables");
    }

    /**
     * 전체 TABLE을 삭제한다.
     * @param db
     */
    private void dropTables(SQLiteDatabase db) {
        execSQL(db, TBL_NEW_LIST.DROP, "dropTables");
        execSQL(db, TBL_HISTORY.DROP, "dropTables");
        execSQL(db, TBL_BOOKMARK.DROP, "dropTables");
        execSQL(db, TBL_SEARCH_RESULT.DROP, "dropTables");
    }

    /**
     * 개별 쿼리(생성/삭제)를 수행한다.
     * @param db
     * @param query
     * @param f
     */
    private void execSQL(SQLiteDatabase db, String query, String f) {
        Log.d(TAG, "execSQL() - f: " + f + ", query: " + query);
        db.execSQL(query);
    }

}
