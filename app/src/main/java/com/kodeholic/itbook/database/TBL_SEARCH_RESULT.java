package com.kodeholic.itbook.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.common.data.Bookmark;
import com.kodeholic.itbook.common.data.SearchResult;
import com.kodeholic.itbook.lib.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TBL_SEARCH_RESULT extends TABLE<SearchResult> {
	public static final String TABLE_NAME = "tbl_search_result";

	public static final String QUERY_STRING= "_id";
	public static final String PAGE_NO     = "_page_no";
	public static final String JSON_RESULT = "_json_result";
	public static final String SEARCH_TIME = "_search_time";

	public static final String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " " +
			"(" +
			QUERY_STRING+ " TEXT NOT NULL," +
			PAGE_NO     + " INTEGER NOT NULL," +
			JSON_RESULT + " TEXT NOT NULL," +
			SEARCH_TIME + " INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP," +
			"UNIQUE (" + QUERY_STRING + ", " + PAGE_NO + ") ON CONFLICT REPLACE" +
			");";

	public static final String DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	private static class INDEX {
		public int QUERY_STRING, PAGE_NO, JSON_RESULT, SEARCH_TIME;
	}

	private static INDEX cursorToIndex(Cursor c) throws Exception {
		INDEX idx = new INDEX();
		idx.QUERY_STRING= c.getColumnIndex(QUERY_STRING);
		idx.PAGE_NO     = c.getColumnIndex(PAGE_NO);
		idx.JSON_RESULT = c.getColumnIndex(JSON_RESULT);
		idx.SEARCH_TIME = c.getColumnIndex(SEARCH_TIME);

		return idx;
	}

	public TBL_SEARCH_RESULT(SQLiteDatabase db) {
		super(db, TABLE_NAME);
	}

	@Override
	public ContentValues fetchObject2Values(SearchResult o) {
		ContentValues values = new ContentValues();
		values.put(QUERY_STRING, o.getQueryString());
		values.put(PAGE_NO     , o.getPageNo());
		values.put(JSON_RESULT , o.getJsonResult());
		values.put(SEARCH_TIME , o.getSearchTime());

		return values;
	}

	@Override
	public List<SearchResult> fetchCursor2List(Cursor c) throws Exception {
		List<SearchResult> list = new ArrayList<SearchResult>();
		try {
			if (c.moveToFirst()) {
				INDEX idx = cursorToIndex(c);
				do {
					list.add(fetchCursor2Object(idx, c));
				}
				while (c.moveToNext());
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}

	public SearchResult fetchCursor2Object(INDEX idx, Cursor c) throws Exception {
		SearchResult o = new SearchResult();
		if (idx.QUERY_STRING!= -1) o.setQueryString(c.getString(idx.QUERY_STRING));
		if (idx.PAGE_NO     != -1) o.setPageNo(c.getInt(idx.PAGE_NO));
		if (idx.JSON_RESULT != -1) o.setJsonResult(c.getString(idx.JSON_RESULT));
		if (idx.SEARCH_TIME != -1) o.setSearchTime(c.getInt(idx.SEARCH_TIME));

		return o;
	}

	public SearchResult getSearchResult(String queryString, int pageNo, String f) {
		Log.d(TABLE_NAME, "getSearchResult() - f: " + f + ", queryString: " + queryString + ", pageNo: " + pageNo);
		String where =  QUERY_STRING + "='" + queryString + "' AND " + PAGE_NO + "=" + pageNo;

		return select1(null, where, null);
	}

    public List<SearchResult> getSearchResult(String queryString, String f) {
        Log.d(TABLE_NAME, "getSearchResult() - f: " + f + ", queryString: " + queryString);
        String where =  QUERY_STRING + "='" + queryString + "'";
        return select(null, where, null, null, null);
    }

	public boolean addSearchResult(SearchResult result, String f) {
		Log.d(TABLE_NAME, "addSearchResult() - f: " + f);
		String where =  QUERY_STRING + "='" + result.getQueryString() + "' AND " + PAGE_NO + "=" + result.getPageNo();
		int count = getCount(where, null);
		if (count > 0) {
			//이미 테이블에 존재하는 경우...
			return true;
		}

		Log.d(TABLE_NAME, "addSearchResult() - insert!");
		return insert(result) > 0;
	}

	public boolean delSearchResult(String queryString, String f) {
        Log.d(TABLE_NAME, "delSearchResult() - f: " + f + ", queryString: " + queryString);
        String whereClause = QUERY_STRING + "='" + queryString + "'";
        return delete(whereClause, null) > 0;
    }

}
